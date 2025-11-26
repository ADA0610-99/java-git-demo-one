package ru.otus.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String username;
    private Boolean authenticated;
    private ClientRole role;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(ClientRole role) {
        this.role = role;
    }

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        this.server = server;
        this.username = "user" + socket.getPort();
        this.role = ClientRole.USER;


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
                        if (message.startsWith("/reg ")) {
                            String[] token = message.split(" ");
                            if (token.length != 5) {
                                sendMsg("Неверный формат команды /req");
                                continue;
                            }
                            if (!token[4].equals(ClientRole.ADMIN.name()) && !token[4].equals(ClientRole.USER.name())) {
                                sendMsg("Роль задана неверно!");
                                continue;
                            }
                            if (server.getAutenticatedProvider()
                                    .register(this, token[1], token[2], token[3], ClientRole.valueOf(token[4]))) {
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
                        if (message.startsWith("/kick")) {
                            String[] token = message.split(" ");
                            if (token.length < 2) {
                                sendMsg("Введите username");
                                continue;
                            }
                            if (this.role == ClientRole.ADMIN) {
                                server.getClientFromName(token[1]).sendMsg("/kickOk вас отсоединили");
                                disconnect(server.getClientFromName(token[1]));
                                continue;
                            } else {
                                sendMsg("Кикать могут только админы!");
                            }
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
                disconnect(this);
            }
        }).start();
    }

    private void setAuthenticated(boolean bool) {
        this.authenticated = bool;
    }


    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void disconnect(ClientHandler clientHandler) {
        server.unsubscribe(clientHandler);
        try {
            if (clientHandler.in != null) {
                clientHandler.in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (clientHandler.out != null) {
                clientHandler.out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (clientHandler.socket != null) {
                clientHandler.socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}