package com.blooco.eyeris;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CreateAccountActivity extends Activity
{
    static String TAG = "CreateAccount";
    private ProgressDialog progressDialog;
    private SignatureMgr signatureMgr;

    private Handler handler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {

            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }

            if (msg.arg1 != RESULT_OK)
            {
                Bundle bundle = new Bundle();
                bundle.putString("message", (String)msg.obj);
                removeDialog(0);
                showDialog(0, bundle);
                return;
            }

            Intent intent = new Intent(CreateAccountActivity.this, ScanActivity.class);
            intent.putExtra("signatureMgr", signatureMgr);
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount);

        Button ok = (Button) findViewById(R.id.create_ok);
        ok.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                ok();
            }

        });

    }

    private class CertCreateThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {

                EditText et = (EditText) findViewById(R.id.create_email);

                String alias = et.getText().toString();

                et = (EditText) findViewById(R.id.create_password);
                String pw = et.getText().toString();

                validateParams(alias, pw);

                KeyStoreMgr ksm = new KeyStoreMgr();
                ksm.setPassword(pw);
                KeyStore ks = ksm.get();
                CertMgr cm = new CertMgr();
                cm.setAlias(alias);
                cm.setPassword(pw);
                Certificate cert = cm.create(ks);

                registerCert(alias, cert);

                // Once registered, we can save the keystore to permanent
                // storage
                ksm.save();

                signatureMgr = new SignatureMgr();
                signatureMgr.init(ks, alias, pw);

                Message msg = Message.obtain();
                msg.arg1 = RESULT_OK;
                handler.sendMessage(msg);
            }
            catch (Exception e)
            {
                // Pass off the exception to the UI thread
                Message msg = Message.obtain();
                msg.arg1 = RESULT_CANCELED;
                msg.obj = e.getLocalizedMessage();
                handler.sendMessage(msg);
            }
        }
    }

    private void ok()
    {
        progressDialog = ProgressDialog.show(this, null, "Creating and registering certificate...");
        new CertCreateThread().start();
    }

    private void validateParams(String alias, String pw) throws EyerisException
    {
        
//        if (alias.length() < 8)
//        {
//            throw new EyerisException("Invalid username");
//        }
//        if (pw.length() < 8)
//        {
//            throw new EyerisException("Invalid password");
//        }
    }

    private void registerCert(String subject, Certificate cert) throws EyerisException
    {
        String certString;
        certString = encodeCert(cert);
        Log.d(TAG, "Cert: " + certString);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("subject", subject);
        params.put("cert", certString);
        int result = NetworkMgr.post(getString(R.string.register_url), null, params, null);
        Log.i(TAG, "Register return value = " + result);
        if (result == 403)
        {
            String msg = "Username " + subject
                    + " is already registered. Please select a different username.";
            Log.d(TAG, msg);
            throw new EyerisException(msg);
        }

        if (result != 200)
        {
            String msg = "A network error has occurred.";
            Log.d(TAG, msg);
            throw new EyerisException(msg);
        }
    }

    public String encodeCert(Certificate cert) throws EyerisException
    {
        String ret = "";
        try
        {
            // Get the encoded form which is suitable for exporting
            byte[] buf = cert.getEncoded();
            String certString = Base64.encodeToString(buf, Base64.DEFAULT);

            // Write in text form
            Writer wr = new StringWriter();
            wr.write("-----BEGIN CERTIFICATE-----\n");
            wr.write(certString);
            wr.write("-----END CERTIFICATE-----\n");
            ret = wr.toString();
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            throw new EyerisException("Unable to encode certificate.");
        }
        catch (CertificateEncodingException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            throw new EyerisException("Unable to encode certificate.");
        }
        return ret;
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