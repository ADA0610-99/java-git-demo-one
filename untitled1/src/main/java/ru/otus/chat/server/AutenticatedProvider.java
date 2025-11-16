package ru.otus.chat.server;

public interface AutenticatedProvider {
    void initialize();

    boolean authenticate(ClientHandler clientHandler, String login, String password);

    boolean register(ClientHandler clientHandler, String login, String password, String username, ClientRole role);

    boolean isUsernAdmin(String username);
}
