# HTTPDNS SDK Android

## GitHub 目录结构说明

| 目录名称      | 说明                                 | 适用范围         |
| ------------- | ------------------------------------ | ---------------- |
| HttpDNSLibs   | HttpDNS Android SDK 库目录           | 所有业务         |
| HttpDnsSample | HttpDNS Android 示例                 | SDK 开发维护人员 |
| README.md     | HttpDNS Android 客户端接入文档       | 所有业务         |
| CHANGELOG.md  | HttpDNS Android SDK 历史版本修改记录 | SDK 开发维护人员 |

## HTTPDNS SDK 接入步骤

### 文件拷贝

将 HttpDnsLibs 目录下的 aar 包及 jar 拷贝至项目工程中 libs 相应位置

HttpDnsLibs 目录下包含两个包：

- 文件名以 HTTPDNS 为前缀的 aar 包（HTTPDNS_Android_xxxx.aar）为 HTTPDNS SDK
- 文件名以 beacon 为前缀的 jar 包（beacon-android-xxxx.jar）为灯塔 SDK
  - HTTPDNS SDK 使用灯塔 SDK 进行数据上报

### aar 引入配置

在 App module 的 build.gradle 文件中, 添加如下配置

```groovy
android {

    // ...

    repositories {
        flatDir {
            dirs 'libs'
        }
    }
}

dependencies {

    // ...

    implementation(name: 'HTTPDNS_Android_xxxx', ext: 'aar')
}
```

### 网络安全配置兼容

App targetSdkVersion >= 28(Android 9.0)情况下，系统默认不允许 HTTP 网络请求，详细信息参见[Opt out of cleartext traffic](https://developer.android.com/training/articles/security-config#CleartextTrafficPermitted)，[Protecting users with TLS by default in Android P](https://android-developers.googleblog.com/2018/04/protecting-users-with-tls-by-default-in.html)

这种情况下，业务侧需要将 HTTPDNS 请求使用的 IP 配置到域名白名单中：

- AndroidManifest 文件中配置

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest ... >
    <application android:networkSecurityConfig="@xml/network_security_config"
                    ... >
        ...
    </application>
</manifest>
```

- xml 目录下添加 network_security_config.xml 配置文件

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">119.29.29.99</domain>
        <domain includeSubdomains="false">119.29.29.98</domain>
    </domain-config>
</network-security-config>
```

### 反混淆配置

```
# 灯塔
-keep class com.tencent.beacon.** {*;}
```

## 初始化

参考 Android SDK 文档 https://cloud.tencent.com/document/product/379/17655

i.初始化配置服务(可选，4.0.0版本开始支持)

在获取服务实例之前，我们可以通过初始化配置，设置服务的一些属性在SDK初始化时进行配置项传入。
```Java
DnsConfig dnsConfigBuilder = DnsConfig.Builder()
    //（必填）dns 解析 id，即授权 id，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
    .dnsId("xxx")
    //（必填）dns 解析 key，即授权 id 对应的 key（加密密钥），在申请 SDK 后的邮箱里，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
    .dnsKey("xxx")
    //（必填）Channel为desHttp()或aesHttp()时使用 119.29.29.98（默认填写这个就行），channel为https()时使用 119.29.29.99
    .dnsIp("xxx")
    //（可选）channel配置：基于 HTTP 请求的 DES 加密形式，默认为 desHttp()，另有 aesHttp()、https() 可选。（注意仅当选择 https 的 channel 需要选择 119.29.29.99 的dnsip并传入token，例如：.dnsIp('119.29.29.99').https().token('....') ）。
    .desHttp()
    //（可选，选择 https channel 时进行设置）腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于 HTTPS 校验。仅当选用https()时进行填写
    .token("xxx")
    //（可选）日志粒度，如开启Debug打印则传入"Log.VERBOSE"
    .logLevel(Log.VERBOSE)
    //（可选）填写形式："baidu.com", "qq.com"，预解析域名, 建议不要设置太多预解析域名, 当前限制为最多 10 个域名
    .preLookupDomains("baidu.com", "qq.com")
    //（可选）手动指定网络栈支持情况，仅进行 IPv4 解析传 1，仅进行 IPv6 解析传 2，进行 IPv4、IPv6 双栈解析传 3。默认为根据客户端本地网络栈支持情况发起对应的解析请求。
    .setCustomNetStack(3)
    //（可选）设置域名解析请求超时时间，默认为1000ms
    .timeoutMills(1000)
    //（可选）是否开启解析异常上报，默认false，不上报
    .enableReport(true)
    // 以build()结束
    .build();
    
MSDKDnsResolver.getInstance().init(this, dnsConfigBuilder);
```

ii. 老版本初始化方法
>
- HTTP 协议服务地址为 `119.29.29.98`，HTTPS 协议服务地址为 `119.29.29.99`（仅当采用自选加密方式并`channel`为`Https`时使用`99`的IP）。
- 新版本 API 更新为使用 `119.29.29.99/98` 接入，同时原移动解析 HTTPDNS 服务地址 `119.29.29.29` 仅供开发调试使用，无 SLA 保障，不建议用于正式业务，请您尽快将正式业务迁移至 `119.29.29.99/98`。
- 具体以 [API 说明](https://cloud.tencent.com/document/product/379/54976) 提供的 IP 为准。
- 使用 SDK 方式接入 HTTPDNS，若 HTTPDNS 未查询到解析结果，则通过 LocalDNS 进行域名解析，返回 LocalDNS 的解析结果。

#### 默认使用 DES 加密
##### 默认不进行解析异常上报

```Java
// 以下鉴权信息可在腾讯云控制台（https://console.cloud.tencent.com/httpdns/configure）开通服务后获取

/**
 * 初始化 HTTPDNS（默认为 DES 加密）：如果接入了 MSDK，建议初始化 MSDK 后再初始化 HTTPDNS
 *
 * @param context 应用上下文，最好传入 ApplicationContext
 * @param appkey 业务 appkey，即 SDK AppID，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于上报
 * @param dnsid dns解析id，即授权id，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
 * @param dnskey dns解析key，即授权id对应的 key（加密密钥），在申请 SDK 后的邮箱里，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
 * @param dnsIp 由外部传入的dnsIp，可选："119.29.29.98"，以腾讯云文档（https://cloud.tencent.com/document/product/379/54976）提供的 IP 为准
 * @param debug 是否开启 debug 日志，true 为打开，false 为关闭，建议测试阶段打开，正式上线时关闭
 * @param timeout dns请求超时时间，单位ms，建议设置1000
 */
MSDKDnsResolver.getInstance().init(MainActivity.this, appkey, dnsid, dnskey, dnsIp, debug, timeout);
```

##### 手动开启异常解析上报
```Java
// 以下鉴权信息可在腾讯云控制台（https://console.cloud.tencent.com/httpdns/configure）开通服务后获取

/**
 * 初始化 HTTPDNS（默认为 DES 加密）：如果接入了 MSDK，建议初始化 MSDK 后再初始化 HTTPDNS
 *
 * @param context 应用上下文，最好传入 ApplicationContext
 * @param appkey 业务 appkey，即 SDK AppID，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于上报
 * @param dnsid dns解析id，即授权id，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
 * @param dnskey dns解析key，即授权id对应的 key（加密密钥），在申请 SDK 后的邮箱里，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
 * @param dnsIp 由外部传入的dnsIp，可选："119.29.29.98"（仅支持 http 请求，channel为DesHttp和AesHttp时选择），"119.29.29.99"（仅支持 https 请求，channel为Https时选择）以腾讯云文档（https://cloud.tencent.com/document/product/379/54976）提供的 IP 为准
 * @param debug 是否开启 debug 日志，true 为打开，false 为关闭，建议测试阶段打开，正式上线时关闭
 * @param timeout dns请求超时时间，单位ms，建议设置1000
 * @param enableReport 是否开启解析异常上报，默认false，不上报
 */
MSDKDnsResolver.getInstance().init(MainActivity.this, appkey, dnsid, dnskey, dnsIp, debug, timeout, enableReport);
```


#### 自选加密方式（DesHttp, AesHttp, Https）

```Java
/**
 * 初始化 HTTPDNS（自选加密方式）：如果接入了 MSDK，建议初始化 MSDK 后再初始化 HTTPDNS
 *
 * @param context 应用上下文，最好传入 ApplicationContext
 * @param appkey 业务 appkey，即 SDK AppID，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于上报
 * @param dnsid dns解析id，即授权id，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
 * @param dnskey dns解析key，即授权id对应的 key（加密密钥），在申请 SDK 后的邮箱里，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
 * @param dnsIp 由外部传入的dnsIp，可选："119.29.29.98"（仅支持 http 请求，channel为DesHttp和AesHttp时选择），"119.29.29.99"（仅支持 https 请求，channel为Https时选择）以腾讯云文档（https://cloud.tencent.com/document/product/379/54976）提供的 IP 为准
 * @param debug 是否开启 debug 日志，true 为打开，false 为关闭，建议测试阶段打开，正式上线时关闭
 * @param timeout dns请求超时时间，单位ms，建议设置1000
 * @param channel 设置 channel，可选：DesHttp（默认）, AesHttp, Https
 * @param token 腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于 HTTPS 校验
 * @param enableReport 是否开启解析异常上报，默认false，不上报
 */
MSDKDnsResolver.getInstance().init(MainActivity.this, appkey, dnsid, dnskey, dnsIp, debug, timeout, channel, token, true);
```
