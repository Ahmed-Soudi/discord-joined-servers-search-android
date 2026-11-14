package com.joinedservers.search;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
public class OAuthCallbackActivity extends Activity {
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        Intent i=new Intent(this,MainActivity.class);
        i.setData(getIntent().getData());

        startActivity(i);
        finish();
    }
}
