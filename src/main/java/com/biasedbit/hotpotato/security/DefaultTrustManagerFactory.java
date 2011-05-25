package com.biasedbit.hotpotato.security;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.InvalidAlgorithmParameterException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;

public class DefaultTrustManagerFactory extends TrustManagerFactorySpi
{
    public DefaultTrustManagerFactory()
    {
        super();
    }

    @Override
    protected void engineInit(KeyStore ks) throws KeyStoreException
    {

    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException
    {

    }

    @Override
    protected TrustManager[] engineGetTrustManagers()
    {
        return null;
    }
}

