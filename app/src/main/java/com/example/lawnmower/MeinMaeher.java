package com.example.lawnmower;


import android.app.MediaRouteButton;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;


public class MeinMaeher extends BaseAppCompatAcitivty implements View.OnClickListener{
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




    // Status Error Messages
    private static final String UNRECOGNIZED = "Unbekannter Fehler";
    private static final String NO_ERROR = "Kein Fehler";
    private static final String ROBOT_STUCK = " Mäher hängt fest";
    private static final String BLADE_STUCK = "Klinge hängt fest";
    private static final String PICKUP = "Roboter aufnehmen";
    private static final String LOST = "Roboter lost";

    //
    private final int START = 1;
    private final int STOP = 2;
    private final int PAUSE = 3;
    private final int HOME = 4;

    private ButtonMessageGenerator btnMessageGenerator = new ButtonMessageGenerator();
    //private ErrorMessageGenerator errorMessageGenerator = new ErrorMessageGenerator();
    // Variablen für Mäher Funktionen
    private ImageButton buttonStartMow;
    private ImageButton buttonPauseMow;
    private ImageButton buttonStopMow;
    private ImageButton buttonGoHome;
    private Socket socket;
    private String SERVER_IP = "192.168.0.8";
    private int SERVER_PORT = 6750;
    private NotificationHandler nfhandler;

    // messages to send and receive from tcp server
    private OutputStream toServer ;
    private DataInputStream data_Server;
    private boolean isConnected= false;
    public ImageView MowingStatusView;
    private Thread HealthCheckThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meinmaeher);
        nfhandler =  new NotificationHandler(this);
        this.MowingStatusView= (ImageView)findViewById(R.id.MowingStatusView);

        socket = SocketService.getSocket();



        // Toast starte Mähvorgang
        buttonStartMow = (ImageButton) findViewById(R.id.buttonStartMow);
        buttonStartMow.setOnClickListener(this);
        // Toast pausiere Mähvorgang
        buttonPauseMow = (ImageButton) findViewById(R.id.buttonPauseMow);
        buttonPauseMow.setOnClickListener(this);
        // Toast stoppe Mähvorgang
        buttonStopMow = (ImageButton) findViewById(R.id.buttonStopMow);
        buttonStopMow.setOnClickListener(this);
        // Toast  Mäher kehrt zurück
        buttonGoHome = (ImageButton) findViewById(R.id.buttonGoHome);
        buttonGoHome.setOnClickListener(this);
        //connectionHandler();


    }

    /*
     Check if connection to tcp server is possbible, start thread if connected
     display toast if client is not connected
     */
    public  void connectionHandler(){

    if (!socket.isConnected()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), NO_CONNECTION, Toast.LENGTH_LONG).show();
                      isConnected= false;
                }
            });
            setNoConnection();
            return;
        }
        else {
            setConnection();
            isConnected= true;
            Thread listenFromServerThread = new Thread(new ListenerThread());
             listenFromServerThread.start();

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
            handleStatus(lawnmowerStatus.getStatus());
            handleMowingErrors(lawnmowerStatus.getError());

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
    /*
     Handle status updates coming from Lawnmower
     */
    private void handleStatus(AppControlsProtos.LawnmowerStatus.Status status){

        switch (status.getNumber()){
            case 0:{
                Toast.makeText(getApplicationContext(),ready, Toast.LENGTH_LONG).show();
                break;
            }
            case 1:{

                Toast.makeText(getApplicationContext(),mowing, Toast.LENGTH_LONG).show();
                break;
            }
            case 2:{
                nfhandler.sendStatusNotification(paused);
                Toast.makeText(getApplicationContext(),paused, Toast.LENGTH_LONG).show();
                break;
            }
            case 3:{
                Toast.makeText(getApplicationContext(),manual, Toast.LENGTH_LONG).show();
                break;
            }
            case 4:{
                nfhandler.sendStatusNotification(low_Light);
                Toast.makeText(getApplicationContext(),low_Light, Toast.LENGTH_LONG).show();
                break;
            }
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
                nfhandler.sendErrorNotification( ROBOT_STUCK);
                nfhandler.sendErrorNotification(ROBOT_STUCK);
                Toast.makeText(getApplicationContext(),ROBOT_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case 2:{
                nfhandler.sendErrorNotification( BLADE_STUCK);
                nfhandler.sendErrorNotification(BLADE_STUCK);
                Toast.makeText(getApplicationContext(),BLADE_STUCK, Toast.LENGTH_LONG).show();
                break;
            }
            case 3:{
                nfhandler.sendErrorNotification( PICKUP);
                nfhandler.sendErrorNotification(PICKUP);
                Toast.makeText(getApplicationContext(),PICKUP, Toast.LENGTH_LONG).show();
                break;
            }
            case 4:{
                nfhandler.sendErrorNotification( LOST);
                nfhandler.sendErrorNotification(LOST);
                Toast.makeText(getApplicationContext(),LOST, Toast.LENGTH_LONG).show();
                break;
            }
            case -1:{
                nfhandler.sendErrorNotification( UNRECOGNIZED);
                nfhandler.sendErrorNotification( UNRECOGNIZED );
                Toast.makeText(getApplicationContext(),UNRECOGNIZED, Toast.LENGTH_LONG).show();
                break;
            }
    }
    }

    /*
            Send Messages to TCP Server
             */
    @Override
    public void onClick(final View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    private ImageView MowingStatusView = (ImageView)findViewById(R.id.MowingStatusView);
                    @Override
                    public void run() {
                        switch (v.getId()){
                            case R.id.buttonStartMow:{
                                byte[] msg = btnMessageGenerator.buildMessage(START).toByteArray();
                                try {
                                    this.MowingStatusView.setVisibility(MowingStatusView.GONE);
                                    int imgageResource = getResources().getIdentifier("@drawable/mahvorgang",null,getPackageName());
                                    this.MowingStatusView.setImageResource(imgageResource);
                                    MowingStatusView.setVisibility(MowingStatusView.VISIBLE);
                                    serialize(msg);
                                    Toast.makeText(getApplicationContext(), start, Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case R.id.buttonPauseMow:{
                                byte[] msg = btnMessageGenerator.buildMessage(PAUSE).toByteArray();

                                try {
                                    nfhandler.sendErrorNotification( ROBOT_STUCK);
                                    nfhandler.sendStatusNotification("nice");
                                    this.MowingStatusView.setVisibility(MowingStatusView.GONE);
                                    int imgageResource = getResources().getIdentifier("@drawable/mahvorgangpausiert",null,getPackageName());
                                    this.MowingStatusView.setImageResource(imgageResource);
                                    this.MowingStatusView.setVisibility(MowingStatusView.VISIBLE);
                                    serialize(msg);
                                    Toast.makeText(getApplicationContext(), pausiere, Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                break;
                            }
                            case R.id.buttonStopMow:{
                                try {
                                    Thread.sleep(2000);
                                    nfhandler.sendErrorNotification( "''''''''''''''''''''####");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                byte[] msg = btnMessageGenerator.buildMessage(STOP).toByteArray();

                                try {
                                    this.MowingStatusView.setVisibility(MowingStatusView.GONE);
                                    int imgageResource = getResources().getIdentifier("@drawable/mahvorgangstop",null,getPackageName());
                                    this.MowingStatusView.setImageResource(imgageResource);
                                    this.MowingStatusView.setVisibility(MowingStatusView.VISIBLE);
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

                                    this.MowingStatusView.setVisibility(MowingStatusView.GONE);
                                    int imgageResource = getResources().getIdentifier("@drawable/backhome",null,getPackageName());
                                    this.MowingStatusView.setImageResource(imgageResource);
                                    this.MowingStatusView.setVisibility(MowingStatusView.VISIBLE);


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
    /* Schaltet die Funktionsweise der Buttons (Start,Pause,Stop, GoHome)aus indem
       die  Buttons grau dargestellt werden und oben genannte Buttons haben keine Funkionsweise mehr,
       wenn keine Connection aufgebaut werden kann;
    */
    void setNoConnection(){
        buttonStartMow.setEnabled(false);
        buttonPauseMow.setEnabled(false);
        buttonStopMow.setEnabled(false);
        buttonGoHome.setEnabled(false);
        ((ImageButton) findViewById(R.id.buttonStartMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonPauseMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonStopMow)).setAlpha(0.3f);
        ((ImageButton) findViewById(R.id.buttonGoHome)).setAlpha(0.3f);
    }
    // Setzt die Sichtbarkeit auf den normalen Wert wenn eine Connection aufgebaut werden kann.
    void setConnection(){
        ((ImageButton) findViewById(R.id.buttonStartMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonPauseMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonStopMow)).setAlpha(1.0F);
        ((ImageButton) findViewById(R.id.buttonGoHome)).setAlpha(1.0F);
    }


    // Serialize und versendet die Nachricht
    public void serialize(byte[]message) throws IOException {
         //  1. Möglichkeit
         //byteArrayOutStr = new ByteArrayOutputStream();
         // outputStr = new ByteArrayOutputStream();
         // byteArrayOutStr.write(message);
         // byteArrayOutStr.writeTo(outputStr);

        //  2. Möglichkeit
       try{
           toServer= new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

           toServer.write(message,0,message.length);

           toServer.flush();

           // liest Nachricht vom Server
           //BufffromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

       }
       catch (java.net.SocketException e){
           Toast toast = Toast.makeText(MeinMaeher.this,NO_CONNECTION,Toast.LENGTH_LONG);
          toast.setGravity(Gravity.CENTER,0,50);
          toast.show();
          e.printStackTrace();
          throw e;
       }

        //  3. Möglichkeit
//        try{
//            toServer = socket.getOutputStream();
//            toServer.write(message);
//            toServer.flush();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        ----------------------------
//        try{
//            while(connected){
//                outToServer= socket.getOutputStream();
//                inFromServer = socket.getInputStream();
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }





   }
        /* wahrscheinlich unnötig
        public void sendMessage(final AppControlsProtos.AppControls proto_buff) throws IOException {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress(InetAddress.getByName(socket.getInetAddress().toString()),socket.getPort()));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(message_BufferOut != null){
                    message_BufferOut.println(proto_buff);
                    message_BufferOut.flush();
                    try {
                        message_BufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                    } catch (IOException e) {
                        System.out.println("_________________________________");
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        }
         */
}


//    /*
//     Creates Channel for sending error notifications
//     */
//    public void createErrorNotificationChannel(){
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel("001","Error",NotificationManager.IMPORTANCE_DEFAULT);
//            notificationChannel.setDescription("This is a error notification channel");
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//    }
//    /*
//    Creates Channel for sending status notifications
//     */
//        public void createStatusNotificationChannel(){
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                NotificationChannel notificationChannel = new NotificationChannel("002","Status",NotificationManager.IMPORTANCE_DEFAULT);
//                notificationChannel.setDescription("This is a status notification channel");
//
//                NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                notificationManager.createNotificationChannel(notificationChannel);
//            }
//    }
//
//    /*
//    Send Error Notifiaction
//     */
//    public void sendErrorNotification(String message ){
//
//        NotificationCompat.Builder notficiationBuilder =
//                new NotificationCompat.Builder(this,"001")
//                        .setSmallIcon(R.drawable.ic_stat_error_outline)
//                        .setContentTitle("Lawnmower Error")
//                        .setContentText(message)
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                        .setAutoCancel(true)
//                ;
//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
//        notificationManagerCompat.notify(001,notficiationBuilder.build());
//    }
//    /*
//    Send Status Notifiaction
//     */
//    public void sendStatusNotification(String message ){
//        if (LawnmowerApp.isVisibile()) return;
//        NotificationCompat.Builder notficiationBuilder =
//                new NotificationCompat.Builder(this,"002")
//                        .setSmallIcon(R.drawable.icon_status)
//                        .setContentTitle("Lawnmower Status Update")
//                        .setContentText(message)
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                        .setAutoCancel(true);
//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
//        notificationManagerCompat.notify(002,notficiationBuilder.build());
//    }
