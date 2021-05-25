package com.yx.dnsTest

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tencent.msdk.dns.MSDKDnsResolver

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val TAG = "MainActivity"

        val textInput: EditText = findViewById<EditText>(R.id.textInput)
        val btn = findViewById<Button>(R.id.button)
        val resultText = findViewById<TextView>(R.id.resultText)

        /**
         * 初始化HTTPDNS（自选加密方式）
         * @param context 应用上下文，最好传入ApplicationContext
         * @param appkey 业务appkey，即SDK AppID，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于上报
         * @param dnsid dns解析id，即授权id，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
         * @param dnskey dns解析key，即授权id对应的key(加密密钥)，在申请SDK后的邮箱里，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
         * @param dnsIp 由外部传入的dnsIp，如"119.29.29.99"，从<a href="https://cloud.tencent.com/document/product/379/17655"></a> 文档提供的IP为准
         * @param debug 是否开启debug日志，true为打开，false为关闭，建议测试阶段打开，正式上线时关闭
         * @param timeout dns请求超时时间，单位ms，建议设置1000
         * @param channel 设置channel，可选：DesHttp(默认), AesHttp, Https
         * @param token 腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于HTTPS校验
         */
        MSDKDnsResolver.getInstance().init(this, appkey, dnsid, dnskey, "119.29.29.99", true, 1000, "Https", token);

        btn.setOnClickListener {
            val hostname = textInput.getText().toString();

            // 进⾏行行域名解析
            // NOTE: ***** 域名解析接⼝口是耗时同步接⼝口，不不应该在主线程调⽤用 *****
            // useHttp即是否通过HTTP协议访问HTTP服务进⾏行行域名解析
            // useHttp为true时通过HTTP协议访问HTTP服务进⾏行行域名解析, 否则通过UDP协议访问HTTP服务 进⾏行行域名解析
            // ipSet即解析得到的IP集合
            // ipSet.v4Ips为解析得到IPv4集合, 可能为null
            // ipSet.v6Ips为解析得到IPv6集合, 可能为null
            Log.d(TAG, "HTTPDNS：" + hostname)
            Thread(Runnable {
                val ips = MSDKDnsResolver.getInstance().getAddrsByName(hostname);
                runOnUiThread {
                    Log.d(TAG, "HTTPDNS：" + hostname + ": " + ips)
                    resultText.setText("解析结果为：" + ips);
                }
            }).start()
        }

    }
}