package org.example.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.manager.TaskManager;
import org.example.model.Epic;
import org.example.model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        System.out.println("Обрабатывается запрос " + method + " " + path);

        try {
            if (path.equals("/epics")) {
                switch (method) {
                    case "GET":
                        handleGetAllEpics(exchange);
                        break;
                    case "POST":
                        handlePostEpic(exchange);
                        break;
                    case "DELETE":
                        taskManager.removeAllEpics();
                        sendText(exchange, "Все эпики удалены", HttpURLConnection.HTTP_OK);
                        break;
                    default:
                        sendText(exchange, "Метод не поддерживается", HttpURLConnection.HTTP_BAD_METHOD);
                }
            } else if (path.matches("/epics/\\d+$")) {
                int id = extractId(path);
                if (id == -1) {
                    sendNotFound(exchange, "Неверный ID эпика в запросе.");
                    return;
                }

                switch (method) {
                    case "GET":
                        handleGetEpicById(exchange, id);
                        break;
                    case "DELETE":
                        taskManager.removeEpic(id);
                        sendText(exchange, "Эпик удален", HttpURLConnection.HTTP_OK);
                        break;
                    default:
                        sendText(exchange, "Метод не поддерживается", HttpURLConnection.HTTP_BAD_METHOD);
                }
            } else if (path.matches("/epics/\\d+/subtasks$")) {
                int id = extractId(path);
                if (id == -1) {
                    sendNotFound(exchange, "Неверный ID эпика в запросе.");
                    return;
                }
                if (method.equals("GET")) {
                    handleGetEpicSubtasks(exchange, id);
                } else {
                    sendText(exchange, "Метод не поддерживается", HttpURLConnection.HTTP_BAD_METHOD);
                }
            } else {
                sendNotFound(exchange, "Неверный путь.");
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            System.err.println("Внутренняя ошибка сервера при обработке запроса " + path + ": " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        sendText(exchange, gson.toJson(epics), HttpURLConnection.HTTP_OK);
    }

    private void handleGetEpicById(HttpExchange exchange, int id) throws IOException {
        Epic epic = taskManager.getEpic(id);
        if (epic == null) {
            sendNotFound(exchange, "Эпик с ID " + id + " не найден.");
        } else {
            sendText(exchange, gson.toJson(epic), HttpURLConnection.HTTP_OK);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, int epicId) throws IOException {
        Epic epic = taskManager.getEpic(epicId);
        if (epic == null) {
            sendNotFound(exchange, "Эпик с ID " + epicId + " не найден.");
            return;
        }
        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
        sendText(exchange, gson.toJson(subtasks), HttpURLConnection.HTTP_OK);
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        InputStream input = exchange.getRequestBody();
        String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic == null) {
            sendText(exchange, "Некорректные данные эпика.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        // ИСПРАВЛЕНО: Проверяем, является ли ID null (для новых эпиков)
        if (epic.getId() == null) { // Создание нового эпика
            int newId = taskManager.createEpic(epic); // createEpic должен присвоить ID объекту epic
            sendText(exchange, "Эпик создан с ID: " + newId, HttpURLConnection.HTTP_CREATED);
        } else { // Обновление существующего эпика
            // Проверяем, существует ли эпик с таким ID
            if (taskManager.getEpic(epic.getId()) != null) {
                taskManager.updateEpic(epic);
                sendText(exchange, "Эпик обновлён.", HttpURLConnection.HTTP_OK); // 200 OK для обновления
            } else {
                // Если ID был передан, но такого эпика нет
                sendNotFound(exchange, "Эпик с ID " + epic.getId() + " не найден для обновления.");
            }
        }
    }
}