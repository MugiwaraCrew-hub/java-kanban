package org.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestEpic {
    @Test
    public void epicTestEqualId() {
        Epic epic = new Epic("Epic1", "Description", 1);
        Epic epic1 = new Epic("Epic2", "Description", 1);
        assertEquals(epic, epic1, "Эпики с одинаковым id должны быть равны");
    }

    @Test
    public void epicTestNotEqualId() {
        Epic epic = new Epic("Epic1", "Description", 1);
        Epic epic1 = new Epic("Epic2", "Description", 2);
        assertNotEquals(epic, epic1, "Эпики с разными id не должны быть равны");
    }
}