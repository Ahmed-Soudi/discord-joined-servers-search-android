package com.joinedservers.search;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.widget.*;

public class MainActivity extends Activity {
    private LinearLayout root;
    private void message(String s){runOnUiThread(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}
    private boolean hasUsageAccess(){
        AppOpsManager ops=(AppOpsManager)getSystemService(APP_OPS_SERVICE);
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),getPackageName())==AppOpsManager.MODE_ALLOWED;
    }
    private void startOverlay(String action){
        if(!DiscordAuth.connected(this)){message("Connect Discord first");return;}
        if(!Settings.canDrawOverlays(this)){message("Grant overlay permission first");return;}
        if(ManualOverlayService.ACTION_MONITOR.equals(action)&&!hasUsageAccess()){message("Grant Usage Access first");return;}
        Intent i=new Intent(this,ManualOverlayService.class);i.setAction(action);startForegroundService(i);finish();
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        if(getIntent()!=null && getIntent().getData()!=null && "oauth".equals(getIntent().getData().getHost())){
            DiscordAuth.callback(this,getIntent().getData(),()->{message("Connected and synced");runOnUiThread(this::render);},e->message("Login error: "+e));
        }
        render();
    }
    private void render(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(32,40,32,24);
        ScrollView scroll=new ScrollView(this);scroll.addView(root);setContentView(scroll);
        TextView heading=new TextView(this);heading.setText("Discord Joined Servers Search for Android");heading.setTextSize(22);root.addView(heading);
        TextView status=new TextView(this);status.setPadding(0,20,0,20);
        status.setText(DiscordAuth.connected(this)?"Discord connected. Joined servers sync automatically while monitoring.":"Connect your Discord account to load joined servers. No manual entry.");
        root.addView(status);
        Button connect=new Button(this);connect.setText(DiscordAuth.connected(this)?"Reconnect Discord":"Connect Discord");
        connect.setOnClickListener(v->{try{startActivity(DiscordAuth.login(this));}catch(Exception e){message(e.getMessage());}});root.addView(connect);
        Button sync=new Button(this);sync.setText("Sync joined servers now");
        sync.setOnClickListener(v->DiscordAuth.sync(this,e->message(e==null?"Joined servers updated":"Sync failed: "+e)));root.addView(sync);
        Button overlay=new Button(this);overlay.setText("Grant overlay permission");
        overlay.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))));root.addView(overlay);
        Button usage=new Button(this);usage.setText("Grant Usage Access");
        usage.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));root.addView(usage);
        Button monitor=new Button(this);monitor.setText("Start automatic Discord monitoring");
        monitor.setOnClickListener(v->startOverlay(ManualOverlayService.ACTION_MONITOR));root.addView(monitor);
        Button manual=new Button(this);manual.setText("Open search popup");
        manual.setOnClickListener(v->startOverlay(ManualOverlayService.ACTION_OPEN));root.addView(manual);
        Button stop=new Button(this);stop.setText("Stop monitoring and close popup");
        stop.setOnClickListener(v->{stopService(new Intent(this,ManualOverlayService.class));message("Stopped");});root.addView(stop);
        Button disconnect=new Button(this);disconnect.setText("Disconnect Discord and erase saved servers");
        disconnect.setOnClickListener(v->{stopService(new Intent(this,ManualOverlayService.class));DiscordAuth.disconnect(this);render();});root.addView(disconnect);
    }
}
