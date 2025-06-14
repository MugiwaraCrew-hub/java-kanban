package org.example.model;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        task1.setId(1); // Здесь id - Integer

        Task task2 = new Task("Task 2", "Description 2", TaskStatus.DONE, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        task2.setId(1); // Здесь id - Integer

        assertEquals(task1, task2, "Задачи с одинаковыми ID должны считаться равными");
    }

    @Test
    void tasksWithDifferentIdShouldNotBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", TaskStatus.DONE, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        task2.setId(2);

        assertNotEquals(task1, task2, "Задачи с разными ID не должны считаться равными");
    }

    @Test
    void tasksWithNullIdShouldNotBeEqual() { // Изменено название теста для ясности
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        task1.setId(null);

        Task task2 = new Task("Task 2", "Description 2", TaskStatus.DONE, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        task2.setId(null);

        // ИСПРАВЛЕНИЕ: Теперь мы ожидаем, что они НЕ будут равны, потому что это разные объекты
        // и у них нет общего идентифицирующего ID
        assertNotEquals(task1, task2, "Две разные задачи без присвоенного ID не должны считаться равными");

        Task task3 = new Task("Task 3", "Description 3", TaskStatus.IN_PROGRESS, Duration.ofMinutes(45), LocalDateTime.now().plusHours(2));
        task3.setId(3);

        assertNotEquals(task1, task3, "Задача с null ID не должна быть равна задаче с не-null ID");
    }
}