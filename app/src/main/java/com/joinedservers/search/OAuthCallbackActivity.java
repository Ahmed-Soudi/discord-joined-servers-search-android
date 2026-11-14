package com.joinedservers.search;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
public class OAuthCallbackActivity extends Activity {
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        Intent i=new Intent(this,MainActivity.class);
        i.setData(getIntent().getData());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}
