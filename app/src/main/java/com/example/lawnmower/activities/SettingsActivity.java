package com.example.lawnmower.activities;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.lawnmower.AppControlsProtos;
import com.example.lawnmower.R;
import com.example.lawnmower.data.SocketService;

import java.io.IOException;

public class SettingsActivity extends BaseAppCompatAcitivty {

    private static final String CONNECTION_SERVER = "Versuche Verbindung zum Roboter aufzubauen";
    private static final String CONNECTION_LOST = "Verbindung Fehlgeschlagen";
    private static final String NO_INPUT = "Bitte IP-Adresse und Port eingeben";
    private static final String CONNECT_SUCCESS = "Verbindung erfolgreich";
    private static final String CONNECT_FAILED = "Verbindung fehlgeschlagen";

    // Variablen
    private EditText Button_Ip, Button_Port;
    private String SERVER_IP;
    private int SERVER_PORT;
    private Button disconnect, connect;
    private ImageButton lawnmowerShutdown, lawnmowerReboot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button_Ip = findViewById(R.id.Button_Ip);
        Button_Port = findViewById(R.id.Button_Port);
        connect = findViewById(R.id.Button_Connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Keine Eingabe von Ip-Adresse und/oder Port
                if (Button_Port.getText().toString().isEmpty() && Button_Ip.getText().toString().trim().isEmpty() || Button_Port.getText().toString().isEmpty() || Button_Ip.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), NO_INPUT, Toast.LENGTH_LONG).show();
                    return;
                }
                //Initalisierung des Server Socket
                SERVER_IP = Button_Ip.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(Button_Port.getText().toString().trim());

                new Thread(new SocketConnectThread()).start();
                Toast.makeText(getApplicationContext(), CONNECTION_SERVER, Toast.LENGTH_LONG).show();
            }
        });
        disconnect = findViewById(R.id.Button_disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SocketService.getInstance().disconnect();
                disconnect.setEnabled(false);
            }
        });
        disconnect.setEnabled(false);
        lawnmowerShutdown = findViewById(R.id.lawnmowerShutdown);
        lawnmowerShutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SocketService.getInstance().isConnected()) {
                    try {
                        SocketService.getInstance().send(AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.SHUTDOWN).build().toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    SocketService.getInstance().disconnect();
                }
            }
        });
        lawnmowerReboot = findViewById(R.id.lawnmowerReboot);
        lawnmowerReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SocketService.getInstance().isConnected()) {
                    try {
                        SocketService.getInstance().send(AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.REBOOT).build().toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    SocketService.getInstance().disconnect();
                }
            }
        });
    }

    public class SocketConnectThread implements Runnable {
        @Override
        public void run() {
            try {
                SocketService.getInstance().connect(SERVER_IP, SERVER_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), CONNECT_SUCCESS, Toast.LENGTH_LONG).show();
                        disconnect.setEnabled(true);
                    }
                });
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), CONNECT_FAILED, Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
        }
    }
}
