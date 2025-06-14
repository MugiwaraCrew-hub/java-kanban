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
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriorityHandlerTest extends HttpTaskServerTest {

    @Test
    @DisplayName("Должен вернуть приоритезированные задачи")
    void shouldReturnPrioritizedTasks() throws IOException, InterruptedException {
        // Создаем задачи с разными временами начала
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now().plusHours(2));
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.IN_PROGRESS, Duration.ofMinutes(45), LocalDateTime.now()); // Раньше
        Epic epic = new Epic("Epic", "Epic Desc");
        taskManager.createEpic(epic);
        // ИСПРАВЛЕНО: Правильный конструктор Subtask
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Desc 1", TaskStatus.DONE, epic.getId(), Duration.ofMinutes(15), LocalDateTime.now().plusHours(3));
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask Desc 2", TaskStatus.NEW, epic.getId(), Duration.ofMinutes(60), LocalDateTime.now().plusHours(1)); // Позже task2, но раньше task1, subtask1

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        List<Task> prioritizedTasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(prioritizedTasks, "Приоритезированные задачи не должны быть null");
        assertEquals(4, prioritizedTasks.size(), "Должно быть 4 приоритезированные задачи");

        // Проверяем порядок: task2, subtask2, task1, subtask1 (по времени начала)
        assertEquals(task2.getId(), prioritizedTasks.get(0).getId(), "Первая задача должна быть Task 2");
        assertEquals(subtask2.getId(), prioritizedTasks.get(1).getId(), "Вторая задача должна быть Subtask 2");
        assertEquals(task1.getId(), prioritizedTasks.get(2).getId(), "Третья задача должна быть Task 1");
        assertEquals(subtask1.getId(), prioritizedTasks.get(3).getId(), "Четвертая задача должна быть Subtask 1");
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если нет приоритезированных задач")
    void shouldReturnEmptyPrioritizedListIfNoTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        List<Task> prioritizedTasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(prioritizedTasks, "Приоритезированные задачи не должны быть null");
        assertTrue(prioritizedTasks.isEmpty(), "Список приоритезированных задач должен быть пустым");
    }

    @Test
    @DisplayName("Приоритезированные задачи не должны включать пересекающиеся")
    void prioritizedTasksShouldNotIncludeIntersecting() throws IOException, InterruptedException {
        // Создаем пересекающиеся задачи
        Task task1 = new Task("Task A", "Desc A", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        Task task2 = new Task("Task B", "Desc B", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 30)); // Пересекается с Task A
        taskManager.createTask(task1);
        // task2 не будет добавлена, т.к. метод createTask менеджера должен отклонить её из-за пересечения

        // Создаем Epic
        Epic epic = new Epic("Epic C", "Desc C");
        taskManager.createEpic(epic);
        // Создаем Subtask, которая будет пересекаться с task1
        // ИСПРАВЛЕНО: Правильный конструктор Subtask
        Subtask subtask1 = new Subtask("Subtask C1", "Desc C1", TaskStatus.NEW, epic.getId(), Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 15));
        // subtask1 также не будет добавлена из-за пересечения

        // Создаем задачу, которая НЕ пересекается
        Task task3 = new Task("Task D", "Desc D", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 11, 30));
        taskManager.createTask(task3);


        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        List<Task> prioritizedTasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(prioritizedTasks, "Приоритезированные задачи не должны быть null");
        // Должны быть только task1 и task3 (если менеджер правильно отклонил task2 и subtask1)
        assertEquals(2, prioritizedTasks.size(), "Должны быть только 2 задачи (без пересечений)");

        // Проверяем, что в приоритезированном списке только корректные задачи
        assertTrue(prioritizedTasks.stream().anyMatch(t -> t.getId().equals(task1.getId())), "Должна быть Task A");
        assertTrue(prioritizedTasks.stream().anyMatch(t -> t.getId().equals(task3.getId())), "Должна быть Task D");
    }
}