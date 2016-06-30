# Android版接入说明文档 #
----

## 1. 功能介绍
### MSDKDns的主要功能是为了有效的避免由于运营商传统LocalDns解析导致的无法访问最佳接入点的方案。原理为使用Http加密协议替代传统的DNS协议，整个过程不使用域名，大大减少劫持的可能性。

> ### 注意：
> ### 如果客户端的业务是与host绑定的，比如是绑定了host的http服务或者是cdn的服务，那么在用HTTPDNS返回的IP替换掉URL中的域名以后，还需要指定下Http头的Host字段。以curl为例，假设你要访问www.qq.com，通过HTTPDNS解析出来的IP为192.168.0.111，那么通过这个方式来调用即可： curl -H "Host:www.qq.com" http://192.168.0.111/aaa.txt.


## 2. 接入
### 2.1. AndroidMainfest配置：

>```<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />```

>```<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />```

>```<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />```

>```<uses-permission android:name="android.permission.READ_PHONE_STATE" />```

>```<uses-permission android:name="android.permission.INTERNET" />```

>```<!-- DNS接收网络切换广播 -->```

>```<receiver```

>```android:name="com.tencent.msdk.dns. HttpDnsCache$ConnectivityChangeReceiver"```

>```android: label="NetworkConnection" >```

>```<intent-filter>```

>```<action android:name= “android.net. conn. CONNECTIVITY_CHANGE" />```

>```</intent-filter>```

>``` </receiver>```

>```<!-- 添加应用自身的灯塔appkey，如0I3002SDUA14CRW8-->```

>```<meta-data```
>```android:name="APPKEY_DENGTA"```

>```android: value="XXXXXXXXXXXXXXXX" />```

>### 注意：
> ### Android: value的值在提供的版本包key_android.txt文件中，即appkey，请按照此文件中的内容修改，AndroidMainfest中的权限如果已经存在不需要重复添加。

### 2.2 接入HttpDns库：
> ### 将HttpDnsDemo\libs\msdkhttpdns_xxxx.jar库文件拷贝至应用libs相应的位置，将HttpDnsDemo\assets\dnsconfig.ini配置文件拷贝到应用Android\assets目录下；
> ### 注意：
> ### 拷贝dnsconfig.ini文件前，先修改此文件里的相关配置，但不要改变文件原有的编码格式，具体修改方法如下：

| 修改项          | 修改字段          |  修改方法   |
|  -------------  |  :-------------:  |  --------:  |
| 厂商开关        | IS_COOPERATOR     |  腾讯外部应用填"true"腾讯内部应用填"false" |
| 厂商上报appID   | COOPERATOR_APPID  |  key_android.txt文件中的appkey的值，如果是内部应用接入了MSDK的，可以不填 |
| SDK日志开关     | IS_DEBUG          |  true为打开日志开关，false为关闭日志 |
| 服务端分配的ID  | DNS_ID            |  key_android.txt对应的值 |
| 服务端分配的KEY | DNS_KEY           |  key_android.txt对应的值 |

### 2.3 接入依赖库：
> ###（注意：检查应用是否接入过已经接入了腾讯msdk，如果已经接入了腾讯msdk则忽略此步）
> ### 将HttpDnsDemo\libs\ beacon_android_v1.9.4.jar拷贝至游戏libs相应的位置；

### 2.4 HttpDns Java接口调用：
>/**
> ##* 初始化HttpDns
> ###* @param context  传入Application Context
> ###*/
> ### MSDKDnsResolver.getInstance().init(MainActivity.this.getApplicationContext());

> ###/**
> ##* 初始化灯塔
> ###*注意：如果已接入腾讯msdk并且初始化了msdk，则不用再次初始化灯塔
> ###* @param this 传入主Activity或者Application Context
> ###*/
> ### UserAction.initUserAction(MainActivity.this. getApplicationContext ()); 

> ###/**
> ##* HttpDns同步解析接口
> ###* 注意：domain只能传入域名不能传入IP，返回结果需要做非空判断
> ###* 首先查询缓存，若存在则返回结果，若不存在则进行同步域名解析请求，
> ###* 解析完成返回最新解析结果，若解析失败返回空对象
> ###* @param domain 域名(如www.qq.com)
> ###* @return 域名对应的解析IP结果集合
> ###*/
> ###String ips = MSDKDnsResolver.getInstance(). getAddrByName(domain);

## 3．备注：
### 3.1 客户端使用代理：当客户端使用代理时HttpDns解析的结果就变成了根据代理的IP来判断用户的所在的依据，从而可能会返回和直接用用户IP访问时的不同的结果。

### 3.2 建议调用HttpDns同步接口时最好在子线程调用，getAddrByName(domain)接口做了超时管理，超时时间由应用自己在dnsconfig.ini文件中配置，默认超时时间为3秒(TIME_OUT=3000)。

### 3.3 若想自己使用灯塔上报内容，在接入HttpDns后可以直接调用灯塔接口上报：
> ###例如：
> ### Map<String, String> map = new HashMap<String, String>();
> ### map.put("resultKey", "resultValue");
> ### UserAction.onUserAction("WGGetHostByNameResult", true, -1, -1, map, true);