package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.io.*;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveData;

    public FileBackedTaskManager(File saveData) {
        this.saveData = saveData;
    }

    protected void save() {
        try (FileWriter fileWriter = new FileWriter(saveData)) {
            fileWriter.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                fileWriter.write(task.getId() + ",TASK," + task.getTitle() + "," + task.getStatus() + "," + task.getDescription() + "," + task.getDuration().toMinutes() + "," + task.getStartTime() + ",\n");
            }
            for (Epic epic : getAllEpics()) {
                fileWriter.write(epic.getId() + ",EPIC," + epic.getTitle() + "," + epic.getStatus() + "," + epic.getDescription() + ",\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                fileWriter.write(subtask.getId() + ",SUBTASK," + subtask.getTitle() + "," + subtask.getStatus() + "," + subtask.getDescription() + "," + subtask.getEpicId() + "," + subtask.getDuration().toMinutes() + "," + subtask.getStartTime()
                        + "\n");
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
                String[] parts = line.split(",");
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
                        if (parts.length == 7) {
                            long durationMinutes = Long.parseLong(parts[5]);
                            LocalDateTime startTime = LocalDateTime.parse(parts[6]);
                            Task task = new Task(name, description, id, status);
                            manager.tasks.put(id, task);
                        }
                        break;
                    case "EPIC":
                        if (parts.length == 5) {
                            Epic epic = new Epic(name, description, id);
                            epic.setStatus(status);
                            manager.epics.put(id, epic);
                        }
                        break;
                    case "SUBTASK":
                        if (parts.length == 8) {
                            int epicId = Integer.parseInt(parts[5]);
                            long durationMinutes = Long.parseLong(parts[6]);
                            LocalDateTime startTime = LocalDateTime.parse(parts[7]);
                            Subtask subtask = new Subtask(name, description, id, status, epicId);
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