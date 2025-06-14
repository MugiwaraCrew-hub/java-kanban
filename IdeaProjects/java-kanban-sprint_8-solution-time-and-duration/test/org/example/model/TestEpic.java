package org.example.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestEpic {

    @Test
    @DisplayName("Эпики с одинаковым ID должны быть равны")
    public void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic A", "Description A");
        epic1.setId(1); // Принудительно устанавливаем ID для теста

        Epic epic2 = new Epic("Epic B", "Description B"); // Даже с другими данными
        epic2.setId(1); // Принудительно устанавливаем тот же ID

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны, независимо от других полей");
        assertEquals(epic1.hashCode(), epic2.hashCode(), "Хэш-коды эпиков с одинаковым ID должны быть равны");
    }

    @Test
    @DisplayName("Эпики с разными ID должны быть не равны")
    public void epicsWithDifferentIdShouldNotBeEqual() {
        Epic epic1 = new Epic("Epic A", "Description A");
        epic1.setId(1); // Принудительно устанавливаем ID для теста

        Epic epic2 = new Epic("Epic B", "Description B");
        epic2.setId(2); // Принудительно устанавливаем другой ID

        assertNotEquals(epic1, epic2, "Эпики с разными ID должны быть не равны");
        assertNotEquals(epic1.hashCode(), epic2.hashCode(), "Хэш-коды эпиков с разными ID должны быть не равны");
    }

    @Test
    @DisplayName("Эпик без ID (null) и эпик с ID должны быть не равны")
    public void epicWithNullIdAndEpicWithIdShouldNotBeEqual() {
        Epic epic1 = new Epic("Epic A", "Description A"); // ID = null
        Epic epic2 = new Epic("Epic B", "Description B");
        epic2.setId(1);

        assertNotEquals(epic1, epic2, "Эпик без ID и эпик с ID не должны быть равны");
    }

    @Test
    @DisplayName("Два эпика без ID (null) должны быть не равны (разные объекты)")
    public void twoEpicsWithNullIdShouldNotBeEqual() {
        Epic epic1 = new Epic("Epic A", "Description A"); // ID = null
        Epic epic2 = new Epic("Epic B", "Description B"); // ID = null

        // Они не равны, потому что это разные объекты и у них нет общего идентифицирующего ID
        assertNotEquals(epic1, epic2, "Два эпика без присвоенного ID должны быть не равны");
    }
}