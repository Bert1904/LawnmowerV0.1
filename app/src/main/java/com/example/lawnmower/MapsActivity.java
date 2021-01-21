package com.example.lawnmower;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private DataInputStream data_Server;
    private GoogleMap mMap;
    private Socket socket;
    private boolean isConnected= false;
    private double latitute = 51.574534546129726d;
    private double longitude = 7.026503346726579d;
    private NotificationHandler nfhandler ;


    private static final String NO_CONNECTION = "Verbindung nicht möglich. \nBitte überprüfe deine Einstellung";
    // Status Error Messages
    private static final String UNRECOGNIZED = "Unbekannter Fehler";
    private static final String NO_ERROR = "Kein Fehler";
    private static final String ROBOT_STUCK = " Mäher hängt fest";
    private static final String BLADE_STUCK = "Klinge hängt fest";
    private static final String PICKUP = "Roboter aufnehmen";
    private static final String LOST = "Roboter lost";


    @Override
    protected void onPause() {
        super.onPause();
        LawnmowerApp.onPauseActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LawnmowerApp.onResumeActivity();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        socket = SocketService.getSocket();
        nfhandler =  new NotificationHandler(this);
        createErrorNotificationChannel();
        connectionHandler();
    }
    public  void connectionHandler(){

        if (!socket.isConnected()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), NO_CONNECTION, Toast.LENGTH_LONG).show();
                    isConnected=false;
                }
            });
            return;
        }else{
            new ListenerThread().execute();
            isConnected=true;
        }
    }

    /*
    ListenerThread to read incoming messages from tcp server
    */
    class ListenerThread extends AsyncTask<String, Void, Boolean> {

        Activity activity;
        IOException ioException;

        @Override
        protected Boolean doInBackground(String... Boolean) {
            try {
                Log.i("Do Background", "Background task started");
                data_Server = new DataInputStream(socket.getInputStream());
                while (socket.isConnected()) {
                    int length = data_Server.readChar();
                    byte[] data = new byte[length];
                    data_Server.readFully(data);
                    healthCheck(data);
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
            try {
                data_Server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return isConnected;
        }

        protected void onPostExecute() {

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
     Deals with LawnmowerStatus
     */
    protected void healthCheck(byte[] data) {

        try {
            AppControlsProtos.LawnmowerStatus lawnmowerStatus = AppControlsProtos.LawnmowerStatus.parseFrom(data);
               handleMowingErrors(lawnmowerStatus.getError());
               latitute = lawnmowerStatus.getLatitude();
               longitude = lawnmowerStatus.getLongitude();

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /*
    Handle mowing-errors coming from Lawnmower
    */
    private void handleMowingErrors(AppControlsProtos.LawnmowerStatus.Error error) {


        switch (error.getNumber()){
            case 0:{
                Toast.makeText(getApplicationContext(),NO_ERROR, Toast.LENGTH_LONG).show();
                return;
            }
            case 1:{
                sendErrorNotification(ROBOT_STUCK);
                Toast.makeText(getApplicationContext(),ROBOT_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case 2:{
                sendErrorNotification(BLADE_STUCK);
                Toast.makeText(getApplicationContext(),BLADE_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case 3:{
                sendErrorNotification(PICKUP);
                Toast.makeText(getApplicationContext(),PICKUP, Toast.LENGTH_LONG).show();
                break;
            }
            case 4:{
                sendErrorNotification(LOST);
                Toast.makeText(getApplicationContext(),LOST, Toast.LENGTH_LONG).show();
                break;
            }
            case -1:{
                sendErrorNotification(UNRECOGNIZED);
                Toast.makeText(getApplicationContext(),UNRECOGNIZED, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }
    /*
    Creates Channel for sending error notifications
    */
    public void createErrorNotificationChannel(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("001","Error", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("This is a error notification channel");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    /*
    Send Error Notifiaction
     */
    public void sendErrorNotification(String message ){
        if(LawnmowerApp.isVisibile()) return;

        NotificationCompat.Builder notficiationBuilder =
                new NotificationCompat.Builder(this,"001")
                        .setSmallIcon(R.drawable.ic_stat_error_outline)
                        .setContentTitle("Lawnmover Error")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                ;
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(001,notficiationBuilder.build());
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If there is no connection possible, default marker is set to WHS Gelsenkirchen
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        connectionHandler();
        mMap = googleMap;
        int height = 200;
        int width = 200;


        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.logomark);
        Bitmap b = bitmapdraw.getBitmap();

        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(b);

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Add a marker to current lawnmower position and move the camera
        LatLng lawnmower_gps = new LatLng(latitute, longitude);
        //mMap.addGroundOverlay(new GroundOverlayOptions().image(bitmapDescriptor).position(lawnmower_gps,100));
        System.out.println("*******************"+"" +lawnmower_gps);
        // Create marker Options
        MarkerOptions options = new MarkerOptions().position(lawnmower_gps).title("Lawnmower").icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        mMap.addMarker(options.position(lawnmower_gps).title("Lawnmower Position"));

       googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lawnmower_gps,16));

        googleMap.addMarker(options);
    }
}