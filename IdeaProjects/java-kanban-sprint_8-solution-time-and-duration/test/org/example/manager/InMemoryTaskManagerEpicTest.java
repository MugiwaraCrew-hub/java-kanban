package org.example.manager;

import org.example.model.Epic;
import org.example.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerEpicTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic epic = new Epic("Epic Test", "Epic Description");
        int epicId = taskManager.createEpic(epic);

        Epic retrieved = taskManager.getEpic(epicId);

        assertNotNull(retrieved);
        assertEquals("Epic Test", retrieved.getTitle());
        assertEquals("Epic Description", retrieved.getDescription());
        assertEquals(TaskStatus.NEW, retrieved.getStatus());
        assertTrue(retrieved.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldUpdateEpic() {
        Epic epic = new Epic("Original", "Original Desc");
        int epicId = taskManager.createEpic(epic);

        Epic updated = new Epic("Updated", "Updated Desc");
        updated.setId(epicId);
        taskManager.updateEpic(updated);

        Epic retrieved = taskManager.getEpic(epicId);
        assertNotNull(retrieved);
        assertEquals("Updated", retrieved.getTitle());
        assertEquals("Updated Desc", retrieved.getDescription());
    }

    @Test
    void shouldRemoveEpic() {
        Epic epic = new Epic("ToRemove", "ToRemove Desc");
        int epicId = taskManager.createEpic(epic);

        taskManager.removeEpic(epicId);
        Epic removed = taskManager.getEpic(epicId);

        assertNull(removed);
    }

    @Test
    void shouldReturnAllEpics() {
        Epic epic1 = new Epic("Epic1", "Desc1");
        Epic epic2 = new Epic("Epic2", "Desc2");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(2, epics.size());
    }

    @Test
    void shouldRemoveAllEpics() {
        Epic epic = new Epic("Epic", "Desc");
        taskManager.createEpic(epic);

        taskManager.removeAllEpics();
        List<Epic> empty = taskManager.getAllEpics();

        assertTrue(empty.isEmpty());
    }
}
