package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.io.*;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveData;

    public FileBackedTaskManager(File saveData) {
        this.saveData = saveData;
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(saveData)) {
            fileWriter.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                fileWriter.write(task.getId() + ",TASK," + task.getTitle() + "," + task.getStatus() + "," + task.getDescription() + ",\n");
            }
            for (Epic epic : getAllEpics()) {
                fileWriter.write(epic.getId() + ",EPIC," + epic.getTitle() + "," + epic.getStatus() + "," + epic.getDescription() + ",\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                fileWriter.write(subtask.getId() + ",SUBTASK," + subtask.getTitle() + "," + subtask.getStatus() + "," + subtask.getDescription() + "," + subtask.getEpicId() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // пропускаем заголовок
            int maxId = 0;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split(",", 6);
                if (parts.length < 6) continue;

                int id = Integer.parseInt(parts[0]);
                String type = parts[1];
                String name = parts[2];
                TaskStatus status = TaskStatus.valueOf(parts[3]);
                String description = parts[4];
                String epicPart = parts[5];

                if (id > maxId) maxId = id;

                switch (type) {
                    case "TASK":
                        Task task = new Task(name, description, id, status);
                        manager.tasks.put(id, task);
                        break;
                    case "EPIC":
                        Epic epic = new Epic(name, description, id);
                        epic.setStatus(status);
                        manager.epics.put(id, epic);
                        break;
                    case "SUBTASK":
                        int epicId = Integer.parseInt(epicPart);
                        Subtask subtask = new Subtask(name, description, id, status, epicId);
                        manager.subtasks.put(id, subtask);
                        Epic epicForSub = manager.epics.get(epicId);
                        if (epicForSub != null) {
                            epicForSub.getSubtaskIds().add(id);
                        }
                        break;
                }
            }
            manager.idCounter = maxId;

            for (Epic epic : manager.getAllEpics()) {
                manager.calculateEpicStatus(epic);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла: " + e.getMessage());
        }

        return manager;
    }

    @Override
    public List<Task> getAllTasks() {
        return super.getAllTasks();
    }

    @Override
    public List<Epic> getAllEpics() {
        return super.getAllEpics();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return super.getAllSubtasks();
    }
}
