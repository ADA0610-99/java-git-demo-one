//package ru.otus.chat.server;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//public class InMamoryAuthenticatedProvider implements AutenticatedProvider {
//    private class User {
//        private String login;
//        private String password;
//        private String username;
//        private ClientRole role;
//
//        public User(String login, String password, String username, ClientRole role) {
//            this.login = login;
//            this.password = password;
//            this.username = username;
//            this.role = role;
//        }
//    }
//
//    private List<User> users;
//    private Server server;
//
//    private boolean isLoginAlredyExists(String login) {
//        for (User u : users) {
//            if (u.login.equalsIgnoreCase(login)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean isUsernameAlredyExists(String username) {
//        for (User u : users) {
//            if (u.username.equalsIgnoreCase(username)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
////    @Override
////    public boolean isUsernAdmin(String username) {
////        for (User u : users) {
////            if (u.username.equals(username) && u.role == ClientRole.ADMIN) {
////                return true;
////            }
////        }
////        return false;
////    }
//
//    public InMamoryAuthenticatedProvider(Server server) {
//        users = new CopyOnWriteArrayList<>();
//        users.add(new User("qwe", "qwe", "qwe1", ClientRole.ADMIN));
//        users.add(new User("asd", "asd", "asd1", ClientRole.USER));
//        users.add(new User("zxc", "zxc", "zxc1", ClientRole.USER));
//        this.server = server;
//    }
//
//    private String getUsernameByLoginAndPassword(String login, String password) {
//        for (User u : users) {
//            if (u.login.equalsIgnoreCase(login) && u.password.equals(password)) {
//                return u.username;
//            }
//        }
//        return null;
//    }
//
//    private ClientRole getRoleByLoginAndPassword(String login, String password) {
//        for (User u : users) {
//            if (u.login.equalsIgnoreCase(login) && u.password.equals(password)) {
//                return u.role;
//            }
//        }
//        return null;
//    }
//
//
////    @Override
////    public void initialize() {
////        System.out.println("InMemoryAuthenticatedProvider");
////    }
//
//    @Override
//    public boolean authenticate(ClientHandler clientHandler, String login, String password) {
//        String authUsername = getUsernameByLoginAndPassword(login, password);
//        if (authUsername == null) {
//            clientHandler.sendMsg("Некоректный логин/проль");
//            return false;
//        }
//        if (server.isUserNameBusy(authUsername)) {
//            clientHandler.sendMsg("Указанная учетная запись уже используется");
//            return false;
//        }
//        clientHandler.setUsername(authUsername);
//        clientHandler.setRole(getRoleByLoginAndPassword(login, password));
//        server.subscribe(clientHandler);
//        clientHandler.sendMsg("/authok" + authUsername);
//        return true;
//    }
//
//
//    @Override
//    public boolean register(ClientHandler clientHandler,
//                            String login, String password, String username, ClientRole role) {
//        if (login.length() < 3) {
//            clientHandler.sendMsg("Логин должен содержать не меньше 3 символов");
//            return false;
//        }
//        if (!login.toLowerCase().matches("[a-z]+")) {
//            clientHandler.sendMsg("Логин должен состоять из латинских символов");
//            return false;
//        }
//        if (password.length() < 3) {
//            clientHandler.sendMsg("Пароль должен содержать не меньше 3 символов");
//            return false;
//        }
//        if (isLoginAlredyExists(login)) {
//            clientHandler.sendMsg("Такой логин уже занят");
//            return false;
//        }
//        if (isUsernameAlredyExists(username)) {
//            clientHandler.sendMsg("Такое имя пользователя уже занято");
//        }
//        users.add(new User(login, password, username, role));
//        clientHandler.setUsername(username);
//        clientHandler.setRole(getRoleByLoginAndPassword(login, password));
//        server.subscribe(clientHandler);
//        clientHandler.sendMsg("/regok " + username);
//        return true;
//    }
//}