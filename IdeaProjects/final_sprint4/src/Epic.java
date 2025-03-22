import java.util.ArrayList;

public class Epic extends Task {
 ArrayList<Integer> subtaskIds;

 public Epic(String title, String description, int id, TaskStatus status, ArrayList<Integer> subtaskIds) {
        super(title, description, id, status);
        this.subtaskIds = subtaskIds;
 }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }
}
