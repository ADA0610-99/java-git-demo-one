package ru.otus.chat.server;

import javax.lang.model.type.NullType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DbAuthenticatedProvider implements AutenticatedProvider {
    private class User {
        private String login;
        private String password;
        private String username;
        private ClientRole role;

        public User(String login, String password, String username, ClientRole role) {
            this.login = login;
            this.password = password;
            this.username = username;
            this.role = role;
        }
    }

    //private User user;
    private Server server;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/otus_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Zelenodolsk9";
    private static final String USER_QUERY = "select us1.*, r.name_role  from role r \n" +
            "right join usertorole u on r.id = u.id_role \n" +
            "right join user_list us1 on u.id_user = us1.id " +
            "where us1.email = ?";
    private static final String USER_QUERY_BY_NAME = "select us1.*, r.name_role  from role r \n" +
            "right join usertorole u on r.id = u.id_role \n" +
            "right join user_list us1 on u.id_user = us1.id " +
            "where us1.user_name = ?";
    private static final String USER_CREATE = "WITH new_user AS (\n" +
            "    INSERT INTO user_list (password, email, user_name)\n" +
            "    VALUES (?, ?, ?)\n" +
            "    RETURNING id\n" +
            ")\n" +
            "INSERT INTO usertorole (id_user, id_role)\n" +
            "SELECT u.id, r.id\n" +
            "FROM new_user u\n" +
            "JOIN role r ON r.name_role = ?";

    private final Connection connection;


    private boolean isLoginAlredyExists(String login) {
        if (getClientByLogin(login) == null) {
            return false;
        }
        return true;
    }

    private boolean isUsernameAlredyExists(String username) {
        if (getClientByUserName(username) == null) {
            return false;
        }
        return true;
    }


    public DbAuthenticatedProvider(Server server) throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        this.server = server;
    }

    private User getClientByLogin(String str) {
        try (PreparedStatement statement = connection.prepareStatement(USER_QUERY)) {
            statement.setString(1, str);
            try (ResultSet res = statement.executeQuery()) {
                if (res.next()) {
                    String login = res.getString(3);
                    String password = res.getString(2);
                    String username = res.getString(4);
                    ClientRole role = ClientRole.valueOf(res.getString(5));
                    User user = new User(login, password, username, role);
                    return user;
                } else return null;

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User getClientByUserName(String username1) {
        try (PreparedStatement statement = connection.prepareStatement(USER_QUERY_BY_NAME)) {
            statement.setString(1, username1);
            try (ResultSet res = statement.executeQuery()) {
                if (res.next()) {
                    String login = res.getString(3);
                    String password = res.getString(2);
                    String username = res.getString(4);
                    ClientRole role = ClientRole.valueOf(res.getString(5));
                    User user = new User(login, password, username, role);
                    return user;
                } else return null;

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUsernameByLoginAndPassword(String login, String password) {
        User user = getClientByLogin(login);
        if (user == null) {
            return null;
        }
        if (user.password.equals(password)) {
            return user.username;
        }
        return null;
    }

    private ClientRole getRoleByLoginAndPassword(String login, String password) {
        User user = getClientByLogin(login);
        if (user.password.equals(password)) {
            return user.role;
        }
        return null;
    }

    @Override
    public boolean authenticate(ClientHandler clientHandler, String login, String password) {
        String authUsername = getUsernameByLoginAndPassword(login, password);
        if (authUsername == null) {
            clientHandler.sendMsg("Некоректный логин/проль");
            return false;
        }
        if (server.isUserNameBusy(authUsername)) {
            clientHandler.sendMsg("Указанная учетная запись уже используется");
            return false;
        }
        clientHandler.setUsername(authUsername);
        clientHandler.setRole(getRoleByLoginAndPassword(login, password));
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/authok" + authUsername);
        return true;
    }


    @Override
    public boolean register(ClientHandler clientHandler,
                            String login, String password, String username, ClientRole role) {
        if (login.length() < 3) {
            clientHandler.sendMsg("Логин должен содержать не меньше 3 символов");
            return false;
        }
        if (!login.toLowerCase().matches("[a-z]+")) {
            clientHandler.sendMsg("Логин должен состоять из латинских символов");
            return false;
        }
        if (password.length() < 3) {
            clientHandler.sendMsg("Пароль должен содержать не меньше 3 символов");
            return false;
        }
        if (isLoginAlredyExists(login)) {
            clientHandler.sendMsg("Такой логин уже занят");
            return false;
        }
        if (isUsernameAlredyExists(username)) {
            clientHandler.sendMsg("Такое имя пользователя уже занято");
        }
        //users.add(new User(login, password, username, role));
        try (PreparedStatement statement = connection.prepareStatement(USER_CREATE)) {
            statement.setString(1, password);
            statement.setString(2, login);
            statement.setString(3, username);
            statement.setString(4, String.valueOf(role));
            statement.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        clientHandler.setUsername(username);
        clientHandler.setRole(getRoleByLoginAndPassword(login, password));
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/regok " + username);
        return true;
    }
}
