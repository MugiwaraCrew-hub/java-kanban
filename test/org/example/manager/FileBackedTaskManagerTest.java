import org.example.manager.FileBackedTaskManager;
import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private FileBackedTaskManager fileBackedTaskManager;
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        // Создаем временный файл для тестирования
        Path tempFile = Files.createTempFile("task_manager_test", ".csv");
        testFile = tempFile.toFile();
        testFile.deleteOnExit(); // Удаляем файл после завершения теста

        // Инициализируем FileBackedTaskManager с временным файлом
        fileBackedTaskManager = new FileBackedTaskManager(testFile);
    }

    @Test
    void testAddTask() {
        Task task = new Task("Test Task", "Test Description", 0, TaskStatus.NEW);
        int taskId = fileBackedTaskManager.createTask(task);
        Task savedTask = fileBackedTaskManager.getTask(taskId);
        assertEquals(task, savedTask, "Задача должна быть сохранена и получена корректно");
    }

    @Test
    void testAddEpicAndSubtask() {
        Epic epic = new Epic("Test Epic", "Test Epic Description", 0);
        int epicId = fileBackedTaskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Test Subtask Description", 0, TaskStatus.NEW, epicId);
        int subtaskId = fileBackedTaskManager.createSubtask(subtask);

        Epic savedEpic = fileBackedTaskManager.getEpic(epicId);
        Subtask savedSubtask = fileBackedTaskManager.getSubtask(subtaskId);

        assertEquals(epic, savedEpic, "Эпик должен быть сохранен и получен корректно");
        assertEquals(subtask, savedSubtask, "Подзадача должна быть сохранена и получена корректно");
        assertEquals(1, savedEpic.getSubtaskIds().size(), "Эпик должен содержать ID подзадачи");
        assertEquals(subtaskId, savedEpic.getSubtaskIds().get(0), "ID подзадачи должен совпадать");
    }

    @Test
    void testSaveAndLoadTasks() {
        // Создаем несколько задач, эпиков и подзадач
        Task task1 = new Task("Task 1", "Description 1", 0, TaskStatus.NEW);
        int taskId1 = fileBackedTaskManager.createTask(task1);

        Epic epic1 = new Epic("Epic 1", "Epic Description 1", 0);
        int epicId1 = fileBackedTaskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description 1", 0, TaskStatus.NEW, epicId1);
        int subtaskId1 = fileBackedTaskManager.createSubtask(subtask1);

        // Сохраняем задачи в файл
        fileBackedTaskManager.save();

        // Загружаем задачи из файла через статический метод loadFromFile
        FileBackedTaskManager loadedTaskManager = FileBackedTaskManager.loadFromFile(testFile);

        List<Task> loadedTasks = loadedTaskManager.getAllTasks();
        List<Epic> loadedEpics = loadedTaskManager.getAllEpics();
        List<Subtask> loadedSubtasks = loadedTaskManager.getAllSubtasks();

        assertEquals(1, loadedTasks.size(), "Должна быть загружена 1 задача");
        assertEquals(1, loadedEpics.size(), "Должен быть загружен 1 эпик");
        assertEquals(1, loadedSubtasks.size(), "Должна быть загружена 1 подзадача");

        assertEquals(task1, loadedTaskManager.getTask(taskId1), "Загруженная задача должна совпадать с исходной");
        assertEquals(epic1, loadedTaskManager.getEpic(epicId1), "Загруженный эпик должен совпадать с исходным");
        assertEquals(subtask1, loadedTaskManager.getSubtask(subtaskId1), "Загруженная подзадача должна совпадать с исходной");
    }
}
