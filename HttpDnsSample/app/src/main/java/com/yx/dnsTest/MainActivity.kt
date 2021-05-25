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

        // SDK初始化 请输入相应的配置信息
        MSDKDnsResolver.getInstance().init(this, appkey, dnsid, dnskey, dnsIp debug, timeout, channel, token);

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