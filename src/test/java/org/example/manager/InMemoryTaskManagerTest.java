package org.example.manager;

import org.example.model.Task;
import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
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
        Task task1 = new Task("Task1", "Description1", 1, TaskStatus.NEW); // Указываем id
        Task task2 = new Task("Task2", "Description2", 2, TaskStatus.NEW); // Указываем id
        Task task3 = new Task("Task3","Description3", 3, TaskStatus.NEW); // Указываем id

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task1.getId());
        manager.getTask(task3.getId());
        manager.getTask(task2.getId());

        List<Task> historyWatch = manager.getHistory();
        Assertions.assertEquals(3, historyWatch.size(), "Размер истории должен быть 3");
        Assertions.assertEquals(task1.getId(), historyWatch.get(0).getId(), "Первая в истории - task1");
        Assertions.assertEquals(task3.getId(), historyWatch.get(1).getId(), "Вторая в истории - task3");
        Assertions.assertEquals(task2.getId(), historyWatch.get(2).getId(), "Третья в истории - task2");
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


    @Test
    public void removeSubtaskUpdateEpic(){
        TaskManager manager = Managers.getDefault();
        Epic epic1 = new Epic("Эпик1","Описани1",0);
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Первая подзадача","Описание первой подзадачи",0,TaskStatus.NEW, epic1.getId());
        Subtask addedSubtask1 = manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Вторая подзадача","Описание второй подзадачи",0,TaskStatus.DONE, epic1.getId());
        Subtask addedSubtask2 = manager.addSubtask(subtask2);

        manager.removeSubtask(addedSubtask2.getId());
        Epic updatedEpic =  manager.getEpic(epic1.getId());

        Assertions.assertNull(manager.getSubtask(addedSubtask2.getId()), "Удаленная подзадача не должна существовать");
        Assertions.assertFalse(updatedEpic.getSubtaskIds().contains(addedSubtask2.getId()), "Id удаленной подзадачи не должно быть в списке эпика");
        Assertions.assertEquals(TaskStatus.NEW, updatedEpic.getStatus(), "Статус эпика должен быть NEW");
    }
    @Test
        public void checkRemoveEpic(){
            TaskManager manager = Managers.getDefault();
            Epic epic1 = new Epic("Эпик1","Описани1",0);
            manager.addEpic(epic1);

            Subtask subtask1 = new Subtask("Первая подзадача","Описание первой подзадачи",0,TaskStatus.NEW, epic1.getId());
            Subtask addSubtask1 = manager.addSubtask(subtask1);
            Subtask subtask2 = new Subtask("Вторая подзадача","Описание второй подзадачи",0,TaskStatus.DONE, epic1.getId());
            Subtask addSubtask2 = manager.addSubtask(subtask2);

            manager.removeEpic(epic1.getId());

            Assertions.assertNull(manager.getEpic(epic1.getId()), "Эпик должен быть удален");
            Assertions.assertNull(manager.getSubtask(addSubtask1.getId()), "Первая подзадача должна быть удалена");
            Assertions.assertNull(manager.getSubtask(addSubtask2.getId()), "Вторая подзадача должна быть удалена");
        }
    @Test
    public void copySetterCheckProblem() {
        TaskManager manager = Managers.getDefault();
        Epic epic1 = new Epic("Эпик1","Описани1",0);
        manager.addEpic(epic1);
        epic1.setTitle("Описание2");
        Epic newEpic1 = manager.getEpic(epic1.getId());
        Assertions.assertNotEquals(epic1.getTitle(), newEpic1.getTitle(), "Название эпика в менеджере НЕ должно измениться");
    }
}