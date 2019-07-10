package com.tencent.httpdns.network

import android.webkit.WebResourceResponse
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.base.DEFAULT_ENCODING
import com.tencent.httpdns.network.base.DEFAULT_MIME_TYPE
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.ByteArrayInputStream

object WebsiteService {

    private const val CONTENT_TYPE = "Content-Type"
    private const val CHARSET_PARAM = "; charset="

    fun getContentAsync(url: String) = GlobalScope.async {
        val requestFuture = RequestFuture.newFuture<String>()
        val request = StringRequest(url, requestFuture, requestFuture)
        VolleyHelper.requestQueue.add(request)
        requestFuture.get()
    }

    fun getResourceAsync(url: String) = GlobalScope.async {
        getResource(url)
    }

    fun getResource(url: String) =
        try {
            val requestFuture = RequestFuture.newFuture<NetworkResponse>()
            val request = RawRequest(url, requestFuture, requestFuture)
            VolleyHelper.requestQueue.add(request)
            val response = requestFuture.get()
            val (mimeType, encoding) = getMimeTypeAndEncoding(response)
            WebResourceResponse(
                mimeType,
                encoding,
                ByteArrayInputStream(response.data)
            )
        } catch (e: Exception) {
            null
        }

    private fun getMimeTypeAndEncoding(response: NetworkResponse): MimeTypeAndEncoding {
        val contentType = response.headers[CONTENT_TYPE] ?: return DEFAULT_MIME_TYPE to DEFAULT_ENCODING
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

internal class RawRequest(
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(Method.GET, url, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<NetworkResponse> =
        if (null != response) Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        else Response.error(VolleyError())

    override fun deliverResponse(response: NetworkResponse?) = listener.onResponse(response)
}

typealias MimeTypeAndEncoding = Pair<String, String>
