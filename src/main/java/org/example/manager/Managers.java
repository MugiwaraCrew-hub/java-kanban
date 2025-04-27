package org.example.manager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(); // Без параметра, так как конструктор сам берёт historyManager
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}