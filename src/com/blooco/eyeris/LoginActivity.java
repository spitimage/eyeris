package com.blooco.eyeris;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;

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
import android.widget.EditText;

public class LoginActivity extends Activity
{
    static String TAG = "LoginActivity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button ok = (Button) findViewById(R.id.login_ok);
        ok.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                ok();
            }

        });

        Button cancel = (Button) findViewById(R.id.login_cancel);
        cancel.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                cancel();
            }

        });
    }

    private void cancel()
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void ok()
    {
        try
        {
            EditText et = (EditText) findViewById(R.id.login_email);

            String alias = et.getText().toString();

            et = (EditText) findViewById(R.id.login_password);
            String password = et.getText().toString();

            KeyStoreMgr ksm = new KeyStoreMgr();
            ksm.setPassword(password);
            KeyStore ks = ksm.get();
            SignatureMgr signatureMgr = new SignatureMgr();
            signatureMgr.init(ks, alias, password);

            Intent intent = new Intent();
            intent.putExtra("signatureMgr", signatureMgr);

            setResult(RESULT_OK, intent);
            finish();

        }
        catch (KeyStoreException e)
        {
            Log.i(TAG, "KeyStoreException");
            // Assume this is a failed authentication
            showDialog(0);
        }
        catch (IOException e)
        {
            Log.i(TAG, "IOException");
            // Strangely this is what happens when the keystore password fails!
            showDialog(0);
        }
        catch (Exception e)
        {
            Log.i(TAG, "Other Exception");
            // Some other error
            showDialog(1); 
        }
    }
    
    private Dialog createAlertDialog(String msg)
    {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setMessage(msg);
        dialog.setButton("Ok", new android.content.DialogInterface.OnClickListener()
        {            
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        
        
        return dialog;
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id){
            case 0:
                return createAlertDialog("Invalid Login Attempt");
            case 1:
                return createAlertDialog("Bad things happened");
        }
        
        return createAlertDialog("Lost");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "Delete All Accounts");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch (item.getItemId())
        {
        case 0:
            KeyStoreMgr ksm = new KeyStoreMgr();
            ksm.delete();

            Intent intent = new Intent();
            // Send back impotent signature manager
            intent.putExtra("signatureMgr", new SignatureMgr());
            setResult(RESULT_OK, intent);
            finish();
        }

        return super.onMenuItemSelected(featureId, item);
    }

}