package com.example.geng.streetsweeping;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by xuejing on 11/10/15.
 */
public class NotificationHandler {
    private Context context;
    private NotificationCompat.Builder mBuilder;
    private static final int NOTI_ID = 1;

    public NotificationHandler(Context context) {
        this.context = context;
        this.mBuilder = new NotificationCompat.Builder(context);
    }

    public void Notify(String title, String text, long time, int icon) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

        mBuilder.setSmallIcon(icon).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND).setContentTitle(title)
                .setContentText(text);

        Intent resultIntent = new Intent(context, MapsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(MapsActivity.class);

        stackBuilder.addNextIntent(resultIntent);



        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(NOTI_ID, mBuilder.build());


    }
}
