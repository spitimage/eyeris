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
    private String subject = null;

    public void init(KeyStore ks, String alias, String password) throws EyerisException
    {
        try
        {
            Log.i(TAG, "Looking up private key for alias " + alias);
            PasswordProtection pwp = new PasswordProtection(password.toCharArray());
            PrivateKeyEntry entry = (PrivateKeyEntry) ks.getEntry(alias, pwp);
            subject = alias;
            if (entry != null)
            {
                privateKey = entry.getPrivateKey();
            }
            else
            {
                Log.e(TAG, "No private key for " + alias);
                throw new EyerisException("Unable to find key for user " + alias);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            String msg = "There was a problem creating the signature.";
            throw new EyerisException(msg);

        }
        catch (UnrecoverableEntryException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            String msg = "There was a problem creating the signature.";
            throw new EyerisException(msg);
        }
        catch (KeyStoreException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            String msg = "There was a problem creating the signature.";
            throw new EyerisException(msg);
        }
    }

    public String sign(String content) throws EyerisException
    {
        String ret = "";
        try
        {
            if (null == privateKey)
            {
                Log.e(TAG, "Attempted to sign without initializing signature manager");
                String msg = "There was a problem creating the signature.";
                throw new EyerisException(msg);
            }
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(privateKey);
            signature.update(content.getBytes());
            byte[] bytes = signature.sign();
            ret = Base64.encodeToString(bytes, Base64.DEFAULT);
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            String msg = "There was a problem creating the signature.";
            throw new EyerisException(msg);
        }
        catch (InvalidKeyException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            String msg = "There was a problem creating the signature.";
            throw new EyerisException(msg);
        }
        catch (SignatureException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            String msg = "There was a problem creating the signature.";
            throw new EyerisException(msg);
        }
        return ret;
    }

    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    public String getSubject()
    {
        return subject;
    }

}
