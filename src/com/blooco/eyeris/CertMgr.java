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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class CertMgr
{
    static private String TAG = "CertMgr";
    private String commonName = "default";
    private String orgUnit = "default";
    private String org = "default";
    private String locality = "default";
    private String state = "default";
    private String country = "default";
    private long validity = 365;
    private int keySize = 1024;
    private String alias = "user@email.com";
    private String password = "password";
    private String algoType = "RSA";
    private String sigAlgoType = "SHA1WithRSA";
    private byte[] algoTag = Asn1Protocol.sha1RSA;
    
    // This allows you to easily override default RSA algorithm
    public void setToDSA()
    {
        algoType = "DSA";
        sigAlgoType = "SHA1WithDSA";
        algoTag = Asn1Protocol.sha1DSA;
    }

    /** Creates a new key pair, stores the new cert in the provided keystore,
     * and returns the created cert.
     * 
     * @param keystore
     * @return
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws KeyStoreException
     * @throws IOException
     */
    public Certificate create(KeyStore keystore) throws CertificateException,
            NoSuchAlgorithmException, InvalidKeyException, SignatureException, KeyStoreException,
            IOException
    {
        Log.i(TAG, "Creating new self-signed certificate of algorithm type " + algoType);
        Certificate ret = null;
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algoType);
        Log.i(TAG, "Key size is " + keySize);
        keyGen.initialize(keySize);
        KeyPair keyPair = keyGen.genKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        Asn1Protocol[] asn1Cert = createAsn1Cert(publicKey);
        byte[] r = new byte[0];
        r = Asn1Protocol.asn1_add(r, Asn1Protocol.SEQUENCE, asn1Cert);

        Signature sign = Signature.getInstance(sigAlgoType);
        sign.initSign(privateKey);
        sign.update(r);

        r = Asn1Protocol.asn1_add(new byte[0], Asn1Protocol.SEQUENCE, new Asn1Protocol[]
        {
                new Asn1Protocol(Asn1Protocol.RAW_DATA, r),
                new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                {
                        new Asn1Protocol(Asn1Protocol.OBJECT_ID, algoTag),
                        new Asn1Protocol(Asn1Protocol.NULL, new byte[0])
                }), new Asn1Protocol(Asn1Protocol.BIT_STRING, new Asn1Protocol[]
                {
                    new Asn1Protocol(Asn1Protocol.INTEGER, sign.sign())
                })
        });

        CertificateFactory cf = CertificateFactory.getInstance("x509");
        ret = cf.generateCertificate(new ByteArrayInputStream(r));

        Certificate[] chain =
        {
            ret
        };
        PrivateKeyEntry entry = new PrivateKeyEntry(privateKey, chain);

        PasswordProtection pwp = new PasswordProtection(password.toCharArray());

        keystore.setEntry(alias, entry, pwp);
 
        return ret;
    }

    private Asn1Protocol[] createAsn1Cert(PublicKey publicKey)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddhhmmss'Z'");
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime() + (validity * 24 * 3600 * 1000));
        
        Log.i(TAG, "New certificate expiration: " + endDate);
        Log.i(TAG, "New certificate alias: " + alias);
        Log.i(TAG, "New certificate common name: " + commonName);

        Asn1Protocol[] ret = new Asn1Protocol[]
        {
                new Asn1Protocol(Asn1Protocol.OPTIONAL, new Asn1Protocol[]
                {
                    new Asn1Protocol(Asn1Protocol.INTEGER, new byte[]
                    {
                        (byte) 0x02
                    })
                }),
                new Asn1Protocol(Asn1Protocol.INTEGER, new byte[]
                {
                    (byte) 0x01
                }),
                new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                {
                        new Asn1Protocol(Asn1Protocol.OBJECT_ID, algoTag),
                        new Asn1Protocol(Asn1Protocol.NULL, new byte[0])
                }),
                new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                {
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.commonName),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, commonName.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.organizationUnit),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, orgUnit.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.organization),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, org.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.locality),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, locality.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.state),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, state.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.country),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, country.getBytes())
                            })
                        })
                }),
                new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                {
                        new Asn1Protocol(Asn1Protocol.UTC_TIME, formatter.format(beginDate).getBytes()),
                        new Asn1Protocol(Asn1Protocol.UTC_TIME, formatter.format(endDate).getBytes())
                }),
                new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                {
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.commonName),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, commonName.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.organizationUnit),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, orgUnit.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.organization),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, org.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.locality),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, locality.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.state),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, state.getBytes())
                            })
                        }),
                        new Asn1Protocol(Asn1Protocol.SET, new Asn1Protocol[]
                        {
                            new Asn1Protocol(Asn1Protocol.SEQUENCE, new Asn1Protocol[]
                            {
                                    new Asn1Protocol(Asn1Protocol.OBJECT_ID, Asn1Protocol.country),
                                    new Asn1Protocol(Asn1Protocol.PRINTABLE_STRING, country.getBytes())
                            })
                        })
                }), new Asn1Protocol(Asn1Protocol.RAW_DATA, publicKey.getEncoded())
        };

        return ret;
    }
    
    public void setAlias(String alias)
    {
        Log.i(TAG, "Setting alias: " + alias);
        this.alias = alias;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public void setCommonName(String commonName)
    {
        Log.i(TAG, "Setting common name: " + commonName);
        this.commonName = commonName;
    }
    
    public void setOrgUnit(String orgUnit)
    {
        Log.i(TAG, "Setting org unit: " + orgUnit);
        this.orgUnit = orgUnit;
    }

    public void setOrg(String org)
    {
        Log.i(TAG, "Setting org: " + org);
        this.org = org;
    }

    public void setLocality(String locality)
    {
        Log.i(TAG, "Setting locality: " + locality);
        this.locality = locality;
    }

    public void setState(String state)
    {
        Log.i(TAG, "Setting state: " + state);
        this.state = state;
    }

    public void setCountry(String country)
    {
        Log.i(TAG, "Setting country: " + country);
        this.country = country;
    }

    public void setValidity(long validity)
    {
        Log.i(TAG, "Setting validity: " + validity);
        this.validity = validity;
    }

    public void setKeySize(int keySize)
    {
        Log.i(TAG, "Setting key size: " + keySize);
        this.keySize = keySize;
    }

    
}
