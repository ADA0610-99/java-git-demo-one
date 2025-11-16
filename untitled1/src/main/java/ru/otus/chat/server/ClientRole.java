package ru.otus.chat.server;

public enum ClientRole {
    ADMIN, USER;

    public static boolean isItClientRole(String role) {
        if (ADMIN.name().equals(role)) {
            return true;
        }
        if (USER.name().equals(role)) {
            return true;
        }
        return false;
    }
}
