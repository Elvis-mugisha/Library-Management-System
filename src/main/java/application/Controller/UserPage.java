package application.Controller;

import application.Model.Book;
import application.Model.User;
import application.Util.DB;
import application.Util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class UserPage {

    @FXML
    private TableView<Book> bookTableView;

    @FXML
    private TableView<Book> borrowedBooksTableView;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> authorColumn;

    @FXML
    private TableColumn<Book, String> borrowedTitleColumn;

    @FXML
    private TableColumn<Book, String> borrowedAuthorColumn;

    @FXML
    private TableColumn<Book, LocalDate> borrowedDateColumn;

    @FXML
    private Button borrowButton;

    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private ObservableList<Book> borrowedBooksList = FXCollections.observableArrayList(); // List for borrowed books
    private Connection connection;

    @FXML
    public void initialize() {
        // Initialize TableView columns for available books
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());

        // Initialize TableView columns for borrowed books
        borrowedTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        borrowedAuthorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());


        try {
            connection = DB.getConnection();
            if (connection != null) {
                loadBooks();
                loadBorrowedBooks();
            } else {
                showErrorAlert("Database connection failed!");
            }
        } catch (SQLException e) {
            showErrorAlert("Database connection error: " + e.getMessage());
        }
    }

    private void loadBooks() {
        String query = "SELECT * FROM book WHERE is_borrowed = false";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");

                Book book = new Book( title, author);
                bookList.add(book);
            }

            if (bookTableView != null) {
                bookTableView.setItems(bookList);
            } else {
                showErrorAlert("TableView not initialized!");
            }
        } catch (SQLException e) {
            showErrorAlert("Error loading books: " + e.getMessage());
        }
    }

    @FXML
    void borrowBook() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            try {
                connection.setAutoCommit(false);

                User currentUser = getCurrentUser(); // Get the current logged-in user from session
                LocalDate transactionDate = LocalDate.now();
                String insertQuery = "INSERT INTO transactions (user_id, book_id, transaction_date, status) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setInt(1, currentUser.getId());
                insertStatement.setInt(2, selectedBook.getId());
                insertStatement.setDate(3, java.sql.Date.valueOf(transactionDate));
                insertStatement.setString(4, "pending");
                insertStatement.executeUpdate();

                connection.commit();

                showInfoAlert("Borrow request sent for approval: " + selectedBook.getTitle());
                // Refresh borrowed books table after borrowing
                loadBorrowedBooks();
            } catch (SQLException e) {
                rollbackTransaction();
                showErrorAlert("Error sending borrow request: " + e.getMessage());
            } finally {
                resetAutoCommit();
            }
        } else {
            showErrorAlert("Please select a book to borrow!");
        }
    }

    @FXML
    void loadBorrowedBooks() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            String query = "SELECT b.id, b.title, b.author, t.transaction_date " +
                    "FROM book b JOIN transactions t ON b.id = t.book_id " +
                    "WHERE t.user_id = ? AND t.status = 'approved'";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, currentUser.getId());
                ResultSet resultSet = statement.executeQuery();

                borrowedBooksList.clear(); // Clear existing data

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    String author = resultSet.getString("author");
                    LocalDate borrowedDate = resultSet.getDate("transaction_date").toLocalDate();

                    Book book = new Book( title, author);
                    borrowedBooksList.add(book);
                }

                // Set borrowed books list to the borrowedBooksTableView
                borrowedBooksTableView.setItems(borrowedBooksList);

            } catch (SQLException e) {
                showErrorAlert("Error loading borrowed books: " + e.getMessage());
            }
        } else {
            showErrorAlert("User not logged in!");
        }
    }

    private User getCurrentUser() {
        return Session.getInstance().getCurrentUser();
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

    private void rollbackTransaction() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetAutoCommit() {
        try {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
