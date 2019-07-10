package com.tencent.httpdns.base.ext

import java.io.Closeable

class AutoCloseContext : Closeable {

    private val autoCloseResList = mutableListOf<Closeable>()

    override fun close() {
        autoCloseResList
            .reversed()
            .forEach { res -> res.close() }
    }

    fun <T : Closeable> T.autoClose() =
        apply { autoCloseResList.add(this) }
}

fun <R> using(autoCloseBlock: AutoCloseContext.() -> R) =
    AutoCloseContext()
        .use { context -> context.autoCloseBlock() }
