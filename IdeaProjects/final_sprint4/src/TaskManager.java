import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int idCounter = 0;

    private final HashMap<Integer, Task> tasks = new HashMap<>();

    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public void removeTask(int id) {
        tasks.remove(id);
    }

    public void removeEpic(int id) {
        epics.remove(id);
    }

    public void removeSubtask(int id) {
        subtasks.remove(id);
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
    }

    public void clearSubtasks() {
        subtasks.clear();
    }

    public Task createTask(String title, String description, TaskStatus status) {
        idCounter++; // Увеличиваем счётчик
        Task task = new Task(title, description, idCounter, status); // Создаём задачу
        tasks.put(idCounter, task); // Добавляем в HashMap
        return task; // Возвращаем созданную задачу
    }

    public Epic createEpic(String title, String description, TaskStatus status, ArrayList<Integer> subtaskIds) {
        idCounter++;
        Epic epic = new Epic(title, description, idCounter, status, subtaskIds);
        epics.put(idCounter, epic);
        calculateEpicStatus(epic);
        return epic;
    }

    public Subtask createSubtask(String title, String description, TaskStatus status, int epicId) {
        idCounter++;
        Subtask subtask = new Subtask(title, description, idCounter, status, epicId);
        subtasks.put(idCounter, subtask);
        return subtask;
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        calculateEpicStatus(epic);
    }

    public void updateSubTask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            calculateEpicStatus(epic);
        }
    }


    public ArrayList<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        ArrayList<Subtask> result = new ArrayList<>();

        for (int id : subtaskIds) {
            Subtask subtask = subtasks.get(id);
            if (subtask != null) {
                result.add(subtask);
            }
        }

        return result;
    }
    private void calculateEpicStatus(Epic epic) {
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) { // Нет подзадач
            epic.setStatus(TaskStatus.NEW); // Устанавливаем NEW
            return;
        }

        boolean allNew = true; // Все ли NEW
        boolean allDone = true; // Все ли DONE

        for (int id : subtaskIds) { // Смотрим каждую подзадачу
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

}

