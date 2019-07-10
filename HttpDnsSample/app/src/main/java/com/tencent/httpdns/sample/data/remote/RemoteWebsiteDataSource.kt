package com.tencent.httpdns.sample.data.remote

import android.webkit.WebResourceResponse
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.WebsiteService
import com.tencent.httpdns.sample.data.WebsiteDataSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.lang.UnsupportedOperationException

internal class RemoteWebsiteDataSource : WebsiteDataSource {

    override fun get(url: String) =
        runBlocking {
            try {
                coroutineScope {
                    WebsiteService.getContentAsync(url).await()
                }
            } catch (e: Exception) {
                DnsLog.w(e, "Load content from RemoteWebsiteDataSource[$url] failed")
                null
            }
        }

    override fun set(url: String, content: String) {
        throw UnsupportedOperationException()
    }

    override fun getResource(url: String) =
        runBlocking {
            try {
                coroutineScope {
                    WebsiteService.getResourceAsync(url).await()
                }
            } catch (e: Exception) {
                DnsLog.w(e, "Load resource from RemoteWebsiteDataSource[$url] failed")
                null
            }
        }

    override fun setResource(url: String, webResourceResponse: WebResourceResponse) {
        throw UnsupportedOperationException()
    }
}
