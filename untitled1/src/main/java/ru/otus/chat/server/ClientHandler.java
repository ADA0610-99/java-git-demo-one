package ru.otus.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String username;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        this.server = server;
        this.username = "user" + socket.getPort();

        new Thread(() -> {
            System.out.println("Клиент подключился " + socket.getPort());
            sendMsg("Вы подключились сником: " + username);
            try {
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.startsWith("/exit")) {
                            sendMsg("/exitOk");
                            break;
                        }

                    } else {
                        server.broadcastMessage(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
