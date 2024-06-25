package application.Controller;

import application.Model.Book;
import application.Model.Transaction;
import application.Util.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class AdminPage {

    @FXML
    private TextField bookTitleField;

    @FXML
    private TextField authorField;

    @FXML
    private Button addBookButton;

    @FXML
    private Button editBookButton;

    @FXML
    private Button deleteBookButton;

    @FXML
    private Button lendBookButton;

    @FXML
    private TableView<Book> bookTableView;

    @FXML
    private TableColumn<Book, Integer> idColumn;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> authorColumn;

    @FXML
    private TableView<Transaction> transactionTableView;

    @FXML
    private TableColumn<Transaction, Integer> transactionIdColumn;

    @FXML
    private TableColumn<Transaction, Integer> userIdColumn;

    @FXML
    private TableColumn<Transaction, Integer> bookIdColumn;

    @FXML
    private TableColumn<Transaction, LocalDate> transactionDateColumn;

    @FXML
    private TableColumn<Transaction, String> statusColumn;


    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    private Connection connection;

    @FXML
    private Button updateBookButton;

    @FXML
    public void initialize() {
        try {
            connection = DB.getConnection();
            loadBooks();
            loadTransactions();

            // Initialize the TableView columns
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));

            // Initialize the TableView columns for transactions
            transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
            bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
            transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Set the book list to the TableView
            bookTableView.setItems(bookList);
            // Set the transaction list to the TableView
            transactionTableView.setItems(transactionList);

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database connection error!");
        }
    }

    private void loadBooks() {
        String query = "SELECT * FROM book";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            bookList.clear(); // Clear existing data

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");

                Book book = new Book(title, author);
                bookList.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error loading books: " + e.getMessage());
        }
    }


    private void loadTransactions() {
        // Load transactions from database
        String query = "SELECT * FROM transactions";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            transactionList.clear(); // Clear existing data

            while (resultSet.next()) {
                int transactionId = resultSet.getInt("transaction_id");
                int userId = resultSet.getInt("user_id");
                int bookId = resultSet.getInt("book_id");
                LocalDate transactionDate = resultSet.getDate("transaction_date").toLocalDate();
                String status = resultSet.getString("status");

                // Create Transaction object using the updated constructor
                Transaction transaction = new Transaction(transactionId, userId, bookId,status);
                transactionList.add(transaction);
            }

            // Set the updated transactionList to the transactionTableView
            transactionTableView.setItems(transactionList);

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error loading transactions: " + e.getMessage());
        }
    }


    @FXML
    void addBook() {
        String title = bookTitleField.getText().trim();
        String author = authorField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            showErrorAlert("Title and author fields cannot be empty!");
            return;
        }

        // Insert book into database
        String insertQuery = "INSERT INTO book (title, author) VALUES (?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(1, title);
            statement.setString(2, author);
            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                showInfoAlert("Book added successfully!");
                loadBooks(); // Refresh the book list after adding a new book
            } else {
                showErrorAlert("Failed to add book!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error adding book: " + e.getMessage());
        }
    }

    @FXML
    void editBook() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("Please select a book to update!");
            return;
        }

        String newTitle = bookTitleField.getText().trim();
        String newAuthor = authorField.getText().trim();

        if (newTitle.isEmpty() || newAuthor.isEmpty()) {
            showErrorAlert("Title and author fields cannot be empty!");
            return;
        }

        String updateQuery = "UPDATE book SET title = ?, author = ? WHERE id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setString(1, newTitle);
            statement.setString(2, newAuthor);
            statement.setInt(3, selectedBook.getId());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                showInfoAlert("Book updated successfully!");
                loadBooks(); // Refresh the book list after updating
            } else {
                showErrorAlert("Failed to update book!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error updating book: " + e.getMessage());
        }
    }

    @FXML
    void deleteBook() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("Please select a book to delete!");
            return;
        }

        String deleteTransactionsQuery = "DELETE FROM transactions WHERE book_id = ?";
        String deleteBookQuery = "DELETE FROM book WHERE id = ?";

        try {
            // Begin transaction
            connection.setAutoCommit(false);

            // Delete associated transactions
            PreparedStatement deleteTransactionsStmt = connection.prepareStatement(deleteTransactionsQuery);
            deleteTransactionsStmt.setInt(1, selectedBook.getId());
            deleteTransactionsStmt.executeUpdate();

            // Delete the book
            PreparedStatement deleteBookStmt = connection.prepareStatement(deleteBookQuery);
            deleteBookStmt.setInt(1, selectedBook.getId());
            int rowsDeleted = deleteBookStmt.executeUpdate();

            // Commit transaction
            connection.commit();
            connection.setAutoCommit(true);

            if (rowsDeleted > 0) {
                showInfoAlert("Book and its associated transactions deleted successfully!");
                loadBooks(); // Refresh the book list after deleting
            } else {
                showErrorAlert("Failed to delete book!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error deleting book: " + e.getMessage());

            try {
                // Rollback transaction in case of error
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
                showErrorAlert("Error rolling back transaction: " + rollbackEx.getMessage());
            }
        }
    }



    @FXML
    void approveBook() {
        Transaction selectedTransaction = transactionTableView.getSelectionModel().getSelectedItem();

        if (selectedTransaction == null) {
            showErrorAlert("Please select a transaction to approve!");
            return;
        }

        // Update the status to "Approved"
        selectedTransaction.setStatus("Approved");

        // Update status in the database
        String updateQuery = "UPDATE transactions SET status = ? WHERE transaction_id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setString(1, "Approved");
            statement.setInt(2, selectedTransaction.getTransactionId());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                showInfoAlert("Transaction approved successfully!");
                // Refresh transaction list and table view
                loadTransactions();
            } else {
                showErrorAlert("Failed to approve transaction!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error approving transaction: " + e.getMessage());
        }
    }

    @FXML
    void rejectBook() {
        Transaction selectedTransaction = transactionTableView.getSelectionModel().getSelectedItem();

        if (selectedTransaction == null) {
            showErrorAlert("Please select a transaction to reject!");
            return;
        }

        // Update the status to "Rejected"
        selectedTransaction.setStatus("Rejected");

        // Update status in the database
        String updateQuery = "UPDATE transactions SET status = ? WHERE transaction_id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setString(1, "Rejected");
            statement.setInt(2, selectedTransaction.getTransactionId());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                showInfoAlert("Transaction rejected successfully!");
                // Refresh transaction list and table view
                loadTransactions();
            } else {
                showErrorAlert("Failed to reject transaction!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error rejecting transaction: " + e.getMessage());
        }
    }



    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}