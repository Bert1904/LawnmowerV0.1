
package com.example.lawnmower;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import org.freedesktop.gstreamer.GStreamer;

public class Steuerung extends AppCompatActivity
        //implements SurfaceHolder.Callback
        {

    /*private JoystickView mJoystick;
    private JoystickMessageGenerator mJoystickMessageGenerator;
    private final double DEADZONE = 0.15;*/

    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    //private native void nativePlay();     // Set pipeline to PLAYING
    //private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    //private native void nativeSurfaceInit(Object surface);
    //private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING

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

        setContentView(R.layout.activity_steuerung);

        /*ImageButton play = this.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nativePlay();
            }
        });

        ImageButton pause = this.findViewById(R.id.stop);
        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = false;
                nativePause();
            }
        });

        this.findViewById(R.id.play).setEnabled(false);
        this.findViewById(R.id.stop).setEnabled(false);*/
        nativeInit();

        /*SurfaceView sv = (SurfaceView) this.findViewById(R.id.gStream);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);*/

        /*if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            Log.i ("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
        } else {
            is_playing_desired = false;
            Log.i ("GStreamer", "Activity created. There is no saved state, playing: false");
        }*/

        // Start with disabled buttons, until native code is initialized
        //init();
    }

    /*private void init() {
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
    }*/


    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("GStreamer", "Saving state, playing:" + is_playing_desired);
        outState.putBoolean("playing", is_playing_desired);
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
        Log.i ("GStreamer", "Gst initialized. Restoring state, playing:" + is_playing_desired);
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("GStream");
        nativeClassInit();
    }
}