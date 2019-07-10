package com.tencent.httpdns.network

import android.webkit.WebResourceResponse
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.base.DEFAULT_ENCODING
import com.tencent.httpdns.network.base.DEFAULT_MIME_TYPE
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object WebsiteService {

    private const val CONTENT_TYPE = "Content-Type"
    private const val CHARSET_PARAM = "; charset="

    fun getContentAsync(url: String): Deferred<String> {
        val deferred = CompletableDeferred<String>()

        val call =
            Request
                .Builder()
                .get()
                .url(url)
                .build()
                .let { OkHttpHelper.okHttpClient.newCall(it) }

        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }
        call.enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                deferred.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    deferred.complete(response.body()!!.string())
                } else {
                    deferred.completeExceptionally(HttpException(response))
                }
            }
        })

        return deferred
    }

    fun getResourceAsync(url: String) = GlobalScope.async {
        getResource(url)
    }

    fun getResource(url: String) =
        try {
            val rsp = Request
                .Builder()
                .get()
                .url(url)
                .build()
                .let { OkHttpHelper.okHttpClient.newCall(it) }
                .execute()
            val rspBody = rsp.body() ?: throw IOException("Cannot get body")
            val (mimeType, encoding) = getMimeTypeAndEncoding(rsp)
            WebResourceResponse(
                mimeType,
                encoding,
                rspBody.byteStream()
            )
        } catch (e: Exception) {
            null
        }

    private fun getMimeTypeAndEncoding(rsp: Response): MimeTypeAndEncoding {
        val contentType = rsp.header(CONTENT_TYPE) ?: return DEFAULT_MIME_TYPE to DEFAULT_ENCODING
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
class HttpException(response: Response) : RuntimeException("HTTP ${response.code()} ${response.message()}")

typealias MimeTypeAndEncoding = Pair<String, String>
