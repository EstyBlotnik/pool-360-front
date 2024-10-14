package myandroid.app.hhobzic.a360pool.classes;


public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public LoginRequest(String email) {
        this.email = email;
    }
}