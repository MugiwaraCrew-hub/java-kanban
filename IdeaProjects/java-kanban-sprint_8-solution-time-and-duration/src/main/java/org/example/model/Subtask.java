package org.example.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(title, description, status, duration, startTime);
        this.epicId = epicId;
    }

    // Конструктор без ID, для создания новых подзадач
    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status, null, null);
        this.epicId = epicId;
    }

    // Конструктор для FileBackedTaskManager, где id уже известен
    public Subtask(int id, String title, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(id, title, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + (duration != null ? duration.toMinutes() : null) + "m" +
                ", startTime=" + (startTime != null ? startTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null) +
                '}';
    }

    // Правильная реализация equals и hashCode (сравнение только по id)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false; // Важно для наследования
        Subtask subtask = (Subtask) o;
        return id == subtask.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}