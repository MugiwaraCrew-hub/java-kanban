package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveData;

    public FileBackedTaskManager(File saveData) {
        this.saveData = saveData;
    }

    protected void save() {
        try (FileWriter fileWriter = new FileWriter(saveData)) {
            fileWriter.write("id,type,name,status,description,duration,startTime,epic\n");
            for (Task task : getAllTasks()) {
                fileWriter.write(task.getId() + ",TASK," + task.getTitle() + "," + task.getStatus() + "," +
                        task.getDescription() + "," + (task.getDuration() != null ? task.getDuration().toMinutes() : "null") + "," +
                        (task.getStartTime() != null ? task.getStartTime().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "null") + ",\n");
            }
            for (Epic epic : getAllEpics()) {
                fileWriter.write(epic.getId() + ",EPIC," + epic.getTitle() + "," + epic.getStatus() + "," +
                        epic.getDescription() + ",null,null,\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                fileWriter.write(subtask.getId() + ",SUBTASK," + subtask.getTitle() + "," + subtask.getStatus() + "," +
                        subtask.getDescription() + "," + (subtask.getDuration() != null ? subtask.getDuration().toMinutes() : "null") + "," +
                        (subtask.getStartTime() != null ? subtask.getStartTime().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "null") + "," + subtask.getEpicId() + "\n");
            }
            fileWriter.write("\n");
            String historyLine = getHistory().stream()
                    .map(task -> String.valueOf(task.getId()))
                    .collect(Collectors.joining(","));
            fileWriter.write("history:" + historyLine + "\n");
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Пропускаем заголовок
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (line.startsWith("history:")) {
                    System.out.println("Обработка строки истории: " + line);
                    System.out.println("Состояние subtasks: " + manager.subtasks.keySet());
                    String[] historyIds = line.substring("history:".length()).split(",");
                    ArrayList<Task> tasksToAdd = new ArrayList<>();
                    for (String idStr : historyIds) {
                        if (!idStr.isEmpty()) {
                            try {
                                int id = Integer.parseInt(idStr);
                                Task task = manager.getTask(id);
                                if (task == null) {
                                    task = manager.getEpic(id);
                                }
                                if (task == null) {
                                    task = manager.getSubtask(id);
                                    System.out.println("getSubtask(" + id + ") вернул: " + task);
                                }
                                if (task != null) {
                                    tasksToAdd.add(task);
                                } else {
                                    System.err.println("Задача с ID " + id + " не найдена при загрузке истории");
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Ошибка парсинга ID в истории: " + idStr);
                            }
                        }
                    }
                    for (Task task : tasksToAdd) {
                        System.out.println("Добавляем в историю задачу с ID: " + task.getId());
                        manager.historyManager.add(task);
                    }
                    break;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 5) {
                    System.err.println("Ошибка при чтении строки: недостаточно полей: " + line);
                    continue;
                }

                int id = Integer.parseInt(parts[0]);
                String type = parts[1];
                String name = parts[2];
                TaskStatus status = TaskStatus.valueOf(parts[3]);
                String description = parts[4];

                if (id > maxId) {
                    maxId = id;
                }

                Duration duration = null;
                LocalDateTime startTime = null;

                if (parts.length > 5 && !parts[5].equals("null") && !parts[5].isEmpty()) {
                    try {
                        duration = Duration.ofMinutes(Long.parseLong(parts[5]));
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка парсинга длительности: " + parts[5]);
                    }
                }
                if (parts.length > 6 && !parts[6].equals("null") && !parts[6].isEmpty()) {
                    try {
                        startTime = LocalDateTime.parse(parts[6], java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (DateTimeParseException e) {
                        System.err.println("Ошибка парсинга времени начала: " + parts[6]);
                    }
                }

                switch (type) {
                    case "TASK":
                        Task task = new Task(id, name, description, status, duration, startTime);
                        manager.tasks.put(id, task);
                        break;
                    case "EPIC":
                        Epic epic = new Epic(id, name, description, status);
                        manager.epics.put(id, epic);
                        break;
                    case "SUBTASK":
                        if (parts.length < 8) {
                            System.err.println("Ошибка при чтении подзадачи: отсутствует epicId: " + line);
                            continue;
                        }
                        int epicId = Integer.parseInt(parts[7]);
                        Subtask subtask = new Subtask(id, name, description, status, epicId, duration, startTime);
                        manager.subtasks.put(id, subtask);
                        Epic epicForSub = manager.getEpic(epicId);
                        if (epicForSub != null) {
                            epicForSub.getSubtaskIds().add(id);
                        }
                        break;
                    default:
                        System.err.println("Неизвестный тип задачи при загрузке: " + type);
                        break;
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла: " + e.getMessage());
        }

        manager.getAllEpics().forEach(manager::updateEpicStatusAndTimes);
        manager.idCounter = maxId;
        return manager;
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }
}