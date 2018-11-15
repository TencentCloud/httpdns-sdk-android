# HttpDns(Android)接入说明文档
## GitHub目录结构说明
| 目录名称       | 说明           | 适用范围  |
| ------------- |-------------| -------------|
| HttpDnsLibs | HttpDns Android SDK库目录 | 所有业务 |
| HttpDns Android客户端接入文档（腾讯内部业务专用）.pdf | HttpDns Android客户端接入文档（腾讯内部业务专用） | 腾讯内部业务 |
| HttpDns Android客户端接入文档（腾讯云客户专用）.pdf | HttpDns Android客户端接入文档（腾讯云客户专用） | 腾讯云客户 |
| README.md | HTTPDNS Android客户端接入文档 | 腾讯云客户 |
| VERSION.md | HTTPDNS Android SDK历史版本修改记录 | SDK开发维护人员 |
| 数据报表申请方法.pdf | 数据报表申请方法 | SDK开发维护人员 |
## 功能介绍
HttpDns是为了有效的避免由于运营商传统LocalDns解析导致的无法访问最佳接入点而提出的解决方案。原理为使用Http加密协议替代传统的DNS协议，整个过程不使用域名，大大减少劫持的可能性。
## 接入
### AndroidMainfest配置
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>

<!-- 灯塔 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
### 接入HttpDns
- 将HttpDnsLibs\HttpDns_xxxx.jar拷贝至应用libs相应位置
- 将HttpDnsLibs\dnsconfig.ini拷贝至应用assets目录下
	- **注意**：拷贝dnsconfig.ini前，先按照提示修改文件里的相关配置，但不要改变文件原有的编码格式
### 接入灯塔
- 将HttpDnsLibs\beacon_android_xxxx.jar拷贝至应用libs相应位置
	- **注意**：已经接入了腾讯灯塔(beacon)组件的应用忽略此步
### 接口调用
```
// 初始化灯塔：如果已经接入MSDK或者IMSDK或者单独接入了腾讯灯塔(Beacon)则不需再初始化该接口
try {
    // 注意：这里业务需要输入自己的灯塔appkey
    UserAction.setAppKey("0I000LT6GW1YGCP7");
    UserAction.initUserAction(MainActivity.this.getApplicationContext());
} catch (Exception e) {
    Log.e(TAG, "Init beacon failed", e);
}

/**
 * 初始化HttpDns：如果接入了MSDK，建议初始化MSDK后再初始化HttpDns
 * 
 * @param context 应用上下文，最好传入ApplicationContext
 */
MSDKDnsResolver.getInstance().init(MainActivity.this.getApplicationContext()); 
	
/**
 * 设置OpenId，已接入MSDK业务直接传MSDK OpenId，其它业务传“NULL”
 * 
 * @param String openId
 */
MSDKDnsResolver.getInstance().WGSetDnsOpenId("10000");

/**
 * HttpDns同步解析接口
 * 注意：domain只能传入域名不能传入IP
 * 首先查询缓存，若存在则返回结果，若不存在则进行同步域名解析请求
 * 解析完成返回最新解析结果，解析结果有多个IP则以“;”分隔
 * 
 * @param domain 域名(如www.qq.com)
 * @return 域名对应的解析IP结果集合
 */
String ips = MSDKDnsResolver.getInstance().getAddrByName(domain);
```
### 接入验证
dnsconfig.ini中打开debug开关，过滤TAG为“WGGetHostByName”的日志。查看到LocalDns（日志上为ldns_ip）和HttpDns（日志上为hdns_ip）相关日志，则可以确认接入无误
### 注意事项
- getAddrByName是耗时同步接口，应当在子线程调用
- 如果客户端的业务是与host绑定的，比如是绑定了host的http服务或者是cdn的服务，那么在用HttpDns返回的IP替换掉URL中的域名以后，还需要指定下Http头的Host字段
	- 以URLConnection为例： 
		```
		URL oldUrl = new URL(url); 
    	URLConnection connection = oldUrl.openConnection(); 
		// 获取HttpDns域名解析结果 
		String ips = MSDKDnsResolver.getInstance().getAddrByName(oldUrl.getHost());
		if (null != ips) { // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置 
            String ip; 
            if (ips.contains(";")) {
		        ip = ips.substring(0, ips.indexOf(";")); 
            } else {
                ip = ips; 
            }
		    String newUrl = url.replaceFirst(oldUrl.getHost(), ip); 
		    connection = (HttpURLConnection) new URL(newUrl).openConnection(); // 设置HTTP请求头Host域名 
		    connection.setRequestProperty("Host", oldUrl.getHost());		 
		}
		``` 
	- 以curl为例，假设你要访问www.qq.com，通过HttpDns解析出来的IP为192.168.0.111，那么可以这么访问： 
		curl -H "Host:www.qq.com" http://192.168.0.111/aaa.txt
- 检测本地是否使用了Http代理，如果使用了Http代理，建议**不要使用**HttpDns做域名解析
	示例如下：
	```
	String host = System.getProperty("http.proxyHost");
	String port= System.getProperty("http.proxyPort");
	if (null != host && null != port) {
		// 使用了本地代理模式
	}
	```
## 实践场景
### OkHttp
OkHttp提供了Dns接口用于向OkHttp注入Dns实现。得益于OkHttp的良好设计，使用OkHttp进行网络访问时，实现Dns接口即可接入HttpDns进行域名解析，在较复杂场景（Https/Https + SNI）下也不需要做额外处理，侵入性极小。
示例如下：
```
mOkHttpClient =
    new OkHttpClient.Builder()
        .dns(new Dns() {
            @NonNull
            @Override
            public List<InetAddress> lookup(String hostname) {
                Utils.checkNotNull(hostname, "hostname can not be null");
                // HttpDns是对HttpDns接口处理的简单封装类
                String ip = HttpDns.getAddrByName(hostname);
                if (null == ip) {
                    return Collections.emptyList();
                }
                try {
                    InetAddress inetAddress = InetAddress.getByName(ip);
                    List<InetAddress> inetAddressList = new ArrayList<>();
                    inetAddressList.add(inetAddress);
                    return inetAddressList;
                } catch (UnknownHostException e) {
                    Log.d(TAG, "lookup failed", e);
                    return Collections.emptyList();
                }
            }
        })
        .build();
```
### Retrofit + OkHttp
Retrofit实际上是一个基于OkHttp，对接口做了一层封装桥接的lib。因此只需要仿OkHttp的接入方式，定制Retrofit中的OkHttpClient，即可方便地接入HttpDns。
示例如下：
```
mRetrofit =
	new Retrofit.Builder()
		.client(mOkHttpClient)
		.baseUrl(baseUrl)
		.build();
```
### WebView
Android系统提供了API以实现WebView中的网络请求拦截与自定义逻辑注入。我们可以通过该API拦截WebView的各类网络请求，截取URL请求的host，然后调用HttpDns解析该host，通过得到的IP组成新的URL来进行网络请求。
```
mWebView.setWebViewClient(new WebViewClient() { 
	// API 21及之后使用此方法 
	@SuppressLint("NewApi") 
	@Override 
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) { 
		if (request != null && request.getUrl() != null && request.getMethod().equalsIgnoreCase("get")) { 
			String scheme = request.getUrl().getScheme().trim(); 
			String url = request.getUrl().toString(); 
			Log.d(TAG, "url:" + url); 
			// HttpDns解析css文件的网络请求及图片请求 
			if ((scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) 
			&& (url.contains(".css") || url.endsWith(".png") || url.endsWith(".jpg") || url .endsWith(".gif"))) { 
				try { 
					URL oldUrl = new URL(url); 
					URLConnection connection = oldUrl.openConnection(); 
					// 获取HttpDns域名解析结果 
					String ips = MSDKDnsResolver.getInstance().getAddrByName(oldUrl.getHost());
					if (null != ips) { // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置 
						String ip; 
						if (ips.contains(";")) {
							ip = ips.substring(0, ips.indexOf(";")); 
						} else {
							ip = ips; 
						}
						String newUrl = url.replaceFirst(oldUrl.getHost(), ip); 
						connection = (HttpURLConnection) new URL(newUrl).openConnection(); // 设置HTTP请求头Host域名 
						connection.setRequestProperty("Host", oldUrl.getHost()); 
					}
					Log.d(TAG, "contentType:" + connection.getContentType()); 
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
			Log.d(TAG, "url:" + url); 
			// HttpDns解析css文件的网络请求及图片请求 
			if ((scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) 
	&& (url.contains(".css") || url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gif"))) { 
				try { 
					URL oldUrl = new URL(url); 
					URLConnection connection = oldUrl.openConnection();
					// 获取HttpDns域名解析结果
					String ips = MSDKDnsResolver.getInstance().getAddrByName(oldUrl.getHost());
					if (null != ips) { // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置
						String ip;
						if (ips.contains(";")) {
							ip = ips.substring(0, ips.indexOf(";"));
						} else {
							ip = ips;
						}
						String newUrl = url.replaceFirst(oldUrl.getHost(), ip); 
						connection = (HttpURLConnection) new URL(newUrl).openConnection(); // 设置HTTP请求头Host域名 
						connection.setRequestProperty("Host", oldUrl.getHost()); 
					}
					Log.d(TAG, "contentType:" + connection.getContentType()); 
					return new WebResourceResponse("text/css", "UTF-8", connection.getInputStream()); 
				} catch (MalformedURLException e) { 
					e.printStackTrace(); 
				} catch (IOException e) { 
					e.printStackTrace(); 
				} 
			} 
		} 
		return null; 
	}}); 
// 加载web资源 
mWebView.loadUrl(targetUrl); 
```
### HttpURLConnection
- Https
	示例如下：
	```
	// 以域名为www.qq.com，HttpDns解析得到的IP为192.168.0.1为例
    String url = "https://192.168.0.1/"; // 业务自己的请求连接
	HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
	connection.setRequestProperty("Host", "www.qq.com");
	connection.setHostnameVerifier(new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return HttpsURLConnection.getDefaultHostnameVerifier().verify("www.qq.com", session);
		}
	});
	connection.setConnectTimeout(mTimeOut); // 设置连接超时
	connection.setReadTimeout(mTimeOut); // 设置读流超时
	connection.connect();
	```
- Https + SNI
	示例如下：
	```
	// 以域名为www.qq.com，HttpDns解析得到的IP为192.168.0.1为例
	String url = "https://192.168.0.1/"; // 用HttpDns解析得到的IP封装业务的请求URL
	HttpsURLConnection sniConn = null;
	try {
		sniConn = (HttpsURLConnection) new URL(url).openConnection();
		// 设置HTTP请求头Host域
		sniConn.setRequestProperty("Host", "www.qq.com");
		sniConn.setConnectTimeout(3000);
		sniConn.setReadTimeout(3000);
		sniConn.setInstanceFollowRedirects(false);
		// 定制SSLSocketFactory来带上请求域名 ***关键步骤
		SniSSLSocketFactory sslSocketFactory = new SniSSLSocketFactory(sniConn);
		sniConn.setSSLSocketFactory(sslSocketFactory);
		// 验证主机名和服务器验证方案是否匹配
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return HttpsURLConnection.getDefaultHostnameVerifier().verify("原解析的域名", session);
			}
		};
		sniConn.setHostnameVerifier(hostnameVerifier);
		...
	} catch (Exception e) {
		Log.w(TAG, "Request failed", e);
	} finally {
		if (sniConn != null) {
			sniConn.disconnect();
		}
	}

	class SniSSLSocketFactory extends SSLSocketFactory {
	
		private HttpsURLConnection mConn;

		public SniSSLSocketFactory(HttpsURLConnection conn) {
			mConn = conn;
		}

		@Override
		public Socket createSocket() throws IOException {
			return null;
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return null;
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
			return null;
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			return null;
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
			return null;
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return new String[0];
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return new String[0];
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
			String realHost = mConn.getRequestProperty("Host");
			if (realHost == null) {
				realHost = host;
			}
			Log.i(TAG, "customized createSocket host is: " + realHost);
			InetAddress address = socket.getInetAddress();
			if (autoClose) {
				socket.close();
			}
			SSLCertificateSocketFactory sslSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0);
			SSLSocket ssl = (SSLSocket) sslSocketFactory.createSocket(address, port);
			ssl.setEnabledProtocols(ssl.getSupportedProtocols());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				Log.i(TAG, "Setting SNI hostname");
				sslSocketFactory.setHostname(ssl, realHost);
			} else {
				Log.d(TAG, "No documented SNI support on Android < 4.2, trying with reflection");
				try {
					Method setHostnameMethod = ssl.getClass().getMethod("setHostname", String.class);
					setHostnameMethod.invoke(ssl, realHost);
				} catch (Exception e) {
					Log.w(TAG, "SNI not useable", e);
				}
			}
			// verify hostname and certificate
			SSLSession session = ssl.getSession();
			HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
			if (!hostnameVerifier.verify(realHost, session)) {
				throw new SSLPeerUnverifiedException("Cannot verify hostname: " + realHost);
			}			
			Log.i(TAG, "Established " + session.getProtocol() + " connection with " + session.getPeerHost() + " using " + session.getCipherSuite());
			return ssl;
		}
    }
	```
### HttpClient
HttpClient接入HttpDns方式和HttpURLConnection类似；涉及Https情况则有所不同。以下以Https + SNI场景为例（示例也适用于Https场景）：
```
@Nullable
private HttpClient mHttpClient = null;

{
    try {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", new SniSSLSocketFactory(trustStore), 443));
        HttpParams httpParams = new BasicHttpParams();
        mHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, schemeRegistry), httpParams);
    } catch (Exception e) {
        Log.d(TAG, "set HttpClient failed", e);
    }
}

private static final class SniSSLSocketFactory extends SSLSocketFactory {

    private SSLContext mSSLContext;

    {
        try {
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(null, new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
            }, null);
        } catch (NoSuchAlgorithmException e) {
            // might never reach
            Log.d(TAG, "get SSLContext failed", e);
            mSSLContext = null;
        }
    }

    public SniSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        if (null == mSSLContext) {
            // might never reach
            Log.d(TAG, "null == mSSLContext");
            return socket;
        }
        Log.i(TAG, "host:" + host);
        // HttpDns是对HttpDns接口处理的简单封装类
        String ip = HttpDns.getAddrByName(host);
        if (null == ip) {
            Log.d(TAG, "null == ip");
            throw new UnknownHostException(host);
        }
        Log.d(TAG, "ip:" + ip);
        InetAddress inetAddress = InetAddress.getByName(ip);
        if (autoClose) {
            socket.close();
        }
        javax.net.ssl.SSLSocketFactory sslSocketFactory = mSSLContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(inetAddress, port);
        sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
        try {
            Method setHostnameMethod = sslSocket.getClass().getMethod("setHostname", String.class);
            setHostnameMethod.invoke(sslSocket, host);
        } catch (Exception e) {
            Log.w(TAG, "set sni failed", e);
        }
        return sslSocket;
    }
}
```
### Unity
- 初始化HttpDns和灯塔接口
	**注意**：若已接入msdk或者单独接入了腾讯灯塔则不用初始化灯塔。
	示例如下：
	```
	private static AndroidJavaObject sHttpDnsObj;
	public static void Init() {
		AndroidJavaClass unityPlayerClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
		if (unityPlayerClass == null) {
			return;
		}	
		AndroidJavaObject activityObj = unityPlayerClass.GetStatic<AndroidJavaObject>("currentActivity");
		if (activityObj == null) {
			return;
		}
		AndroidJavaObject contextObj = activityObj.Call<AndroidJavaObject>("getApplicationContext");
		// 初始化HttpDns
		AndroidJavaObject httpDnsClass = new AndroidJavaObject("com.tencent.msdk.dns.MSDKDnsResolver");
		if (httpDnsClass == null) {
			return;
		}
		sHttpDnsObj = httpDnsClass.CallStatic<AndroidJavaObject>("getInstance");
		if (sHttpDnsObj == null) {
			return;
		}
		sHttpDnsObj.Call("init", contextObj, appkey, dnsid, dnskey, debug, timeout);
	}
	```
- 调用getAddrByName接口解析域名
	示例如下：
	```
	// 该操作建议在子线程中处理
	// 注意在子线程中调用需要在调用前后做AttachCurrentThread和DetachCurrentThread处理 
	public static string GetHttpDnsIP(string strUrl) {
		string strIp = string.Empty;
		AndroidJNI.AttachCurrentThread(); // 子线程中调用需要加上
		// 解析得到IP配置集合
		strIp = sHttpDnsObj.Call<string>("getAddrByName", strUrl);
		AndroidJNI.DetachCurrentThread(); // 子线程中调用需要加上
		if (null != strIp) {
			string[] strIps = strIp.Split(';');
			strIp = strIps[0];
		}
		return strIp;
	}
	```
