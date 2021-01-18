package com.example.lawnmower;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

 /**
 * Creates notification channel in createErrorNotificationChannel and createStatusNotificationChannel.
 * Send messages via notification to the ui if the status from lawnmower is changed
 */
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
     /**
      * Creates notification channel for lawnmower error status
      *
      */
    private void createErrorNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            errorChannel = new NotificationChannel("1","Error", NotificationManager.IMPORTANCE_DEFAULT);
            errorChannel.setDescription("This is a error notification channel");
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(errorChannel);
        }
    }
     /**
      * Creates notification channel for lawnmowerstatus
      *
      */
    private void createStatusNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            statusChannel = new NotificationChannel("2","Status",NotificationManager.IMPORTANCE_DEFAULT);
            statusChannel.setDescription("This is a status notification channel");

            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(statusChannel);
        }
    }
     /**
      * Opens sendNotification for error Notification
      *
      */
    public void sendErrorNotification(String msg){
        this.sendNotification(1, ATTENTION, msg, R.drawable.ic_stat_error_outline);
    }

     /**
      * Opens sendNotification for Status Notification
      *
      */
    public void sendStatusNotification(String msg){
        this.sendNotification(2, "Lawnmower Status", msg, R.drawable.logomarkerr);
    }

     /**
      * Publish notification with  channelId,  title,  msg,  status
      *
      */
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
        createErrorNotificationChannel();
        createStatusNotificationChannel();
    }
}
