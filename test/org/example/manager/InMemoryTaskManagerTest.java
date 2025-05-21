package org.example.manager;

import org.example.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void createTaskShouldReturnIdAndSaveTask() {
        TaskManager manager = createManager();
        Task task = new Task("Test", "Description", 0, TaskStatus.NEW);
        int taskId = manager.createTask(task);
        Task savedTask = manager.getTask(taskId);
        assertNotNull(savedTask, "Задача должна сохраняться");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Названия должны совпадать");
        assertNotEquals(0, taskId, "ID должен быть сгенерирован");
    }

    @Test
    void createEpicShouldReturnIdAndSaveEpic() {
        TaskManager manager = createManager();
        Epic epic = new Epic("Epic", "Description", 0);
        int epicId = manager.createEpic(epic);
        Epic savedEpic = manager.getEpic(epicId);
        assertNotNull(savedEpic, "Эпик должен сохраняться");
        assertEquals(epic.getTitle(), savedEpic.getTitle(), "Названия должны совпадать");
        assertNotEquals(0, epicId, "ID должен быть сгенерирован");
    }

    @Test
    void createSubtaskShouldReturnIdAndLinkToEpic() {
        TaskManager manager = createManager();
        Epic epic = new Epic("Epic", "Description", 0);
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", 0, TaskStatus.NEW, epicId);
        int subtaskId = manager.createSubtask(subtask);

        Subtask savedSubtask = manager.getSubtask(subtaskId);
        assertNotNull(savedSubtask, "Подзадача должна сохраняться");
        assertEquals(1, manager.getSubtasksByEpicId(epicId).size(), "Эпик должен содержать подзадачу");
        assertNotEquals(0, subtaskId, "ID должен быть сгенерирован");
    }

    @Test
    void addTaskShouldSaveTask() {
        TaskManager manager = createManager();
        Task task = new Task("Test", "Description", 0, TaskStatus.NEW);
        manager.addTask(task);
        Task savedTask = manager.getTask(task.getId());
        assertNotNull(savedTask, "Задача должна сохраняться");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Названия должны совпадать");
    }

    @Test
    void addEpicShouldSaveEpic() {
        TaskManager manager = createManager();
        Epic epic = new Epic("Epic", "Description", 0);
        manager.addEpic(epic);
        assertNotNull(manager.getEpic(epic.getId()), "Эпик должен сохраняться");
    }

    @Test
    void addSubtaskShouldSaveAndLinkToEpic() {
        TaskManager manager = createManager();
        Epic epic = new Epic("Epic", "Description", 0);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", 0, TaskStatus.NEW, epic.getId());
        manager.addSubtask(subtask);

        assertNotNull(manager.getSubtask(subtask.getId()), "Подзадача должна сохраняться");
        assertEquals(1, manager.getSubtasksByEpicId(epic.getId()).size(), "Эпик должен содержать подзадачу");
    }

    @Test
    void updateTaskStatusShouldBeSaved() {
        TaskManager manager = createManager();
        Task task = new Task("Task", "Description", 0, TaskStatus.NEW);
        manager.addTask(task);

        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);

        assertEquals(TaskStatus.DONE, manager.getTask(task.getId()).getStatus(), "Статус должен обновляться");
    }

    @Test
    void removeTaskShouldDeleteTask() {
        TaskManager manager = createManager();
        Task task = new Task("Task", "Description", 0, TaskStatus.NEW);
        manager.addTask(task);

        manager.removeTask(task.getId());

        assertNull(manager.getTask(task.getId()), "Задача должна удаляться");
    }

    @Test
    void getHistoryShouldReturnViewedTasks() {
        TaskManager manager = createManager();
        Task task = new Task("Task", "Description", 0, TaskStatus.NEW);
        manager.addTask(task);

        manager.getTask(task.getId());
        List<Task> history = manager.getHistory();

        assertEquals(1, history.size(), "История должна содержать просмотренные задачи");
        assertEquals(task.getId(), history.get(0).getId(), "История должна содержать правильную задачу");
    }
}
