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
            
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra("signatureMgr", signatureMgr);
            startActivity(intent);
        }
        catch (KeyStoreException e)
        {
            Log.i(TAG, e.getLocalizedMessage());
            // Assume this is a failed authentication
            Bundle bundle = new Bundle();
            bundle.putString("message", "Invalid Login");
            removeDialog(0);
            showDialog(0, bundle);
        }
        catch (IOException e)
        {
            Log.i(TAG, e.getLocalizedMessage());
            // Strangely this is what happens when the keystore password fails!
            Bundle bundle = new Bundle();
            bundle.putString("message", "Invalid Login");
            removeDialog(0);
            showDialog(0, bundle);
        }
        catch (EyerisException e)
        {
            Log.i(TAG, e.getLocalizedMessage());
            Bundle bundle = new Bundle();
            bundle.putString("message", e.getLocalizedMessage());
            removeDialog(0);
            showDialog(0, bundle);
        }
        catch (Exception e)
        {
            Log.i(TAG, e.getLocalizedMessage());
            // Strangely this is what happens when the keystore password fails!
            Bundle bundle = new Bundle();
            bundle.putString("message", "An unexpected error has occurred");
            removeDialog(0);
            showDialog(0, bundle);
        }
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
            // TODO Notify the server of the removal
            KeyStoreMgr ksm = new KeyStoreMgr();
            ksm.delete();

            Intent intent = new Intent(this, EyerisActivity.class);
            startActivity(intent);
        }

        return super.onMenuItemSelected(featureId, item);
    }

}