package com.blooco.eyeris;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class EyerisActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        doStateTransition();
    }

    private void doStateTransition()
    {
        KeyStoreMgr ksm = new KeyStoreMgr();
        if (ksm.keystoreFileExists())
        {
            // Transition to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else
        {
            // Transition to create account activity
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        }
        
    }
}