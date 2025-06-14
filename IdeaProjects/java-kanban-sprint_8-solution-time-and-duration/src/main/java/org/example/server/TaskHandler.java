package org.example.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.manager.TaskManager;
import org.example.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        System.out.println("Обрабатывается запрос " + method + " " + path);

        try {
            if (path.equals("/tasks")) { // Получение всех задач или POST для создания/обновления
                switch (method) {
                    case "GET":
                        handleGetAllTasks(exchange);
                        break;
                    case "POST":
                        handlePostTask(exchange);
                        break;
                    case "DELETE": // Удаление всех задач
                        taskManager.removeAllTasks();
                        sendText(exchange, "Все задачи удалены.", HttpURLConnection.HTTP_OK);
                        break;
                    default:
                        sendText(exchange, "Метод не поддерживается.", HttpURLConnection.HTTP_BAD_METHOD);
                }
            } else if (path.matches("/tasks/\\d+$")) { // Получение или удаление задачи по ID
                int id = extractId(path);
                if (id == -1) {
                    sendNotFound(exchange, "Неверный ID задачи в запросе.");
                    return;
                }

                switch (method) {
                    case "GET":
                        handleGetTaskById(exchange, id);
                        break;
                    case "DELETE":
                        taskManager.removeTask(id);
                        sendText(exchange, "Задача удалена.", HttpURLConnection.HTTP_OK);
                        break;
                    default:
                        sendText(exchange, "Метод не поддерживается.", HttpURLConnection.HTTP_BAD_METHOD);
                }
            } else {
                sendNotFound(exchange, "Неверный путь.");
            }
        } catch (IllegalArgumentException e) {
            // Если TaskManager выбросил IllegalArgumentException (например, из-за пересечения времени)
            sendHasInteractions(exchange, e.getMessage()); // Отправляем 406
        } catch (Exception e) {
            // Для всех остальных непредвиденных ошибок
            System.err.println("Внутренняя ошибка сервера при обработке запроса " + path + ": " + e.getMessage());
            e.printStackTrace(); // Вывод трассировки стека для отладки
            sendInternalServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage()); // Отправляем 500
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        sendText(exchange, gson.toJson(tasks), HttpURLConnection.HTTP_OK);
    }

    private void handleGetTaskById(HttpExchange exchange, int id) throws IOException {
        Task task = taskManager.getTask(id);
        if (task == null) {
            sendNotFound(exchange, "Задача с ID " + id + " не найдена.");
        } else {
            sendText(exchange, gson.toJson(task), HttpURLConnection.HTTP_OK);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        InputStream input = exchange.getRequestBody();
        String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        Task incomingTaskData = gson.fromJson(body, Task.class);

        if (incomingTaskData == null) {
            sendText(exchange, "Некорректные данные задачи.", HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        if (incomingTaskData.getId() == null) { // Создание новой задачи
            int newId = taskManager.createTask(incomingTaskData);
            sendText(exchange, "Задача создана с ID: " + newId, HttpURLConnection.HTTP_CREATED);
        } else { // Обновление существующей задачи
            // Сначала проверяем, существует ли задача, которую пытаются обновить
            Task existingTask = taskManager.getTask(incomingTaskData.getId());
            if (existingTask != null) {
                // *** ИСПРАВЛЕНО ***
                // Передаем в менеджер объект с новыми данными, пришедший из запроса.
                // Менеджер сам должен проверить возможность обновления и применить изменения.
                // Мы НЕ МЕНЯЕМ 'existingTask' напрямую, чтобы избежать побочных эффектов при ошибке валидации.
                taskManager.updateTask(incomingTaskData);
                sendText(exchange, "Задача обновлена.", HttpURLConnection.HTTP_OK);
            } else {
                // Если ID был передан, но такой задачи нет, это ошибка
                sendNotFound(exchange, "Задача с ID " + incomingTaskData.getId() + " не найдена для обновления.");
            }
        }
    }
}