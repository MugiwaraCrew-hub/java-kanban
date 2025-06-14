package org.example.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected Integer id;
    protected String title;
    protected String description;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String title, String description, TaskStatus status) {
        this(title, description, status, null, null);
    }

    public Task(Integer id, String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this(title, description, status, duration, startTime);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + (duration != null ? duration.toMinutes() : null) + "m" +
                ", startTime=" + (startTime != null ? startTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Если это один и тот же объект, они равны
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;

        // ИЗМЕНЕНО: Если оба id == null, они не равны (если это не один и тот же объект)
        if (this.id == null && task.id == null) {
            return false; // Два объекта без ID не равны, если это не один и тот же объект (уже проверено this == o)
        }

        // Если хотя бы один id не null, сравниваем их
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        // ИЗМЕНЕНО: Если id null, возвращаем 0, чтобы избежать проблем.
        // Более сложное решение, но для нашего случая достаточно
        return Objects.hash(id); // Objects.hash(null) вернет 0, это безопасно
    }
}