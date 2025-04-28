package org.example.manager;

import org.example.model.Task;
import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;


public class InMemoryTaskManagerTest {
    @Test
    public void testAddAndGetTask() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Task", "Description", 0, TaskStatus.NEW);
        manager.addTask(task);
        Task foundTask = manager.getTask(task.getId());
        assertNotNull(foundTask, "Задача должна быть найдена");
        assertEquals(task, foundTask, "Задача должна быть найдена по id");
    }

    @Test
    public void testAddEpicAndSubtask() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description", 0);
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", 0, TaskStatus.NEW, epic.getId());
        manager.addSubtask(subtask);
        Epic foundEpic = manager.getEpic(epic.getId());
        Subtask foundSubtask = manager.getSubtask(subtask.getId());
        assertNotNull(foundEpic, "Эпик должен быть найден");
        assertNotNull(foundSubtask, "Подзадача должна быть найдена");
        assertEquals(epic, foundEpic, "Эпик должен совпадать");
        assertEquals(subtask, foundSubtask, "Подзадача должна совпадать");
    }

    @Test
    public void testEpicCannotBeOwnSubtask() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description", 0);
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", 0, TaskStatus.NEW, epic.getId());
        manager.addSubtask(subtask);
        assertNotEquals(subtask.getId(), subtask.getEpicId(), "Подзадача не должна иметь epicId, равный её собственному id");
    }

    @Test
    public void testSubtaskCannotBeOwnEpic() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description", 0);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask1", "Description", 0, TaskStatus.NEW, epic.getId());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask2", "Description", 0, TaskStatus.NEW, subtask1.getId());
        Subtask result = manager.addSubtask(subtask2);
        assertNull(result, "Подзадача не должна добавляться, если epicId указывает на другую подзадачу");
    }

    @Test
    public void testNoIdConflicts() {
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Task1", "Description1", 5, TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", 5, TaskStatus.NEW);
        manager.addTask(task1);
        manager.addTask(task2);
        assertNotEquals(task1.getId(), task2.getId(), "Задачи с одинаковым заданным id должны иметь разные сгенерированные id");
        assertNotNull(manager.getTask(task1.getId()), "Первая задача должна быть найдена");
        assertNotNull(manager.getTask(task2.getId()), "Вторая задача должна быть найдена");
    }

    @Test
    public void testHistorySaving() {
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Task1", "Description1", 0, TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", 0, TaskStatus.NEW);

        manager.addTask(task1);
        manager.addTask(task2);

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task1.getId());

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size(), "История должна содержать все просмотры");
    }

    @Test
    public void testManagersReturnsInitializedInstances() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "TaskManager должен быть проинициализирован");
    }

    @Test
    public void testTaskImmutabilityOnAdd() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Task", "Description", 0, TaskStatus.NEW);
        String originalTitle = task.getTitle();
        String originalDescription = task.getDescription();
        TaskStatus originalStatus = task.getStatus();
        manager.addTask(task);
        assertEquals(originalTitle, task.getTitle(), "Название не должно измениться");
        assertEquals(originalDescription, task.getDescription(), "Описание не должно измениться");
        assertEquals(originalStatus, task.getStatus(), "Статус не должен измениться");
    }

    @Test
    public void testHistoryPreservesTaskData() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Task", "Description", 0, TaskStatus.NEW);
        manager.addTask(task);
        manager.getTask(task.getId());
        List<Task> history = manager.getHistory();
        Task historyTask = history.get(0);
        assertEquals(task.getTitle(), historyTask.getTitle(), "Название в истории должно совпадать");
    }
}