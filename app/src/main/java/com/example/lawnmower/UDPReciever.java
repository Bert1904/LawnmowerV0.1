package com.example.lawnmower;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPReciever extends Thread {

    /* Consider working with Interrupts instead of flags
     */
    private DatagramSocket mDatagramSocket;
    private DatagramPacket packet;
    private boolean running;
    private final int SIZE = 64048;
    private byte[] buf = new byte[SIZE];
    private int PORT = 6750;
    private int index;
    private int data_size;
    private byte[] imgPart = new byte[64000];

    @Override
    public void run() {
        running = true;
        try {
            while(running) {
                mDatagramSocket = new DatagramSocket(PORT);
                packet = new DatagramPacket(buf, buf.length);
                Log.i("UPD Client: ", "about to wait to receive");
                mDatagramSocket.receive(packet);
                String text = new String(buf, 0, packet.getLength());
                Log.d("Received data", text);
                //use data in packet.getData()
            }
        } catch (IOException e) {
            running = false;
            e.printStackTrace();
        }
    }

    public void close() {
        running = false;
    }
}