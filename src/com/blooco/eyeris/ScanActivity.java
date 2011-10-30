package com.blooco.eyeris;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ScanActivity extends Activity
{
    private SignatureMgr signatureMgr = null;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
        
        signatureMgr = (SignatureMgr) getIntent().getExtras().getSerializable("signatureMgr");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "Logout");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch (item.getItemId())
        {
        case 0:
            signatureMgr = null;
            Intent intent = new Intent(this, EyerisActivity.class);
            intent.putExtra("logout", true);
            startActivity(intent);
        }

        return super.onMenuItemSelected(featureId, item);
    }
}