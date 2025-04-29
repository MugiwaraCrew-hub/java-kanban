package org.example.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void taskTestEqualId() {
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", 1, TaskStatus.DONE);
        assertEquals(task1, task2, "Задачи с одинаковым АЙДИ должны быть равны");
    }

    @Test
    public void taskTestNotEqualsId() {
        Task task1 = new Task("Task1", "Description 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description 2", 2, TaskStatus.NEW);
        assertNotEquals(task1, task2, "разные АЙДИ не должны быть равны");
    }

    @Test
    public void taskFieldsShouldBeCorrect() {
        Task task = new Task("Test", "Desc", 1, TaskStatus.NEW);
        assertEquals("Test", task.getTitle());
        assertEquals("Desc", task.getDescription());
        assertEquals(TaskStatus.NEW, task.getStatus());
        assertEquals(1, task.getId());
    }
}