package com.example.lawnmower;


import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;


public class MeinMaeher extends BaseAppCompatAcitivty implements LawnmowerStatusDataChangedListener{
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

    //ButtonMessageGenerator for protobuf file
    private ButtonMessageGenerator btnMessageGenerator = new ButtonMessageGenerator();

    // Values for mower function
    private ImageButton buttonStartMow;
    private ImageButton buttonPauseMow;
    private ImageButton buttonStopMow;
    private ImageButton buttonGoHome;
    private ImageView batteryStatusIcon;
    private TextView batteryStatus;

    //use this as an error occurance indicator
    private ImageView connectionStatus;

    private AsyncTask<Void, AppControlsProtos.LawnmowerStatus, Void> backgroundTask;

    // Creates notification channel and publish notification
    private NotificationHandler nfhandler;

    // Keep the  ui updated if the Lawnmower status changed
    private StatusViewHandler svhandler;
    private BatteryStatusHandler bshandler;
    // ImageView to display Lawnmower Status
    public ImageView MowingStatusView;

    private boolean isConnected;

    // OutputStream  to send stream to tcp server
    private OutputStream toServer;
    // DataInputStream to   receive incoming messages from  tcp server
    private DataInputStream data_Server;

    // False if activity is onStop, True if activity is runnung
    private static boolean active = false;

    // Timer to set up for connection handler to repeat tcp connection attempt
    private Timer t = new Timer();

    public SharedPreferences lawnmowerpref;
    public double latitude;
    public double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meinmaeher);
        nfhandler = new NotificationHandler(this);
        this.MowingStatusView = (ImageView) findViewById(R.id.MowingStatusView);
        svhandler = new StatusViewHandler((ImageView) findViewById(R.id.MowingStatusView));

        /*
         * looks useless, just add some code to the LawnmowerStatusDataChangedListener
         */
        progressBar= (ProgressBar)findViewById(R.id.progressBar);
        progressTextView = (TextView)findViewById(R.id.progressTextView);
        if (SocketService.getInstance().isConnected()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    while(progressStatus<100){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress((int)LawnmowerStatusData.getInstance().getLawnmowerStatus().getMowingProgress());
                            }
                        });
                    }
                }
            });
        }else{
            progressBar.setVisibility(View.GONE);
            progressTextView.setVisibility(View.GONE);
        }

        batteryStatus = findViewById(R.id.batteryStatusMow);
        batteryStatusIcon = findViewById(R.id.batteryStatusIconMow);
        batteryStatus.setVisibility(View.INVISIBLE);
        batteryStatusIcon.setVisibility(View.INVISIBLE);
        connectionStatus = findViewById(R.id.connectionSymbol);
        bshandler = new BatteryStatusHandler(batteryStatusIcon);

        // Toast start mowing process
        buttonStartMow = (ImageButton) findViewById(R.id.buttonStartMow);
        buttonStartMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] msg = btnMessageGenerator.buildMessage(AppControlsProtos.AppControls.Command.START_VALUE).toByteArray();
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
        connectionHandler();
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


    /*
     * ListenerThread to read incoming messages from tcp server
     */
    /*class ListenerThread extends AsyncTask<Void, AppControlsProtos.LawnmowerStatus, Void> {
        Activity activity;
        IOException ioException;
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... params) {
            Log.i("Do Background", "Background task started");
            byte[] length;
            int msgLength;
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            byte[] msg;
            AppControlsProtos.LawnmowerStatus lawnmowerStatus = null;
            while (true) {
                try {
                    // Methode 1
                    length = new byte[4];
                    socket.getInputStream().read(length);
                    msgLength = convertByteArrayToInt(length);
                    msg = new byte[msgLength];
                    readExact(socket.getInputStream(), msg, 0, msgLength);
                    Log.i("bytes read", "successfully read bytes");
                    try {
                        lawnmowerStatus = AppControlsProtos.LawnmowerStatus.parseFrom(msg);
                        Log.i("msg", "" + lawnmowerStatus.toString());
                    } catch (InvalidProtocolBufferException e) {
                        Log.i("Exception", "msg: " + e.getMessage());
                    }
                    Log.i("Message", "reveived Message");
                    handleStatus(lawnmowerStatus.getStatus());
                    setBatteryState(lawnmowerStatus.getBatteryState());
                    //sets data to the singleton
                    lawnmowerStatusData.setLawnmowerStatus(lawnmowerStatus);
                    /*lawnmowerStatusData.setStatus(lawnmowerStatus.getStatus());
                    lawnmowerStatusData.setError(lawnmowerStatus.getError());
                    lawnmowerStatusData.setBatteryState(lawnmowerStatus.getBatteryState());
                    lawnmowerStatusData.setLatitude(lawnmowerStatus.getLatitude());
                    lawnmowerStatusData.setLongitude(lawnmowerStatus.getLongitude());
                    lawnmowerStatusData.setError_msg(lawnmowerStatus.getErrorMsg());
                    lawnmowerStatusData.setMowing_finished(lawnmowerStatus.getFinishedMowing());*/
    //Thread.sleep(10);
                /*} catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //return null;
        }
        //least significant bit first
        private int convertByteArrayToInt(byte[] data) {
            if (data == null || data.length != 4) return 0x0;
            // ----------
            return
                    ((0xff & data[0]) << 0 |
                            (0xff & data[1]) << 8 |
                            (0xff & data[2]) << 16 |
                            (0xff & data[3]) << 24);
        }
        private void readExact(InputStream stream, byte[] buffer, int offset, int count) throws Exception {
            int bytesRead;
            if (count < 0) {
                throw new IllegalArgumentException();
            }
            while (count != 0 &&
                    (bytesRead = stream.read(buffer, offset, count)) > 0) {
                offset += bytesRead;
                count -= bytesRead;
            }
            if (count != 0) throw new Exception("End of stream was reached.");
        }
        @Override
        protected void onProgressUpdate(AppControlsProtos.LawnmowerStatus... values) {
            super.onProgressUpdate(values);
        }
        protected void onPostExecute() {
            Log.i(" Background", "Background task ended");
            if (this.ioException != null) {
                new AlertDialog.Builder(this.activity)
                        .setTitle("Ein Fehler ist aufgetreten")
                        .setMessage(this.ioException.toString())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }*/

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
            }
        });
        //handleMowingErrors(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError());
    }

    /*private void handleGeoCoordinatesLatitude(double latitude) {
        this.latitude = latitude;
        System.out.println(latitude);
        geoServiceHandler.setLatitude(latitude);
    }
    private void handleGeoCoordinatesLongitude(double longitude) {
        this.longitude = longitude;
        System.out.println(longitude);
        geoServiceHandler.setLongitude(longitude);
    }*/

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
                svhandler.setView(getResources().getIdentifier("@drawable/mahvorgang", null, getPackageName()));
                //Toast.makeText(getApplicationContext(), mowing, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Status.PAUSED_VALUE: {
                isPaused = true;
                svhandler.setView(getResources().getIdentifier("@drawable/mahvorgangpausiert", null, getPackageName()));
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

    /*
     Handle mowing-errors coming from Lawnmower
     */
    private void handleMowingErrors(AppControlsProtos.LawnmowerStatus.Error error) {
        switch (error.getNumber()) {
            case AppControlsProtos.LawnmowerStatus.Error.NO_ERROR_VALUE: {
                Toast.makeText(getApplicationContext(), NO_ERROR, Toast.LENGTH_LONG).show();
                return;
            }
            case AppControlsProtos.LawnmowerStatus.Error.ROBOT_STUCK_VALUE: {
                nfhandler.sendErrorNotification(ROBOT_STUCK);
                Toast.makeText(getApplicationContext(), ROBOT_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Error.BLADE_STUCK_VALUE: {
                nfhandler.sendErrorNotification(BLADE_STUCK);
                Toast.makeText(getApplicationContext(), BLADE_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Error.PICKUP_VALUE: {
                nfhandler.sendErrorNotification(PICKUP);
                Toast.makeText(getApplicationContext(), PICKUP, Toast.LENGTH_LONG).show();
                break;
            }
            case AppControlsProtos.LawnmowerStatus.Error.LOST_VALUE: {
                nfhandler.sendErrorNotification(LOST);
                Toast.makeText(getApplicationContext(), LOST, Toast.LENGTH_LONG).show();
                break;
            }
            case -1: {
                nfhandler.sendErrorNotification(UNRECOGNIZED);
                Toast.makeText(getApplicationContext(), UNRECOGNIZED, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    /**
     * Send Messages to TCP Server
     * Call svhandler to update the ui
     * Publish toast if a button is pressed
     */
    /*@Override
    public void onClick(final View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    //private ImageView MowingStatusView = (ImageView) findViewById(R.id.MowingStatusView);
                    @Override
                    public void run() {
                        switch (v.getId()) {
                            case R.id.buttonStartMow: {
                                byte[] msg = btnMessageGenerator.buildMessage(START).toByteArray();
                                try {
                                    svhandler.setView(getResources().getIdentifier("@drawable/mowing", null, getPackageName()));
                                    serialize(msg);
                                    Toast.makeText(getApplicationContext(), start, Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case R.id.buttonPauseMow: {
                                byte[] msg = btnMessageGenerator.buildMessage(PAUSE).toByteArray();
                                try {
                                    isPaused = true;
                                    svhandler.setView(getResources().getIdentifier("@drawable/mowpause", null, getPackageName()));
                                    serialize(msg);
                                    Toast.makeText(getApplicationContext(), pausiere, Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case R.id.buttonStopMow: {
                                byte[] msg = btnMessageGenerator.buildMessage(STOP).toByteArray();
                                try {
                                    isStopped = true;
                                    svhandler.setView(getResources().getIdentifier("@drawable/mowstop", null, getPackageName()));
                                    serialize(msg);
                                    Toast.makeText(getApplicationContext(), stoppe, Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case R.id.buttonGoHome: {
                                byte[] msg = btnMessageGenerator.buildMessage(HOME).toByteArray();
                                try {
                                    isGoingHome = true;
                                    svhandler.setView(getResources().getIdentifier("@drawable/mowback", null, getPackageName()));
                                    serialize(msg);
                                    Toast.makeText(getApplicationContext(), GoHome, Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                });
            }
        }).start();
    }*/

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


    // Serialized and sends message to tcp server
    /*public void serialize(final byte[] message) throws IOException {
        Log.i("serialize", "SendDataToNetwork: opened  method serialize");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    toServer = socket.getOutputStream();
                    toServer.write(message);
                    toServer.flush();
                    Log.i("Success", "SendDataToNetwork:  Send message. ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        ;
    }*/

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
            svhandler.setView(getResources().getIdentifier("@drawable/mahvorgangpausiert", null, getPackageName()));
        } else if (lawnmowerpref.getBoolean("MowingTrue", false)) {
            svhandler.setView(getResources().getIdentifier("@drawable/mahvorgang", null, getPackageName()));
        } else if (lawnmowerpref.getBoolean("GoHomeTrue", false)) {
            svhandler.setView(getResources().getIdentifier("@drawable/backhome", null, getPackageName()));
        } else if (lawnmowerpref.getBoolean("StopTrue", false)) {
            svhandler.setView(getResources().getIdentifier("@drawable/mahvorgangstop", null, getPackageName()));
        }
        LSDListenerManager.addListener(this);
        Log.i("MyMower","add Listener");
    }
}