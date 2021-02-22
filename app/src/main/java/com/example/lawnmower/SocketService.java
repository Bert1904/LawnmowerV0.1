package com.example.lawnmower;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SocketService {
    private static final int TIMEOUT = 5000;
    private static Socket SOCKET = new Socket();
    private static String Ip;
    private static Integer Port;
    //private static boolean isConnected = false;
    //consider moving the send and receive methods here for better code
    //and to handle failed sending, dcs etc

    /**
     * A Method, that tries to connect to the server on the lawnmower.
     * If the connection fails, we need to create a new socket before trying to reconnect.
     * @param ip the {@code IP}
     * @param port the {@code Port}
     * @throws IOException if an error occures during the connection
     */
    public static void connect(String ip, int port) throws IOException {
        try {
            SOCKET.connect(new InetSocketAddress(ip, port), TIMEOUT);
        } catch (Exception e) {
            disconnect();
            throw e;
        }
        Ip = ip;
        Port = port;
        //isConnected = true;
    }

    public static Socket getSocket() {
        return SOCKET;
    }

    public static String getIp() {
        return Ip;
    }

    public static int getPort() {
        int i = Port.intValue();
        return i;
    }

    public static void disconnect() {
        SOCKET = new Socket();
        Ip = null;
        Port = null;
        //isConnected = false;
    }

    /*public static boolean isConnected() {
        return isConnected;
    }*/
}
