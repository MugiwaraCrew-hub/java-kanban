package org.example.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds; // ID подзадач, принадлежащих этому эпику
    private LocalDateTime endTime; // Время окончания эпика (вычисляемое)

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW, null, null); // Эпик начинается как NEW
        this.subtaskIds = new ArrayList<>();
    }

    // Конструктор для FileBackedTaskManager, где id уже известен
    public Epic(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status, null, null);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Переопределение сеттера для startTime (чтобы не устанавливать вручную)
    @Override
    public void setStartTime(LocalDateTime startTime) {
        // У эпика startTime и duration вычисляются, поэтому не устанавливаем их напрямую
        // super.setStartTime(startTime); // Закомментировано или удалено
    }

    // Переопределение сеттера для duration (чтобы не устанавливать вручную)
    @Override
    public void setDuration(Duration duration) {
        // super.setDuration(duration); //
    }

    // Переопределение getEndTime для эпика
    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Сеттер для вычисляемого endTime
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                ", duration=" + (duration != null ? duration.toMinutes() : null) + "m" + // Duration эпика
                ", startTime=" + (startTime != null ? startTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null) + // StartTime эпика
                ", endTime=" + (endTime != null ? endTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null) + // EndTime эпика
                '}';
    }

    // ИСПРАВЛЕНО: Правильная реализация equals и hashCode (сравнение только по id)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Важно: здесь мы сравниваем по классу Task, так как Epic наследует id от Task
        // и логика equals для id должна быть в Task, а здесь просто вызываем super.equals
        // или дублируем логику Task, что не очень хорошо
        // Если equals в Task сравнивает только по ID, то можно так:
        if (!super.equals(o)) return false; // Вызываем equals родительского класса (Task)

        // Дополнительная проверка на null и класс, если super.equals не справляется
        // (хотя super.equals должен это делать)
        if (o == null || getClass() != o.getClass()) return false;

        // Если super.equals уже проверил id, то для Epic больше ничего не нужно сравнивать,
        // так как subtaskIds, duration, startTime, endTime - это внутренние данные,
        // а не часть идентичности Epic, которые определяются его ID.
        return true; // Если super.equals вернул true, значит id совпадает.
    }

    @Override
    public int hashCode() {
        // ИСПРАВЛЕНО: hashCode должен использовать Objects.hash для Integer id
        return Objects.hash(id); // Вызываем Objects.hash для Integer id
    }
}