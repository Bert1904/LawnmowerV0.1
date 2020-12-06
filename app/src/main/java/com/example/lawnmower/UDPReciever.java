package com.example.lawnmower;

import android.renderscript.ScriptGroup;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.nio.ByteBuffer;

public class UDPReciever extends Thread {

    /* Consider working with Interrupts instead of flags
     */
    private DatagramSocket mDatagramSocket;
    private DatagramPacket packet;
    private boolean running;
    private final int SIZE = 64048;
    private byte[] buf = new byte[SIZE];
    private int PORT = 6750;
    private short index;
    private int data_size;
    private byte[] imgPart = new byte[64000];

    /**
     * Should check for image numbers to make sure it's assembled the right way.
     * Other option is to do this in the ImageAdapter.
     */
    @Override
    public void run() {
        running = true;
        try {
            while(running) {
                mDatagramSocket = new DatagramSocket(PORT);
                packet = new DatagramPacket(buf, buf.length);
                Log.i("UPD Client: ", "about to wait to receive");
                mDatagramSocket.receive(packet);
                seperatePacketData(packet.getData());
                String text = new String(buf, 0, packet.getLength());
                Log.d("Received data", text);
                //use data in packet.getData()
            }
        } catch (IOException e) {
            running = false;
            e.printStackTrace();
        }
    }

    private void seperatePacketData(byte[] data) {
        byte[] index = new byte[16];
        byte[] data_size = new byte[32];
        for(int i: index) {
            index[i] = data[i];
        }
        for(int j: data_size) {
            data_size[j] = data[16 + j];
        }
        this.index = ByteBuffer.wrap(index).getShort();
        this.data_size = ByteBuffer.wrap(data_size).getInt();
        this.imgPart = data;
    }

    public void close() {
        running = false;
    }
}