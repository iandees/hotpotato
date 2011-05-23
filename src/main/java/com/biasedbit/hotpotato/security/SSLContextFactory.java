package com.biasedbit.hotpotato.security;

import javax.net.ssl.SSLContext;

public interface SSLContextFactory
{
    public SSLContext getClientContext();
    public SSLContext getServerContext();
}

