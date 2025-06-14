package org.example.server;

import org.example.model.Epic;
import org.example.model.Subtask;
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

import static org.junit.jupiter.api.Assertions.*;

class SubtaskHandlerTest extends HttpTaskServerTest {

    @Test
    @DisplayName("Должен создать подзадачу")
    void shouldCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для подзадачи", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Тестовая подзадача", "Описание", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 Created");
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertNotNull(subtasks, "Список подзадач не должен быть null");
        assertEquals(1, subtasks.size(), "Должна быть создана одна подзадача");
        assertEquals("Тестовая подзадача", subtasks.get(0).getTitle(), "Название подзадачи должно совпадать");
    }

    @Test
    @DisplayName("Должен вернуть подзадачу по ID")
    void shouldGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для подзадачи", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Тестовая подзадача", "Описание", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(retrievedSubtask, "Полученная подзадача не должна быть null");
        assertEquals(subtaskId, retrievedSubtask.getId(), "ID подзадачи должен совпадать");
    }

    @Test
    @DisplayName("Должен вернуть 404, если подзадача не найдена")
    void shouldReturn404ForNonExistentSubtask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 Not Found");
    }

    @Test
    @DisplayName("Должен вернуть все подзадачи")
    void shouldGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для подзадач", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", TaskStatus.DONE, epicId, Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(15));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        List<Subtask> subtasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Subtask>>() {}.getType());
        assertNotNull(subtasks, "Список подзадач не должен быть null");
        assertEquals(2, subtasks.size(), "Должны быть возвращены все 2 подзадачи");
        assertTrue(subtasks.stream().anyMatch(s -> s.getId().equals(subtask1.getId())), "Список должен содержать Subtask 1");
        assertTrue(subtasks.stream().anyMatch(s -> s.getId().equals(subtask2.getId())), "Список должен содержать Subtask 2");
    }

    @Test
    @DisplayName("Должен удалить подзадачу по ID")
    void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для подзадачи", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Подзадача для удаления", "Описание", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        assertNull(taskManager.getSubtask(subtaskId), "Подзадача должна быть удалена из менеджера");
    }

    @Test
    @DisplayName("Должен обновить подзадачу")
    void shouldUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для подзадачи", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Подзадача для обновления", "Описание", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        Subtask updatedSubtask = new Subtask("Обновленная подзадача", "Новое описание", TaskStatus.DONE, epicId, Duration.ofMinutes(15), LocalDateTime.now().plusHours(1));
        updatedSubtask.setId(subtaskId); // Устанавливаем ID для обновления

        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson)) // POST для обновления
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // ИЗМЕНЕНО: Ожидаем 200 OK для обновления
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK для обновления");
        Subtask retrievedSubtask = taskManager.getSubtask(subtaskId);
        assertNotNull(retrievedSubtask, "Обновленная подзадача не должна быть null");
        assertEquals("Обновленная подзадача", retrievedSubtask.getTitle(), "Название подзадачи должно быть обновлено");
        assertEquals("Новое описание", retrievedSubtask.getDescription(), "Описание подзадачи должно быть обновлено");
        assertEquals(TaskStatus.DONE, retrievedSubtask.getStatus(), "Статус подзадачи должен быть обновлен");
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request при обновлении с несуществующим эпиком")
    void shouldReturn400IfSubtaskUpdatedWithNonExistentEpic() throws IOException, InterruptedException {
        // Создаем подзадачу, но epicId указывает на несуществующий эпик
        Subtask subtask = new Subtask("Подзадача", "Описание", TaskStatus.NEW, 999, Duration.ofMinutes(10), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Код ответа должен быть 400 Bad Request при обновлении с несуществующим эпиком");
    }

    @Test
    @DisplayName("Должен вернуть 400 Bad Request, если подзадача становится своим же эпиком")
    void shouldReturn400IfSubtaskIsItsOwnEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для подзадачи", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Подзадача", "Описание", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        // Попытка обновить подзадачу, установив ее epicId равным ее собственному id
        Subtask invalidSubtask = new Subtask("Невалидная подзадача", "Описание", TaskStatus.NEW, subtaskId, Duration.ofMinutes(10), LocalDateTime.now());
        invalidSubtask.setId(subtaskId);
        String invalidSubtaskJson = gson.toJson(invalidSubtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidSubtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Код ответа должен быть 400 Bad Request, если подзадача становится своим же эпиком");
    }
}