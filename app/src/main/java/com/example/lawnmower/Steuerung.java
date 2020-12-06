package com.example.lawnmower;

import android.os.Bundle;
import android.view.TextureView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.IOException;
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

    //add udp port
    //private final int UDPPORT = 4567;
    private Socket socket;
    private ImageAdapter imgAdapter;
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
                mTextView.setText(msg.toString());
            }
        });
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