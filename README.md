# HTTPDNS SDK Android

## GitHub目录结构说明

| 目录名称       | 说明           | 适用范围  |
| ------------- |-------------| -------------|
| HttpDNSLibs | HttpDNS Android SDK库目录 | 所有业务 |
| HttpDnsSample | HttpDNS Android 示例 | SDK开发维护人员 |
| README.md | HttpDNS Android客户端接入文档 | 所有业务 |
| CHANGELOG.md | HttpDNS Android SDK历史版本修改记录 | SDK开发维护人员 |

## HTTPDNS SDK接入步骤

### 文件拷贝

将HttpDnsLibs目录下的aar包及jar拷贝至项目工程中libs相应位置

HttpDnsLibs目录下包含两个包：

- 文件名以HTTPDNS为前缀的aar包（HTTPDNS_Android_xxxx.aar）为HTTPDNS SDK
- 文件名以beacon为前缀的jar包（beacon-android-xxxx.jar）为灯塔SDK
  - HTTPDNS SDK使用灯塔SDK进行数据上报

### aar引入配置

在App module的build.gradle文件中, 添加如下配置

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

App targetSdkVersion >= 28(Android 9.0)情况下，系统默认不允许HTTP网络请求，详细信息参见[Opt out of cleartext traffic](https://developer.android.com/training/articles/security-config#CleartextTrafficPermitted)，[Protecting users with TLS by default in Android P](https://android-developers.googleblog.com/2018/04/protecting-users-with-tls-by-default-in.html)

这种情况下，业务侧需要将HTTPDNS请求使用的IP配置到域名白名单中：

- AndroidManifest文件中配置

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest ... >
    <application android:networkSecurityConfig="@xml/network_security_config"
                    ... >
        ...
    </application>
</manifest>
```

- xml目录下添加network_security_config.xml配置文件

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">119.29.29.99</domain>
    </domain-config>
</network-security-config>
```

### 反混淆配置

```
# 灯塔
-keep class com.tencent.beacon.** {*;}
```

## 初始化
参考Android SDK文档 https://cloud.tencent.com/document/product/379/17655