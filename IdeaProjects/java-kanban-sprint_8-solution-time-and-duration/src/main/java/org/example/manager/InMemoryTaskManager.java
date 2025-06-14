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

    // --- Методы для работы со временем и статусом Эпиков ---

    // Вычисление общей длительности эпика
    private Duration getEpicDuration(Epic epic) {
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull) // Фильтруем null подзадачи, если они вдруг есть
                .map(Task::getDuration)
                .filter(Objects::nonNull) // Фильтруем null Duration
                .reduce(Duration.ZERO, Duration::plus); // Суммируем все длительности
    }

    // Вычисление времени начала эпика (самая ранняя подзадача)
    private Optional<LocalDateTime> getEpicStartTime(Epic epic) {
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null && subtask.getStartTime() != null) // Фильтруем подзадачи без времени начала
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo); // Находим минимальное время
    }

    // Вычисление времени окончания эпика (самая поздняя подзадача)
    private Optional<LocalDateTime> getEpicEndTime(Epic epic) {
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null && subtask.getEndTime() != null) // Фильтруем подзадачи без времени окончания
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo); // Находим максимальное время
    }

    // Обновление статуса и временных меток эпика на основе его подзадач
    protected void updateEpicStatusAndTimes(Epic epic) {
        List<TaskStatus> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull) // Фильтруем null подзадачи
                .map(Task::getStatus)
                .collect(Collectors.toList());

        if (statuses.isEmpty() || statuses.stream().allMatch(s -> s == TaskStatus.NEW)) {
            epic.setStatus(TaskStatus.NEW); // Если подзадач нет или все NEW
        } else if (statuses.stream().allMatch(s -> s == TaskStatus.DONE)) {
            epic.setStatus(TaskStatus.DONE); // Если все DONE
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS); // В противном случае IN_PROGRESS
        }

        // Обновляем вычисляемые поля времени
        epic.setDuration(getEpicDuration(epic));
        epic.setStartTime(getEpicStartTime(epic).orElse(null));
        epic.setEndTime(getEpicEndTime(epic).orElse(null));
    }

    // --- Методы для проверки пересечений времени ---

    // Проверяет, пересекаются ли две задачи по времени
    private boolean isIntersect(Task t1, Task t2) {
        // Если у какой-либо задачи отсутствует время начала ИЛИ время окончания, они не могут пересекаться
        if (t1.getStartTime() == null || t1.getEndTime() == null ||
                t2.getStartTime() == null || t2.getEndTime() == null) {
            return false;
        }

        LocalDateTime start1 = t1.getStartTime();
        LocalDateTime end1 = t1.getEndTime();
        LocalDateTime start2 = t2.getStartTime();
        LocalDateTime end2 = t2.getEndTime();

        // Условие, при котором НЕТ пересечения:
        // (Задача 1 заканчивается в или до момента начала Задачи 2) ИЛИ
        // (Задача 2 заканчивается в или до момента начала Задачи 1)
        return !(end1.compareTo(start2) <= 0 || end2.compareTo(start1) <= 0);
    }

    // Проверяет, пересекается ли новая задача с любой существующей задачей
    private boolean hasIntersection(Task newTask) {
        // Если у новой задачи нет времени начала, она не может пересекаться
        if (newTask.getStartTime() == null) {
            return false;
        }

        // Получаем все приоритезированные задачи (отсортированные по времени)
        List<Task> allPrioritizedTasks = getPrioritizedTasks(); // Get all tasks including subtasks

        for (Task existingTask : allPrioritizedTasks) {
            // Игнорируем проверку на пересечение с самой собой при обновлении
            if (existingTask.getId() != null && existingTask.getId().equals(newTask.getId())) {
                continue;
            }
            if (isIntersect(existingTask, newTask)) {
                return true; // Найдено пересечение
            }
        }
        return false; // Пересечений не найдено
    }

    // --- Методы для работы с Task ---

    @Override
    public int createTask(Task task) {
        if (hasIntersection(task)) {
            throw new IllegalArgumentException("Ошибка: Задача пересекается по времени с другой задачей.");
        }
        idCounter++;
        task.setId(idCounter);
        tasks.put(idCounter, task);
        return idCounter;
    }

    @Override
    public int addTask(Task task) {
        // Просто вызывает createTask, так как логика добавления одинакова
        return createTask(task);
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public void updateTask(Task updatedTaskData) { // renamed 'task' to 'updatedTaskData' for clarity
        Task existingTask = tasks.get(updatedTaskData.getId());
        if (existingTask == null) {
            return; // Task not found, cannot update
        }

        // Create a temporary task object to check for intersection
        // This is crucial: check intersection with the *new* proposed time, but ignore the task itself.
        // We use the ID of the updatedTaskData, but the actual object from the map
        // to prevent accidental self-comparison in hasIntersection logic.
        Task tempTaskForIntersectionCheck = new Task(
                updatedTaskData.getId(),
                updatedTaskData.getTitle(), // These fields don't matter for intersection check, but for completeness
                updatedTaskData.getDescription(),
                updatedTaskData.getStatus(),
                updatedTaskData.getDuration(),
                updatedTaskData.getStartTime()
        );

        if (hasIntersection(tempTaskForIntersectionCheck)) {
            throw new IllegalArgumentException("Ошибка: Обновленная задача пересекается по времени с другой задачей.");
        }

        // If no intersection, then actually update the properties of the EXISTING task object in the map
        existingTask.setTitle(updatedTaskData.getTitle());
        existingTask.setDescription(updatedTaskData.getDescription());
        existingTask.setStatus(updatedTaskData.getStatus());
        existingTask.setDuration(updatedTaskData.getDuration());
        existingTask.setStartTime(updatedTaskData.getStartTime());
        // No need to call tasks.put again as existingTask is a reference to the object in the map
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id); // Удаляем из истории
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAllTasks() {
        // Удаляем все задачи из карты и из истории
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    // --- Методы для работы с Epic ---

    @Override
    public int createEpic(Epic epic) {
        idCounter++;
        epic.setId(idCounter);
        epics.put(idCounter, epic);
        updateEpicStatusAndTimes(epic); // Обновляем статус и время эпика
        return idCounter;
    }

    @Override
    public void addEpic(Epic epic) {
        // Просто вызывает createEpic
        createEpic(epic);
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        // Для эпика обновляются только название и описание
        Epic existing = epics.get(epic.getId());
        if (existing == null) {
            return;
        }
        existing.setTitle(epic.getTitle());
        existing.setDescription(epic.getDescription());
        // Временные метки и статус эпика зависят от подзадач, обновляются в updateEpicStatusAndTimes
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            // Удаляем все подзадачи, связанные с этим эпиком, из карты и истории
            epic.getSubtaskIds().forEach(subtaskId -> {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            });
            epics.remove(id); // Удаляем сам эпик
            historyManager.remove(id); // Удаляем эпик из истории
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAllEpics() {
        // Удаляем все эпики и все подзадачи из карт и истории
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // --- Методы для работы с Subtask ---

    @Override
    public int createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Ошибка: Эпик для подзадачи не найден.");
        }
        if (hasIntersection(subtask)) {
            throw new IllegalArgumentException("Ошибка: Подзадача пересекается по времени с другой задачей.");
        }
        idCounter++;
        subtask.setId(idCounter);
        subtasks.put(idCounter, subtask);
        epic.getSubtaskIds().add(idCounter); // Добавляем ID подзадачи в эпик
        updateEpicStatusAndTimes(epic); // Обновляем статус и время эпика
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
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask updatedSubtaskData) { // Renamed for clarity
        Subtask existingSubtask = subtasks.get(updatedSubtaskData.getId());
        if (existingSubtask == null) {
            return; // Subtask not found
        }

        // Check if epicId has changed. If so, it's an invalid operation.
        if (existingSubtask.getEpicId() != updatedSubtaskData.getEpicId()) {
            throw new IllegalArgumentException("Ошибка: Нельзя изменить EpicId подзадачи.");
        }

        // Create a temporary subtask object for intersection check
        Subtask tempSubtaskForIntersectionCheck = new Subtask(
                updatedSubtaskData.getId(),
                updatedSubtaskData.getTitle(),
                updatedSubtaskData.getDescription(),
                updatedSubtaskData.getStatus(),
                updatedSubtaskData.getEpicId(),
                updatedSubtaskData.getDuration(),
                updatedSubtaskData.getStartTime()
        );

        // Check for time intersection before applying changes
        if (hasIntersection(tempSubtaskForIntersectionCheck)) {
            throw new IllegalArgumentException("Ошибка: Обновленная подзадача пересекается по времени с другой задачей.");
        }

        // If all checks pass, update the existing subtask's properties
        existingSubtask.setTitle(updatedSubtaskData.getTitle());
        existingSubtask.setDescription(updatedSubtaskData.getDescription());
        existingSubtask.setStatus(updatedSubtaskData.getStatus());
        existingSubtask.setDuration(updatedSubtaskData.getDuration());
        existingSubtask.setStartTime(updatedSubtaskData.getStartTime());

        Epic epic = epics.get(existingSubtask.getEpicId());
        if (epic != null) {
            updateEpicStatusAndTimes(epic); // Update parent epic's status and times
        }
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
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeAllSubtasks() {
        // Удаляем все подзадачи из карты и истории
        subtasks.keySet().forEach(historyManager::remove);
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
                .thenComparing(Task::getId); // Добавлено для стабильной сортировки при одинаковом времени

        // Собираем все задачи и подзадачи
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(subtasks.values());

        // Сортируем и возвращаем
        allTasks.sort(comparator);
        return allTasks;
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }
        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }
}