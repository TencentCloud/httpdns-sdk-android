package com.tencent.httpdns.network.base

import com.tencent.httpdns.base.log.DnsLog
import com.tencent.msdk.dns.DnsService
import com.tencent.msdk.dns.core.IpSet
import java.net.InetAddress
import java.net.UnknownHostException

object DnsServiceWrapper {

    private val EMPTY_ADDRESSES = arrayOf<InetAddress>()

    private val proxyHost by lazy { System.getProperty("http.proxyHost") }
    private val proxyPort by lazy { System.getProperty("http.proxyPort") }
    private val useHttpProxy by lazy {
        @Suppress("LocalVariableName")
        val _useHttpProxy = null != proxyHost && null != proxyPort
        DnsLog.d("useHttpProxy: %b", _useHttpProxy)
        _useHttpProxy
    }

    val useHttpDns = BuildConfig.USE_HTTP_DNS

    fun getAddrsByName(hostname: String): Array<out InetAddress> {
        // 客户端启用HTTP代理时, 不使用HTTPDNS
        if (useHttpProxy || !useHttpDns) {
            // LocalDNS只取第一个IP
            return getAddrByNameByLocal(hostname)?.let { arrayOf(it) } ?: EMPTY_ADDRESSES
        }
        DnsLog.d("DnsServiceWrapper lookup by HttpDns")
        val ipSet = DnsService.getAddrsByName(hostname)
        if (IpSet.EMPTY == ipSet) {
            return EMPTY_ADDRESSES
        }
        // 当前v6环境质量较差, 优先选择v4 IP, 且只考虑使用第一个v6 IP
        return when {
            ipSet.v6Ips.isNotEmpty() && ipSet.v4Ips.isNotEmpty() ->
                arrayOf(
                    *(ipSet.v4Ips.map { InetAddress.getByName(it) }.toTypedArray()),
                    InetAddress.getByName(ipSet.v6Ips[0])
                )
            ipSet.v6Ips.isNotEmpty() -> arrayOf(InetAddress.getByName(ipSet.v6Ips[0]))
            ipSet.v4Ips.isNotEmpty() -> ipSet.v4Ips.map { InetAddress.getByName(it) }.toTypedArray()
            else -> EMPTY_ADDRESSES
        }
    }

    private fun getAddrByNameByLocal(hostname: String) =
        try {
            InetAddress.getByName(hostname)
        } catch (e: UnknownHostException) {
            null
        }
}
