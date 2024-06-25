package application.Model;

public class User {
    private int id;
    private String username;
    private String password;
    private String surname;
    private String name;
    private String role;

    public User(int id, String username, String password, String surname, String name) {
        this(id, username, password, surname, name, "user");  // Call the other constructor with default role
    }

    public User(int id, String username, String password, String surname, String name, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.surname = surname;
        this.name = name;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
