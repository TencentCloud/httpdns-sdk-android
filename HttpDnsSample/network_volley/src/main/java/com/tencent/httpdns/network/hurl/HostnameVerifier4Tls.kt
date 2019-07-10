package com.tencent.httpdns.network.hurl

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

internal class HostnameVerifier4Tls(private val hostname: String) : HostnameVerifier {

    private val realHostnameVerifier by lazy {
        HttpsURLConnection.getDefaultHostnameVerifier()
    }

    override fun verify(ip: String, session: SSLSession) =
        realHostnameVerifier.verify(hostname, session)
}
