package ru.otus.chat.server;

import java.util.List;

public interface AutenticatedProvider {
    void initialize();

    boolean authenticate(ClientHandler clientHandler, String login, String password);

    boolean register(ClientHandler clientHandler, String login, String password, String username, ClientRole role);

}
