# WebView请求使用HTTPDNS

[TOC]

以下代码片段摘自SDK使用Sample（HttpDnsSample目录），完整代码请参考使用Sample

WebView的网络访问请求，默认不会通过原生网络库发起，我们可以尝试拦截WebView的相关网络请求，通过原生网络库发起请求，原生网络库如何接入HTTPDNS SDK请参考对应的接入文档（当前目录下），本文档示例代码仅演示如何拦截WebView网络请求

WebView涉及的网络访问请求大概可以分为两种：

- 客户端传入的URL
- 客户端传入的URL解析得到的HTML上的链接（通常是指向CSS，图片等资源文件）

## 客户端传入的URL

通常，我们可以通过WebView的[loadUrl(String url)](https://developer.android.google.cn/reference/android/webkit/WebView#loadUrl(java.lang.String))方法来加载一个URL，我们也可以通过WebView的[loadData(String data, String mimeType, String encoding)](https://developer.android.google.cn/reference/android/webkit/WebView.html#loadData(java.lang.String,%20java.lang.String,%20java.lang.String))及[loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl)](https://developer.android.google.cn/reference/android/webkit/WebView.html#loadDataWithBaseURL(java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String))方法，直接将解析得到的HTML交给WebView去渲染

示例代码如下：

```kotlin
// WebsiteFragment.kt
class WebsiteFragment : Fragment() {
    
    // ...
    
	override fun onStart() {
        super.onStart()

        DnsLog.d("$this onStart()")

        WebViewHelper.initWebView(webView)
        websiteContent
            .observe(this@WebsiteFragment, Observer { website ->
                DnsLog.d("Loaded data from ${website.url}")
                webView.loadDataWithBaseURL(
                    website.url,
                    website.content,
                    HTML_MIME_TYPE,
                    DEFAULT_ENCODING,
                    null
                )
            })
    }
    
    // ...
    
}
```

## HTML上的链接

对于HTML上的链接，我们可以通过WebViewClient的[shouldInterceptRequest(view: WebView!, url: String!)](https://developer.android.google.cn/reference/kotlin/android/webkit/WebViewClient?hl=en#shouldInterceptRequest(android.webkit.WebView,%20kotlin.String))和[shouldInterceptRequest(view: WebView!, request: WebResourceRequest!)](https://developer.android.google.cn/reference/kotlin/android/webkit/WebViewClient?hl=en#shouldInterceptRequest(android.webkit.WebView,%20android.webkit.WebResourceRequest))方法来拦截请求

示例代码如下：

```kotlin
// WebViewHelper.kt
object WebViewHelper {

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

                // ...
                
            }
        }
}
```

