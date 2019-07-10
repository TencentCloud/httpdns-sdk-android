package com.tencent.httpdns.network

import android.webkit.WebResourceResponse
import com.tencent.httpdns.network.base.DEFAULT_ENCODING
import com.tencent.httpdns.network.base.DEFAULT_MIME_TYPE
import com.tencent.httpdns.network.base.DnsServiceWrapper
import com.tencent.httpdns.base.ext.toUriFormat
import com.tencent.httpdns.base.ext.using
import com.tencent.httpdns.base.log.DnsLog
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

object WebsiteService {

    private const val GET_METHOD = "GET"
    private const val MAX_CONNECT_TIMES = 5
    private const val BUFFER_SIZE = 4 * 1024
    private const val CONTENT_TYPE = "Content-Type"
    private const val CHARSET_PARAM = "; charset="

    fun getContentAsync(url: String) = GlobalScope.async {
        val hostname = URL(url).host
        var urlConnection: HttpURLConnection? = null
        if (DnsServiceWrapper.useHttpDns) {
            // Step1: 域名解析
            DnsLog.d("WebsiteService lookup for $hostname")
            val inetAddrs = DnsServiceWrapper.getAddrsByName(hostname)
            if (inetAddrs.isEmpty()) {
                throw UnknownHostException("Cannot resolve $hostname")
            }
            if (!isActive) {
                throw CancellationException()
            }
            // Step2: 建立连接
            val maxConnectTimes = Math.min(MAX_CONNECT_TIMES, inetAddrs.size)
            var i = 0
            while (i < maxConnectTimes && isActive) {
                try {
                    urlConnection =
                        URL(url.replace(hostname, inetAddrs[i].toUriFormat())).openConnection() as HttpURLConnection
                    urlConnection.requestMethod = GET_METHOD
                    HttpUrlConnectionHelper.compat4ChangeHost(urlConnection, hostname)
                    urlConnection.connect()
                    break
                } catch (e: SocketTimeoutException) {
                    i++
                }
            }
        } else {
            try {
                urlConnection = URL(url).openConnection() as HttpURLConnection
                urlConnection.requestMethod = GET_METHOD
                urlConnection.connect()
            } catch (ignored: SocketTimeoutException) {
            }
        }
        if (null == urlConnection) {
            throw IOException("Cannot connect $hostname")
        }
        if (!isActive) {
            throw CancellationException()
        }
        // Step3: Request & Response
        val rspCode = urlConnection.responseCode
        if (HttpURLConnection.HTTP_OK != rspCode) {
            throw HttpException(rspCode)
        }
        if (!isActive) {
            throw CancellationException()
        }
        using {
            val inputStream = BufferedInputStream(urlConnection.inputStream).autoClose()
            val outputStream = ByteArrayOutputStream().autoClose()
            val buffer = ByteArray(BUFFER_SIZE)
            var readLen = inputStream.read(buffer)
            while (-1 != readLen) {
                outputStream.write(buffer, 0, readLen)
                readLen = inputStream.read(buffer)
            }
            String(outputStream.toByteArray())
        }
    }

    fun getResourceAsync(url: String) = GlobalScope.async {
        getResource(url)
    }

    fun getResource(url: String) =
        try {
            val hostname = URL(url).host
            var urlConnection: HttpURLConnection? = null
            if (DnsServiceWrapper.useHttpDns) {
                // Step1: 域名解析
                DnsLog.d("WebsiteService lookup for $hostname")
                val inetAddrs = DnsServiceWrapper.getAddrsByName(hostname)
                if (inetAddrs.isEmpty()) {
                    throw UnknownHostException("Cannot resolve $hostname")
                }
                // Step2: 建立连接
                val maxConnectTimes = Math.min(MAX_CONNECT_TIMES, inetAddrs.size)
                var i = 0
                while (i < maxConnectTimes) {
                    try {
                        urlConnection =
                            URL(url.replace(hostname, inetAddrs[i].toUriFormat())).openConnection() as HttpURLConnection
                        urlConnection.requestMethod = GET_METHOD
                        HttpUrlConnectionHelper.compat4ChangeHost(urlConnection, hostname)
                        urlConnection.connect()
                        break
                    } catch (e: SocketTimeoutException) {
                        i++
                    }
                }
            } else {
                try {
                    urlConnection = URL(url).openConnection() as HttpURLConnection
                    urlConnection.requestMethod = GET_METHOD
                    urlConnection.connect()
                } catch (ignored: SocketTimeoutException) {
                }
            }
            if (null == urlConnection) {
                throw IOException("Cannot connect $hostname")
            }
            // Step3: Request & Response
            val rspCode = urlConnection.responseCode
            if (HttpURLConnection.HTTP_OK != rspCode) {
                throw HttpException(rspCode)
            }
            val (mimeType, encoding) = getMimeTypeAndEncoding(urlConnection)
            WebResourceResponse(
                mimeType,
                encoding,
                urlConnection.inputStream
            )
        } catch (e: Exception) {
            null
        }

    private fun getMimeTypeAndEncoding(urlConnection: HttpURLConnection): MimeTypeAndEncoding {
        val contentType = urlConnection.getHeaderField(CONTENT_TYPE) ?: return DEFAULT_MIME_TYPE to DEFAULT_ENCODING
        DnsLog.d("$CONTENT_TYPE: $contentType")
        val contentTypes = contentType.split(CHARSET_PARAM)
        return when {
            2 <= contentTypes.size -> contentTypes[0] to contentTypes[1]
            contentTypes.isNotEmpty() -> contentTypes[0] to DEFAULT_ENCODING
            else -> contentType to DEFAULT_ENCODING
        }.also {
            DnsLog.d("MimeTypeAndEncoding: $it")
        }
    }
}

// see retrofit2.HttpException
class HttpException(rspCode: Int) : RuntimeException("HTTP $rspCode")

typealias MimeTypeAndEncoding = Pair<String, String>
