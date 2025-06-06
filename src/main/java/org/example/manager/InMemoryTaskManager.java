package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected int idCounter = 0;
    protected final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    // ======= Duration и время для Epic =======

    private Duration getEpicDuration(Epic epic) {
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Task::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    private Optional<LocalDateTime> getEpicStartTime(Epic epic) {
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null && subtask.getStartTime() != null)
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo);
    }

    private Optional<LocalDateTime> getEpicEndTime(Epic epic) {
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null && subtask.getEndTime() != null)
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo);
    }

    private void updateEpicStatusAndTimes(Epic epic) {
        List<TaskStatus> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Task::getStatus)
                .collect(Collectors.toList());

        if (statuses.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (statuses.stream().allMatch(s -> s == TaskStatus.NEW)) {
            epic.setStatus(TaskStatus.NEW);
        } else if (statuses.stream().allMatch(s -> s == TaskStatus.DONE)) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

        epic.setDuration(getEpicDuration(epic));
        epic.setStartTime(getEpicStartTime(epic).orElse(null));
        epic.setEndTime(getEpicEndTime(epic).orElse(null)); // ✅ ВОЗВРАЩЕНО
    }

    // ======= Проверка пересечения =======

    private boolean isIntersect(Task t1, Task t2) {
        if (t1.getStartTime() == null || t2.getStartTime() == null) return false;

        LocalDateTime start1 = t1.getStartTime();
        LocalDateTime end1 = t1.getEndTime();
        LocalDateTime start2 = t2.getStartTime();
        LocalDateTime end2 = t2.getEndTime();

        return !(end1.isEqual(start2) || end1.isBefore(start2) ||
                start1.isEqual(end2) || start1.isAfter(end2));
    }

    private boolean hasIntersection(Task newTask) {
        if (newTask.getStartTime() == null) return false;

        List<Task> allTasks = getPrioritizedTasks();
        for (Task task : allTasks) {
            if (task.getId() == newTask.getId()) continue;
            if (isIntersect(task, newTask)) return true;
        }
        return false;
    }

    // ======= Интерфейсные методы =======

    @Override
    public int createTask(Task task) {
        if (hasIntersection(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой");
        }
        idCounter++;
        task.setId(idCounter);
        tasks.put(idCounter, task);
        return idCounter;
    }

    @Override
    public void addTask(Task task) {
        createTask(task);
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) return;
        if (hasIntersection(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой");
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    @Override
    public int createEpic(Epic epic) {
        idCounter++;
        epic.setId(idCounter);
        epics.put(idCounter, epic);
        updateEpicStatusAndTimes(epic);
        return idCounter;
    }

    @Override
    public void addEpic(Epic epic) {
        createEpic(epic);
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic existing = epics.get(epic.getId());
        if (existing == null) return;
        existing.setTitle(epic.getTitle());
        existing.setDescription(epic.getDescription());
        // Подзадачи не трогаем
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subtasks::remove);
            epics.remove(id);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public int createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не найден");
        }
        if (hasIntersection(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой");
        }
        idCounter++;
        subtask.setId(idCounter);
        subtasks.put(idCounter, subtask);
        epic.getSubtaskIds().add(idCounter);
        updateEpicStatusAndTimes(epic);
        return idCounter;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        createSubtask(subtask);
        return subtask;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask existing = subtasks.get(subtask.getId());
        if (existing == null || existing.getEpicId() != subtask.getEpicId()) return;
        if (hasIntersection(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой");
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) updateEpicStatusAndTimes(epic);
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id));
                updateEpicStatusAndTimes(epic);
            }
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeAllSubtasks() {
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatusAndTimes(epic);
        });
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        Comparator<Task> comparator = Comparator
                .comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(Task::getId);
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(subtasks.values());
        // allTasks.addAll(epics.values());
        allTasks.sort(comparator);
        return allTasks;
    }
}
