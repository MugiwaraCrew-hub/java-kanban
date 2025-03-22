public class Task {
    private String title;
    private String description;
    private int id;
    private TaskStatus status;

    public Task(String title, String description, int id, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) { // Добавляем сеттер
        this.status = status;
    }
}