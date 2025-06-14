
package org.example.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.manager.TaskManager;
import org.example.model.Task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        System.out.println("Обрабатывается запрос " + method + " " + path); // Для отладки

        try {
            if ("GET".equals(method) && "/history".equals(path)) {
                List<Task> history = taskManager.getHistory();
                sendText(exchange, gson.toJson(history), HttpURLConnection.HTTP_OK);
            } else {
                sendText(exchange, "Метод не поддерживается или неверный путь.", HttpURLConnection.HTTP_BAD_METHOD);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}