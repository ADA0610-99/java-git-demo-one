package ru.otus.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        new Client();
    }
}