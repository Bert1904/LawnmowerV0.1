package com.example.lawnmower;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private DataInputStream data_Server;
    private GoogleMap mMap;
    private Socket socket;
    private boolean isConnected= false;



    private static final String NO_CONNECTION = "Verbindung nicht möglich. \nBitte überprüfe deine Einstellung";
    // Status Error Messages
    private static final String UNRECOGNIZED = "Unbekannter Fehler";
    private static final String NO_ERROR = "Kein Fehler";
    private static final String ROBOT_STUCK = " Mäher hängt fest";
    private static final String BLADE_STUCK = "Klinge hängt fest";
    private static final String PICKUP = "Roboter aufnehmen";
    private static final String LOST = "Roboter lost";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        socket = SocketService.getSocket();
        connectionHandler();
        createErrorNotificationChannel();
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
            Thread listenFromServerThread = new Thread(new ListenerThread());
            listenFromServerThread.start();
            isConnected=true;
        }
    }

    /*
    ListenerThread to read incoming messages from tcp server
    */
    class ListenerThread implements  Runnable{

        @Override
        public void run() {
            while(isConnected){
                try{
                    data_Server = new DataInputStream(socket.getInputStream());
                    int length = data_Server.readChar();
                    byte[]data = new byte[length];
                    data_Server.readFully(data);
                    healthCheck(data);

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
    /*
     Deals with LawnmowerStatus
     */
    protected void healthCheck(byte[] data) {
        AppControlsProtos.LawnmowerStatus status = null;
        try {
            AppControlsProtos.LawnmowerStatus lawnmowerStatus = AppControlsProtos.LawnmowerStatus.parseFrom(data);
               handleMowingErrors(lawnmowerStatus.getError());
               handleGpsLatitude();
               handleGpslongitude();

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
   private double handleGpsLatitude(){
        double latitude= AppControlsProtos.LawnmowerStatus.LATITUDE_FIELD_NUMBER;
        return latitude;
    }
    private double handleGpslongitude(){
       double longitude=AppControlsProtos.LawnmowerStatus.LONGITUDE_FIELD_NUMBER;
        
        return longitude;
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
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        // Add a marker to current lawnmower position and move the camera
        LatLng lawnmower_gps = new LatLng(handleGpsLatitude(), handleGpslongitude());

        System.out.println("*******************"+"" +lawnmower_gps);
        // Create marker Options
        MarkerOptions options = new MarkerOptions().position(lawnmower_gps).title("Lawnmower");
        mMap.addMarker(new MarkerOptions().position(lawnmower_gps).title("Lawnmower Position"));
       // googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lawnmower_gps,100));

        googleMap.addMarker(options);
    }

  


}