// package org.example.server; - Убедись, что пакеты совпадают с твоими
// PriorityHandler.java
package org.example.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.manager.TaskManager;
import org.example.model.Task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

public class PriorityHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public PriorityHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        System.out.println("Обрабатывается запрос " + method + " " + path); // Для отладки

        try {
            if ("GET".equals(method) && "/prioritized".equals(path)) {
                List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                sendText(exchange, gson.toJson(prioritizedTasks), HttpURLConnection.HTTP_OK);
            } else {
                sendText(exchange, "Метод не поддерживается или неверный путь.", HttpURLConnection.HTTP_BAD_METHOD);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}