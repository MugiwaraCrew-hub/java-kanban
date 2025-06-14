package org.example.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.manager.TaskManager;
import org.example.model.Epic; // Добавил импорт Epic для проверки существования
import org.example.model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        System.out.println("Обрабатывается запрос " + method + " " + path);

        try {
            if (path.equals("/subtasks")) {
                switch (method) {
                    case "GET":
                        handleGetAllSubtasks(exchange);
                        break;
                    case "POST":
                        handlePostSubtask(exchange);
                        break;
                    case "DELETE":
                        taskManager.removeAllSubtasks();
                        sendText(exchange, "Все подзадачи удалены", HttpURLConnection.HTTP_OK);
                        break;
                    default:
                        sendText(exchange, "Метод не поддерживается", HttpURLConnection.HTTP_BAD_METHOD);
                }
            } else if (path.matches("/subtasks/\\d+$")) {
                int id = extractId(path);
                if (id == -1) {
                    sendNotFound(exchange, "Неверный ID подзадачи в запросе.");
                    return;
                }

                switch (method) {
                    case "GET":
                        handleGetSubtaskById(exchange, id);
                        break;
                    case "DELETE":
                        taskManager.removeSubtask(id);
                        sendText(exchange, "Подзадача удалена", HttpURLConnection.HTTP_OK);
                        break;
                    default:
                        sendText(exchange, "Метод не поддерживается", HttpURLConnection.HTTP_BAD_METHOD);
                }
            } else {
                sendNotFound(exchange, "Неверный путь.");
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка валидации данных: " + e.getMessage()); // Для отладки
            sendHasInteractions(exchange, e.getMessage()); // Отправляем 406 Not Acceptable
        } catch (Exception e) {
            System.err.println("Внутренняя ошибка сервера при обработке запроса " + path + ": " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        sendText(exchange, gson.toJson(subtasks), HttpURLConnection.HTTP_OK);
    }

    private void handleGetSubtaskById(HttpExchange exchange, int id) throws IOException {
        Subtask subtask = taskManager.getSubtask(id);
        if (subtask == null) {
            sendNotFound(exchange, "Подзадача с ID " + id + " не найдена.");
        } else {
            sendText(exchange, gson.toJson(subtask), HttpURLConnection.HTTP_OK);
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        InputStream input = exchange.getRequestBody();
        String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        if (subtask == null) {
            sendText(exchange, "Некорректные данные подзадачи.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        // Проверка на несуществующий Epic (ошибка 400)
        Integer epicId = subtask.getEpicId();
        if (epicId == null || taskManager.getEpic(epicId) == null) {
            sendText(exchange, "Подзадача должна быть привязана к существующему эпику. Эпик с ID " + epicId + " не найден.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        // Проверка, что подзадача не является своим же эпиком (ошибка 400)
        if (subtask.getId() != null && subtask.getId().equals(epicId)) {
            sendText(exchange, "Подзадача не может быть своим собственным эпиком.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        // ИСПРАВЛЕНО: Проверяем, является ли ID null (для новых подзадач)
        if (subtask.getId() == null) { // Создание новой подзадачи
            int newId = taskManager.createSubtask(subtask);
            sendText(exchange, "Подзадача создана с ID: " + newId, HttpURLConnection.HTTP_CREATED);
        } else { // Обновление существующей подзадачи
            // Проверяем, существует ли подзадача с таким ID
            if (taskManager.getSubtask(subtask.getId()) != null) {
                taskManager.updateSubtask(subtask);
                sendText(exchange, "Подзадача обновлена.", HttpURLConnection.HTTP_OK); // 200 OK для обновления
            } else {
                // Если ID был передан, но такой подзадачи нет
                sendNotFound(exchange, "Подзадача с ID " + subtask.getId() + " не найдена для обновления.");
            }
        }
    }
}