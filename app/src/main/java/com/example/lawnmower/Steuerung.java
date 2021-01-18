
package com.example.lawnmower;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import org.freedesktop.gstreamer.GStreamer;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.Socket;

public class Steuerung extends AppCompatActivity implements SurfaceHolder.Callback {

    private JoystickView mJoystick;
    private JoystickMessageGenerator mJoystickMessageGenerator;
    private final double DEADZONE = 0.15;

    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    //useless method cuz robot image is always playing
    //private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private long native_custom_data;      // Native code will use this to keep private data

    //private boolean is_playing_desired;   // Whether the user asked to go to PLAYING
    private Socket socket;
    private String host = "192.168.0.8";
    private int port = 6750;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        socket = SocketService.getSocket();

        setContentView(R.layout.activity_steuerung);

        SurfaceView sv = this.findViewById(R.id.gStreamer);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        //Check connection status before calling nativeInit.
        //if(socket.isConnected()) {
            nativeInit();
        //}

        init();
    }

    private void init() {
        mJoystick = findViewById(R.id.JoystickView);
        mJoystickMessageGenerator = new JoystickMessageGenerator();

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

                AppControlsProtos.AppControls msg = mJoystickMessageGenerator.buildMessage(x, y);
                //sendMessage(mJoystickMessageGenerator.buildMessage(x,y));
            }
        });
    }

    private void serialize(final byte[] message) throws IOException {
        Log.i("serialize","SendDataToNetwork: opened method serialze");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.getOutputStream().write(message);
                    socket.getOutputStream().flush();
                    Log.i("serialize","SendDataToNetwork: Success");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onDestroy() {
        nativeFinalize();
        super.onDestroy();
    }

    private void setMessage(final String message) {
        final TextView tv = this.findViewById(R.id.status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(message);
            }
        });
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "Gst initialized. Restoring state");
        nativePlay();
        final Activity activity = this;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit (holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize ();
    }

    private void setHostAndPort() {
        //nativeSetHostAndPort();
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("GStream");
        nativeClassInit();
    }

    /*private void onMediaSizeChanged (int width, int height) {
        Log.i ("GStreamer", "Media size changed to " + width + "x" + height);
        final GStreamerSurfaceView gsv = this.findViewById(R.id.gStreamer);
        gsv.media_width = width;
        gsv.media_height = height;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gsv.requestLayout();
            }
        });
    }*/
}