package com.tencent.httpdns.network

import com.tencent.httpdns.base.log.DnsLog

class HostnameContainer {

    private val hostname2IpPairContainer = ThreadLocal<Ip2HostnamePair>()

    fun put(ip: String, hostname: String) {
        DnsLog.d("HostnameContainer put $ip to $hostname")
        hostname2IpPairContainer.set(ip to hostname)
    }

    fun get(ip: String) =
        hostname2IpPairContainer.get()?.let {
            val (expectedIp, hostname) = it
            if (expectedIp == ip) hostname else null
        }
}

typealias Ip2HostnamePair = Pair<String, String>
