package org.example.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;

    public Epic(String title, String description, int id) {
        super(title, description, id, TaskStatus.NEW);
        this.subtaskIds = new ArrayList<>();
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
    }

    // Конструктор копирования
    public Epic(Epic other) {
        super(other);
        this.subtaskIds = new ArrayList<>(other.subtaskIds);
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.duration = other.duration;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic)) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds) &&
                Objects.equals(startTime, epic.startTime) &&
                Objects.equals(endTime, epic.endTime) &&
                Objects.equals(duration, epic.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds, startTime, endTime, duration);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtaskIds=" + subtaskIds +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", " + super.toString() +
                '}';
    }
}
