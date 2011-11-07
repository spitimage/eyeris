package com.blooco.eyeris;

import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ScanActivity extends Activity
{
    private static String TAG = "ScanActivity";
    private SignatureMgr signatureMgr = null;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
        
        signatureMgr = (SignatureMgr) getIntent().getExtras().getSerializable("signatureMgr");
        
        Button scan = (Button) findViewById(R.id.scan_scan);
        scan.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                scan();
            }

        });
    }

    protected void scan()
    {
        try
        {
            if (signatureMgr == null)
            {
                Log.e(TAG, "Signature manager is null");
                throw new EyerisException("An initialization error has occurred");
            }
            // TODO Generate a real nonce
            String nonce = Long.toHexString(new Date().getTime());
            String subject = signatureMgr.getSubject();
            String content = "resource1";
            String signature = signatureMgr.sign(content+nonce);
            Log.d(TAG, "Produced Signature: " + signature);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("nonce", nonce);
            params.put("subject", subject);
            params.put("content", content);
            params.put("signature", signature);
            int result = NetworkMgr.post("http://192.168.1.106:8000/authorize/", null, params, null);
            Log.i(TAG, "Signature return value = " + result);
            if (result != 200)
            {
                String msg = "Unexpected result from signature server";
                Log.e(TAG, msg);
                throw new EyerisException(msg);
            }

        }
        catch (EyerisException e)
        {
            Bundle bundle = new Bundle();
            bundle.putString("message", e.getLocalizedMessage());
            removeDialog(0);
            showDialog(0, bundle);
        }
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
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle)
    {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setMessage(bundle.getString("message"));
        dialog.setButton("Ok", new android.content.DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        return dialog;
    }

}