package com.tencent.httpdns.network.hurl

import com.tencent.httpdns.base.compat.CollectionCompat
import com.tencent.httpdns.network.SSLSocketFactory4SniHack
import java.net.HttpURLConnection
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

internal object HttpUrlConnectionHelper {

    private const val HOST_HEADER_KEY = "Host"

    private val hostname2VerifierMap by lazy {
        Collections.synchronizedMap(CollectionCompat.createMap<String, HostnameVerifier>())
    }
    private val hostname2SSLSocketFactoryMap by lazy {
        Collections.synchronizedMap(CollectionCompat.createMap<String, SSLSocketFactory>())
    }

    fun compat4ChangeHost(urlConnection: HttpURLConnection, hostname: String) {
        urlConnection.setRequestProperty(HOST_HEADER_KEY, hostname)
        if (urlConnection is HttpsURLConnection) {
            urlConnection.hostnameVerifier =
                getHostnameVerifier(hostname)
            urlConnection.sslSocketFactory =
                getSSLSocketFactory(hostname)
        }
    }

    private fun getHostnameVerifier(hostname: String): HostnameVerifier =
        hostname2VerifierMap[hostname] ?: HostnameVerifier4Tls(hostname).also { hostname2VerifierMap[hostname] = it }

    private fun getSSLSocketFactory(hostname: String): SSLSocketFactory =
        hostname2SSLSocketFactoryMap[hostname]
            ?: SSLSocketFactory4SniHack(hostname).also { hostname2SSLSocketFactoryMap[hostname] }
}
