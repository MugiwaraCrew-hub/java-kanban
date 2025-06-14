import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();


        Task task1 = manager.createTask("Найти карту сокровищ", "Обыскать остров", TaskStatus.NEW);
        Task task2 = manager.createTask("Починить Гоинг Мерри", "Залатать пробоины после шторма", TaskStatus.IN_PROGRESS);

        // (поиск Ван Пис)
        ArrayList<Integer> subtaskIds1 = new ArrayList<>();
        Epic epic1 = manager.createEpic("Найти Ван Пис", "Стать Королём Пиратов", TaskStatus.NEW, subtaskIds1);
        Subtask subtask1 = manager.createSubtask("Победить Кайдо", "Сражение на Вано", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = manager.createSubtask("Разгадать Поунеглифы", "Найти Робин", TaskStatus.NEW, epic1.getId());
        subtaskIds1.add(subtask1.getId());
        subtaskIds1.add(subtask2.getId());
        manager.updateEpic(epic1); // Обновляем эпик, чтобы статус пересчитался

        //(тренировка Луффи)
        ArrayList<Integer> subtaskIds2 = new ArrayList<>();
        Epic epic2 = manager.createEpic("Освоить Гир 5", "Тренировка с Рейли", TaskStatus.NEW, subtaskIds2);
        Subtask subtask3 = manager.createSubtask("Улучшить Хаки", "Усилить наблюдение и вооружение", TaskStatus.IN_PROGRESS, epic2.getId());
        subtaskIds2.add(subtask3.getId());
        manager.updateEpic(epic2);


        System.out.println("Все задачи команды: " + manager.getAllTasks());
        System.out.println("Все  цели: " + manager.getAllEpics());
        System.out.println("Все подзадачи Мугивар: " + manager.getAllSubtasks());

        // Меняем статусы
        task1.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubTask(subtask1);

        // чекаем статусы
        System.out.println("Статус поиска карты: " + task1.getStatus());
        System.out.println("Статус боя с Кайдо: " + subtask1.getStatus());
        System.out.println("Статус поиска Ван Пис: " + epic1.getStatus());
        System.out.println("Статус Гир Фифс: " + epic2.getStatus()); //

        // delete 
        manager.removeTask(task1.getId());
        manager.removeEpic(epic2.getId());

        // Проверяем списки после удаления
        System.out.println("Оставшиеся задачи команды: " + manager.getAllTasks());
        System.out.println("Оставшиеся великие цели: " + manager.getAllEpics());
    }
}