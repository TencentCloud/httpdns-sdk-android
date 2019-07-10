package com.tencent.httpdns.network

import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.base.DnsServiceWrapper
import okhttp3.Dns
import okhttp3.OkHttpClient

internal object OkHttpHelper {

    private val dns by lazy {
        Dns { hostname ->
            DnsLog.d("OkHttpHelper lookup for $hostname")
            DnsServiceWrapper.getAddrsByName(hostname).toMutableList()
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .dns(dns)
            .build()
    }
}
