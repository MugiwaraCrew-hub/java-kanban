package org.example.manager;

import org.example.model.*;
import java.util.*;

public interface TaskManager {
    int createTask(Task task);
    int createEpic(Epic epic);
    int createSubtask(Subtask subtask);

    void addTask(Task task);
    void addEpic(Epic epic);
    Subtask addSubtask(Subtask subtask);

    Task getTask(int id);
    Epic getEpic(int id);
    Subtask getSubtask(int id);

    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);

    void removeTask(int id);
    void removeEpic(int id);
    void removeSubtask(int id);

    List<Task> getAllTasks();
    List<Epic> getAllEpics();
    List<Subtask> getAllSubtasks();
    List<Subtask> getSubtasksByEpicId(int epicId);

    List<Task> getHistory();
}
