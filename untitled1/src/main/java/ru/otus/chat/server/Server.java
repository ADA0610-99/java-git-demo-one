package ru.otus.chat.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AutenticatedProvider autenticatedProvider;

    public Server(int port) throws SQLException {
        this.port = port;
        clients = new CopyOnWriteArrayList<>();
        autenticatedProvider = new InMamoryAuthenticatedProvider(this);
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запустился на порту: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        System.out.println("Клиент отключился " + clientHandler.getUsername());
        clients.remove(clientHandler);
    }

    public void broadcastMessage(String username, String message) {
        for (ClientHandler c : clients) {
            c.sendMsg(ConsoleColors.CYAN_BOLD + username + ConsoleColors.RESET + ": " + message);
        }
    }

    public boolean isUserNameBusy(String username) {
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }


    public ClientHandler getClientFromName(String message) {
        for (ClientHandler c : this.clients) {
            if (message.startsWith(c.getUsername())) {
                return c;
            }
        }
        return null;
    }

    public AutenticatedProvider getAutenticatedProvider() {
        return this.autenticatedProvider;
    }
}