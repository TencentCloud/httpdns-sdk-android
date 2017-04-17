# Android版接入说明文档 #
----

## GitHub目录结构说明

| 目录名称       | 说明           | 适用范围  |
| ------------- |-------------| -------------|
| HttpDnsDemo | Android客户端使用HttpDns api示例Demo | 所有业务 |
| HttpDnsLibs | HttpDns Android SDK库目录 | 所有业务 |
| MSDKDnsUnityDemo | Unity工程使用HTTPDNS api示例Demo | 使用Unity引擎的业务 |
| HttpDns Android客户端接入文档（腾讯内部业务专用）.doc | HttpDns Android客户端接入文档（腾讯内部业务专用） | 腾讯内部业务 |
| HttpDns Android客户端接入文档（腾讯内部业务专用）.md | HttpDns Android客户端接入文档（腾讯内部业务专用） | 腾讯内部业务 |
| HttpDns Android客户端接入文档（腾讯云客户专用）.doc | HttpDns Android客户端接入文档（腾讯云客户专用） | 腾讯云客户 |
| README.md | HTTPDNS Android客户端接入文档 | 腾讯外部客户 |
| VERSION.md | HTTPDNS Android SDK历史版本修改记录 | SDK开发维护人员 |
| 安卓配置文件修改示例.docx | 安卓配置文件修改示例 | SDK开发维护人员 |
| 数据报表申请方法.docx | 数据报表申请方法 | SDK开发维护人员 |

## 1. 功能介绍
### HttpDns的主要功能是为了有效的避免由于运营商传统LocalDns解析导致的无法访问最佳接入点的方案。原理为使用Http加密协议替代传统的DNS协议，整个过程不使用域名，大大减少劫持的可能性。

## 2. 接入
### 2.1. AndroidMainfest配置：
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<!-- DNS接收网络切换广播 -->
<receiver
    android:name="com.tencent.msdk.dns.HttpDnsCache$ConnectivityChangeReceiver"
    android:label="NetworkConnection" >
    <intent-filter>
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
    </intent-filter>
</receiver>
```
>####
> ###

### 2.2 接入HttpDns库：
> ### 将HttpDnsLibs\httpdns_xxxx.jar库文件拷贝至应用libs相应的位置，将HttpDnsLibs\dnsconfig.ini配置文件拷贝到应用Android\assets目录下；
> ### 注意：
> ### 拷贝dnsconfig.ini文件前，先修改此文件里的相关配置，但不要改变文件原有的编码格式，具体修改方法如下：

| 修改项          | 修改字段          |  修改方法   |
|  ------------- |  :-------------:  |  --------:  |
| 厂商开关        | IS_COOPERATOR     | 填"true" |
| 外部厂商测试开关 | IS_COOPERATOR_TEST| 测试环境填"true"，直接使用正式环境填"false"，正式上线时必须为false  |
| 厂商上报appID   | COOPERATOR_APPID  | 注册后由系统或管理员分配 |
| SDK日志开关     | IS_DEBUG          | true为打开日志开关，false为关闭日志，建议测试阶段打开，正式上线时关闭 |
| 服务端分配的ID  | DNS_ID            | 注册后由系统或管理员分配 |
| 服务端分配的KEY | DNS_KEY           | 注册后由系统或管理员分配 |

### 2.3 接入依赖库：（注意：已经接入了腾讯灯塔(beacon)组件的应用忽略此步）
> ### 将HttpDnsLibs\beacon_android_vxxxx.jar灯塔库拷贝至游戏libs相应的位置；

### 2.4 HttpDns Java接口调用：

    // 初始化灯塔：如果已经接入MSDK或者IMSDK或者单独接入了腾讯灯塔(Beacon)则不需再初始化该接口
    try {
        // 设置灯塔App_Key，注：部分灯塔低版本使用setAppKey(MainActivity.this,"0I000LT6GW1YGCP7")接口
        // UserAction.setAppKey(MainActivity.this, "0I000LT6GW1YGCP7");
        // ***注意：这里业务需要输入自己的灯塔AppKey
        UserAction.setAppkey("0I000LT6GW1YGCP7");
        UserAction.initUserAction(MainActivity.this);
    } catch (Exception e) {
	    Logger.e("init beacon error:" + e.getMessage());
    }

	/**
	* 初始化HttpDns
	* @param Activity  传入Application Activity
	*/
	MSDKDnsResolver.getInstance().init(MainActivity.this); 

	/**
	* HttpDns同步解析接口
	* 注意：domain只能传入域名不能传入IP，返回结果需要做非空判断
	* 首先查询缓存，若存在则返回结果，若不存在则进行同步域名解析请求，
	* 解析完成返回最新解析结果，若解析失败返回空对象
	* @param domain 域名(如www.qq.com)
	* @return 域名对应的解析IP结果集合
	*/
	String ips = MSDKDnsResolver.getInstance(). getAddrByName(domain);

## 3．注意事项
### 3.1 建议调用HttpDns同步接口时最好在子线程调用getAddrByName(domain)接口。
### 3.2 如果客户端的业务是与host绑定的，比如是绑定了host的http服务或者是cdn的服务，那么在用HTTPDNS返回的IP替换掉URL中的域名以后，还需要指定下Http头的Host字段。
	- 以URLConnection为例： 
		URL oldUrl = new URL(url); 
        URLConnection connection = oldUrl.openConnection(); 
        // 获取HttpDns域名解析结果 
        String ips = MSDKDnsResolver.getInstance().getAddrByName(oldUrl.getHost()); 
        if (ips != null) { // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置 
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
	- 以curl为例：
		假设你要访问www.qq.com，通过HTTPDNS解析出来的IP为192.168.0.111，那么通过这个方式来调用即可： 
		curl -H "Host:www.qq.com" http://192.168.0.111/aaa.txt.



## 实践场景
## 1.Unity接入说明:
### (1)先初始化HttpDns和灯塔接口：
### 注意：若已接入msdk或者单独接入了腾讯灯塔则不用初始化灯塔。
	private static AndroidJavaObject m_dnsJo;
	private static AndroidJavaClass sGSDKPlatformClass;
	public static void Init() {
		AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
		if (jc == null)
			return;	
		AndroidJavaObject joactivety = jc.GetStatic<AndroidJavaObject>("currentActivity");
		if (joactivety == null)
			return;
		AndroidJavaObject context = joactivety.Call<AndroidJavaObject>("getApplicationContext");
		// 初始化HttpDns
		AndroidJavaObject joDnsClass = new AndroidJavaObject("com.tencent.msdk.dns.MSDKDnsResolver");
		Debug.Log(" WGGetHostByName ===========" + joDnsClass);
		if (joDnsClass == null)
			return;
		m_dnsJo = joDnsClass.CallStatic<AndroidJavaObject>("getInstance");
			Debug.Log(" WGGetHostByName ===========" + m_dnsJo);
		if (m_dnsJo == null)
			return;
		m_dnsJo.Call("init", context);
	}

### (2)调用HttpDns接口解析域名：
	// 该操作建议在子线程中处理
	public static string GetHttpDnsIP( string strUrl ) {
		string strIp = string.Empty;
		// 解析得到IP配置集合
		strIp = m_dnsJo.Call<string>("getAddrByName", strUrl);
		Debug.Log( strIp );
		if( strIp != null )
		{
			// 取第一个
		string[] strIps = strIp.Split(';');
		strIp = strIps[0];
		}
		return strIp;
	}




## 2. H5页面内元素HTTP_DNS加载
### 原理：
### Android原生系统提供了系统API以实现WebView中的网络请求拦截与自定义逻辑注入，我们可以通过上述拦截WebView的各类网络请求，截取URL请求的host，然后调用HttpDns解析该host，通过得到的ip组成新的URL来请求网络地址。
### 实现方法：
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
			&& (url.contains(".css") || url.endsWith(".png") || url.endsWith(".jpg") || url .endsWith(".gif"))) { 
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
	&& (url.contains(".css") || url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gif"))) { 
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
	}}); 
	// 加载web资源 
	mWebView.loadUrl(targetUrl); 