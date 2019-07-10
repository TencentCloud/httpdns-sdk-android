package com.tencent.httpdns.base.ext

import java.net.Inet6Address
import java.net.InetAddress

fun InetAddress.toUriFormat() =
    if (this is Inet6Address) "[$hostAddress]" else "$hostAddress"
