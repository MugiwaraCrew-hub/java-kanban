package org.example.manager;

import org.example.model.Task;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    @Test
    void testAddTask() {
        // Тестовая реализация
    }

    @Test
    void testGetHistoryWithEmptyList() {
        TaskManager manager = new InMemoryTaskManager();
        List<Task> history = manager.getHistory();
        
        assertNotNull(history, "История не должна быть null");
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void testRemoveTask() {
        TaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Test", "Description");
        manager.addTask(task);
        
        manager.removeTask(task.getId());
        assertNull(manager.getTaskById(task.getId()), "Задача должна быть удалена");
    }
}
