package org.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {
    @Test
    public void subTaskEqualed() {
        Subtask subtask = new Subtask("SubTask1", "Description1", 1, TaskStatus.NEW, 1);
        Subtask subtask1 = new Subtask("SubTask2", "Description2", 1, TaskStatus.NEW, 1);
        assertEquals(subtask, subtask1, "Задачи с одинаковым id должны быть равны");
    }

    @Test
    public void subTaskNotEquals() {
        Subtask subtask = new Subtask("SubTask1", "Description1", 1, TaskStatus.DONE, 1);
        Subtask subtask1 = new Subtask("SubTask1", "Description2", 2, TaskStatus.DONE, 1);
        assertNotEquals(subtask, subtask1, "Задачи с разными id не должны быть равны");
    }
}