package application.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Book {
    private final StringProperty title;
    private final StringProperty author;
    private final int id; // Add ID property

    private static int counter = 1; // Static counter to manage IDs

    public Book(String title, String author) {
        this.id = counter++;
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
    }

    // Getters and setters for ID, title, and author
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getAuthor() {
        return author.get();
    }

    public void setAuthor(String author) {
        this.author.set(author);
    }

    public StringProperty authorProperty() {
        return author;
    }

    // Method to reset the counter
    public static void resetCounter() {
        counter = 1;
    }
}
