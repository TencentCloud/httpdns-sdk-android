package com.tencent.httpdns.network

import android.webkit.WebResourceResponse
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.base.DEFAULT_ENCODING
import com.tencent.httpdns.network.base.DEFAULT_MIME_TYPE
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.IOException

interface WebsiteService {

    @GET
    fun getContentAsync(@Url url: String): Deferred<String>

    @GET
    fun getResources(@Url url: String): Call<ResponseBody>

    companion object {

        private const val CONTENT_TYPE = "Content-Type"
        private const val CHARSET_PARAM = "; charset="

        private val websiteService by lazy {
            RetrofitHelper.retrofit.create(WebsiteService::class.java)
        }

        fun getContentAsync(url: String) = websiteService.getContentAsync(url)

        fun getResourceAsync(url: String) = GlobalScope.async {
            getResource(url)
        }

        fun getResource(url: String) =
            try {
                val rsp = websiteService.getResources(url).execute()
                val rawRsp = rsp.raw()
                val rspBody = rsp.body() ?: throw IOException("Cannot get body")
                val (mimeType, encoding) = getMimeTypeAndEncoding(rawRsp)
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
}

typealias MimeTypeAndEncoding = Pair<String, String>
