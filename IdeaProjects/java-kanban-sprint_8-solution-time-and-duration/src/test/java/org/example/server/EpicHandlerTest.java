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

class EpicHandlerTest extends HttpTaskServerTest {

    @Test
    @DisplayName("Должен создать эпик")
    void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201 Created");
        List<Epic> epics = taskManager.getAllEpics();
        assertNotNull(epics, "Список эпиков не должен быть null");
        assertEquals(1, epics.size(), "Должен быть создан один эпик");
        assertEquals("Тестовый эпик", epics.get(0).getTitle(), "Название эпика должно совпадать");
    }

    @Test
    @DisplayName("Должен вернуть эпик по ID")
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(retrievedEpic, "Полученный эпик не должен быть null");
        assertEquals(epicId, retrievedEpic.getId(), "ID эпика должен совпадать");
    }

    @Test
    @DisplayName("Должен вернуть 404, если эпик не найден")
    void shouldReturn404ForNonExistentEpic() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 Not Found");
    }

    @Test
    @DisplayName("Должен вернуть все эпики")
    void shouldGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        List<Epic> epics = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Epic>>() {}.getType());
        assertNotNull(epics, "Список эпиков не должен быть null");
        assertEquals(2, epics.size(), "Должны быть возвращены все 2 эпика");
        assertTrue(epics.stream().anyMatch(e -> e.getId().equals(epic1.getId())), "Список должен содержать Epic 1");
        assertTrue(epics.stream().anyMatch(e -> e.getId().equals(epic2.getId())), "Список должен содержать Epic 2");
    }

    @Test
    @DisplayName("Должен удалить эпик по ID")
    void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для удаления", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        assertNull(taskManager.getEpic(epicId), "Эпик должен быть удален из менеджера");
    }

    @Test
    @DisplayName("Должен вернуть подзадачи эпика")
    void shouldReturnEpicsSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик с подзадачами", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        // ИСПРАВЛЕНО: Правильный конструктор Subtask
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", TaskStatus.DONE, epicId, Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(15));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK");
        List<Subtask> subtasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Subtask>>() {}.getType());
        assertNotNull(subtasks, "Список подзадач не должен быть null");
        assertEquals(2, subtasks.size(), "Должны быть возвращены 2 подзадачи");
        assertTrue(subtasks.stream().anyMatch(s -> s.getId().equals(subtask1.getId())), "Список должен содержать Subtask 1");
        assertTrue(subtasks.stream().anyMatch(s -> s.getId().equals(subtask2.getId())), "Список должен содержать Subtask 2");
    }

    @Test
    @DisplayName("Должен вернуть 404 для подзадач несуществующего эпика")
    void shouldReturn404ForSubtasksOfNonExistentEpic() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404 Not Found");
    }

    @Test
    @DisplayName("Должен обновить эпик")
    void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для обновления", "Описание");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Epic updatedEpic = new Epic("Обновленный эпик", "Новое описание");
        updatedEpic.setId(epicId); // Устанавливаем ID для обновления
        String updatedEpicJson = gson.toJson(updatedEpic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedEpicJson)) // POST для обновления
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // ИЗМЕНЕНО: Ожидаем 200 OK для обновления
        assertEquals(200, response.statusCode(), "Код ответа должен быть 200 OK для обновления");
        Epic retrievedEpic = taskManager.getEpic(epicId);
        assertNotNull(retrievedEpic, "Обновленный эпик не должен быть null");
        assertEquals("Обновленный эпик", retrievedEpic.getTitle(), "Название эпика должно быть обновлено");
        assertEquals("Новое описание", retrievedEpic.getDescription(), "Описание эпика должно быть обновлено");
    }
}