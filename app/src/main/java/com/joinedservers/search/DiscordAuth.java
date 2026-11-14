package com.joinedservers.search;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DiscordAuth {
    static final String CLIENT_ID="1571201606890950806";
    static final String REDIRECT="com.joinedservers.search://oauth/callback";
    private static final String AUTH="https://discord.com/oauth2/authorize";
    private static final String TOKEN="https://discord.com/api/v10/oauth2/token";
    private static final String GUILDS="https://discord.com/api/v10/users/@me/guilds";
    private static final ExecutorService IO=Executors.newSingleThreadExecutor();
    private DiscordAuth(){}
    private static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("discord_oauth",0);}
    public static boolean connected(Context c){return !prefs(c).getString("refresh","").isEmpty();}
    private static String random(int bytes) throws Exception {
        byte[] b=new byte[bytes];new SecureRandom().nextBytes(b);
        return Base64.encodeToString(b,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
    }
    public static Intent login(Context c) throws Exception {
        String verifier=random(32),state=random(24);
        String challenge=Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)),Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
        prefs(c).edit().putString("verifier",verifier).putString("state",state).apply();
        Uri uri=Uri.parse(AUTH).buildUpon().appendQueryParameter("client_id",CLIENT_ID)
            .appendQueryParameter("response_type","code").appendQueryParameter("redirect_uri",REDIRECT)
            .appendQueryParameter("scope","guilds").appendQueryParameter("state",state)
            .appendQueryParameter("code_challenge",challenge).appendQueryParameter("code_challenge_method","S256").build();
        return new Intent(Intent.ACTION_VIEW,uri);
    }
    public static void callback(Context c,Uri uri,Runnable success,java.util.function.Consumer<String> error){
        String expected=prefs(c).getString("state","");
        String code=uri.getQueryParameter("code");
        if(expected.isEmpty() || !expected.equals(uri.getQueryParameter("state")) || code==null){
            error.accept("Authorization failed or state mismatch");return;
        }
        String verifier=prefs(c).getString("verifier","");
        prefs(c).edit().remove("state").remove("verifier").apply();
        IO.execute(()->{
            try{
                String data="client_id="+enc(CLIENT_ID)+"&grant_type=authorization_code&code="+enc(code)+"&redirect_uri="+enc(REDIRECT)+"&code_verifier="+enc(verifier);
                saveTokens(c,request(TOKEN,data,null));
                syncInternal(c);
                success.run();
            }catch(Exception ex){error.accept(ex.getMessage());}
        });
    }
    private static String enc(String s)throws Exception{return java.net.URLEncoder.encode(s,"UTF-8");}
    private static JSONObject request(String url,String body,String bearer)throws Exception{
        HttpURLConnection conn=(HttpURLConnection)new URL(url).openConnection();
        conn.setConnectTimeout(12000);conn.setReadTimeout(12000);
        if(body!=null){conn.setRequestMethod("POST");conn.setDoOutput(true);conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");try(OutputStream out=conn.getOutputStream()){out.write(body.getBytes(StandardCharsets.UTF_8));}}
        if(bearer!=null)conn.setRequestProperty("Authorization","Bearer "+bearer);
        int status=conn.getResponseCode();
        InputStream stream=status>=200&&status<300?conn.getInputStream():conn.getErrorStream();
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();byte[] chunk=new byte[4096];int n;
        if(stream!=null)try(InputStream in=stream){while((n=in.read(chunk))!=-1){buffer.write(chunk,0,n);if(buffer.size()>1024*1024)throw new IOException("Response too large");}}
        String result=buffer.toString("UTF-8");conn.disconnect();
        if(status<200||status>=300)throw new IOException("Discord API HTTP "+status+": "+result.substring(0,Math.min(result.length(),160)));
        if(url.equals(GUILDS))return new JSONObject().put("guilds",new JSONArray(result));
        return new JSONObject(result);
    }
    private static void saveTokens(Context c,JSONObject json)throws Exception{
        long expires=System.currentTimeMillis()+json.getLong("expires_in")*1000L;
        prefs(c).edit().putString("access",json.getString("access_token"))
            .putString("refresh",json.getString("refresh_token")).putLong("expiry",expires).apply();
    }
    private static void syncInternal(Context c)throws Exception{
        String access=prefs(c).getString("access","");
        if(access.isEmpty())throw new IOException("Connect Discord first");
        if(System.currentTimeMillis()>prefs(c).getLong("expiry",0)-60000){
            String refresh=prefs(c).getString("refresh","");
            if(refresh.isEmpty())throw new IOException("Sign in again");
            saveTokens(c,request(TOKEN,"client_id="+enc(CLIENT_ID)+"&grant_type=refresh_token&refresh_token="+enc(refresh),null));
            access=prefs(c).getString("access","");
        }
        JSONArray guilds=request(GUILDS,null,access).getJSONArray("guilds");
        JSONArray saved=new JSONArray();
        for(int i=0;i<guilds.length();i++){
            JSONObject g=guilds.getJSONObject(i);
            saved.put(new JSONObject().put("id",g.getString("id")).put("name",g.getString("name")));
        }
        c.getSharedPreferences("joined_guilds",0).edit().putString("list",saved.toString()).putLong("updated",System.currentTimeMillis()).apply();
        c.sendBroadcast(new Intent("com.joinedservers.search.GUILDS_UPDATED").setPackage(c.getPackageName()));
    }
    public static void sync(Context c,java.util.function.Consumer<String> done){
        IO.execute(()->{try{syncInternal(c);done.accept(null);}catch(Exception e){done.accept(e.getMessage());}});
    }
    public static void disconnect(Context c){
        prefs(c).edit().clear().apply();
        c.getSharedPreferences("joined_guilds",0).edit().clear().apply();
        c.sendBroadcast(new Intent("com.joinedservers.search.GUILDS_UPDATED").setPackage(c.getPackageName()));
    }
}
