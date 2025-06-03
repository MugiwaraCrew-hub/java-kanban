package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;

import java.util.List;

public interface TaskManager {
    // Методы задач
    int createTask(Task task);


    List<Task> getPrioritizedTasks();


    void addTask(Task task);

    Task getTask(int id);

    void updateTask(Task task);

    void removeTask(int id);

    List<Task> getAllTasks();

    // Методы эпиков
    int createEpic(Epic epic);

    void addEpic(Epic epic);

    Epic getEpic(int id);

    void updateEpic(Epic epic);

    void removeEpic(int id);

    List<Epic> getAllEpics();

    List<Subtask> getSubtasksByEpicId(int epicId);

    // Методы подзадач
    int createSubtask(Subtask subtask);

    Subtask addSubtask(Subtask subtask);

    Subtask getSubtask(int id);

    void updateSubtask(Subtask subtask);

    void removeSubtask(int id);

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubtasks();


    List<Subtask> getAllSubtasks();

    // История
    List<Task> getHistory();
}
