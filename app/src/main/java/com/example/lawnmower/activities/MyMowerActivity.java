package com.example.lawnmower.activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lawnmower.AppControlsProtos;
import com.example.lawnmower.viewhandler.BatteryStatusHandler;
import com.example.lawnmower.data.LSDListenerManager;
import com.example.lawnmower.data.LawnmowerStatusData;
import com.example.lawnmower.data.LawnmowerStatusDataChangedListener;
import com.example.lawnmower.viewhandler.NotificationHandler;
import com.example.lawnmower.R;
import com.example.lawnmower.data.SocketService;
import com.example.lawnmower.viewhandler.StatusViewHandler;

import java.io.IOException;


public class MyMowerActivity extends BaseAppCompatAcitivty implements LawnmowerStatusDataChangedListener {
    // Output Messages
    private static final String start = "Starte Mähvorgang";
    private static final String pausiere = "Pausiere Mähvorgang";
    private static final String stoppe = "Stoppe Mähvorgang";
    private static final String GoHome = "Fahre zur Ladestadion";
    private static final String NO_CONNECTION = "Verbindung nicht möglich. \nBitte überprüfe deine Einstellung";
    // Status Messages
    private static final String ready = "Lawnmower bereit ";
    private static final String mowing = "Mähvorgang";
    private static final String paused = "Mähvorgang pausiert";
    private static final String manual = "Starte manuelle Bedienung";
    private static final String low_Light = "Geringer Batteriestatus";
    // Boolean values to restore Image Status , set to true if image view was previos visible
    private boolean isStopped = false;
    private boolean isMowing = false;
    private boolean isPaused = false;
    private boolean isGoingHome = false;

    // Status Error Messages
    private static final String UNRECOGNIZED = "Unbekannter Fehler";
    private static final String NO_ERROR = "Kein Fehler";
    private static final String ROBOT_STUCK = " Mäher hängt fest";
    private static final String BLADE_STUCK = "Klinge hängt fest";
    private static final String PICKUP = "Roboter aufnehmen";
    private static final String LOST = "Roboter lost";

    // Display progress bar and textview
    private ProgressBar progressBar;
    private double progressStatus = 0;
    private Handler handler= new Handler();
    private TextView progressTextView ;

    // Values for mower function
    private ImageButton buttonStartMow;
    private ImageButton buttonPauseMow;
    private ImageButton buttonStopMow;
    private ImageButton buttonGoHome;
    private ImageView batteryStatusIcon;
    private TextView batteryStatus;
    private TextView errorStatus;
    private ImageView errorIcon;

    //use this as an error occurance indicator
    private ImageView connectionStatus;

    // Creates notification channel and publish notification
    private NotificationHandler nfhandler;

    // Keep the  ui updated if the Lawnmower status changed
    private StatusViewHandler svhandler;
    private BatteryStatusHandler bshandler;
    // ImageView to display Lawnmower Status
    public ImageView MowingStatusView;

    private boolean isConnected;

    // False if activity is onStop, True if activity is runnung
    private static boolean active = false;


    public SharedPreferences lawnmowerpref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mymower);
        nfhandler = new NotificationHandler(this);
        this.MowingStatusView = (ImageView) findViewById(R.id.MowingStatusView);
        svhandler = new StatusViewHandler((ImageView) findViewById(R.id.MowingStatusView));

        /*
         * looks useless, just add some code to the LawnmowerStatusDataChangedListener
         */
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressTextView = (TextView)findViewById(R.id.progressTextView);
        /*if (SocketService.getInstance().isConnected()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    while(progressStatus<100){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress((int) LawnmowerStatusData.getInstance().getLawnmowerStatus().getMowingProgress());
                            }
                        });
                    }
                }
            });
        } else {*/
            progressBar.setVisibility(View.GONE);
            progressTextView.setVisibility(View.GONE);
        //}

        batteryStatus = findViewById(R.id.batteryStatusMow);
        batteryStatusIcon = findViewById(R.id.batteryStatusIconMow);
        batteryStatus.setVisibility(View.INVISIBLE);
        batteryStatusIcon.setVisibility(View.INVISIBLE);
        connectionStatus = findViewById(R.id.connectionSymbol);
        errorStatus = findViewById(R.id.errorStatusMower);
        errorIcon = findViewById(R.id.errorIconMower);
        bshandler = new BatteryStatusHandler(batteryStatusIcon);

        // Toast start mowing process
        buttonStartMow = (ImageButton) findViewById(R.id.buttonStartMow);
        buttonStartMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //svhandler.setView(getResources().getIdentifier("@drawable/mowing", null, getPackageName()));
                    SocketService.getInstance().send(
                            AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.START).build().toByteArray());
                    Toast.makeText(getApplicationContext(), R.string.Start, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // Toast paused mowing process
        buttonPauseMow = (ImageButton) findViewById(R.id.buttonPauseMow);
        buttonPauseMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    isPaused = true;
                    //svhandler.setView(getResources().getIdentifier("@drawable/mowpause", null, getPackageName()));
                    SocketService.getInstance().send(
                            AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.PAUSE).build().toByteArray());
                    Toast.makeText(getApplicationContext(), R.string.Pause, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // Toast stop mowing process
        buttonStopMow = (ImageButton) findViewById(R.id.buttonStopMow);
        buttonStopMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    isStopped = true;
                    //svhandler.setView(getResources().getIdentifier("@drawable/mowstop", null, getPackageName()));
                    SocketService.getInstance().send(
                            AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.STOP).build().toByteArray());
                    Toast.makeText(getApplicationContext(), R.string.Stop, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // Toast  lawnmower back home
        buttonGoHome = (ImageButton) findViewById(R.id.buttonGoHome);
        buttonGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    isGoingHome = true;
                    //svhandler.setView(getResources().getIdentifier("@drawable/mowback", null, getPackageName()));
                    SocketService.getInstance().send(
                            AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.GO_HOME).build().toByteArray());
                    Toast.makeText(getApplicationContext(), R.string.GoHome, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        errorIcon.setVisibility(View.INVISIBLE);
        errorStatus.setVisibility(View.INVISIBLE);
        //connectionHandler();
        // Restore ui elements when BackButton is clicked
        lawnmowerpref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /*
     *Check if connection to tcp server is possbible, start thread if connected
     *display toast if client is not connected,
     * RunOnUi Thread establish a connection if activity is running
     * If  no  connection is possible thread repeats functions after a period of 10000 ms
     */
    public void connectionHandler() {
        if (!SocketService.getInstance().isConnected()) {
            /*t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!active) {
                                t.cancel();
                            } else {
                                Toast.makeText(getApplicationContext(), NO_CONNECTION, Toast.LENGTH_LONG).show();
                                isConnected = false;
                            }
                        }

                    });
                    setNoConnection();
                    return;
                }

            }, 0, 10000);*/
            setNoConnection();
        } else {
            setConnection();
        }
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
    public void onLSDChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBatteryState(LawnmowerStatusData.getInstance().getLawnmowerStatus().getBatteryState());
                handleStatus(LawnmowerStatusData.getInstance().getLawnmowerStatus().getStatus());
                handleError();
            }
        });
        //handleMowingErrors(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError());
    }

    /*
     Handle status updates coming from Lawnmower
     */
    private void handleStatus(AppControlsProtos.LawnmowerStatus.Status status) {

        switch (status.getNumber()) {
            case AppControlsProtos.LawnmowerStatus.Status.READY_VALUE: {
                svhandler.setView(getResources().getIdentifier("@drawable/ready", null, getPackageName()));
                //Toast.makeText(getApplicationContext(), ready, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Status.MOWING_VALUE: {
                isMowing = true;
                svhandler.setView(getResources().getIdentifier("@drawable/mowing", null, getPackageName()));
                //Toast.makeText(getApplicationContext(), mowing, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Status.PAUSED_VALUE: {
                isPaused = true;
                svhandler.setView(getResources().getIdentifier("@drawable/mowpause", null, getPackageName()));
                nfhandler.sendStatusNotification(paused);
                //Toast.makeText(getApplicationContext(), paused, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Status.MANUAL_VALUE: {
                //Toast.makeText(getApplicationContext(), manual, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Status.LOW_LIGHT_VALUE: {
                nfhandler.sendStatusNotification(low_Light);
                //Toast.makeText(getApplicationContext(), low_Light, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    private void handleError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError().getNumber() != AppControlsProtos.LawnmowerStatus.Error.NO_ERROR_VALUE) {
                    if(errorIcon.getVisibility() != View.VISIBLE) {
                        errorIcon.setVisibility(View.VISIBLE);
                    }
                    if(errorStatus.getVisibility() != View.VISIBLE) {
                        errorStatus.setVisibility(View.VISIBLE);
                    }
                    if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() == AppControlsProtos.LawnmowerStatus.Error.ROBOT_STUCK) {
                        //robot stuck
                        errorStatus.setText("Fehler: Roboter steckt fest!");
                    } else if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() == AppControlsProtos.LawnmowerStatus.Error.BLADE_STUCK) {
                        //robotblade stuck
                        errorStatus.setText("Fehler: Klinge steckt fest!");
                    } else if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() == AppControlsProtos.LawnmowerStatus.Error.PICKUP) {
                        errorStatus.setText("Roboter wird angehoben");
                        //robot pickup
                    } else if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() == AppControlsProtos.LawnmowerStatus.Error.LOST) {
                        errorStatus.setText("Fehler: Orientierung verloren!");
                        //robot lost
                    } else {
                        errorStatus.setText("Ein unerwarteter Fehler ist aufgetreten!");
                        //unrecognized error
                    }
                } else {
                    if(errorIcon.getVisibility() != View.INVISIBLE) {
                        errorIcon.setVisibility(View.INVISIBLE);
                    }
                    if(errorStatus.getVisibility() != View.INVISIBLE) {
                        errorStatus.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    /*
     Handle mowing-errors coming from Lawnmower
     */
    private void handleMowingErrors(AppControlsProtos.LawnmowerStatus.Error error) {
        switch (error.getNumber()) {
            case AppControlsProtos.LawnmowerStatus.Error.NO_ERROR_VALUE: {
                //Toast.makeText(getApplicationContext(), NO_ERROR, Toast.LENGTH_LONG).show();
                return;
            }
            case AppControlsProtos.LawnmowerStatus.Error.ROBOT_STUCK_VALUE: {
                nfhandler.sendErrorNotification(ROBOT_STUCK);
                //Toast.makeText(getApplicationContext(), ROBOT_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Error.BLADE_STUCK_VALUE: {
                nfhandler.sendErrorNotification(BLADE_STUCK);
                //Toast.makeText(getApplicationContext(), BLADE_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Error.PICKUP_VALUE: {
                nfhandler.sendErrorNotification(PICKUP);
                //Toast.makeText(getApplicationContext(), PICKUP, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Error.LOST_VALUE: {
                nfhandler.sendErrorNotification(LOST);
                //Toast.makeText(getApplicationContext(), LOST, Toast.LENGTH_LONG).show();
                break;
            }
            case -1: {
                nfhandler.sendErrorNotification(UNRECOGNIZED);
                //Toast.makeText(getApplicationContext(), UNRECOGNIZED, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }



    /* Disable the functionality of the buttons (Start,Pause,Stop,GoHome)
     * Set visibility to grey if there is no connection
     */
    private void setNoConnection() {
        buttonStartMow.setEnabled(false);
        buttonPauseMow.setEnabled(false);
        buttonStopMow.setEnabled(false);
        buttonGoHome.setEnabled(false);
        batteryStatusIcon.setVisibility(View.INVISIBLE);
        batteryStatus.setVisibility(View.INVISIBLE);
        connectionStatus.setVisibility(View.GONE);
        connectionStatus.setImageResource(getResources().getIdentifier("@drawable/notconnected", null, getPackageName()));
        connectionStatus.setVisibility(View.VISIBLE);
        ((ImageButton) findViewById(R.id.buttonStartMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonPauseMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonStopMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonGoHome)).setAlpha(0.3f);
    }

    /* Set visibility to normal value if connection is possible.
     *
     */
    private void setConnection() {
        ((ImageButton) findViewById(R.id.buttonStartMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonPauseMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonStopMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonGoHome)).setAlpha(1.0F);
    }

    /* Method to restore ui status when moving bewteen screen or activites in Lawnmower app
     * Set status true if imagage is visible
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isPaused) {
            lawnmowerpref.edit().putBoolean("PausedTrue", true).apply();
        } else {
            lawnmowerpref.edit().putBoolean("PausedTrue", false).apply();
        }
        if (isMowing) {
            lawnmowerpref.edit().putBoolean("MowingTrue", true).apply();
        } else {
            lawnmowerpref.edit().putBoolean("MowingTrue", false).apply();
        }
        if (isGoingHome) {
            lawnmowerpref.edit().putBoolean("GoHomeTrue", true).apply();
        } else {
            lawnmowerpref.edit().putBoolean("GoHomeTrue", false).apply();
        }
        if (isStopped) {
            lawnmowerpref.edit().putBoolean("StopTrue", true).apply();
        } else {
            lawnmowerpref.edit().putBoolean("StopTrue", false).apply();
        }
        LSDListenerManager.removeListener(this);
        Log.i("MyMower","remove Listener");
    }

    @Override
    protected void onDestroy() {
        //backgroundTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void finish() {
        super.finish();
        //backgroundTask.cancel(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    /* Method to restore ui status
     * Set previous image view in activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (lawnmowerpref.getBoolean("PausedTrue", false)) {
            svhandler.setView(getResources().getIdentifier("@drawable/mowpause", null, getPackageName()));
        } else if (lawnmowerpref.getBoolean("MowingTrue", false)) {
            svhandler.setView(getResources().getIdentifier("@drawable/mowing", null, getPackageName()));
        } else if (lawnmowerpref.getBoolean("GoHomeTrue", false)) {
            svhandler.setView(getResources().getIdentifier("@drawable/mowback", null, getPackageName()));
        } else if (lawnmowerpref.getBoolean("StopTrue", false)) {
            svhandler.setView(getResources().getIdentifier("@drawable/mowstop", null, getPackageName()));
        }
        LSDListenerManager.addListener(this);
        Log.i("MyMower","add Listener");
    }
}