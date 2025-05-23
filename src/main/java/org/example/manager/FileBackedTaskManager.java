package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.io.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveData;

    public FileBackedTaskManager(File saveData) {
        this.saveData = saveData;
    }

    // Метод сохранения — protected, доступен только в классе и наследниках
    protected void save() {
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
                            epicForSub.addSubtaskId(id);
                        }
                        break;
                }
            }
            manager.idCounter = maxId;

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла: " + e.getMessage());
        }

        return manager;
    }

    // Переопределяем методы, чтобы после изменения сразу сохранять данные
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
}
