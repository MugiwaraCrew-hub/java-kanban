// package org.example.server; - Убедись, что пакеты совпадают с твоими
// HttpTaskServer.java
package org.example.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import org.example.manager.Managers;
import org.example.manager.TaskManager;
import org.example.utill.DurationAdapter;
import org.example.utill.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080; // Изменено на 8080
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = getGson(); // Используем общий Gson объект

        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Регистрируем обработчики, передавая taskManager и gson
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PriorityHandler(taskManager, gson));
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен.");
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault(); // Используем InMemeoryTaskManager по умолчанию
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
        // Сервер будет работать, пока приложение не будет остановлено
    }
}