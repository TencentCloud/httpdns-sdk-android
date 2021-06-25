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
