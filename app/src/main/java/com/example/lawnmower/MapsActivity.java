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
    private double latitute ;
    private double longitude ;
    private NotificationHandler nfhandler ;


    private static final String NO_CONNECTION = "Verbindung nicht möglich. \nBitte überprüfe deine Einstellung";
    // Status Error Messages
    private static final String UNRECOGNIZED = "Unbekannter Fehler";
    private static final String NO_ERROR = "Kein Fehler";
    private static final String ROBOT_STUCK = " Mäher hängt fest";
    private static final String BLADE_STUCK = "Klinge hängt fest";
    private static final String PICKUP = "Roboter aufnehmen";
    private static final String LOST = "Roboter lost";
    private  GeographicCoordinateService geoService;

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
        geoService=GeographicCoordinateService.getGeographicCoordinateService();
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
        mMap = googleMap;
        int height = 200;
        int width = 200;


        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.logomark);
        Bitmap b = bitmapdraw.getBitmap();

        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(b);
        System.out.println(geoService.getlatitude()+"<------------------");
        System.out.println(geoService.getlongitude()+"<------------------");
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Add a marker to current lawnmower position and move the camera
        LatLng lawnmower_gps = new LatLng(geoService.getlatitude(), geoService.getlongitude());
        //mMap.addGroundOverlay(new GroundOverlayOptions().image(bitmapDescriptor).position(lawnmower_gps,100));
        System.out.println("*******************"+"" +lawnmower_gps);
        // Create marker Options
        MarkerOptions options = new MarkerOptions().position(lawnmower_gps).title("Lawnmower").icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        mMap.addMarker(options.position(lawnmower_gps).title("Lawnmower Position"));

       googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lawnmower_gps,16));

        googleMap.addMarker(options);
    }
}