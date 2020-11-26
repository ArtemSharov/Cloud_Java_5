public class User {
    public int getID() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    private int id;
    private String login;
    private String password;

    public User(int id, String login, String password){
        this.id = id;
        this.login = login;
        this.password = password;
    }

}
