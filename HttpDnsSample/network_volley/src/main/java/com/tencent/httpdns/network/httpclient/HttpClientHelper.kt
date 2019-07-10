@file:Suppress("DEPRECATION")

package com.tencent.httpdns.network.httpclient

import com.tencent.httpdns.base.ext.toUriFormat
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.base.DnsServiceWrapper
import org.apache.http.client.HttpClient
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.BasicHttpParams
import java.net.URL

internal object HttpClientHelper {

    private const val HTTP_PROTOCOL = "http"
    private const val HTTP_PORT = 80
    private const val HTTPS_PROTOCOL = "https"
    private const val HTTPS_PORT = 443

    private val layeredSocketFactory4Dns by lazy {
        LayeredSocketFactory4Dns.getSocketFactory()
    }

    val httpClient: HttpClient by lazy {
        val httpParams = BasicHttpParams()
        SchemeRegistry()
            .apply {
                register(Scheme(HTTP_PROTOCOL, PlainSocketFactory.getSocketFactory(), HTTP_PORT))
                register(
                    Scheme(
                        HTTPS_PROTOCOL,
                        if (DnsServiceWrapper.useHttpDns) layeredSocketFactory4Dns
                        else SSLSocketFactory.getSocketFactory(),
                        HTTPS_PORT
                    )
                )
            }
            .let { ThreadSafeClientConnManager(httpParams, it) }
            .let { DefaultHttpClient(it, httpParams) }
    }

    fun url2HostPair(urlStr: String): Url2HostPair {
        val url = URL(urlStr)
        val hostname = url.host
        val newUrl = if (DnsServiceWrapper.useHttpDns) {
            DnsLog.d("HttpClientHelper lookup for $hostname")
            val inetAddrs = DnsServiceWrapper.getAddrsByName(hostname)
            if (inetAddrs.isEmpty()) urlStr
            else {
                val inetAddr = inetAddrs[0]
                if (HTTPS_PROTOCOL == url.protocol) {
                    layeredSocketFactory4Dns.hostnameContainer.put(inetAddr.hostAddress, hostname)
                }
                urlStr.replace(hostname, inetAddr.toUriFormat())
            }
        } else urlStr
        return newUrl to hostname
    }
}

typealias Url2HostPair = Pair<String, String>
