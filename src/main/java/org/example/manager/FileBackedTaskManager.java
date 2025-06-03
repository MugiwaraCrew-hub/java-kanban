package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;

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
                        (task.getStartTime() != null ? task.getStartTime() : "null") + ",\n");
            }
            for (Epic epic : getAllEpics()) {
                fileWriter.write(epic.getId() + ",EPIC," + epic.getTitle() + "," + epic.getStatus() + "," +
                        epic.getDescription() + ",null,null,\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                fileWriter.write(subtask.getId() + ",SUBTASK," + subtask.getTitle() + "," + subtask.getStatus() + "," +
                        subtask.getDescription() + "," + (subtask.getDuration() != null ? subtask.getDuration().toMinutes() : "null") + "," +
                        (subtask.getStartTime() != null ? subtask.getStartTime() : "null") + "," + subtask.getEpicId() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine(); // Пропускаем заголовок
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1); // -1 чтобы не отбрасывать пустые поля
                if (parts.length < 3) {
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

                switch (type) {
                    case "TASK":
                        if (parts.length >= 7) {
                            String durationStr = parts[5];
                            String startTimeStr = parts[6];
                            Duration duration = (durationStr == null || durationStr.equals("null") || durationStr.isEmpty())
                                    ? null
                                    : Duration.ofMinutes(Long.parseLong(durationStr));
                            LocalDateTime startTime = (startTimeStr == null || startTimeStr.equals("null") || startTimeStr.isEmpty())
                                    ? null
                                    : LocalDateTime.parse(startTimeStr);
                            Task task = new Task(name, description, id, status);
                            task.setDuration(duration);
                            task.setStartTime(startTime);
                            manager.tasks.put(id, task);
                        }
                        break;
                    case "EPIC":
                        Epic epic = new Epic(name, description, id);
                        epic.setStatus(status);
                        manager.epics.put(id, epic);
                        break;
                    case "SUBTASK":
                        if (parts.length >= 8) {
                            String durationStr = parts[5];
                            String startTimeStr = parts[6];
                            int epicId = Integer.parseInt(parts[7]);
                            Duration duration = (durationStr == null || durationStr.equals("null") || durationStr.isEmpty())
                                    ? null
                                    : Duration.ofMinutes(Long.parseLong(durationStr));
                            LocalDateTime startTime = (startTimeStr == null || startTimeStr.equals("null") || startTimeStr.isEmpty())
                                    ? null
                                    : LocalDateTime.parse(startTimeStr);
                            Subtask subtask = new Subtask(name, description, id, status, epicId);
                            subtask.setDuration(duration);
                            subtask.setStartTime(startTime);
                            manager.subtasks.put(id, subtask);
                            Epic epicForSub = manager.epics.get(epicId);
                            if (epicForSub != null) {
                                epicForSub.addSubtaskId(id);
                            }
                        }
                        break;
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла: " + e.getMessage());
        }
        manager.idCounter = maxId;
        return manager;
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
