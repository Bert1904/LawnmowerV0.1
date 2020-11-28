package com.example.lawnmower;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Einstellungen extends AppCompatActivity {
    Thread Thread1 = null;
    EditText Button_Ip, Button_Port;
    String SERVER_IP;
    int SERVER_PORT;
    private final String CONNECTION_SERVER = "Versuche Verbindung zum Roboter aufzubauen";
    private final String CONNECTION_LOST = "Verbindung Fehlgeschlagen";
    public  Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_einstellungen);
    Button_Ip = findViewById(R.id.Button_Ip);
    Button_Port = findViewById(R.id.Button_Port);
    Button Button_Connect = findViewById(R.id.Button_Connect);
    Button_Connect.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SERVER_IP = Button_Ip.getText().toString().trim();
            SERVER_PORT = Integer.parseInt(Button_Port.getText().toString().trim());
            Thread1 = new Thread(new Thread1());
            Thread1.start();
            Toast.makeText(getApplicationContext(), CONNECTION_SERVER, Toast.LENGTH_LONG).show();
        }
    });

}
    private PrintWriter output;
    private BufferedReader input;

    public class Thread1 implements Runnable {

        @Override
        public void run() {
            try {
                socket = new Socket (SERVER_IP, SERVER_PORT);
                output= new PrintWriter(socket.getOutputStream());
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Connect", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

    }
    public Socket getSocket(){
        return socket;}

}
