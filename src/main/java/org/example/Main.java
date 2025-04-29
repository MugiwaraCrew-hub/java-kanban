package org.example;

import org.example.manager.Managers;
import org.example.manager.TaskManager;
import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

public class Main {
    public static void main(String[] args) {
        String txt = "Был блеск и богатство, могущество трона,\n" +
                "Всемирная слава, хвала и почёт...\n" +
                "И было кольцо у царя Соломона,\n" +
                "На нём была надпись: \"И это пройдёт\".";
        TaskManager manager = Managers.getDefault();

        // Создание задач
        Task task1 = new Task("Найти карту сокровищ", "Обыскать остров", 0, TaskStatus.NEW);
        Task task2 = new Task("Починить Гоинг Мерри", "Залатать пробоины после шторма", 0, TaskStatus.IN_PROGRESS);
        int task1Id = manager.createTask(task1);
        int task2Id = manager.createTask(task2);

        // Создание эпиков и подзадач
        Epic epic1 = new Epic("Найти Ван Пис", "Стать Королём Пиратов", 0);
        int epic1Id = manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Победить Кайдо", "Арка Вано", 0, TaskStatus.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Разгадать Поунеглифы", "Найти Робин", 0, TaskStatus.NEW, epic1Id);
        int subtask1Id = manager.createSubtask(subtask1);
        int subtask2Id = manager.createSubtask(subtask2);

        Epic epic2 = new Epic("Освоить Гир Фифс", "Тренировка с Рэлеем", 0);
        int epic2Id = manager.createEpic(epic2);
        Subtask subtask3 = new Subtask("Улучшить Хаки", "Усилить наблюдение и вооружение", 0, TaskStatus.IN_PROGRESS, epic2Id);
        int subtask3Id = manager.createSubtask(subtask3);

        // Обновление задач
        task1.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        // Получение обновленных задач
        Task updatedTask1 = manager.getTask(task1Id);
        Subtask updatedSubtask1 = manager.getSubtask(subtask1Id);
        Epic updatedEpic1 = manager.getEpic(epic1Id);
        Epic updatedEpic2 = manager.getEpic(epic2Id);

        // Удаление задач
        manager.removeTask(task1Id);
        manager.removeEpic(epic2Id);

        // Вывод истории
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println("- " + task.getTitle());
        }
    }
}