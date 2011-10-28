/*
 * Copyright (C) 2010 keystoremanager authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blooco.eyeris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import android.os.Environment;
import android.util.Log;

/**
 * This class abstracts the file-level management of a keystore.
 *
 */
public class KeyStoreMgr
{
    private static String TAG = "KeyStore";
    private KeyStore keystore = null;
    private String password = "password";
    private File keystoreDirectory = Environment.getExternalStorageDirectory();
    private String keystoreFilename = "default.jks";
    
    public KeyStore get() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException
    {
        if (null == keystore)
        {
            keystore = load();
        }
        return keystore;
    }
    
    public boolean keystoreFileExists()
    {
        File file = new File(keystoreDirectory, keystoreFilename);
        return file.exists();
    }

    private KeyStore load() throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException
    {

        KeyStore ret = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fis = null;
        try
        {
            File file = new File(keystoreDirectory, keystoreFilename);
            fis = new FileInputStream(file);
            ret.load(fis, password.toCharArray());
        }
        catch (FileNotFoundException fnfe)
        {
            Log.i(TAG, "No keystore file found. Creating new keystore");
            ret.load(null, password.toCharArray());
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
        }

        return ret;
    }

    public void save() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException
    {
        if (null != keystore)
        {
            FileOutputStream fos = null;
            try
            {
                File file = new File(keystoreDirectory, keystoreFilename);
                fos = new FileOutputStream(file);
                keystore.store(fos, password.toCharArray());
            }
            catch (FileNotFoundException e)
            {
                Log.e(TAG, "Unable to open keystore file for writing");
                throw e;
            }
            finally
            {
                if (fos != null)
                {
                    fos.close();
                }
            }
        }
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setKeystoreDirectory(File keystoreDirectory)
    {
        this.keystoreDirectory = keystoreDirectory;
    }

    public void setKeystoreFilename(String keystoreFilename)
    {
        this.keystoreFilename = keystoreFilename;
    }



}
