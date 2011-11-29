package com.blooco.eyeris;

import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
    private boolean error = false;
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
                startZxing();
            }

        });
        
        startZxing();
        
    }
    
    private void startZxing()
    {
        if (error)
        {
            return;
        }
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }
    
    @Override
    protected void onResume()
    {
        startZxing();
        super.onResume();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null)
        {
            Log.d(TAG, "Scan Contents: " + result.getContents());
            Log.d(TAG, "Scan Format Name: " + result.getFormatName());
            if (result.getContents() != null)
            {
                scan(result.getContents());
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
    
    protected void go_to_url(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(intent);        
    }

    protected void scan(String content)
    {
        try
        {
            if (signatureMgr == null)
            {
                Log.i(TAG, "Signature manager is null");
                // Go back to login
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
            // TODO Generate a real nonce
            String nonce = Long.toHexString(new Date().getTime());
            String subject = signatureMgr.getSubject();
            String signature = signatureMgr.sign(content+nonce);
            Log.d(TAG, "Produced Signature: " + signature);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("nonce", nonce);
            params.put("subject", subject);
            params.put("content", content);
            params.put("signature", signature);
            
            SharedPreferences settings = getSharedPreferences(getString(R.string.eyeris_pref), 0);
            String hostname = settings.getString(getString(R.string.host_pref), "combo");
            String url = "http://" + hostname + getString(R.string.scan_url);

            int result = NetworkMgr.post(url, null, params, null);
            Log.i(TAG, "Signature return value = " + result);
            
            if (result == 403)
            {
                String msg = "Not authorized";
                Log.i(TAG, "Attempted unauthorized access: " + subject + " for " + content);
                throw new EyerisException(msg);
                
            }
            if (result != 200)
            {
                String msg = "Unexpected result from signature server";
                Log.e(TAG, msg);
                throw new EyerisException(msg); 
            }
            
            url = "http://" + hostname + getString(R.string.log_url);

            go_to_url(url + subject + '/');

        }
        catch (EyerisException e)
        {
            error = true;
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
                error = false;
                dialog.dismiss();
            }
        });

        return dialog;
    }

}