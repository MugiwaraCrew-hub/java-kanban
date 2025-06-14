package org.example.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        // Создаем Epic, так как Subtask требует epicId
        Epic epic = new Epic("Test Epic", "Epic Desc");
        epic.setId(100); // Присвоим ID для примера

        // ИСПРАВЛЕНО: Правильный порядок и типы аргументов для Subtask:
        // title, description, TaskStatus, epicId, Duration, LocalDateTime
        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW, epic.getId(), Duration.ofMinutes(30), LocalDateTime.now());
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", TaskStatus.IN_PROGRESS, epic.getId(), Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        subtask2.setId(1); // Тот же ID, что и у subtask1

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковыми ID должны считаться равными");
    }

    @Test
    void subtasksWithDifferentIdShouldNotBeEqual() {
        Epic epic = new Epic("Test Epic", "Epic Desc");
        epic.setId(100);

        // ИСПРАВЛЕНО: Правильный порядок и типы аргументов для Subtask
        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW, epic.getId(), Duration.ofMinutes(30), LocalDateTime.now());
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", TaskStatus.IN_PROGRESS, epic.getId(), Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        subtask2.setId(2); // Разный ID

        assertNotEquals(subtask1, subtask2, "Подзадачи с разными ID не должны считаться равными");
    }

    @Test
    void subtaskShouldHaveCorrectEpicId() {
        Epic epic = new Epic("Parent Epic", "Parent Description");
        epic.setId(5); // Устанавливаем ID для эпика

        // ИСПРАВЛЕНО: Правильный порядок и типы аргументов для Subtask
        Subtask subtask = new Subtask("Child Subtask", "Child Description", TaskStatus.NEW, epic.getId(), Duration.ofMinutes(60), LocalDateTime.now());
        subtask.setId(10);

        assertEquals(epic.getId(), subtask.getEpicId(), "ID эпика в подзадаче должен совпадать с ID родительского эпика");
    }
}