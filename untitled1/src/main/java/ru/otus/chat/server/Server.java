package ru.otus.chat.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        clients = new CopyOnWriteArrayList<>();
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запустился на порту: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(socket, this));
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
            c.sendMsg(username  + ": " + message);
        }
    }

    public ClientHandler getClientFromName( String message) {
        for (ClientHandler c: this.clients) {
            if (message.startsWith(c.getUsername())) {
                return c;
            }
        }
        return null;
    }
}