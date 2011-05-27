package com.biasedbit.hotpotato.security;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.SecureRandom;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 *
 */
public class DefaultSSLContextFactory implements SSLContextFactory
{
    private final SSLContext serverContext;
    private final SSLContext clientContext;

    private DefaultSSLContextFactory(
        final SSLContext serverContext, final SSLContext clientContext)
    {
        this.serverContext = serverContext;
        this.clientContext = clientContext;
    }

    @Override
    public SSLContext getClientContext()
    {
        return clientContext;
    }

    @Override
    public SSLContext getServerContext()
    {
        return serverContext;
    }

    /**
     *
     */
    public static class Builder
    {
        private InputStream keyAsInputStream = null;
        private String algorithm = null;
        private String protocol = null;
        private KeyStore store = null;

        private String keyStorePassword = null;
        private String certificatePassword = null;

        public Builder() {}

        public Builder setAlgorithm(final String algorithm)
        {
            this.algorithm = algorithm;
            return this;
        }

        public Builder setProtocol(final String protocol)
        {
            this.protocol = protocol;
            return this;
        }

        public Builder setKey(final InputStream key)
        {
            if (null != key)
            {
                keyAsInputStream = key;
            }
            else
            {
                setKey((byte[]) null);
            }
            return this;
        }

        public Builder setKey(final byte[] key)
        {
            keyAsInputStream = new ByteArrayInputStream((null != key) ? key : new byte[] {});
            return this;
        }

        public Builder setKeyStorePassword(final String keyStorePassword)
        {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        public Builder setCertificatePassword(final String certificatePassword)
        {
            this.certificatePassword = certificatePassword;
            return this;
        }

        public Builder setKeyStore(final KeyStore store)
        {
            this.store = store;
            return this;
        }

        public DefaultSSLContextFactory build() throws Exception
        {
            // Falling Back to defaults if user code has called build() without
            // fully populating this builder.
            // We may want to get this stuff from conf since these defaults will
            // not work on all platforms.
            if (null == algorithm) {
                algorithm = "SunX509";
            }
            if (null == protocol)
            {
                protocol = "TLSv1";
            }
            if (null == store)
            {
                store = KeyStore.getInstance("JKS");
            }

            // Load our keystore from disk..
            store.load(
                keyAsInputStream,
                (null == keyStorePassword) ? null : keyStorePassword.toCharArray()
            );

            KeyManagerFactory keyMgrFactory = KeyManagerFactory.getInstance(algorithm);
            keyMgrFactory.init(
                store,
                (null == certificatePassword) ? null : certificatePassword.toCharArray()
            );

            TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(algorithm);
            trustMgrFactory.init(store);

            SSLContext serverContext = SSLContext.getInstance(protocol);
            SSLContext clientContext = SSLContext.getInstance(protocol);

            serverContext.init(
                keyMgrFactory.getKeyManagers(),
                trustMgrFactory.getTrustManagers(),
                new SecureRandom()
            );

            clientContext.init(
                keyMgrFactory.getKeyManagers(),
                trustMgrFactory.getTrustManagers(),
                new SecureRandom()
            );

            return new DefaultSSLContextFactory(serverContext, clientContext);
        }
    }
}

