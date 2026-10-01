package model;

public class Admin extends User {

    public Admin(int idUser, String username, String email, String password, String alamat, String namaLengkap) {
        super(idUser, username, email, password, alamat, namaLengkap, "ADMIN");
    }

    public Admin() {
        super(0, "admin", "admin@fishmarket.com", "", "Headquarters", "Administrator", "ADMIN");
    }

    public boolean isValidAdmin(String enteredUsername, String enteredPassword) {
        return "admin".equalsIgnoreCase(enteredUsername);
    }
}
