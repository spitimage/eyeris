package com.blooco.eyeris;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;

import android.util.Base64;
import android.util.Log;

public class SignatureMgr implements Serializable
{
    private static final long serialVersionUID = 1L;
    static private String TAG = "SignatureMgr";
    private PrivateKey privateKey = null;
    
    public void init(KeyStore ks, String alias, String password) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException
    {
        Log.i(TAG, "Looking up private key for alias " + alias);
        PasswordProtection pwp = new PasswordProtection(password.toCharArray());
        PrivateKeyEntry entry = (PrivateKeyEntry) ks.getEntry(alias, pwp);
        privateKey = entry.getPrivateKey();        
    }

    public String sign(String content) throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException
    {
        if (null == privateKey)
        {
            Log.e(TAG, "Attempted to sign without initializing signature manager");
            throw new InvalidKeyException();
        }
        Signature signature = Signature.getInstance("RSA");
        signature.initSign(privateKey);
        signature.update(content.getBytes());
        byte[] bytes = signature.sign();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

}
