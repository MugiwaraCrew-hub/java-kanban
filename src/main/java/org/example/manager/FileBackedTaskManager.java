package org.example.manager;

import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

import java.io.*;
import java.util.List;

// Класс FileBackedTaskManager
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveData;
    private final InMemoryTaskManager inMemoryTaskManager;

    public FileBackedTaskManager(File saveData, InMemoryTaskManager inMemoryTaskManager) {
        super(inMemoryTaskManager.historyManager); // Используем HistoryManager из inMemoryTaskManager
        this.saveData = saveData;
        this.inMemoryTaskManager = inMemoryTaskManager;
    }

    public FileBackedTaskManager(File saveData) {
        this(saveData, new InMemoryTaskManager());
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(saveData)) {
            fileWriter.write("id,type,name,status,description,epic\n");
            for (Task task : inMemoryTaskManager.getAllTasks()) {
                fileWriter.write(task.getId() + ",TASK," + task.getTitle() + "," + task.getStatus() + "," + task.getDescription() + ",\n");
            }
            for (Epic epic : inMemoryTaskManager.getAllEpics()) {
                fileWriter.write(epic.getId() + ",EPIC," + epic.getTitle() + "," + epic.getStatus() + "," + epic.getDescription() + ",\n");
            }
            for (Subtask subtask : inMemoryTaskManager.getAllSubtasks()) {
                fileWriter.write(subtask.getId() + ",SUBTASK," + subtask.getTitle() + "," + subtask.getStatus() + "," + subtask.getDescription() + "," + subtask.getEpicId() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        FileBackedTaskManager manager = new FileBackedTaskManager(file, inMemoryTaskManager);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // пропускаем заголовок
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 6) {
                    System.out.println("Некорректная строка в файле: " + line);
                    continue;
                }
                int id = Integer.parseInt(parts[0]);
                String type = parts[1];
                String name = parts[2];
                String status = parts[3];
                String description = parts[4];
                manager.setIdCounter(id);

                switch (type) {
                    case "TASK":
                        Task task = new Task(name, description, id, TaskStatus.valueOf(status));
                        manager.inMemoryTaskManager.tasks.put(id, task);
                        break;
                    case "EPIC":
                        Epic epic = new Epic(name, description, id);
                        epic.setStatus(TaskStatus.valueOf(status));
                        manager.inMemoryTaskManager.epics.put(id, epic);
                        break;
                    case "SUBTASK":
                        int epicId = Integer.parseInt(parts[5]);
                        Subtask subtask = new Subtask(name, description, id, TaskStatus.valueOf(status), epicId);
                        manager.inMemoryTaskManager.subtasks.put(id, subtask);
                        Epic epicForSub = manager.inMemoryTaskManager.epics.get(epicId);
                        if (epicForSub != null) {
                            epicForSub.addSubtaskId(id);
                        }
                        break;
                    default:
                        System.out.println("Неизвестный тип задачи: " + type);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла: " + e.getMessage());
        }
        return manager;
    }

    private void setIdCounter(int id) {
        inMemoryTaskManager.idCounter = id;
    }

    @Override
    public List<Task> getAllTasks() {
        return inMemoryTaskManager.getAllTasks();
    }

    @Override
    public List<Epic> getAllEpics() {
        return inMemoryTaskManager.getAllEpics();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return inMemoryTaskManager.getAllSubtasks();
    }
}
