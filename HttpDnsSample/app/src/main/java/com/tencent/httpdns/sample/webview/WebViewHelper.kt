package com.tencent.httpdns.sample.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.webkit.*
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.sample.data.WebsiteRepository

object WebViewHelper {

    private const val GET_METHOD = "GET"
    private const val HTTP_PROTOCOL = "Http"

    fun initWebView(webView: WebView) {
        setDefaultWebSettings(webView)
        setDefaultWebViewClient(webView)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setDefaultWebSettings(webView: WebView) =
        with(webView.settings) {
            // 5.0以上开启混合模式加载
            if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            loadWithOverviewMode = true
            useWideViewPort = false
            // 允许js代码
            javaScriptEnabled = true
            // 禁用放缩
            displayZoomControls = false
            builtInZoomControls = false
            // 禁用文字缩放
            textZoom = 100
        }

    fun setDefaultWebViewClient(webView: WebView) =
        with(webView) {
            webViewClient = object : WebViewClient() {

                override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
                    DnsLog.d("WebViewClient try intercept $url old")
                    return if (!url.startsWith(HTTP_PROTOCOL, true)) null
                    else WebsiteRepository.getResource(url)
                }


                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        DnsLog.d("WebViewClient try intercept ${request.url}")
                        if (GET_METHOD != request.method) null
                        else shouldInterceptRequest(view, request.url.toString())
                    } else null

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    DnsLog.d("WebViewClient load $url started")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    DnsLog.d("WebViewClient load $url finished")
                }
            }
        }
}
