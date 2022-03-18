package com.yx.dnsTest

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.MSDKDnsResolver

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val TAG = "MainActivity"

        val textInput: EditText = findViewById<EditText>(R.id.textInput)
        val btn = findViewById<Button>(R.id.button)
        val btnAsync = findViewById<Button>(R.id.buttonAsync)
        val btnBatch = findViewById<Button>(R.id.buttonBatch)
        val btnAsyncBatch = findViewById<Button>(R.id.buttonAsyncBatch)
        val resultText = findViewById<TextView>(R.id.resultText)

        // 配置项
        val dnsConfigBuilder = DnsConfig.Builder()
            .dnsId("dnsId")
            .dnsIp("119.29.29.98")
            .desHttp()
            .dnsKey("dnsKey")
            .logLevel(Log.VERBOSE)
            .timeoutMills(1000)
            .preLookupDomains("xxx.com")
            .enableReport(true)
            .build()
        MSDKDnsResolver.getInstance().init(this, dnsConfigBuilder)

        //  域名解析
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
                val start_time = System.currentTimeMillis();
                val ips = MSDKDnsResolver.getInstance().getAddrByName(hostname);
                runOnUiThread {
                    Log.d(TAG, "HTTPDNS：" + hostname + ": " + ips)
                    resultText.setText("解析结果为：" + ips + ", elapse:" + (System.currentTimeMillis() - start_time));
                }
            }).start()
        }

        //  域名解析-批量
        btnBatch.setOnClickListener {
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
                val start_time = System.currentTimeMillis();
                val ips = MSDKDnsResolver.getInstance().getAddrsByName(hostname);
                val end_time = System.currentTimeMillis() - start_time;
                runOnUiThread {
                    Log.d(TAG, "HTTPDNS：" + hostname + ": " + ips)
                    resultText.setText("解析结果为：" + ips + ", 耗时:" + end_time);
                }
            }).start()
        }

        //  异步回调
        MSDKDnsResolver.getInstance()
            .setHttpDnsResponseObserver { tag, domain, ipResultSemicolonSep ->
                Log.d(TAG, "tag:" + tag);
                val elapse = System.currentTimeMillis() - tag.toLong();
                val lookedUpResult =
                    "[[getAddrByNameAsync]]:ASYNC:::" + ipResultSemicolonSep +
                            ", domain:" + domain + ", tag:" + tag +
                            ", elapse:" + elapse
                Log.d(TAG, "HTTPDNS_Async：" + lookedUpResult);
                resultText.setText("解析结果为：" + ipResultSemicolonSep + ", elapse:" + elapse);
            }

        //  异步解析
        btnAsync.setOnClickListener(View.OnClickListener {
            val hostname = textInput.getText().toString();

            MSDKDnsResolver.getInstance()
                .getAddrByNameAsync(hostname, System.currentTimeMillis().toString())
        })

        // 异步解析-批量
        btnAsyncBatch.setOnClickListener(View.OnClickListener {
            val hostname = textInput.getText().toString();

            MSDKDnsResolver.getInstance()
                .getAddrsByNameAsync(hostname, System.currentTimeMillis().toString())
        })

    }
}