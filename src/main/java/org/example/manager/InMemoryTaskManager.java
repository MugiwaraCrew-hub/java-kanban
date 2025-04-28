package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int idCounter = 0;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private Task createTask(Task task) {
        idCounter++;
        task.setId(idCounter);
        tasks.put(idCounter, new Task(task)); // Сохраняем копию
        return new Task(task); // Возвращаем копию
    }

    private Epic createEpic(Epic epic) {
        idCounter++;
        epic.setId(idCounter);
        epics.put(idCounter, new Epic(epic)); // Сохраняем копию
        return new Epic(epic); // Возвращаем копию
    }

    private Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }
        idCounter++;
        subtask.setId(idCounter);
        subtasks.put(idCounter, new Subtask(subtask)); // Сохраняем копию
        epic.getSubtaskIds().add(idCounter);
        calculateEpicStatus(epics.get(subtask.getEpicId())); // Пересчитываем статус для копии в epics
        return new Subtask(subtask); // Возвращаем копию
    }

    private void calculateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (int id : subtaskIds) {
            Subtask subtask = subtasks.get(id);
            if (subtask != null) {
                if (subtask.getStatus() != TaskStatus.NEW) {
                    allNew = false;
                }
                if (subtask.getStatus() != TaskStatus.DONE) {
                    allDone = false;
                }
            }
        }
        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public void addTask(Task task) {
        createTask(task);
    }

    @Override
    public void addEpic(Epic epic) {
        createEpic(epic);
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        return createSubtask(subtask);
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), new Task(task)); // Обновляем копией
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic epicCopy = new Epic(epic);
            epics.put(epic.getId(), epicCopy); // Обновляем копией
            calculateEpicStatus(epicCopy);
        }
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        if (subtasks.containsKey(updatedSubtask.getId())) {
            subtasks.put(updatedSubtask.getId(), new Subtask(updatedSubtask)); // Обновляем копией
            Epic epic = epics.get(updatedSubtask.getEpicId());
            if (epic != null) {
                calculateEpicStatus(epic); // Пересчитываем статус для копии в epics
            }
        }
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(new Task(task)); // Добавляем копию в историю
            return new Task(task); // Возвращаем копию
        }
        return null;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(new Epic(epic)); // Добавляем копию в историю
            return new Epic(epic); // Возвращаем копию
        }
        return null;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(new Subtask(subtask)); // Добавляем копию в историю
            return new Subtask(subtask); // Возвращаем копию
        }
        return null;
    }

    @Override
    public void removeTask(int id) {
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.remove(id);
        if (epic != null) {
            List<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            for (int subtaskId : subtaskIds) {
                historyManager.remove(subtaskId);
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.remove(id);
        if (subtask != null) {
            subtasks.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id));
                calculateEpicStatus(epic);
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> originalHistory = historyManager.getHistory();
        List<Task> copiedHistory = new ArrayList<>();
        for (Task task : originalHistory) {
            if (task != null) {
                if (task instanceof Epic) {
                    copiedHistory.add(new Epic((Epic) task));
                } else if (task instanceof Subtask) {
                    copiedHistory.add(new Subtask((Subtask) task));
                } else {
                    copiedHistory.add(new Task(task));
                }
            }
        }
        return copiedHistory;
    }

    public List<Task> getAllTasks() {
        List<Task> copiedTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            copiedTasks.add(new Task(task));
        }
        return copiedTasks;
    }

    public List<Epic> getAllEpics() {
        List<Epic> copiedEpics = new ArrayList<>();
        for (Epic epic : epics.values()) {
            copiedEpics.add(new Epic(epic));
        }
        return copiedEpics;
    }

    public List<Subtask> getAllSubtasks() {
        List<Subtask> copiedSubtasks = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            copiedSubtasks.add(new Subtask(subtask));
        }
        return copiedSubtasks;
    }

    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        List<Subtask> result = new ArrayList<>();
        for (int id : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(id);
            if (subtask != null) {
                result.add(new Subtask(subtask));
            }
        }
        return result;
    }
}