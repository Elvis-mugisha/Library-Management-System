package application.Model;

import java.time.LocalDate;

public class Transaction {
    private static int nextTransactionId = 1; // Static variable to keep track of the next transactionId
    private int transactionId;
    private int userId;
    private int bookId;
    private LocalDate transactionDate;
    private String status; // New status property

    // Constructor with optional transactionId and status
    public Transaction(int transactionId, int userId, int bookId, String status) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.bookId = bookId;
        this.transactionDate = LocalDate.now(); // Set transactionDate to current date
        this.status = status;
    }

    // Getters and setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
