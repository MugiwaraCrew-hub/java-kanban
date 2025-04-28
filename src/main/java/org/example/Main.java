package org.example;

import org.example.manager.Managers;
import org.example.manager.TaskManager;
import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Найти карту сокровищ", "Обыскать остров", 0, TaskStatus.NEW);
        Task task2 = new Task("Починить Гоинг Мерри", "Залатать пробоины после шторма", 0, TaskStatus.IN_PROGRESS);
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Найти Ван Пис", "Стать Королём Пиратов", 0);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Победить Кайдо", "Арка Вано", 0, TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Разгадать Поунеглифы", "Найти Робин", 0, TaskStatus.NEW, epic1.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        Epic epic2 = new Epic("Освоить Гир Фифс", "Тренировка с Рэлеем", 0);
        manager.addEpic(epic2);
        Subtask subtask3 = new Subtask("Улучшить Хаки", "Усилить наблюдение и вооружение", 0, TaskStatus.IN_PROGRESS, epic2.getId());
        manager.addSubtask(subtask3);

        // Вывод до вызовов
        System.out.println("Все задачи команды: " + manager.getAllTasks());
        System.out.println("Все цели: " + manager.getAllEpics());
        System.out.println("Все подзадачи пиратов: " + manager.getAllSubtasks());


        task1.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);


        System.out.println("Статус поиска карты: " + task1.getStatus());
        System.out.println("Статус боя с Кайдо: " + subtask1.getStatus());
        System.out.println("Статус поиска Ван Пис: " + epic1.getStatus());
        System.out.println("Статус Гир Фифс: " + epic2.getStatus());


        manager.removeTask(task1.getId());
        manager.removeEpic(epic2.getId());


        System.out.println("Оставшиеся задачи команды: " + manager.getAllTasks());
        System.out.println("Оставшиеся великие цели: " + manager.getAllEpics());


        System.out.println("История до вызовов: " + manager.getHistory());
        manager.getTask(task2.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask2.getId());
        System.out.println("История после вызовов: " + manager.getHistory());
    }
}