package model;

import javafx.beans.property.*;

public class User {
    private final IntegerProperty idUser;
    private final StringProperty username;
    private final StringProperty email;
    private final StringProperty password;
    private final StringProperty alamat;
    private final StringProperty namaLengkap;
    private final StringProperty role;

    // Constructors
    public User(int idUser, String username, String email, String password, String alamat, String namaLengkap) {
        this(idUser, username, email, password, alamat, namaLengkap, "USER");
    }

    public User(int idUser, String username, String email, String password, String alamat, String namaLengkap, String role) {
        this.idUser = new SimpleIntegerProperty(idUser);
        this.username = new SimpleStringProperty(username);
        this.email = new SimpleStringProperty(email);
        this.password = new SimpleStringProperty(password);
        this.alamat = new SimpleStringProperty(alamat != null ? alamat : "");
        this.namaLengkap = new SimpleStringProperty(namaLengkap != null ? namaLengkap : username);
        this.role = new SimpleStringProperty(role != null ? role : "USER");
    }

    // Property getters for JavaFX TableView bindings
    public IntegerProperty idUserProperty() { return idUser; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty alamatProperty() { return alamat; }
    public StringProperty namaLengkapProperty() { return namaLengkap; }
    public StringProperty roleProperty() { return role; }

    // Getters
    public int getIdUser() { return idUser.get(); }
    public String getUsername() { return username.get(); }
    public String getEmail() { return email.get(); }
    public String getPassword() { return password.get(); }
    public String getAlamat() { return alamat.get(); }
    public String getNamaLengkap() { return namaLengkap.get(); }
    public String getRole() { return role.get(); }

    // Setters
    public void setIdUser(int idUser) { this.idUser.set(idUser); }
    public void setUsername(String username) { this.username.set(username); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPassword(String password) { this.password.set(password); }
    public void setAlamat(String alamat) { this.alamat.set(alamat); }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap.set(namaLengkap); }
    public void setRole(String role) { this.role.set(role); }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }
}
