package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerSubtaskTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateAndGetSubtask() {
        Epic epic = new Epic("Epic", "Epic Desc");
        int epicId = taskManager.createEpic(epic);

        // ИСПРАВЛЕНО: Теперь используется TaskStatus.NEW
        Subtask subtask = new Subtask("Subtask", "Subtask Desc", TaskStatus.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.now());
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask retrieved = taskManager.getSubtask(subtaskId);

        assertNotNull(retrieved, "Подзадача не должна быть null");
        assertEquals("Subtask", retrieved.getTitle(), "Название подзадачи должно совпадать");
        assertEquals("Subtask Desc", retrieved.getDescription(), "Описание подзадачи должно совпадать");
        assertEquals(epicId, retrieved.getEpicId(), "ID эпика должно совпадать");
        assertEquals(TaskStatus.NEW, retrieved.getStatus(), "Статус подзадачи должен совпадать");
        assertEquals(Duration.ofMinutes(20), retrieved.getDuration(), "Длительность подзадачи должна совпадать");
        assertNotNull(retrieved.getStartTime(), "Время начала подзадачи не должно быть null");
    }

    @Test
    void shouldUpdateSubtask() {
        Epic epic = new Epic("Epic", "Epic Desc");
        int epicId = taskManager.createEpic(epic);

        // ИСПРАВЛЕНО: Теперь используется TaskStatus.NEW
        Subtask subtask = new Subtask("Subtask", "Subtask Desc", TaskStatus.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.now());
        int subtaskId = taskManager.createSubtask(subtask);

        // ИСПРАВЛЕНО: Теперь используется TaskStatus.DONE
        Subtask updated = new Subtask("Updated", "Updated Desc", TaskStatus.DONE, epicId,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        updated.setId(subtaskId); // Важно установить ID, чтобы менеджер знал, что обновлять
        taskManager.updateSubtask(updated);

        Subtask retrieved = taskManager.getSubtask(subtaskId);
        assertNotNull(retrieved, "Обновленная подзадача не должна быть null");
        assertEquals("Updated", retrieved.getTitle(), "Название подзадачи должно быть обновлено");
        assertEquals("Updated Desc", retrieved.getDescription(), "Описание подзадачи должно быть обновлено");
        assertEquals(TaskStatus.DONE, retrieved.getStatus(), "Статус подзадачи должен быть обновлен");
        assertEquals(Duration.ofMinutes(30), retrieved.getDuration(), "Длительность подзадачи должна быть обновлена");
        assertNotNull(retrieved.getStartTime(), "Время начала подзадачи не должно быть null после обновления");
    }

    @Test
    void shouldRemoveSubtask() {
        Epic epic = new Epic("Epic", "Epic Desc");
        int epicId = taskManager.createEpic(epic);

        // ИСПРАВЛЕНО: Теперь используется TaskStatus.NEW
        Subtask subtask = new Subtask("Subtask", "Subtask Desc", TaskStatus.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.now());
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.removeSubtask(subtaskId);
        assertNull(taskManager.getSubtask(subtaskId), "Подзадача должна быть удалена");
        Epic epicAfterRemoval = taskManager.getEpic(epicId);
        assertNotNull(epicAfterRemoval, "Эпик не должен быть удален");
        assertFalse(epicAfterRemoval.getSubtaskIds().contains(subtaskId), "ID подзадачи должен быть удален из эпика");
    }

    @Test
    void shouldReturnAllSubtasks() {
        Epic epic = new Epic("Epic", "Epic Desc");
        int epicId = taskManager.createEpic(epic);

        // ИСПРАВЛЕНО: Теперь используется TaskStatus.NEW
        Subtask sub1 = new Subtask("Sub 1", "Desc 1", TaskStatus.NEW, epicId,
                Duration.ofMinutes(15), LocalDateTime.now());
        // ИСПРАВЛЕНО: Теперь используется TaskStatus.NEW
        Subtask sub2 = new Subtask("Sub 2", "Desc 2", TaskStatus.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.now().plusHours(1));

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertEquals(2, subtasks.size(), "Должны быть возвращены все 2 подзадачи");
        // Эти assert'ы теперь должны работать корректно, так как equals/hashCode в Subtask исправлены.
        assertTrue(subtasks.contains(sub1), "Список должен содержать sub1");
        assertTrue(subtasks.contains(sub2), "Список должен содержать sub2");
    }

    @Test
    void shouldClearAllSubtasks() {
        Epic epic = new Epic("Epic", "Epic Desc");
        int epicId = taskManager.createEpic(epic);

        // ИСПРАВЛЕНО: Теперь используется TaskStatus.NEW
        Subtask sub = new Subtask("Sub", "Desc", TaskStatus.NEW, epicId,
                Duration.ofMinutes(15), LocalDateTime.now());
        taskManager.createSubtask(sub);

        taskManager.removeAllSubtasks();
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пуст");
        Epic epicAfterClear = taskManager.getEpic(epicId);
        assertNotNull(epicAfterClear, "Эпик не должен быть удален");
        assertTrue(epicAfterClear.getSubtaskIds().isEmpty(), "Список ID подзадач в эпике должен быть пуст");
    }
}