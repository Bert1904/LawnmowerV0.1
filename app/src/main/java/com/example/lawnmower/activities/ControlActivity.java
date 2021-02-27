package com.example.lawnmower.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lawnmower.AppControlsProtos;
import com.example.lawnmower.viewhandler.BatteryStatusHandler;
import com.example.lawnmower.gstreamer.GStreamerSurfaceView;
import com.example.lawnmower.JoystickMessageGenerator;
import com.example.lawnmower.data.LSDListenerManager;
import com.example.lawnmower.data.LawnmowerStatusData;
import com.example.lawnmower.data.LawnmowerStatusDataChangedListener;
import com.example.lawnmower.R;
import com.example.lawnmower.data.SocketService;

import org.freedesktop.gstreamer.GStreamer;

import io.github.controlwear.virtual.joystick.android.JoystickView;

//import org.freedesktop.gstreamer.GStreamer;
//import org.w3c.dom.Text;

import java.io.IOException;

public class ControlActivity extends AppCompatActivity implements SurfaceHolder.Callback, LawnmowerStatusDataChangedListener {

    private JoystickView mJoystick;
    private JoystickMessageGenerator mJoystickMessageGenerator;
    private final double DEADZONE = 0.0;
    private ToggleButton tracking;
    private Button setHome;

    private native void nativeSurfaceInit(Object surface);
    //private native void nativeSetHostAndPort(String Host, int port);
    private native void nativeSurfaceFinalize();
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    //useless method cuz robot image is always playing
    //private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private long native_custom_data;      // Native code will use this to keep private data

    //private boolean is_playing_desired;   // Whether the user asked to go to PLAYING
    private String ip = SocketService.getInstance().getIp();
    private int port = 6755;
    private double xJoystick = 0.0;
    private double yJoystick = 0.0;
    private double lastSentX = 0.0;
    private double lastSentY = 0.0;
    private final double MINDIF = 0.00;
    //Refreshrate of the Joystick
    private final int REFRESHRATE = 20;
    private ImageView batteryStatusIcon;
    private TextView batteryStatus;
    private BatteryStatusHandler bshandler;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize GStreamer and warn if it fails

        //socket = SocketService.getSocket();

        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_control);


        SurfaceView sv = this.findViewById(R.id.gStreamer);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        //Check connection status before calling nativeInit.
        //if(socket.isConnected()) {
        nativeInit();
        init();
    }

    private void init() {
        mJoystick = findViewById(R.id.JoystickView);
        mJoystickMessageGenerator = new JoystickMessageGenerator();
        setHome = findViewById(R.id.setHome);
        tracking = findViewById(R.id.tracking);
        batteryStatusIcon = findViewById(R.id.batteryStatusIconCntrl);
        batteryStatus = findViewById(R.id.batteryStatusCntrl);
        bshandler = new BatteryStatusHandler(batteryStatusIcon);
        setHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("homebutton","home button pressed");
                try {
                    SocketService.getInstance().send(
                            AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.SET_HOME).build().toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        tracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    Log.i("trackingButton", "startTracking");
                    try {
                        SocketService.getInstance().send(
                                AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.BEGIN_TRACKING).build().toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("trackingButton", "stopTracking");
                    try {
                        SocketService.getInstance().send(
                                AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.FINISH_TRACKING).build().toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //publish AppControls messages for the Joystick
        mJoystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                // rearrange the values of x,y to -1 to 1
                xJoystick = -((mJoystick.getNormalizedY()/50.0)-1.0);
                yJoystick = -((mJoystick.getNormalizedX()/50.0)-1.0);

                if(difference(xJoystick, yJoystick, lastSentX, lastSentY) > MINDIF) {
                    lastSentX = xJoystick;
                    lastSentY = yJoystick;
                    Log.i("Steuerung","X: " + xJoystick + " ,Y: " + yJoystick);
                    AppControlsProtos.AppControls msg = mJoystickMessageGenerator.buildMessage(xJoystick, yJoystick);
                    try {
                        SocketService.getInstance().send(msg.toByteArray());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        },REFRESHRATE);

        if(!SocketService.getInstance().isConnected()) {
            batteryStatusIcon.setVisibility(View.INVISIBLE);
            batteryStatus.setVisibility(View.INVISIBLE);
        } else {
            batteryStatusIcon.setVisibility(View.VISIBLE);
            batteryStatus.setVisibility(View.VISIBLE);
        }
    }

    private double difference(double x, double y,double lastX, double lastY) {
        double diffX = x-lastX;
        double diffY = y-lastY;
        double diff = Math.sqrt(diffY * diffY + diffX * diffX);
        return diff;
    }


    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onDestroy() {
        nativeFinalize();
        super.onDestroy();
    }

    private void setMessage(final String message) {
        //final TextView tv = this.findViewById(R.id.status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //tv.setText(message);
            }
        });
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "Gst initialized");
        nativePlay();
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
        //nativeSetHostAndPort(host, port);
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("GStream");
        nativeClassInit();
    }

    private void onMediaSizeChanged (int width, int height) {
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
    }

    @Override
    public void onLSDChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBatteryState(LawnmowerStatusData.getInstance().getLawnmowerStatus().getBatteryState());
            }
        });
    }

    private void setBatteryState(float batteryState) {

        if(batteryState > 90.0f) {
            bshandler.setView(getResources().getIdentifier("@drawable/batteryfull", null, getPackageName()));
        } else if (batteryState > 70.0f) {
            bshandler.setView(getResources().getIdentifier("@drawable/battery80", null, getPackageName()));
        } else if (batteryState > 50.0f) {
            bshandler.setView(getResources().getIdentifier("@drawable/battery60", null, getPackageName()));
        } else if (batteryState > 30.0f) {
            bshandler.setView(getResources().getIdentifier("@drawable/battery40", null, getPackageName()));
        } else if (batteryState > 10.0f){
            bshandler.setView(getResources().getIdentifier("@drawable/battery20", null, getPackageName()));
        } else {
            bshandler.setView(getResources().getIdentifier("@drawable/battery0", null, getPackageName()));
        }
        if(batteryStatus.getVisibility() == View.INVISIBLE) {
            batteryStatus.setVisibility(View.VISIBLE);
        }
        batteryStatus.setText("" + (int)batteryState + "%");
    }

    @Override
    public void onPause() {
        super.onPause();
        LSDListenerManager.removeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        LSDListenerManager.addListener(this);
    }
}