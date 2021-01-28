package com.example.lawnmower;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;


public class MeinMaeher extends BaseAppCompatAcitivty implements View.OnClickListener {
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

    //Values Lawnmower
    private final int START = 1;
    private final int STOP = 2;
    private final int PAUSE = 3;
    private final int HOME = 4;

    //ButtonMessageGenerator for protobuf file
    private ButtonMessageGenerator btnMessageGenerator = new ButtonMessageGenerator();

    // Variablen für Mäher Funktionen
    private ImageButton buttonStartMow;
    private ImageButton buttonPauseMow;
    private ImageButton buttonStopMow;
    private ImageButton buttonGoHome;
    private Socket socket;
    private AsyncTask<Void, AppControlsProtos.LawnmowerStatus, Void> backgroundTask;

    // Creates notification channel and publish notification
    private NotificationHandler nfhandler;

    // Keep the  ui updated if the Lawnmower status changed
    private StatusViewHandler svhandler;  //

    // OutputStream  to send stream to tcp server
    private OutputStream toServer;
    private InputStream fromServer;
    // DataInputStream to   receive incoming messages from  tcp server
    private DataInputStream data_Server;

    // False if activity is onStop, True if activity is runnung
    private static boolean active = false;

    //Value if TCP Server is connected
    private boolean isConnected = false;
    // ImageView to display Lawnmower Status
    public ImageView MowingStatusView;
    // Timer to set up for connection handler to repeat tcp connection attempt
    private Timer t = new Timer();

    public SharedPreferences lawnmowerpref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meinmaeher);
        nfhandler = new NotificationHandler(this);
        this.MowingStatusView = (ImageView) findViewById(R.id.MowingStatusView);
        svhandler = new StatusViewHandler((ImageView) findViewById(R.id.MowingStatusView));
        socket = SocketService.getSocket();

        // Toast start mowing process
        buttonStartMow = (ImageButton) findViewById(R.id.buttonStartMow);
        buttonStartMow.setOnClickListener(this);
        // Toast paused mowing process
        buttonPauseMow = (ImageButton) findViewById(R.id.buttonPauseMow);
        buttonPauseMow.setOnClickListener(this);
        // Toast stop mowing process
        buttonStopMow = (ImageButton) findViewById(R.id.buttonStopMow);
        buttonStopMow.setOnClickListener(this);
        // Toast  lawnmower back home
        buttonGoHome = (ImageButton) findViewById(R.id.buttonGoHome);
        buttonGoHome.setOnClickListener(this);
        connectionHandler();

        // Restore ui elements when BackButton is clicked
        lawnmowerpref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /*
     *Check if connection to tcp server is possbible, start thread if connected
     *display toast if client is not connected,
     * RunOnUi Thread establish a connection if activity is running but no connection
     * is possible thread repeats functions after a period of 10000 ms
     */
    public void connectionHandler() {
        if (!socket.isConnected()) {
            t.scheduleAtFixedRate(new TimerTask() {
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

            }, 0, 10000);
        } else {
            setConnection();
            backgroundTask = new ListenerThread().execute();
        }
    }

    /*
     * ListenerThread to read incoming messages from tcp server
     */
    class ListenerThread extends AsyncTask<Void, AppControlsProtos.LawnmowerStatus, Void> {

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
            byte[]msg;
            AppControlsProtos.LawnmowerStatus lawnmowerStatus = null;
            while (true) {
                try {
                    // Methode 1
                    length = new byte[4];
                    socket.getInputStream().read(length);
                    msgLength = length[0];
                    msg = new byte[msgLength];
                    readExact(socket.getInputStream(),msg,0,msgLength);
                    Log.i("bytes read", "successfully read bytes");
                    try {
                        lawnmowerStatus = AppControlsProtos.LawnmowerStatus.parseFrom(msg);
                        Log.i("msg", "" + lawnmowerStatus.toString());
                    } catch(InvalidProtocolBufferException e) {
                        Log.i("Exception", "msg: " + e.getMessage());
                    }

                    Log.i("Message","revieved Message");
                    //handleStatus(lawnmowerStatus.getStatus());
                    //handleMowingErrors(lawnmowerStatus.getError());
                    //Thread.sleep(10);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch ( Exception e) {
                    e.printStackTrace();
                }
            }
            //return null;
        }

        private void readExact(InputStream stream, byte[] buffer, int offset, int count) throws Exception{
            int bytesRead;
            if(count < 0) {
                throw new IllegalArgumentException();
            }
            while(count != 0 &&
                    (bytesRead = stream.read(buffer, offset, count)) > 0) {
                offset += bytesRead;
                count -= bytesRead;
            }
            if(count != 0) throw new Exception("End of stream was reached.");
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
    }



    /*
     *Deals with LawnmowerStatus
     */

    protected void healthCheck(AppControlsProtos.LawnmowerStatus lawnmowerStatus) {
        Log.i(" healthCheck", "healthCheck started");
        handleStatus(lawnmowerStatus.getStatus());
        handleMowingErrors(lawnmowerStatus.getError());
    }


    /*
     Handle status updates coming from Lawnmower
     */
    private void handleStatus(AppControlsProtos.LawnmowerStatus.Status status) {

        switch (status.getNumber()) {
            case 0: {
                Toast.makeText(getApplicationContext(), ready, Toast.LENGTH_LONG).show();
                break;
            }
            case 1: {
                isMowing = true;
                svhandler.setView(getResources().getIdentifier("@drawable/mahvorgang", null, getPackageName()));
                Toast.makeText(getApplicationContext(), mowing, Toast.LENGTH_LONG).show();
                break;
            }
            case 2: {
                isPaused = true;
                svhandler.setView(getResources().getIdentifier("@drawable/mahvorgangpausiert", null, getPackageName()));
                nfhandler.sendStatusNotification(paused);
                Toast.makeText(getApplicationContext(), paused, Toast.LENGTH_LONG).show();
                break;
            }
            case 3: {
                Toast.makeText(getApplicationContext(), manual, Toast.LENGTH_LONG).show();
                break;
            }
            case 4: {
                nfhandler.sendStatusNotification(low_Light);
                Toast.makeText(getApplicationContext(), low_Light, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    /*
     Handle mowing-errors coming from Lawnmower
     */
    private void handleMowingErrors(AppControlsProtos.LawnmowerStatus.Error error) {


        switch (error.getNumber()) {
            case 0: {
                Toast.makeText(getApplicationContext(), NO_ERROR, Toast.LENGTH_LONG).show();
                return;
            }
            case 1: {
                nfhandler.sendErrorNotification(ROBOT_STUCK);
                Toast.makeText(getApplicationContext(), ROBOT_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case 2: {
                nfhandler.sendErrorNotification(BLADE_STUCK);
                Toast.makeText(getApplicationContext(), BLADE_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case 3: {
                nfhandler.sendErrorNotification(PICKUP);
                Toast.makeText(getApplicationContext(), PICKUP, Toast.LENGTH_LONG).show();
                break;
            }
            case 4: {
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

    /*
     * Send Messages to TCP Server
     * Call svhandler to update the ui
     * Publish toast if a button is pressed
     */
    @Override
    public void onClick(final View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    private ImageView MowingStatusView = (ImageView) findViewById(R.id.MowingStatusView);

                    @Override
                    public void run() {
                        switch (v.getId()) {
                            case R.id.buttonStartMow: {
                                byte[] msg = btnMessageGenerator.buildMessage(START).toByteArray();
                                try {
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
                                    svhandler.setView(getResources().getIdentifier("@drawable/mahvorgangpausiert", null, getPackageName()));
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
                                    svhandler.setView(getResources().getIdentifier("@drawable/mahvorgangstop", null, getPackageName()));
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
                                    svhandler.setView(getResources().getIdentifier("@drawable/backhome", null, getPackageName()));
                                    serialize(msg);
                                    Toast.makeText(getApplicationContext(), GoHome, Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    break;
                                }
                                break;
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /* Disable the functionality of the buttons (Start,Pause,Stop,GoHome)
     * Set visibility to grey if there is no connection
     */
    private void setNoConnection() {
        buttonStartMow.setEnabled(false);
        buttonPauseMow.setEnabled(false);
        buttonStopMow.setEnabled(false);
        buttonGoHome.setEnabled(false);
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
    public void serialize(final byte[] message) throws IOException {
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
    }

    @Override
    protected void onDestroy() {
        backgroundTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
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
    }
}