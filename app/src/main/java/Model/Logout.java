package Model;

public class Logout {
    private String token;

    public Logout(String token) {
        this.token = token;
    }

    public Logout() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
