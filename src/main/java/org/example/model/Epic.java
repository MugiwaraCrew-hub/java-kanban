package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds;

    public Epic(String title, String description, int id) {
        super(title, description, id, TaskStatus.NEW); // У эпика начальный статус всегда NEW
        this.subtaskIds = new ArrayList<>();
    }

    // Конструктор копирования
    public Epic(Epic other) {
        super(other); // Вызываем конструктор копирования Task
        this.subtaskIds = new ArrayList<>(other.subtaskIds); // Копируем список subtaskIds
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Методы для добавления и удаления id подзадач (могут быть уже реализованы)
    public void addSubtaskId(int subtaskId) {
        this.subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        this.subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return id == epic.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}