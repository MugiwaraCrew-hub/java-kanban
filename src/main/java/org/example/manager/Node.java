package org.example.manager;
import  org.example.model.Task;
public class Node {
    Task data;
    Node prev;
    Node next;

        Node(Task data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }

}
