
package com.example.lawnmower;

import android.os.Bundle;
import android.view.Gravity;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class Steuerung extends AppCompatActivity {

    private TextureView robotVideo;
    private JoystickView mJoystick;
    private TextView mTextView;
    //private JoystickMessageGenerator mJoystickMessageGenerator;
    private final double DEADZONE = 0.15;
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    private static final String NO_CONNECTION = "Verbindung nicht möglich. \nBitte überprüfe deine Einstellung";

    //add udp port
    //private final int UDPPORT = 4567;
    private Socket socket;
    private ImageAdapter imgAdapter;
    private OutputStream toServer ;
    private Thread UDP;

    //private PrintWriter message_BufferOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steuerung);
        init();
    }

    private void init() {
        socket = SocketService.getSocket();
        robotVideo = findViewById(R.id.robotVideo);
        mJoystick = findViewById(R.id.JoystickView);
        //mJoystickMessageGenerator = new JoystickMessageGenerator();
        imgAdapter = new ImageAdapter(WIDTH, HEIGHT);
        mTextView = findViewById(R.id.textView);

        //Check connection status before
        if(socket.isConnected()) {
            UDP = new Thread(new UDPReciever());
            UDP.start();
        }

        //publish AppControls messages for the Joystick
        mJoystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                // rearrange the values of x,y to -1 to 1 with -1,-1 being the top left corner
                double x = (mJoystick.getNormalizedX()/50.0)-1.0;
                if(Math.abs(x) < DEADZONE) {
                    x = 0.0;
                }

                double y = (mJoystick.getNormalizedY()/50.0)-1.0;
                if(Math.abs(y) < DEADZONE) {
                    y = 0.0;
                }

                AppControlsProtos.AppControls msg = JoystickMessageGenerator.buildMessage(x, y);
                //sendMessage(mJoystickMessageGenerator.buildMessage(x,y));
                send(msg);
                //mTextView.setText(msg.toString());
            }
        });
    }

    public void send(AppControlsProtos.AppControls msg) {
        //  1. Möglichkeit

        //byteArrayOutStr = new ByteArrayOutputStream();
        // outputStr = new ByteArrayOutputStream();
        // byteArrayOutStr.write(message);
        // byteArrayOutStr.writeTo(outputStr);

        //  2. Möglichkeit
        /*try{
            toServer= new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            toServer.write(message);
            toServer.flush();

            // liest Nachricht vom Server
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(fromServer +" from server ");
        }
        catch (java.net.SocketException e){
            Toast toast = Toast.makeText(Steuerung.this,NO_CONNECTION,Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,50);
            toast.show();
            e.printStackTrace();
        }*/

        //  3. Möglichkeit
//        try{
//            toServer = socket.getOutputStream();
//            toServer.write(message);
//            toServer.flush();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        //  4. Möglichkeit
        try {
            msg.writeTo(socket.getOutputStream());
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public void sendMessage(final AppControlsProtos.AppControls proto_buff){
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
    }*/
}