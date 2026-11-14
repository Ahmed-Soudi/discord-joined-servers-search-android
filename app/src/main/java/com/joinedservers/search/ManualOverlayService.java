package com.joinedservers.search;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/** Manual fallback when Accessibility is disabled or Discord isn't installed. */
public class ManualOverlayService extends Service {
    private FloatingSearch overlay;
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("manual_overlay", "Manual search overlay", NotificationManager.IMPORTANCE_LOW));
        Intent launch=new Intent(this, MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(this,"manual_overlay")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle("Joined Servers Search")
            .setContentText("Tap to manage the floating search")
            .setContentIntent(pi).setOngoing(true).build();
        startForeground(101,notification);
        overlay=new FloatingSearch(this, this::stopSelf);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        overlay.setVisible(true);
        return START_NOT_STICKY;
    }
    @Override public void onDestroy() { if(overlay!=null)overlay.destroy();super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
