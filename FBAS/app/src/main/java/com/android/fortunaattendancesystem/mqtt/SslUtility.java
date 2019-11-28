package com.android.fortunaattendancesystem.mqtt;

/**
 * Created by fortuna on 7/9/18.
 */


import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SslUtility {

    private static SslUtility mInstance = null;
    private Context mContext = null;
    private HashMap<Integer, SSLSocketFactory> mSocketFactoryMap = new HashMap<Integer, SSLSocketFactory>();

    public SslUtility(Context context) {
        mContext = context;
    }

    public static SslUtility getInstance() {
        if (null == mInstance) {
            throw new RuntimeException("first call must be to SslUtility.newInstance(Context) ");
        }
        return mInstance;
    }

    public static SslUtility newInstance(Context context) {
        if (null == mInstance) {
            mInstance = new SslUtility(context);
        }
        return mInstance;
    }

    public SSLSocketFactory getSocketFactory(int certificateId, String certificatePassword) {

        SSLSocketFactory result = mSocketFactoryMap.get(certificateId);    // check to see if already created

        if ((null == result) && (null != mContext)) {                    // not cached so need to load server certificate

            try {
                KeyStore keystoreTrust = KeyStore.getInstance("BKS");        // Bouncy Castle
                keystoreTrust.load(mContext.getResources().openRawResource(certificateId),
                        certificatePassword.toCharArray());
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keystoreTrust);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                result = sslContext.getSocketFactory();
                mSocketFactoryMap.put(certificateId, result);    // cache for reuse
            } catch (Exception ex) {
                Log.d("TEST","Exception:"+ex.getMessage());
            }
        }
        return result;
    }

    public SSLSocketFactory getSSLSocketFactory (InputStream keyStore, String password) throws MqttSecurityException {
        try{

            SSLContext ctx = null;
            SSLSocketFactory sslSockFactory=null;

            KeyStore ks;
            ks = KeyStore.getInstance("PKCS12");
            ks.load(keyStore, password.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ks);
            TrustManager[] tm = tmf.getTrustManagers();
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tm, null);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                sslSockFactory = new TLSSocketFactory(tm);
            } else {
                sslSockFactory = ctx.getSocketFactory();
            }
            return sslSockFactory;

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new MqttSecurityException(e);
        }
    }


}


//    static SSLSocketFactory getSocketFactory (final String caCrtFile, final String crtFile, final String keyFile,
//                                              final String password) throws Exception
//    {
//        Security.addProvider(new BouncyCastleProvider());
//
//        // load CA certificate
//        PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
//        X509Certificate caCert = (X509Certificate)reader.readObject();
//        reader.close();
//
//        // load client certificate
//        reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(crtFile)))));
//        X509Certificate cert = (X509Certificate)reader.readObject();
//        reader.close();
//
//        // load client private key
//        reader = new PEMReader(
//                new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(keyFile)))),
//                new PasswordFinder() {
//                    @Override
//                    public char[] getPassword() {
//                        return password.toCharArray();
//                    }
//                }
//        );
//        KeyPair key = (KeyPair)reader.readObject();
//        reader.close();
//
//        // CA certificate is used to authenticate server
//        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
//        caKs.load(null, null);
//        caKs.setCertificateEntry("ca-certificate", caCert);
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//        tmf.init(caKs);
//
//        // client key and certificates are sent to server so it can authenticate us
//        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//        ks.load(null, null);
//        ks.setCertificateEntry("certificate", cert);
//        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
//        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        kmf.init(ks, password.toCharArray());
//
//        // finally, create SSL socket factory
//        SSLContext context = SSLContext.getInstance("TLSv1");
//        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//
//        return context.getSocketFactory();
//    }


