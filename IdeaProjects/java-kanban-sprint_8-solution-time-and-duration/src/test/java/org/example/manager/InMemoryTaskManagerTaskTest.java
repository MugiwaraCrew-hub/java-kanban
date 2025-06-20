package org.example.manager;

import org.example.model.Task;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration; // Добавлен импорт Duration
import java.time.LocalDateTime; // Добавлен импорт LocalDateTime
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTaskTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateTask() {
        // ИСПРАВЛЕНО: Теперь используется конструктор Task(String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime)
        Task task = new Task("Test", "Test description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now()); // Добавлены Duration и LocalDateTime
        int id = taskManager.createTask(task);
        Task saved = taskManager.getTask(id);

        assertNotNull(saved);
        assertEquals("Test", saved.getTitle());
        assertEquals("Test description", saved.getDescription());
        assertEquals(TaskStatus.NEW, saved.getStatus());
        assertEquals(Duration.ofMinutes(30), saved.getDuration()); // Добавлена проверка Duration
        assertNotNull(saved.getStartTime()); // Добавлена проверка startTime
    }

    @Test
    void shouldUpdateTask() {
        // ИСПРАВЛЕНО: Теперь используется конструктор Task с Duration и LocalDateTime
        Task task = new Task("Test", "Test description", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.now());
        int id = taskManager.createTask(task);

        // ИСПРАВЛЕНО: Обновленная задача тоже должна использовать конструктор с Duration и LocalDateTime
        // Также убрал лишний 'id' в конструкторе, он устанавливается методом setId
        Task updatedTask = new Task("Updated", "Updated description", TaskStatus.IN_PROGRESS,
                Duration.ofMinutes(90), LocalDateTime.now().plusHours(1));
        updatedTask.setId(id); // Устанавливаем ID для обновления
        taskManager.updateTask(updatedTask);

        Task saved = taskManager.getTask(id);
        assertNotNull(saved);
        assertEquals("Updated", saved.getTitle());
        assertEquals("Updated description", saved.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, saved.getStatus());
        assertEquals(Duration.ofMinutes(90), saved.getDuration()); // Проверка обновленной длительности
        assertNotNull(saved.getStartTime()); // Проверка обновленного времени
    }

    @Test
    void shouldRemoveTaskById() {
        // ИСПРАВЛЕНО: Используем корректный конструктор
        Task task = new Task("Test", "Test description", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.now());
        int id = taskManager.createTask(task);

        taskManager.removeTask(id);
        Task removed = taskManager.getTask(id);

        assertNull(removed);
    }

    @Test
    void shouldReturnAllTasks() {
        // ИСПРАВЛЕНО: Используем корректные конструкторы
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.now().plusMinutes(30));

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> allTasks = taskManager.getAllTasks();
        assertEquals(2, allTasks.size());
        assertTrue(allTasks.contains(task1)); // Убедимся, что задачи есть в списке
        assertTrue(allTasks.contains(task2));
    }

    @Test
    void shouldClearAllTasks() {
        // ИСПРАВЛЕНО: Используем корректный конструктор
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW,
                Duration.ofMinutes(40), LocalDateTime.now());
        taskManager.createTask(task1);

        taskManager.removeAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }
}