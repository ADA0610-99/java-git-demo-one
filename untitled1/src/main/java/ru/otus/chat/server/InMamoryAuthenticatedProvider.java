package ru.otus.chat.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMamoryAuthenticatedProvider implements AutenticatedProvider {
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

    private List<User> users;
    private Server server;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/otus_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Zelenodolsk9";
    private static final String USER_QUERY = "select us1.*, r.name_role  from role r \n" +
            "right join usertorole u on r.id = u.id_role \n" +
            "right join user_list us1 on u.id_user = us1.id ";

    private final Connection connection;


    private boolean isLoginAlredyExists(String login) {
        for (User u : users) {
            if (u.login.equalsIgnoreCase(login)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsernameAlredyExists(String username) {
        for (User u : users) {
            if (u.username.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }


    public InMamoryAuthenticatedProvider(Server server) throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        users = getAll();
        this.server = server;
    }

    private List<User> getAll () {
        List<User> users = new ArrayList<>();
        try(Statement statement = connection.createStatement()){
            try(ResultSet res = statement.executeQuery(USER_QUERY)) {
                while (res.next()){
                    String login = res.getString(3);
                    String password = res.getString(2);
                    String username = res.getString(4);
                    ClientRole role = ClientRole.valueOf(res.getString(5));
                    User user = new User(login, password, username, role);
                    users.add(user);
                }

            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return users;
    }

    private String getUsernameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equalsIgnoreCase(login) && u.password.equals(password)) {
                return u.username;
            }
        }
        return null;
    }

    private ClientRole getRoleByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equalsIgnoreCase(login) && u.password.equals(password)) {
                return u.role;
            }
        }
        return null;
    }


    @Override
    public void initialize() {
        System.out.println("InMemoryAuthenticatedProvider");
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
        users.add(new User(login, password, username, role));
        clientHandler.setUsername(username);
        clientHandler.setRole(getRoleByLoginAndPassword(login, password));
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/regok " + username);
        return true;
    }
}
