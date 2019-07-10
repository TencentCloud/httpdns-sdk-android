package com.tencent.httpdns.base.log

interface ILogNode {

    fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?)
}
