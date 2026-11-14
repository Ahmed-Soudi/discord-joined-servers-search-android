package com.joinedservers.search;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.pm.PackageManager;
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
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,40,32,24);
        TextView heading = new TextView(this); heading.setText("Discord Joined Servers Search for Android"); heading.setTextSize(22); root.addView(heading);
        boolean installed = discordInstalled();
        TextView info = new TextView(this); info.setText((installed ? "Discord is installed. Enable Accessibility for automatic show/hide. " : "Discord is not installed. Tap Open search popup to use it manually. ") + "Grant overlay permission first. Server names are manually entered in this prototype; OAuth2 is not implemented."); info.setPadding(0,24,0,24); root.addView(info);
        Button overlay = new Button(this); overlay.setText("Grant overlay permission"); overlay.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())))); root.addView(overlay);
        Button manual = new Button(this); manual.setText("Open search popup"); manual.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) { Toast.makeText(this,"Grant overlay permission first",Toast.LENGTH_LONG).show(); return; }
            if (DiscordAccessibilityService.instance != null) DiscordAccessibilityService.instance.openManually();
            else startForegroundService(new Intent(this,ManualOverlayService.class));
            finish();
        }); root.addView(manual);
        Button accessibility = new Button(this); accessibility.setText("Enable Accessibility service"); accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); root.addView(accessibility);
        TextView label = new TextView(this); label.setText("Server names (one per line). Add IDs as Name | ServerID for opening a server."); root.addView(label);
        EditText editor = new EditText(this); editor.setMinLines(5); editor.setGravity(Gravity.TOP); editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); editor.setText(getPreferences(0).getString("servers", "")); root.addView(editor);
        Button save = new Button(this); save.setText("Save server list"); save.setOnClickListener(v -> {getPreferences(0).edit().putString("servers", editor.getText().toString()).apply(); android.widget.Toast.makeText(this,"Saved",android.widget.Toast.LENGTH_SHORT).show();}); root.addView(save);
        setContentView(root);
    }
}
