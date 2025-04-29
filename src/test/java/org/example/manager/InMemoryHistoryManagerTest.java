package org.example.manager;

import org.junit.jupiter.api.Test;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    @Test
    public void shouldAddTasksToHistory() {
        InMemoryHistoryManager copyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Задача 1", "Описание 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", 2, TaskStatus.IN_PROGRESS);
        Task task3 = new Task("Задача 3", "Описание 3", 3, TaskStatus.DONE);
        copyManager.add(task1);
        copyManager.add(task2);
        copyManager.add(task3);
        List<Task> history = copyManager.getHistory();

        assertEquals(3, history.size(), "Размер истории должен быть равен 3");
        assertEquals(task1.getId(), history.get(0).getId(), "Первая задача в истории должна быть task1");
        assertEquals(task2.getId(), history.get(1).getId(), "Вторая задача в истории должна быть task2");
        assertEquals(task3.getId(), history.get(2).getId(), "Третья задача в истории должна быть task3");
    }

    @Test
    public void historyRepeat() {
        InMemoryHistoryManager copyManager = new InMemoryHistoryManager();
        Task story1 = new Task("Первая история 1", "Просмотр Ван Пис", 1, TaskStatus.NEW);
        copyManager.add(story1);
        copyManager.add(story1);
        copyManager.add(story1);
        List<Task> history = copyManager.getHistory();
        assertEquals(1, history.size(), "Размер истории при повторном просмотре должен быть 1");
        assertEquals(story1.getId(), history.get(0).getId(), "В истории должна быть задача story1");
    }

    @Test
    public void removeHistory() {
        InMemoryHistoryManager copyManager = new InMemoryHistoryManager();
        Task story1 = new Task("Первая история 1", "Наруто", 1, TaskStatus.NEW);
        Task story2 = new Task("Вторая история 2", "Ван-Пис", 2, TaskStatus.NEW);
        Task story3 = new Task("Третья история 3", "Хантер х Хантер", 3, TaskStatus.NEW);
        copyManager.add(story1);
        copyManager.add(story2);
        copyManager.add(story3);
        copyManager.remove(2);
        List<Task> afterDelete = copyManager.getHistory();
        assertEquals(2, afterDelete.size(), "Размер после удаления = 2");
        assertEquals(story1.getId(), afterDelete.get(0).getId(), "Первая задача после удаления должна быть story1");
        assertEquals(story3.getId(), afterDelete.get(1).getId(), "Вторая задача после удаления должна быть story3");
    }

    @Test
    public void addNullTaskShouldNotChangeHistory() {
        InMemoryHistoryManager copyManager = new InMemoryHistoryManager();
        copyManager.add(null);
        List<Task> history = copyManager.getHistory();
        assertTrue(history.isEmpty(), "Добавление null не должно менять историю");
    }
}