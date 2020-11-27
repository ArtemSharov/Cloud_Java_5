import org.sqlite.JDBC;
import java.sql.*;
import java.util.*;

    public class DbHandler {


        private static final String CON_STR = "jdbc:sqlite:Server/tools/CloudUsers.db";

        private static DbHandler instance = null;

        public static synchronized DbHandler getInstance() throws SQLException {
            if (instance == null)
                instance = new DbHandler();
            return instance;
        }

        private Connection connection;

        DbHandler() throws SQLException {
            DriverManager.registerDriver(new JDBC());
            this.connection = DriverManager.getConnection(CON_STR);
        }

        public List<User> getUsers() {

            try (Statement statement = this.connection.createStatement()) {
                List<User> users = new ArrayList<User>();
                ResultSet resultSet = statement.executeQuery("SELECT id, login, password FROM users");
                while (resultSet.next()) {
                    users.add(new User(resultSet.getInt("id"),
                            resultSet.getString("login"),
                            resultSet.getString("password")));
                }
                return users;

            } catch (SQLException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        public void addUser(User user) {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO Users(`login`, `password`) " +
                            "VALUES(?, ?)")) {
                statement.setObject(1, user.getLogin());
                statement.setObject(2, user.getPassword());

                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

