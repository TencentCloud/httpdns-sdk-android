package com.tencent.msdk.dns;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
// API 21后引入的类
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * HTML5 HTTPDNS解析Demo
 * 
 */
public class WebViewDemo extends Activity {
	private WebView mWebView;

	private String targetUrl = "http://guang.m.yohobuy.com/info/index?id=50541";

	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webshow);
		mWebView = (WebView) findViewById(R.id.webView);

		WebSettings webSettings = mWebView.getSettings();
		// 使用默认的缓存策略，cache没有过期就用cache
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		// 加载网页图片资源
		webSettings.setBlockNetworkImage(false);
		// 支持JavaScript脚本
		webSettings.setJavaScriptEnabled(true);
		// 支持缩放
		webSettings.setSupportZoom(true);

		mWebView.setWebViewClient(new WebViewClient() {

			// API 21及之后使用此方法
			@SuppressLint("NewApi")
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				if (request != null && request.getUrl() != null && request.getMethod().equalsIgnoreCase("get")) {
					String scheme = request.getUrl().getScheme().trim();
					String url = request.getUrl().toString();
					Logger.d("url a: " + url);
					// HttpDns解析css文件的网络请求及图片请求
					if ((scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
							&& (url.contains(".css") || url.endsWith(".png") || url.endsWith(".jpg") || url
									.endsWith(".jif"))) {
						try {
							URL oldUrl = new URL(url);
							URLConnection connection = oldUrl.openConnection();
							// 获取HttpDns域名解析结果
							String ips = MSDKDnsResolver.getInstance().getAddrByName(oldUrl.getHost());
							if (ips != null) { // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置
								Logger.d("HttpDns ips are: " + ips + " for host: " + oldUrl.getHost());
								String ip;
								if (ips.contains(";")) {
									ip = ips.substring(0, ips.indexOf(";"));
								} else {
									ip = ips;
								}
								String newUrl = url.replaceFirst(oldUrl.getHost(), ip);
								Logger.d("newUrl a is: " + newUrl);
								connection = (HttpURLConnection) new URL(newUrl).openConnection(); // 设置HTTP请求头Host域
								connection.setRequestProperty("Host", oldUrl.getHost());
							}
							Logger.d("ContentType a: " + connection.getContentType());
							return new WebResourceResponse("text/css", "UTF-8", connection.getInputStream());
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				return null;
			}

			// API 11至API20使用此方法
			public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
				if (!TextUtils.isEmpty(url) && Uri.parse(url).getScheme() != null) {
					String scheme = Uri.parse(url).getScheme().trim();
					Logger.d("url b: " + url);
					// HttpDns解析css文件的网络请求及图片请求
					if ((scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
							&& (url.contains(".css") || url.endsWith(".png") || url.endsWith(".jpg") || url
									.endsWith(".jif"))) {
						try {
							URL oldUrl = new URL(url);
							URLConnection connection = oldUrl.openConnection();
							// 获取HttpDns域名解析结果
							String ips = MSDKDnsResolver.getInstance().getAddrByName(oldUrl.getHost());
							if (ips != null) {
								// 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置
								Logger.d("HttpDns ips are: " + ips + " for host: " + oldUrl.getHost());
								String ip;
								if (ips.contains(";")) {
									ip = ips.substring(0, ips.indexOf(";"));
								} else {
									ip = ips;
								}
								String newUrl = url.replaceFirst(oldUrl.getHost(), ip);
								Logger.d("newUrl b is: " + newUrl);
								connection = (HttpURLConnection) new URL(newUrl).openConnection();
								// 设置HTTP请求头Host域
								connection.setRequestProperty("Host", oldUrl.getHost());
							}
							Logger.d("ContentType b: " + connection.getContentType());
							return new WebResourceResponse("text/css", "UTF-8", connection.getInputStream());
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				return null;
			}
		});

		// 加载web资源
		mWebView.loadUrl(targetUrl);
	}
}
