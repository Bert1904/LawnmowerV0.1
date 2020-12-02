package com.example.lawnmower;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class MeinMaeher extends AppCompatActivity {
    // output NACHRICHTEN
    private static final String start = "Starte Mähvorgang";
    private static final String pausiere = "Pausiere Mähvorgang";
    private static final String stoppe = "Stoppe Mähvorgang";
    private static final String GoHome = "Fahre zur Ladestadion";
    private static final String NO_CONNECTION = "Verbindung nicht möglich. \nBitte überprüfe deine Einstellung";

    // Protobuffer Nachrichten senden
    AppControlsProtos.AppControls.Command no_CMD = AppControlsProtos.AppControls.Command.NO_CMD;
    AppControlsProtos.AppControls.Command proto_start = AppControlsProtos.AppControls.Command.START;
    AppControlsProtos.AppControls.Command proto_stop = AppControlsProtos.AppControls.Command.STOP;
    AppControlsProtos.AppControls.Command proto_pause = AppControlsProtos.AppControls.Command.PAUSE;
    AppControlsProtos.AppControls.Command proto_home = AppControlsProtos.AppControls.Command.HOME;

    // Protobuffer Nachrichten empfangen
    AppControlsProtos.LawnmowerStatus.Status status_ready = AppControlsProtos.LawnmowerStatus.Status.Ready;
    AppControlsProtos.LawnmowerStatus.Status status_mowing = AppControlsProtos.LawnmowerStatus.Status.Mowing;
    AppControlsProtos.LawnmowerStatus.Status status_paused = AppControlsProtos.LawnmowerStatus.Status.Paused;
    AppControlsProtos.LawnmowerStatus.Status status_Manual = AppControlsProtos.LawnmowerStatus.Status.Low_Light;

    // Protobuffer Error
    AppControlsProtos.LawnmowerStatus.Error no_error =  AppControlsProtos.LawnmowerStatus.Error.NO_ERROR;
    AppControlsProtos.LawnmowerStatus.Error robot_stuck =  AppControlsProtos.LawnmowerStatus.Error.ROBOT_STUCK;
    AppControlsProtos.LawnmowerStatus.Error  blade_stuck =  AppControlsProtos.LawnmowerStatus.Error. BLADE_STUCK;
    AppControlsProtos.LawnmowerStatus.Error pickup =  AppControlsProtos.LawnmowerStatus.Error.PICKUP;
    AppControlsProtos.LawnmowerStatus.Error lost =  AppControlsProtos.LawnmowerStatus.Error.LOST;

    // Variablen für Mäher Funktionen
    private ImageButton buttonStartMow;
    private ImageButton buttonPauseMow;
    private ImageButton buttonStopMow;
    private ImageButton buttonGoHome;
    private Socket socket;
    private String SERVER_IP = "192.168.0.8";
    private int SERVER_PORT = 6750;

    // Variable um Nachrichten zu Senden
    private PrintWriter message_BufferOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meinmaeher);


        socket = SocketService.getSocket();



        // Toast starte Mähvorgang
        buttonStartMow = (ImageButton) findViewById(R.id.buttonStartMow);
        buttonStartMow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), start, Toast.LENGTH_LONG).show();

                sendMessage(proto_start);
            }
        });
        // Toast pausiere Mähvorgang
        buttonPauseMow = (ImageButton) findViewById(R.id.buttonPauseMow);
        buttonPauseMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), pausiere, Toast.LENGTH_LONG).show();
                //commands.AppControls.Command.PAUSE;
            }
        });

        // Toast stoppe Mähvorgang
        buttonStopMow = (ImageButton) findViewById(R.id.buttonStopMow);
        buttonStopMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), stoppe, Toast.LENGTH_LONG).show();
            }
        });
        // Toast  Mäher kehrt zurück
        buttonGoHome = (ImageButton) findViewById(R.id.buttonGoHome);
        buttonGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), GoHome, Toast.LENGTH_LONG).show();
            }
        });
        if (!socket.isConnected()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), NO_CONNECTION, Toast.LENGTH_LONG).show();

                }
            });
            setNoConnection();
            return;
        }
        else {
            setConnection();
        }
    }
    // setzt die Sichtbarkeit der Buttons wenn keine Connection möglich ist
    void setNoConnection(){
        ((ImageButton) findViewById(R.id.buttonStartMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonPauseMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonStopMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonGoHome)).setAlpha(0.3f);
    }
    // setzt die Sichtbarkeit der Buttons wenn Connection möglich ist
    void setConnection(){
        ((ImageButton) findViewById(R.id.buttonStartMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonPauseMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonStopMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonGoHome)).setAlpha(1.0F);
    }

    // Sende Funktion
        public void sendMessage(final AppControlsProtos.AppControls.Command proto_buff){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(message_BufferOut != null){
                    message_BufferOut.println(proto_buff);
                    message_BufferOut.flush();
                    try {
                        message_BufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                    } catch (IOException e) {
                        System.out.println("_________________________________");
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        }


}