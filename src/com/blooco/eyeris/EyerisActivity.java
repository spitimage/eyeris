package com.blooco.eyeris;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

public class EyerisActivity extends Activity
{
    private SignatureMgr signatureMgr = null;

    enum Target
    {
        CREATE_ACCOUNT, LOGIN
    }

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
                signatureMgr = null;
            }
        }

        doStateTransition();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (signatureMgr != null)
        {
            // Transition to scan activity
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra("signatureMgr", signatureMgr);
            startActivity(intent);
        }
    }

    private void doStateTransition()
    {
        if (signatureMgr == null)
        {
            KeyStoreMgr ksm = new KeyStoreMgr();
            if (ksm.keystoreFileExists())
            {
                // Transition to login activity
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, Target.LOGIN.ordinal());
            }
            else
            {
                // Transition to create account activity
                Intent intent = new Intent(this, CreateAccountActivity.class);
                startActivityForResult(intent, Target.CREATE_ACCOUNT.ordinal());
            }
        }
        else
        {
            // Transition to scan activity
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra("signatureMgr", signatureMgr);
            startActivity(intent);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Target.LOGIN.ordinal())
        {
            if (resultCode == RESULT_OK)
            {
                signatureMgr = (SignatureMgr) data.getSerializableExtra("signatureMgr");
            }
            else
            {
                showDialog(0);
            }

        }
        else
            if (requestCode == Target.CREATE_ACCOUNT.ordinal())
            {
                if (resultCode == RESULT_OK)
                {
                    signatureMgr = (SignatureMgr) data.getSerializableExtra("signatureMgr");
                }
                else
                {
                    showDialog(0);
                }

            }

    }

    private Dialog createAlertDialog(String msg)
    {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setMessage(msg);
        dialog.setButton("Ok", new OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                doStateTransition();
            }
        });

        return dialog;
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        return createAlertDialog("Bad things happened");
    }

}