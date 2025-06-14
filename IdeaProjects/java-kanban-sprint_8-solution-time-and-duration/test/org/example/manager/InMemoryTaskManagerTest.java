package org.example.manager;

import org.example.model.Task;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration; // Добавлен импорт Duration
import java.time.LocalDateTime; // Добавлен импорт LocalDateTime
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    protected TaskManager taskManager; // Используем интерфейс TaskManager

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateTask() {
        // ИСПРАВЛЕНО: Добавлены Duration и LocalDateTime.now(), статус теперь TaskStatus.NEW
        Task task = new Task("Test", "Test description", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        int id = taskManager.createTask(task);
        Task saved = taskManager.getTask(id);

        assertNotNull(saved, "Задача не должна быть null после создания");
        assertEquals("Test", saved.getTitle(), "Название задачи должно совпадать");
        assertEquals("Test description", saved.getDescription(), "Описание задачи должно совпадать");
        assertEquals(TaskStatus.NEW, saved.getStatus(), "Статус задачи должен быть NEW");
        assertEquals(Duration.ofMinutes(30), saved.getDuration(), "Длительность задачи должна совпадать");
        assertNotNull(saved.getStartTime(), "Время начала задачи не должно быть null");
    }

    @Test
    void shouldUpdateTask() {
        // ИСПРАВЛЕНО: Добавлены Duration и LocalDateTime.now(), статус теперь TaskStatus.NEW
        Task task = new Task("Test", "Test description", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        int id = taskManager.createTask(task);

        // ИСПРАВЛЕНО: Добавлены Duration и LocalDateTime.now().plusHours(1), статус теперь TaskStatus.IN_PROGRESS
        Task updatedTask = new Task("Updated", "Updated description", TaskStatus.IN_PROGRESS, Duration.ofMinutes(90), LocalDateTime.now().plusHours(1));
        updatedTask.setId(id); // Важно установить ID для обновления
        taskManager.updateTask(updatedTask);

        Task saved = taskManager.getTask(id);
        assertNotNull(saved, "Обновленная задача не должна быть null");
        assertEquals("Updated", saved.getTitle(), "Название задачи должно быть обновлено");
        assertEquals("Updated description", saved.getDescription(), "Описание задачи должно быть обновлено");
        assertEquals(TaskStatus.IN_PROGRESS, saved.getStatus(), "Статус задачи должен быть IN_PROGRESS");
        assertEquals(Duration.ofMinutes(90), saved.getDuration(), "Длительность задачи должна быть обновлена");
        assertNotNull(saved.getStartTime(), "Время начала задачи не должно быть null после обновления");
    }

    @Test
    void shouldRemoveTaskById() {
        // ИСПРАВЛЕНО: Добавлены Duration и LocalDateTime.now(), статус теперь TaskStatus.NEW
        Task task = new Task("Test", "Test description", TaskStatus.NEW, Duration.ofMinutes(45), LocalDateTime.now());
        int id = taskManager.createTask(task);

        taskManager.removeTask(id);
        Task removed = taskManager.getTask(id);

        assertNull(removed, "Задача должна быть null после удаления");
    }

    @Test
    void shouldReturnAllTasks() {
        // ИСПРАВЛЕНО: Добавлены Duration и LocalDateTime.now(), статус теперь TaskStatus.NEW
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(20));

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> allTasks = taskManager.getAllTasks();
        assertNotNull(allTasks, "Список всех задач не должен быть null");
        assertEquals(2, allTasks.size(), "Должны быть возвращены все 2 задачи");
        assertTrue(allTasks.contains(task1), "Список должен содержать task1");
        assertTrue(allTasks.contains(task2), "Список должен содержать task2");
    }

    @Test
    void shouldClearAllTasks() {
        // ИСПРАВЛЕНО: Добавлены Duration и LocalDateTime.now(), статус теперь TaskStatus.NEW
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task1);

        taskManager.removeAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач должен быть пуст после очистки");
    }

    // Тесты на пересечение задач (дополнительно, если это еще не было сделано)
    @Test
    void shouldNotAllowIntersectingTasks() {
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        taskManager.createTask(task1);

        // Задача, которая начинается до окончания task1, но пересекается с ней
        Task intersectingTask1 = new Task("Intersecting 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 30));
        assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(intersectingTask1),
                "Должно быть выброшено исключение при создании пересекающейся задачи");

        // Задача, которая полностью находится внутри task1
        Task intersectingTask2 = new Task("Intersecting 2", "Desc 2", TaskStatus.NEW, Duration.ofMinutes(15), LocalDateTime.of(2025, 1, 1, 10, 15));
        assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(intersectingTask2),
                "Должно быть выброшено исключение при создании задачи, полностью внутри другой");

        // Задача, которая начинается ровно в момент окончания task1 - это допустимо
        Task nonIntersectingTask = new Task("Non-Intersecting", "Desc", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 11, 0));
        assertDoesNotThrow(() -> taskManager.createTask(nonIntersectingTask),
                "Задача, начинающаяся после окончания другой, не должна вызывать исключение");

        // Проверяем, что добавлена только одна задача (task1) и nonIntersectingTask
        assertEquals(2, taskManager.getAllTasks().size(), "Должно быть 2 задачи после попыток добавления");
    }

    @Test
    void shouldNotAllowUpdatingToIntersectingTime() {
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        int task1Id = taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 12, 0));
        int task2Id = taskManager.createTask(task2);

        // Попытка обновить task2 так, чтобы она пересекалась с task1
        Task updatedTask2 = new Task(task2Id, "Updated Task 2", "Desc 2", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 30));
        assertThrows(IllegalArgumentException.class, () -> taskManager.updateTask(updatedTask2),
                "Должно быть выброшено исключение при обновлении на пересекающееся время");
    }
}