package com.joinedservers.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class FloatingSearch {
    private final Context context;
    private final WindowManager manager;
    private LinearLayout root, results;
    private WindowManager.LayoutParams params;
    private EditText search;
    private boolean visible = false;
    private boolean expanded = false;
    private final Runnable onClose;
    FloatingSearch(Context c, Runnable onClose) {context=c; this.onClose=onClose; manager=(WindowManager)c.getSystemService(Context.WINDOW_SERVICE);}
    void setVisible(boolean show) {
        if (show == visible) return;
        if (show && Settings.canDrawOverlays(context)) { create(); try { manager.addView(root,params); visible=true; } catch (RuntimeException ex) { root=null; } }
        else if (!show && visible) {manager.removeView(root); root=null; visible=false;}
    }
    void destroy() {setVisible(false);}
    void dataChanged(){if(visible)refresh();}
    private int dp(int n) {return (int)(n*context.getResources().getDisplayMetrics().density);}
    private void create() {
        params = new WindowManager.LayoutParams(dp(290),WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.LEFT;
        params.x=context.getSharedPreferences("overlay",0).getInt("x",dp(18));
        params.y=context.getSharedPreferences("overlay",0).getInt("y",dp(90));
        root=new LinearLayout(context);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(8),dp(6),dp(8),dp(6));root.setBackgroundColor(0xff202127);
        LinearLayout header=new LinearLayout(context);header.setOrientation(LinearLayout.HORIZONTAL);root.addView(header);
        TextView handle=new TextView(context);handle.setText("⠿");handle.setTextColor(-1);handle.setTextSize(24);handle.setPadding(dp(4),0,dp(12),0);header.addView(handle);
        final float[] down = new float[2];final int[] start = new int[2];
        handle.setOnTouchListener((v,e)->{switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN: down[0]=e.getRawX();down[1]=e.getRawY();start[0]=params.x;start[1]=params.y;return true;
            case MotionEvent.ACTION_MOVE: params.x=start[0]+(int)(e.getRawX()-down[0]);params.y=start[1]+(int)(e.getRawY()-down[1]);manager.updateViewLayout(root,params);return true;
            case MotionEvent.ACTION_UP:context.getSharedPreferences("overlay",0).edit().putInt("x",params.x).putInt("y",params.y).apply();return true;
        }return false;});
        search=new EditText(context);search.setSingleLine(true);search.setTextColor(-1);search.setHintTextColor(0xffbbbbbb);search.setHint("Search joined servers");search.setTextSize(14);header.addView(search,new LinearLayout.LayoutParams(0,dp(46),1));
        Button toggle=new Button(context);toggle.setText("▾");header.addView(toggle,new LinearLayout.LayoutParams(dp(42),dp(46)));
        Button close=new Button(context);close.setText("×");header.addView(close,new LinearLayout.LayoutParams(dp(42),dp(46)));
        close.setOnClickListener(v->{setVisible(false);if(onClose!=null)onClose.run();});
        ScrollView scroller=new ScrollView(context);root.addView(scroller);results=new LinearLayout(context);results.setOrientation(LinearLayout.VERTICAL);scroller.addView(results);
        scroller.getLayoutParams().height=dp(260);scroller.setVisibility(View.GONE);
        toggle.setOnClickListener(v->{expanded=!expanded;scroller.setVisibility(expanded?View.VISIBLE:View.GONE);toggle.setText(expanded?"▴":"▾");refresh();});
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){} public void onTextChanged(CharSequence s,int start,int before,int count){if(!expanded){expanded=true;scroller.setVisibility(View.VISIBLE);toggle.setText("▴");}refresh();}public void afterTextChanged(Editable e){} });
    }
    private void openDiscordServer(String id) {
        // Use a standard HTTPS ACTION_VIEW explicitly delivered to Discord.
        // Do not use SINGLE_TOP: Discord may need a new deep-link activity
        // instead of reusing the foreground activity.
        Intent intent = new Intent(Intent.ACTION_VIEW,
            Uri.parse("https://discord.com/channels/" + id));
        intent.setPackage("com.discord");
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            android.widget.Toast.makeText(context,
                "Discord does not accept this server link on this device.",
                android.widget.Toast.LENGTH_LONG).show();
        }
    }
    private void refresh() {
        if(results==null)return;results.removeAllViews();
        String q=search.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        try {
            org.json.JSONArray guilds=new org.json.JSONArray(context.getSharedPreferences("joined_guilds",0).getString("list","[]"));
            for(int j=0;j<guilds.length();j++){
                org.json.JSONObject guild=guilds.getJSONObject(j);
                String name=guild.getString("name"),id=guild.getString("id");
                if(!name.toLowerCase(java.util.Locale.ROOT).contains(q))continue;
                Button b=new Button(context);b.setAllCaps(false);b.setText(name);results.addView(b);
                b.setOnClickListener(v->openDiscordServer(id));
            }
        }catch(org.json.JSONException e){ }
        if(results.getChildCount()==0){
            TextView empty=new TextView(context);
            empty.setText(DiscordAuth.connected(context)?"No matching joined servers. Try Sync joined servers now.":"Connect Discord in app settings.");
            empty.setTextColor(-1);results.addView(empty);
        }
    }
}
