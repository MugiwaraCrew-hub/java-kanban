// package org.example.server; - Убедись, что пакеты совпадают с твоими
// HttpTaskServerTest.java (Базовый класс для тестов сервера)
package org.example.server;

import com.google.gson.Gson;
import org.example.manager.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.http.HttpClient;

public abstract class HttpTaskServerTest {
    protected InMemoryTaskManager taskManager;
    protected HttpTaskServer httpTaskServer;
    protected Gson gson;
    protected HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager(); // Чистый менеджер для каждого теста
        httpTaskServer = new HttpTaskServer(taskManager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        httpTaskServer.start();
    }

    @AfterEach
    void tearDown() {
        httpTaskServer.stop();
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        taskManager.removeAllSubtasks();
    }
}
