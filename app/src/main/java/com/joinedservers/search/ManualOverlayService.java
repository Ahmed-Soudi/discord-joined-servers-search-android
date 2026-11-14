package com.joinedservers.search;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;

public class ManualOverlayService extends Service {
    public static final String ACTION_MONITOR = "com.joinedservers.search.MONITOR";
    public static final String ACTION_OPEN = "com.joinedservers.search.OPEN";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private FloatingSearch overlay;\n    private long lastSync=0;\n    private final android.content.BroadcastReceiver updates=new android.content.BroadcastReceiver(){\n        @Override public void onReceive(android.content.Context c,Intent i){if(overlay!=null)overlay.dataChanged();}\n    };
    private boolean monitoring = false, discordForeground = false, dismissed = false, manual = false;
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (!monitoring) return;
            if (!Settings.canDrawOverlays(ManualOverlayService.this) || !hasUsageAccess()) {
                if (overlay != null) overlay.setVisible(false);
                stopSelf(); return;
            }
            if(System.currentTimeMillis()-lastSync>300000){lastSync=System.currentTimeMillis();if(DiscordAuth.connected(ManualOverlayService.this))DiscordAuth.sync(ManualOverlayService.this,e->{});}\n            String foreground = foregroundPackage();
            if (foreground != null) {
                boolean nowDiscord = "com.discord".equals(foreground);
                if (nowDiscord && !discordForeground) dismissed = false;
                discordForeground = nowDiscord;
                if (overlay != null) overlay.setVisible(manual || (nowDiscord && !dismissed));
            }
            handler.postDelayed(this, 1200);
        }
    };
    private boolean hasUsageAccess() {
        AppOpsManager ops = (AppOpsManager)getSystemService(APP_OPS_SERVICE);
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }
    private String foregroundPackage() {
        UsageStatsManager mgr = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
        long end = System.currentTimeMillis();
        UsageEvents events = mgr.queryEvents(end - 15000, end);
        UsageEvents.Event event = new UsageEvents.Event();
        String last = null;
        long lastTime = -1;
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED && event.getTimeStamp() >= lastTime) {
                lastTime = event.getTimeStamp();
                last = event.getPackageName();
            }
        }
        return last;
    }
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("manual_overlay", "Search overlay and monitoring", NotificationManager.IMPORTANCE_LOW));
        Intent launch=new Intent(this, MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(this,"manual_overlay")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle("Joined Servers Search")
            .setContentText("Tap to manage the floating search")
            .setContentIntent(pi).setOngoing(true).build();
        startForeground(101,notification);
        registerReceiver(updates,new android.content.IntentFilter("com.joinedservers.search.GUILDS_UPDATED"),android.content.Context.RECEIVER_NOT_EXPORTED);\n        overlay=new FloatingSearch(this, () -> {
            dismissed = true;
            manual = false;
            if (!monitoring) stopSelf();
        });
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        String action = intent == null ? null : intent.getAction();
        if(DiscordAuth.connected(this))DiscordAuth.sync(this,e->{});\n        if (ACTION_MONITOR.equals(action)) {
            monitoring = true; manual = false;
            handler.removeCallbacks(poll);
            handler.post(poll);
        } else if (ACTION_OPEN.equals(action)) {
            manual = true;
            overlay.setVisible(true);
        }
        return START_NOT_STICKY;
    }
    @Override public void onDestroy() {
        monitoring = false;
        handler.removeCallbacks(poll);
        if(overlay!=null)overlay.destroy();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
