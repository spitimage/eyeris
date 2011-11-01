package com.blooco.eyeris;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
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
            // TODO Generate a real nonce
            String nonce = Long.toHexString(new Date().getTime());
            String subject = signatureMgr.getSubject();
            String content = "Some Content Here";
            String signature = signatureMgr.sign(content+nonce);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("nonce", nonce);
            params.put("subject", subject);
            params.put("content", content);
            params.put("signature", signature);
            int result = NetworkMgr.post("http://192.168.1.106:8000/authorize/", null, params, null);
            Log.i(TAG, "Signature return value = " + result);
            if (result != 200)
            {
                throw new SignatureException();
            }

        }
        catch (InvalidKeyException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SignatureException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
}