package org.example.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private final int epicId;

    // Новый конструктор с duration и startTime
    public Subtask(String title, String description, int id, TaskStatus status,
                   int epicId, Duration duration, LocalDateTime startTime) {
        super(title, description, id, status, duration, startTime);
        this.epicId = epicId;
    }

    // Перегруженный конструктор без duration и startTime
    public Subtask(String title, String description, int id, TaskStatus status, int epicId) {
        super(title, description, id, status);
        this.epicId = epicId;
    }

    // Конструктор копирования
    public Subtask(Subtask other) {
        super(other);
        this.epicId = other.epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}
