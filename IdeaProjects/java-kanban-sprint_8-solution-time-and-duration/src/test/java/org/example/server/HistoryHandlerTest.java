package org.example.server;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoryHandlerTest extends HttpTaskServerTest {

    @Test
    @DisplayName("Должен вернуть историю просмотров")
    void shouldReturnHistory() throws IOException, InterruptedException {
        // ИСПРАВЛЕНО: Создаем задачи с НЕ пересекающимися временными интервалами
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.IN_PROGRESS, Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(30)); // +30 минут, чтобы не пересекалось
        Epic epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);

        // ИСПРАВЛЕНО: Подзадача с временем, которое не пересекается с другими задачами
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", TaskStatus.DONE, epic.getId(), Duration.ofMinutes(5), LocalDateTime.now().plusMinutes(60)); // +60 минут

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createSubtask(subtask);

        // Просматриваем задачи, чтобы они попали в историю
        taskManager.getTask(task1.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask.getId());
        taskManager.getTask(task2.getId());

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(history, "История не должна быть null");
        assertEquals(4, history.size(), "История должна содержать 4 элемента");
        assertEquals(task1.getId(), history.get(0).getId(), "Первая задача в истории должна быть task1");
        assertEquals(epic.getId(), history.get(1).getId(), "Вторая задача в истории должна быть epic");
        assertEquals(subtask.getId(), history.get(2).getId(), "Третья задача в истории должна быть subtask");
        assertEquals(task2.getId(), history.get(3).getId(), "Четвертая задача в истории должна быть task2");
    }

    @Test
    @DisplayName("Должен вернуть пустую историю, если задач не было")
    void shouldReturnEmptyHistoryIfNoTasksViewed() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(history, "История не должна быть null");
        assertEquals(0, history.size(), "История должна быть пустой");
    }
}