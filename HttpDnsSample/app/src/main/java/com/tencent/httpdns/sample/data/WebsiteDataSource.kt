package com.tencent.httpdns.sample.data

import android.webkit.WebResourceResponse

interface WebsiteDataSource {

    operator fun get(url: String): String?

    operator fun set(url: String, content: String)

    fun getResource(url: String): WebResourceResponse?

    fun setResource(url: String, webResourceResponse: WebResourceResponse)
}
