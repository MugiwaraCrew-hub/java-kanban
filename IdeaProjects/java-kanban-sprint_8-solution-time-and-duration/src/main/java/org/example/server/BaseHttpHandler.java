
package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message, 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message, 406); // 406 — Not Acceptable
    }

    protected void sendInternalServerError(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message, 500); // 500 — Internal Server Error
    }

    // Вспомогательный метод для извлечения ID из пути.
    // Принимает путь вида "/tasks/123" или "/epics/456/subtasks"
    protected int extractId(String path) {
        String[] parts = path.split("/");
        if (parts.length > 2) {
            try {
                return Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return -1; // Неверный формат ID
            }
        }
        return -1; // ID отсутствует
    }
}