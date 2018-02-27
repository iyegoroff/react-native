/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.modules.network;

import android.os.Build;

import com.facebook.common.logging.FLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

/**
 * Helper class that provides the same OkHttpClient instance that will be used for all networking
 * requests.
 */
public class OkHttpClientProvider {

  // Centralized OkHttpClient for all networking requests.
  private static @Nullable OkHttpClient sClient;

  public static OkHttpClient getOkHttpClient() {
    if (sClient == null) {
      sClient = createClient();
    }
    return sClient;
  }
  
  // okhttp3 OkHttpClient is immutable
  // This allows app to init an OkHttpClient with custom settings.
  public static void replaceOkHttpClient(OkHttpClient client) {
    sClient = client;
  }

  public static OkHttpClient createClient() {
    TrustManager[] trustAllCerts = new TrustManager[] {
      new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return new java.security.cert.X509Certificate[]{};
        }
      }
    };

    // No timeouts by default
    OkHttpClient.Builder client = new OkHttpClient.Builder()
      .connectTimeout(0, TimeUnit.MILLISECONDS)
      .readTimeout(0, TimeUnit.MILLISECONDS)
      .writeTimeout(0, TimeUnit.MILLISECONDS)
      .cookieJar(new ReactCookieJarContainer())
      .hostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });

    return enableTls12(client, trustAllCerts).build();
  }

  /*
    On Android 4.1-4.4 (API level 16 to 19) TLS 1.1 and 1.2 are
    available but not enabled by default. The following method
    enables it.
   */
  public static OkHttpClient.Builder enableTls12(OkHttpClient.Builder client, TrustManager[] trustManager) {
    try {    
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
          client.sslSocketFactory(new TLSSocketFactory(trustManager), (X509TrustManager)trustManager[0]);

          ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                  .tlsVersions(TlsVersion.TLS_1_2)
                  .build();

          List<ConnectionSpec> specs = new ArrayList<>();
          specs.add(cs);
          specs.add(ConnectionSpec.COMPATIBLE_TLS);
          specs.add(ConnectionSpec.CLEARTEXT);

          client.connectionSpecs(specs);
        
      } else {
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManager, new java.security.SecureRandom());
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        client.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustManager[0]);
      }
    } catch (Exception exc) {
      FLog.e("OkHttpClientProvider", "Error while enabling TLS 1.2", exc);
    }

    return client;
  }
}
