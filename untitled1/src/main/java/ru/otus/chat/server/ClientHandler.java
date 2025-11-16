package ru.otus.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;


public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String username;
    private Boolean authenticated;

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
            try {
                //Цикл аутентификации
                while (true) {
                    sendMsg("Перед работой с чатом необходимо выполнить аутентификацию "
                            + ConsoleColors.PURPLE + "'/auth login password'" + ConsoleColors.RESET
                            + " или регистрацию "
                            + ConsoleColors.PURPLE + "'/reg login password'" + ConsoleColors.RESET);
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.startsWith("/exit")) {
                            sendMsg("/exitOk");
                            break;
                        }
                        if (message.startsWith("/auth ")) {
                            String[] token = message.split(" ");
                            if (token.length != 3) {
                                sendMsg("Неверный формат команды");
                                continue;
                            }
                            if (server.getAutenticatedProvider()
                                    .authenticate(this, token[1], token[2])) {
                                authenticated = true;
                                sendMsg("Вы подключились сником: " + username);
                                break;
                            }
                            continue;
                        }
                        if (message.startsWith("/reg ")){
                            String[] token = message.split(" ");
                            if (token.length!=4){
                                sendMsg("Неверный формат команды /req");
                                continue;
                            }
                            if (server.getAutenticatedProvider()
                                    .register(this, token[1], token[2], token[3])){
                                authenticated = true;
                                sendMsg("Вы подключились сником: " + username);
                                break;
                            }
                        }
                    }
                }
                //Цикл работы
                while (authenticated) {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.startsWith("/exit")) {
                            sendMsg("/exitOk");
                            break;
                        }
                        if (message.startsWith("/w ") && server.getClientFromName(message.substring(3)) != null) {
                            String[] token = message.split(" ", 3);
                            server.getClientFromName(message.substring(3)).sendMsg(token[2]);
                        }
                    } else server.broadcastMessage(username, message);
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