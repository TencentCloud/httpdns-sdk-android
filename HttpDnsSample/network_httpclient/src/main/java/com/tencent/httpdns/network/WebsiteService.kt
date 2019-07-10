package com.tencent.httpdns.network

import android.webkit.WebResourceResponse
import com.tencent.httpdns.base.ext.using
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.base.DEFAULT_ENCODING
import com.tencent.httpdns.network.base.DEFAULT_MIME_TYPE
import kotlinx.coroutines.*
import org.apache.http.HttpResponse
import org.apache.http.protocol.HTTP
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection

object WebsiteService {

    private const val BUFFER_SIZE = 4 * 1024

    fun getContentAsync(url: String) = GlobalScope.async {
        val rsp = url2HttpRsp(url)
        if (!isActive) {
            throw CancellationException()
        }
        checkRsp(rsp)
        val rspBody = rsp.entity!!
        using {
            val inputStream = BufferedInputStream(rspBody.content).autoClose()
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
            val rsp = url2HttpRsp(url).also { checkRsp(it) }
            val (mimeType, encoding) = getMimeTypeAndEncoding(rsp)
            WebResourceResponse(mimeType, encoding, rsp.entity.content)
        } catch (e: Exception) {
            null
        }

    private fun url2HttpRsp(url: String) =
        HttpClientHelper
            .url2HttpGet(url)
            .let { HttpClientHelper.httpClient.execute(it) }

    @Suppress("DEPRECATION")
    private fun checkRsp(rsp: HttpResponse) {
        val rspCode = rsp.statusLine.statusCode
        if (HttpURLConnection.HTTP_OK != rspCode) {
            throw HttpException(rspCode)
        }
        if (null == rsp.entity) {
            throw IOException("Cannot get body")
        }
    }

    @Suppress("DEPRECATION")
    private fun getMimeTypeAndEncoding(rsp: HttpResponse): MimeTypeAndEncoding {
        val contentType = rsp.getLastHeader(HTTP.CONTENT_TYPE)?.value ?: return DEFAULT_MIME_TYPE to DEFAULT_ENCODING
        DnsLog.d("${HTTP.CONTENT_TYPE}: $contentType")
        val contentTypes = contentType.split(HTTP.CHARSET_PARAM)
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
