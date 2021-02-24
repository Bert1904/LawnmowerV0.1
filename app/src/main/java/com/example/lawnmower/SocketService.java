package com.example.lawnmower;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public final class SocketService {
    private static final int TIMEOUT = 5000;
    private Socket SOCKET = new Socket();
    private String Ip;
    private Integer Port;
    private AsyncTask<Void, AppControlsProtos.LawnmowerStatus, Void> backgroundtask;
    private static SocketService socketService = new SocketService();
    //private static boolean isConnected = false;
    //consider moving the send and receive methods here for better code
    //and to handle failed sending, dcs etc

    private SocketService() {
    }

    public static SocketService getInstance() {
        return socketService;
    }

    /**
     * A Method, that tries to connect to the server on the lawnmower.
     * If the connection fails, we need to create a new socket before trying to reconnect.
     * @param ip the {@code IP}
     * @param port the {@code Port}
     * @throws IOException if an error occures during the connection
     */
    public void connect(String ip, int port) throws IOException {
        try {
            SOCKET.connect(new InetSocketAddress(ip, port), TIMEOUT);
            backgroundtask = new ListenerThread().execute();
        } catch (Exception e) {
            disconnect();
            throw e;
        }
        Ip = ip;
        Port = port;
        //isConnected = true;
    }

    /*/public Socket getSocket() {
        return SOCKET;
    }*/

    public String getIp() {
        return Ip;
    }

    public int getPort() {
        int i = Port.intValue();
        return i;
    }

    public void disconnect() {
        SOCKET = new Socket();
        Ip = null;
        Port = null;
        //isConnected = false;
    }

    public void send(final byte[] message)  throws IOException {
        Log.i("SocketService","SendDataToNetwork: send method");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SOCKET.getOutputStream().write(message);
                    SOCKET.getOutputStream().flush();
                    Log.i("serialize","SendDataToNetwork: Success");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class ListenerThread extends AsyncTask<Void, AppControlsProtos.LawnmowerStatus, Void> {

        Activity activity;
        IOException ioException;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... params) {
            Log.i("Do Background", "Background task started");
            byte[] length;
            int msgLength;
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(SOCKET.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            byte[] msg;
            AppControlsProtos.LawnmowerStatus lawnmowerStatus = null;
            while (true) {
                try {
                    // Methode 1
                    length = new byte[4];
                    SOCKET.getInputStream().read(length);
                    msgLength = convertByteArrayToInt(length);
                    msg = new byte[msgLength];
                    readExact(SOCKET.getInputStream(), msg, 0, msgLength);
                    Log.i("bytes read", "successfully read bytes");
                    try {
                        lawnmowerStatus = AppControlsProtos.LawnmowerStatus.parseFrom(msg);
                        Log.i("msg", "" + lawnmowerStatus.toString());
                    } catch (InvalidProtocolBufferException e) {
                        Log.i("Exception", "msg: " + e.getMessage());
                    }
                    Log.i("Message", "reveived Message");
                    //handleStatus(lawnmowerStatus.getStatus());
                    //setBatteryState(lawnmowerStatus.getBatteryState());

                    //sets data to the singleton
                    LawnmowerStatusData.getInstance().setLawnmowerStatus(lawnmowerStatus);
                    /*lawnmowerStatusData.setStatus(lawnmowerStatus.getStatus());
                    lawnmowerStatusData.setError(lawnmowerStatus.getError());
                    lawnmowerStatusData.setBatteryState(lawnmowerStatus.getBatteryState());
                    lawnmowerStatusData.setLatitude(lawnmowerStatus.getLatitude());
                    lawnmowerStatusData.setLongitude(lawnmowerStatus.getLongitude());
                    lawnmowerStatusData.setError_msg(lawnmowerStatus.getErrorMsg());
                    lawnmowerStatusData.setMowing_finished(lawnmowerStatus.getFinishedMowing());*/
                    //Thread.sleep(10);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //return null;
        }

        //least significant bit first
        private int convertByteArrayToInt(byte[] data) {
            if (data == null || data.length != 4) return 0x0;
            // ----------
            return
                    ((0xff & data[0]) << 0 |
                            (0xff & data[1]) << 8 |
                            (0xff & data[2]) << 16 |
                            (0xff & data[3]) << 24);
        }

        private void readExact(InputStream stream, byte[] buffer, int offset, int count) throws Exception {
            int bytesRead;
            if (count < 0) {
                throw new IllegalArgumentException();
            }
            while (count != 0 &&
                    (bytesRead = stream.read(buffer, offset, count)) > 0) {
                offset += bytesRead;
                count -= bytesRead;
            }
            if (count != 0) throw new Exception("End of stream was reached.");
        }

        @Override
        protected void onProgressUpdate(AppControlsProtos.LawnmowerStatus... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute() {
            Log.i(" Background", "Background task ended");
            if (this.ioException != null) {
                new AlertDialog.Builder(this.activity)
                        .setTitle("Ein Fehler ist aufgetreten")
                        .setMessage(this.ioException.toString())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

    /*public static boolean isConnected() {
        return isConnected;
    }*/
}
