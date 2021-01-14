package com.example.lawnmower;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHandler implements  Runnable {

    private Context ctx;
    private NotificationChannel errorChannel;
    private NotificationChannel statusChannel;
    private static final String ATTENTION = "Schaue nach deinem Rasenmäher";
    private Thread NotificationThread;

    public NotificationHandler(Context ctx) {
        this.ctx = ctx;
        NotificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                createErrorNotificationChannel();
                createStatusNotificationChannel();
            }
        });
        NotificationThread.start();


    }

    private void createErrorNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            errorChannel = new NotificationChannel("1","Error", NotificationManager.IMPORTANCE_DEFAULT);
            errorChannel.setDescription("This is a error notification channel");
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(errorChannel);
        }
    }

    private void createStatusNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            statusChannel = new NotificationChannel("2","Status",NotificationManager.IMPORTANCE_DEFAULT);
            statusChannel.setDescription("This is a status notification channel");

            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(statusChannel);
        }
    }

    public void sendErrorNotification(String msg){
        this.sendNotification(1, ATTENTION, msg, R.drawable.ic_stat_error_outline);
        System.out.println("Message Send **********************"+ msg);
    }
   // Send Status Notifiaction

    public void sendStatusNotification(String msg){
        this.sendNotification(2, "Lawnmower Status", msg, R.drawable.icon_status);
    }

    private void sendNotification(int channelId, String title, String msg, int status) {
        //if (!LawnmowerApp.isVisibile()) return;

        NotificationCompat.Builder notficiationBuilder =
                new NotificationCompat.Builder(ctx, String.valueOf(channelId))
                        .setSmallIcon(status)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                ;
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);
        notificationManagerCompat.notify(channelId,notficiationBuilder.build());
    }

    @Override
    public void run() {
        System.out.println("-------------------------------wird aufgerufen");
        createErrorNotificationChannel();
        createStatusNotificationChannel();
    }
}
