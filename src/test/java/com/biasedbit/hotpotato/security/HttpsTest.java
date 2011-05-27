package com.biasedbit.hotpotato.client.security;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.biasedbit.hotpotato.security.SSLContextFactory;
import com.biasedbit.hotpotato.security.DefaultSSLContextFactory;
import com.biasedbit.hotpotato.response.BodyAsStringProcessor;
import com.biasedbit.hotpotato.request.HttpRequestFuture;
import com.biasedbit.hotpotato.client.DefaultHttpClient;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.HttpMethod;

/**
 *
 */
public class HttpsTest {
    @Test
    public void testHttpsRequestSuccess() throws Exception
    {
        executeHttpsRequest("encrypted.google.com", "/", loadGoogleCaCert(), false);
    }

    @Test
    public void testHttpsRequestFailure() throws Exception
    {
        // This should fail since we don't have a cert for wellsfargo in our test trustStore...
        executeHttpsRequest("www.wellsfargo.com", "/", loadGoogleCaCert(), true);
    }

    private static SSLContextFactory loadGoogleCaCert() throws Exception
    {
        return new DefaultSSLContextFactory.Builder()
                                        // A keystore containing one cert, trusting encrypted.google.com
            .setKey(new FileInputStream("src/test/resources/certs/encrypted.google.com.jks"))
            .setKeyStore(KeyStore.getInstance("JKS"))
            .setCertificatePassword("ez24get")
            .setKeyStorePassword("ez24get")
            .build();
    }

    private static void executeHttpsRequest(final String host, final String resource, final SSLContextFactory ssl, final boolean shouldFail)
    throws Exception
    {
        DefaultHttpClient client = new DefaultHttpClient();
        client.setSSLContextFactory(ssl);
        client.setUseSsl(true);
        client.init();

        DefaultHttpRequest request =
            new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, resource);

        HttpRequestFuture future =
            client.execute(host, 443, request, new BodyAsStringProcessor()).await();

        if (shouldFail)
        {
            assertThat(future.getResponseStatusCode(), is(not(200)));
        }
        else
        {
            assertEquals(200, future.getResponseStatusCode());
        }

        assertTrue(true);
    }
}

