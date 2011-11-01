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
                showDialog(0);
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
                e.printStackTrace();
                Message msg = Message.obtain();
                msg.arg1 = RESULT_CANCELED;
                handler.sendMessage(msg);
            }
        }
    }

    private void ok()
    {

        progressDialog = ProgressDialog.show(this, null, "Creating and registering certificate...");
        new CertCreateThread().start();
    }

    private void validateParams(String alias, String pw)
    {
        // TODO Auto-generated method stub

    }

    private void registerCert(String subject, Certificate cert) throws CertificateEncodingException, IOException
    {
        String certString = encodeCert(cert);
        Log.d(TAG, "Cert: " + certString);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("subject", subject);
        params.put("cert", certString);
        int result = NetworkMgr.post("http://192.168.1.106:8000/register/", null, params, null);
        Log.i(TAG, "Register return value = " + result);
    }

    public String encodeCert(Certificate cert) throws CertificateEncodingException, IOException
    {
        // Get the encoded form which is suitable for exporting
        byte[] buf = cert.getEncoded();
        String certString = Base64.encodeToString(buf, Base64.DEFAULT);

        // Write in text form
        Writer wr = new StringWriter();
        wr.write("-----BEGIN CERTIFICATE-----\n");
        wr.write(certString);
        wr.write("-----END CERTIFICATE-----\n");
        return wr.toString();
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
                return createAlertDialog("Bad things happened");
        }
        
        return createAlertDialog("Lost");
    }
    

    
}