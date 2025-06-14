package org.example.server;

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

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest extends HttpTaskServerTest {

    @Test
    @DisplayName("Должен создать задачу")
    void shouldCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Тестовая задача", "Описание", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201");
        List<Task> tasks = taskManager.getAllTasks();
        assertNotNull(tasks, "Задачи не должны быть null");
        assertEquals(1, tasks.size(), "Должна быть одна задача");
        assertEquals("Тестовая задача", tasks.get(0).getTitle(), "Название задачи должно совпадать");
    }

    @Test
    @DisplayName("Должен вернуть 406 при создании задачи с пересечением")
    void shouldReturn406WhenCreatingTaskWithIntersection() throws IOException, InterruptedException {
        Task existingTask = new Task("Существующая задача", "Описание", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        taskManager.createTask(existingTask);

        Task newTask = new Task("Пересекающаяся задача", "Описание", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now().plusMinutes(10));
        String taskJson = gson.toJson(newTask);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Код ответа должен быть 406");
        assertEquals(1, taskManager.getAllTasks().size(), "Не должна быть создана новая задача");
    }

    @Test
    @DisplayName("Должен обновить задачу")
    void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Старая задача", "Старое описание", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        taskManager.createTask(task);

        // Получаем оригинальную задачу из менеджера, чтобы убедиться, что изменения в 'task' не повлияют на нее
        Task originalTaskFromManager = taskManager.getTask(task.getId());
        assertNotNull(originalTaskFromManager, "Оригинальная задача не должна быть null");

        // Теперь изменяем 'task' - это объект, который будет отправлен в запросе
        task.setTitle("Новая задача");
        task.setStatus(TaskStatus.DONE);
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        Task updatedTask = taskManager.getTask(task.getId());
        assertEquals("Новая задача", updatedTask.getTitle(), "Название задачи должно быть обновлено");
        assertEquals(TaskStatus.DONE, updatedTask.getStatus(), "Статус задачи должен быть обновлен");
    }

    @Test
    @DisplayName("Должен вернуть 406 при обновлении задачи с пересечением")
    void shouldReturn406WhenUpdatingTaskWithIntersection() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        taskManager.createTask(task1);

        Task task2 = new Task("Задача 2", "Описание", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now().plusHours(2));
        taskManager.createTask(task2);

        // ИСПРАВЛЕНО: Сохраняем начальное время task1, чтобы сравнивать с ним
        LocalDateTime initialTask1StartTime = task1.getStartTime();

        // Обновляем task1 так, чтобы она пересекалась с task2
        // Важно: создаем НОВЫЙ объект для отправки в запросе, чтобы не менять оригинальный объект в менеджере до вызова update.
        Task updatedTask1Data = new Task(
                task1.getId(),
                "Задача 1 (Обновлено)",
                "Описание (Обновлено)",
                TaskStatus.IN_PROGRESS,
                Duration.ofMinutes(60),
                LocalDateTime.now().plusMinutes(90) // Пересекается с task2
        );
        String taskJson = gson.toJson(updatedTask1Data);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Код ответа должен быть 406");

        // Получаем задачу 1 из менеджера ПОСЛЕ попытки обновления
        Task task1AfterAttempt = taskManager.getTask(task1.getId());

        // Сравниваем время начала задачи в менеджере с тем, которое было ДО попытки обновления
        assertEquals(initialTask1StartTime, task1AfterAttempt.getStartTime(), "Время начала задачи не должно было измениться после неудачного обновления");
    }

    @Test
    @DisplayName("Должен вернуть все задачи")
    void shouldGetAllTasks() throws IOException, InterruptedException {
        taskManager.createTask(new Task("Задача 1", "Описание 1", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now()));
        taskManager.createTask(new Task("Задача 2", "Описание 2", TaskStatus.IN_PROGRESS, Duration.ofMinutes(60), LocalDateTime.now().plusHours(1)));

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        List<Task> tasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(tasks, "Список задач не должен быть null");
        assertEquals(2, tasks.size(), "Должно быть две задачи");
    }

    @Test
    @DisplayName("Должен вернуть задачу по ID")
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Тестовая задача", "Описание", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        taskManager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        Task returnedTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(returnedTask, "Задача не должна быть null");
        assertEquals(task.getId(), returnedTask.getId(), "ID задач должны совпадать");
    }

    @Test
    @DisplayName("Должен вернуть 404 для несуществующей задачи")
    void shouldReturn404ForNonExistentTask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
    }

    @Test
    @DisplayName("Должен удалить задачу по ID")
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Тестовая задача", "Описание", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        taskManager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertNull(taskManager.getTask(task.getId()), "Задача должна быть удалена из менеджера");
    }

    @Test
    @DisplayName("Должен удалить все задачи")
    void shouldDeleteAllTasks() throws IOException, InterruptedException {
        taskManager.createTask(new Task("Задача 1", "Описание 1", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now()));
        taskManager.createTask(new Task("Задача 2", "Описание 2", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now().plusHours(1)));

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
    }
}
