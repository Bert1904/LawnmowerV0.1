package com.example.lawnmower;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private final int START = 1;
    private final int STOP = 2;
    private final int PAUSE = 3;
    private final int HOME = 4;

    private ButtonMessageGenerator btnMessageGenerator = new ButtonMessageGenerator();
    //private ErrorMessageGenerator errorMessageGenerator = new ErrorMessageGenerator();

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
                sendMessage(ButtonMessageGenerator.buildMessage(START));
                byte[] msg = btnMessageGenerator.buildMessage(4).toByteArray();

                Toast.makeText(getApplicationContext(), start, Toast.LENGTH_LONG).show();
            }
        });
        // Toast pausiere Mähvorgang
        buttonPauseMow = (ImageButton) findViewById(R.id.buttonPauseMow);
        buttonPauseMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(ButtonMessageGenerator.buildMessage(PAUSE));
                Toast.makeText(getApplicationContext(), pausiere, Toast.LENGTH_LONG).show();
                //commands.AppControls.Command.PAUSE;
            }
        });

        // Toast stoppe Mähvorgang
        buttonStopMow = (ImageButton) findViewById(R.id.buttonStopMow);
        buttonStopMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(ButtonMessageGenerator.buildMessage(STOP));
                Toast.makeText(getApplicationContext(), stoppe, Toast.LENGTH_LONG).show();
            }
        });
        // Toast  Mäher kehrt zurück
        buttonGoHome = (ImageButton) findViewById(R.id.buttonGoHome);
        buttonGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(ButtonMessageGenerator.buildMessage(HOME));
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

    /* Schaltet die Funktionsweise der Buttons (Start,Pause,Stop, GoHome)aus indem
       die  Buttons grau dargestellt werden und oben genannte Buttons haben keine Funkionsweise mehr,
       wenn keine Connection aufgebaut werden kann;
    */
    void setNoConnection(){
        buttonStartMow.setEnabled(false);
        buttonPauseMow.setEnabled(false);
        buttonStopMow.setEnabled(false);
        buttonGoHome.setEnabled(false);
        ((ImageButton) findViewById(R.id.buttonStartMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonPauseMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonStopMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonGoHome)).setAlpha(0.3f);
    }
    // Setzt die Sichtbarkeit auf den normalen Wert wenn eine Connection aufgebaut werden kann.
    void setConnection(){
        ((ImageButton) findViewById(R.id.buttonStartMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonPauseMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonStopMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonGoHome)).setAlpha(1.0F);
    }

    // Sende Funktion
        public void sendMessage(final AppControlsProtos.AppControls proto_buff){
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