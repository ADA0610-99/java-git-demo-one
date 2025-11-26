package ru.otus.chat.server;


import java.sql.SQLException;

public class ServerApp {
    public static final int PORT = 8189;

    public static void main(String[] args) throws SQLException {
        new Server(PORT).start();

    }
}