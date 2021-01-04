
package com.example.lawnmower;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import org.freedesktop.gstreamer.GStreamer;

public class Steuerung extends Activity implements SurfaceHolder.Callback {

    private JoystickView mJoystick;
    //private JoystickMessageGenerator mJoystickMessageGenerator;
    private final double DEADZONE = 0.15;
    private static final String NO_CONNECTION = "Verbindung nicht möglich. \nBitte überprüfe deine Einstellung";

    //add udp port
    //private final int UDPPORT = 4567;
    private Socket socket;
    private OutputStream toServer ;
    private Thread UDP = null;

    //GStreamer Variables
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data; //Native code will use this to keep private data

    //private PrintWriter message_BufferOut;

    private void setMessage(String message) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try  {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_steuerung);

        SurfaceView stream = this.findViewById(R.id.gStream);
        SurfaceHolder sh = stream.getHolder();
        sh.addCallback(this);

        init();
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("Lawnmower");
    }

    private void init() {
        //socket = SocketService.getSocket();
        mJoystick = findViewById(R.id.JoystickView);
        ImageButton play = this.findViewById(R.id.play);
        ImageButton pause = this.findViewById(R.id.pause);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativePlay();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativePause();
            }
        });
        play.setEnabled(false);
        pause.setEnabled(false);
        //mJoystickMessageGenerator = new JoystickMessageGenerator();

        //Check connection status before
        /*if(socket.isConnected()) {
            UDP = new Thread(new UDPReciever());
            UDP.start();
        }*/

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

                //AppControlsProtos.AppControls msg = JoystickMessageGenerator.buildMessage(x, y);
                //sendMessage(mJoystickMessageGenerator.buildMessage(x,y));
                //send(msg);
                //mTextView.setText(msg.toString());
            }
        });
        nativeInit();
    }

    @Override
    protected void onDestroy() {
        nativeFinalize();
        super.onDestroy();
    }

    private void onGStreamerInitialized() {
        Log.i("GStreamer" , "Gst initialized.");
        final Activity activity = this;
        nativePause();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.findViewById(R.id.play).setEnabled(true);
                activity.findViewById(R.id.pause).setEnabled(true);
            }
        });
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("GStream");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit(holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize();
    }
}