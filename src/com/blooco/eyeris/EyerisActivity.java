package com.blooco.eyeris;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class EyerisActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Check for a logout
        if (getIntent().getExtras() != null)
        {
            boolean logout = getIntent().getExtras().getBoolean("logout", false);
            if (logout)
            {
            }
        }

        doStateTransition();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
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