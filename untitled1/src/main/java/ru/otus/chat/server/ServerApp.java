package ru.otus.chat.server;


public class ServerApp {
    public static final int PORT = 8189;

    public static void main(String[] args) {
        new Server(PORT).start();

    }
}