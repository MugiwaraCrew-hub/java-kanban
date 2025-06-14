package org.example;

import org.example.manager.FileBackedTaskManager;
import org.example.manager.InMemoryTaskManager;
import org.example.manager.Managers;
import org.example.model.Epic;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.model.TaskStatus;
import org.example.server.HttpTaskServer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws IOException {
        // Пример использования FileBackedTaskManager
        File tasksFile = new File("tasks.csv");
        FileBackedTaskManager fileBackedManager = new FileBackedTaskManager(tasksFile);

        // Добавляем несколько задач, подзадач и эпиков через FileBackedTaskManager
        Task task1 = new Task("Помыть посуду", "Отмыть до блеска", TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        fileBackedManager.createTask(task1);

        Epic epic1 = new Epic("Переезд", "Организация переезда");
        fileBackedManager.createEpic(epic1);

        Subtask subtask1Epic1 = new Subtask("Собрать коробки", "Собрать все вещи в коробки", TaskStatus.NEW, epic1.getId(), Duration.ofMinutes(120), LocalDateTime.now().plusHours(1));
        fileBackedManager.createSubtask(subtask1Epic1);

        Subtask subtask2Epic1 = new Subtask("Упаковать хрупкое", "Обернуть хрупкие предметы в пупырку", TaskStatus.NEW, epic1.getId(), Duration.ofMinutes(60), LocalDateTime.now().plusHours(3));
        fileBackedManager.createSubtask(subtask2Epic1);

        // Получаем задачи для добавления в историю
        fileBackedManager.getTask(task1.getId());
        fileBackedManager.getEpic(epic1.getId());
        fileBackedManager.getSubtask(subtask1Epic1.getId());

        System.out.println("Задачи после сохранения:");
        System.out.println(fileBackedManager.getAllTasks());
        System.out.println(fileBackedManager.getAllEpics());
        System.out.println(fileBackedManager.getAllSubtasks());
        System.out.println("История после сохранения:");
        System.out.println(fileBackedManager.getHistory());

        // Проверяем загрузку из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tasksFile);

        System.out.println("\nЗадачи после загрузки:");
        System.out.println(loadedManager.getAllTasks());
        System.out.println(loadedManager.getAllEpics());
        System.out.println(loadedManager.getAllSubtasks());
        System.out.println("История после загрузки:");
        System.out.println(loadedManager.getHistory());


        // Пример использования HttpTaskServer
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault()); // Используем InMemoryTaskManager
        server.start();

        // Добавление тестовых данных через менеджер, связанный с сервером
        InMemoryTaskManager manager = (InMemoryTaskManager) Managers.getDefault();

        System.out.println("\nРабота с HttpTaskServer:");

        Task serverTask1 = new Task("Купить молоко", "Не забыть про жирность", TaskStatus.NEW, Duration.ofMinutes(15), LocalDateTime.now().plusDays(1));
        manager.createTask(serverTask1);
        System.out.println("Добавлена задача: " + serverTask1);

        Epic serverEpic1 = new Epic("Проект 'Новая фича'", "Разработка новой функциональности");
        manager.createEpic(serverEpic1);
        System.out.println("Добавлен эпик: " + serverEpic1);

        Subtask serverSubtask1Epic1 = new Subtask("Реализовать метод А", "Написать код для метода А", TaskStatus.IN_PROGRESS, serverEpic1.getId(), Duration.ofMinutes(60), LocalDateTime.now().plusDays(1).plusHours(1));
        manager.createSubtask(serverSubtask1Epic1);
        System.out.println("Добавлена подзадача: " + serverSubtask1Epic1);

        Subtask serverSubtask2Epic1 = new Subtask("Написать тесты для метода А", "Проверить покрытие кода", TaskStatus.NEW, serverEpic1.getId(), Duration.ofMinutes(45), LocalDateTime.now().plusDays(1).plusHours(2));
        manager.createSubtask(serverSubtask2Epic1);
        System.out.println("Добавлена подзадача: " + serverSubtask2Epic1);

        // Получение задач и подзадач для демонстрации истории и приоритетного списка
        manager.getTask(serverTask1.getId());
        manager.getEpic(serverEpic1.getId());
        manager.getSubtask(serverSubtask1Epic1.getId());
        manager.getSubtask(serverSubtask2Epic1.getId());

        System.out.println("\nПриоритетные задачи:");
        manager.getPrioritizedTasks().forEach(System.out::println);

        System.out.println("\nИстория просмотров:");
        manager.getHistory().forEach(System.out::println);

        // Демонстрация обновления статуса подзадачи
        serverSubtask1Epic1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(serverSubtask1Epic1);
        System.out.println("\nПодзадача обновлена до DONE: " + serverSubtask1Epic1);
        System.out.println("Статус эпика после обновления подзадачи: " + manager.getEpic(serverEpic1.getId()).getStatus());


        // Остановка сервера после выполнения примеров
        server.stop();
        System.out.println("HTTP-сервер остановлен.");
    }
}