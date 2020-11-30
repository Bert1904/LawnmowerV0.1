package com.example.lawnmower;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketService {
    private static final int TIMEOUT = 5000;
    private static Socket SOCKET = new Socket();

    public static void connect(String ip, int port) throws IOException {
        SOCKET.connect(new InetSocketAddress(ip, port), TIMEOUT);
    }

    public static Socket getSocket() {
        return SOCKET;
    }
}
