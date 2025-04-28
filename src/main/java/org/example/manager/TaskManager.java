package org.example.manager;

import org.example.model.Task;
import org.example.model.Epic;
import org.example.model.Subtask;
import java.util.List;

public interface TaskManager {
    void addTask(Task task);
    void addEpic(Epic epic);
    Subtask addSubtask(Subtask subtask);
    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);
    Task getTask(int id);
    Epic getEpic(int id);
    Subtask getSubtask(int id);
    void removeTask(int id);
    void removeEpic(int id);
    void removeSubtask(int id);
    List<Task> getAllTasks();      // Добавлено
    List<Epic> getAllEpics();      // Добавлено
    List<Subtask> getAllSubtasks(); // Добавлено
    List<Task> getHistory();
}