package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    protected TaskManager taskManager;
    private File file;

    @BeforeEach
    void setUp() {
        try {
            file = Files.createTempFile("test_tasks", ".csv").toFile();
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
        taskManager = new FileBackedTaskManager(file);
    }

    @AfterEach
    void tearDown() {
        if (file != null && file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                System.err.println("Не удалось удалить временный файл: " + e.getMessage());
            }
        }
    }

    @Test
    void shouldCreateAndGetTask() {
        Task task = new Task("Test Task", "Test Desc", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = taskManager.createTask(task);

        Task retrieved = taskManager.getTask(taskId);

        assertNotNull(retrieved);
        assertEquals("Test Task", retrieved.getTitle());
        assertEquals("Test Desc", retrieved.getDescription());
        assertEquals(TaskStatus.NEW, retrieved.getStatus());
        assertEquals(Duration.ofMinutes(30), retrieved.getDuration());
        assertNotNull(retrieved.getStartTime());
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Task for update", "Desc for update", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        int taskId = taskManager.createTask(task);

        Task updatedTask = new Task("Updated Task", "Updated Desc", TaskStatus.DONE, Duration.ofMinutes(90), LocalDateTime.now().plusHours(1));
        updatedTask.setId(taskId);
        taskManager.updateTask(updatedTask);

        Task retrieved = taskManager.getTask(taskId);
        assertNotNull(retrieved);
        assertEquals("Updated Task", retrieved.getTitle());
        assertEquals("Updated Desc", retrieved.getDescription());
        assertEquals(TaskStatus.DONE, retrieved.getStatus());
        assertEquals(Duration.ofMinutes(90), retrieved.getDuration());
        assertNotNull(retrieved.getStartTime());
    }

    @Test
    void shouldRemoveTask() {
        Task task = new Task("Task to remove", "Desc to remove", TaskStatus.NEW, Duration.ofMinutes(45), LocalDateTime.now());
        int taskId = taskManager.createTask(task);

        taskManager.removeTask(taskId);
        assertNull(taskManager.getTask(taskId));
    }

    @Test
    void shouldReturnAllTasks() {
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(20));

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
    }

    @Test
    void shouldClearAllTasks() {
        Task task = new Task("Task to clear", "Desc to clear", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task);

        taskManager.removeAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
        assertTrue(loadedManager.getHistory().isEmpty());
        assertEquals(0, loadedManager.idCounter);
    }

    @Test
    void shouldSaveAndLoadTasksFromFile() {
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.IN_PROGRESS, Duration.ofMinutes(20), LocalDateTime.now().plusHours(1));
        taskManager.createTask(task2);

        taskManager.getTask(task1.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> loadedTasks = loadedManager.getAllTasks();
        assertEquals(2, loadedTasks.size());

        List<Task> loadedHistory = loadedManager.getHistory();
        assertEquals(1, loadedHistory.size());
        assertEquals(task1.getId(), loadedHistory.get(0).getId());
    }

    @Test
    void shouldSaveAndLoadEpicsAndSubtasksFromFile() {
        Epic epic1 = new Epic("Epic 1", "Epic Desc 1");
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Desc 1", TaskStatus.NEW, epic1.getId(), Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask Desc 2", TaskStatus.DONE, epic1.getId(), Duration.ofMinutes(20), LocalDateTime.now().plusHours(1));
        taskManager.createSubtask(subtask2);

        taskManager.getEpic(epic1.getId());
        taskManager.getSubtask(subtask1.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Epic> loadedEpics = loadedManager.getAllEpics();
        assertEquals(1, loadedEpics.size());
        assertEquals(epic1.getId(), loadedEpics.get(0).getId());

        List<Subtask> loadedSubtasks = loadedManager.getAllSubtasks();
        assertEquals(2, loadedSubtasks.size());

        Epic loadedEpic = loadedManager.getEpic(epic1.getId());
        assertEquals(2, loadedEpic.getSubtaskIds().size());
        assertTrue(loadedEpic.getSubtaskIds().contains(subtask1.getId()));
        assertTrue(loadedEpic.getSubtaskIds().contains(subtask2.getId()));
        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.getStatus());

        List<Task> loadedHistory = loadedManager.getHistory();
        assertEquals(2, loadedHistory.size());
        assertTrue(loadedHistory.stream().anyMatch(t -> t.getId() == epic1.getId()));
        assertTrue(loadedHistory.stream().anyMatch(t -> t.getId() == subtask1.getId()));
    }

    @Test
    void shouldLoadEmptyHistoryIfNoTasksViewed() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loadedManager.getHistory().isEmpty());
    }

    @Test
    void shouldRestoreIdCounterCorrectly() {
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createTask(task1);
        Epic epic1 = new Epic("Epic 1", "Epic Desc 1");
        taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Desc 1", TaskStatus.NEW, epic1.getId(), Duration.ofMinutes(10), LocalDateTime.now().plusHours(1));
        taskManager.createSubtask(subtask1);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(3, loadedManager.idCounter);

        Task newTask = new Task("New Task", "New Desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusHours(2));
        loadedManager.createTask(newTask);
        assertEquals(4, newTask.getId());
    }

    @Test
    void shouldThrowExceptionOnIntersectionWhenCreatingNewTask() throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,duration,startTime,epic\n");
            writer.write("1,TASK,Task 1,NEW,Desc 1,60,2025-01-01T10:00:00,\n");
        }

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Task intersectingTask = new Task("Intersecting", "Desc", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 30));
        assertThrows(IllegalArgumentException.class, () -> loadedManager.createTask(intersectingTask));
    }
}
