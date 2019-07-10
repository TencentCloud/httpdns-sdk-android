package com.tencent.httpdns.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.httpdns.network.base.DnsServiceWrapper
import com.tencent.httpdns.network.base.TENCENT_WEBSITE_URL
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal object RetrofitHelper {

    private val dns by lazy {
        Dns { hostname ->
            DnsLog.d("RetrofitHelper lookup for $hostname")
            DnsServiceWrapper.getAddrsByName(hostname).toMutableList()
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient
            .Builder()
            .dns(dns)
            .build()
    }

    private val stringConverterFactory by lazy {
        object : Converter.Factory() {

            override fun responseBodyConverter(
                type: Type,
                annotations: Array<Annotation>,
                retrofit: Retrofit
            ) =
                if (String::class.java == type) Converter<ResponseBody, String> { it.string() }
                else null
        }
    }

    val retrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(stringConverterFactory)
            .baseUrl(TENCENT_WEBSITE_URL)
            .build()
    }
}
