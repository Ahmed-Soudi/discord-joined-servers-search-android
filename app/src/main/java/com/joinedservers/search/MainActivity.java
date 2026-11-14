package com.joinedservers.search;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.os.Process;
import android.widget.Toast;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private boolean discordInstalled() {
        try { getPackageManager().getPackageInfo("com.discord", 0); return true; }
        catch (PackageManager.NameNotFoundException e) { return false; }
    }
    private boolean hasUsageAccess() {
        AppOpsManager ops = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }
    private void startOverlay(String action) {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this,"Grant overlay permission first",Toast.LENGTH_LONG).show(); return;
        }
        if (ManualOverlayService.ACTION_MONITOR.equals(action) && !hasUsageAccess()) {
            Toast.makeText(this,"Grant Usage Access first",Toast.LENGTH_LONG).show(); return;
        }
        Intent intent = new Intent(this, ManualOverlayService.class);
        intent.setAction(action);
        startForegroundService(intent);
        finish();
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,40,32,24);
        TextView heading = new TextView(this); heading.setText("Discord Joined Servers Search for Android"); heading.setTextSize(22); root.addView(heading);
        TextView info = new TextView(this);
        info.setText((discordInstalled() ? "Discord is installed. Enable Usage Access and start monitoring to show the popup while Discord is open. " : "Discord is not installed. Open the popup manually. ") + "Joined servers are manually entered in this prototype; automatic OAuth2 sync is not yet available.");
        info.setPadding(0,24,0,24); root.addView(info);
        Button overlay = new Button(this); overlay.setText("Grant overlay permission"); overlay.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())))); root.addView(overlay);
        Button usage = new Button(this); usage.setText("Grant Usage Access"); usage.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))); root.addView(usage);
        Button monitor = new Button(this); monitor.setText("Start automatic Discord monitoring"); monitor.setOnClickListener(v -> startOverlay(ManualOverlayService.ACTION_MONITOR)); root.addView(monitor);
        Button manual = new Button(this); manual.setText("Open search popup"); manual.setOnClickListener(v -> startOverlay(ManualOverlayService.ACTION_OPEN)); root.addView(manual);
        Button stop = new Button(this); stop.setText("Stop monitoring and close popup"); stop.setOnClickListener(v -> { stopService(new Intent(this,ManualOverlayService.class)); Toast.makeText(this,"Stopped",Toast.LENGTH_SHORT).show(); }); root.addView(stop);
        TextView label = new TextView(this); label.setText("Joined servers (one per line): Name | ServerID"); root.addView(label);
        EditText editor = new EditText(this); editor.setMinLines(5); editor.setGravity(Gravity.TOP); editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); editor.setText(getPreferences(0).getString("servers", "")); root.addView(editor);
        Button save = new Button(this); save.setText("Save server list"); save.setOnClickListener(v -> {getPreferences(0).edit().putString("servers", editor.getText().toString()).apply(); Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();}); root.addView(save);
        setContentView(root);
    }
}
