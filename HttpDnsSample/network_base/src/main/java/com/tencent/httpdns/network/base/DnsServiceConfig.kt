package com.tencent.httpdns.network.base

import android.os.AsyncTask
import android.util.Log
import com.tencent.httpdns.base.log.DnsLog
import com.tencent.msdk.dns.ILookedUpListener
import com.tencent.msdk.dns.base.executor.DnsExecutors
import com.tencent.msdk.dns.base.log.ILogNode
import com.tencent.msdk.dns.core.LookupResult
import com.tencent.msdk.dns.core.stat.StatisticsMerge

internal const val LOG_LEVEL = Log.VERBOSE

internal const val APP_ID = BuildConfig.APP_ID

internal const val USER_ID = "userId"

internal const val DNS_ID = BuildConfig.DNS_ID

internal const val DNS_KEY = BuildConfig.DNS_KEY

internal const val TIMEOUT_MILLS = 1000

internal val PROTECTED_DOMAINS = TENCENT_DOMAINS

internal val PRE_LOOKUP_DOMAINS =
    arrayOf(
        TENCENT_WEBSITE_HOSTNAME,
        QQ_WEBSITE_HOSTNAME,
        WECHAT_WEBSITE_HOSTNAME,
        TENCENT_SPORTS_WEBSITE_HOSTNAME,
        TENCENT_VEDIO_WEBSITE_HOSTNAME
    )

internal val ASYNC_LOOKUP_DOMAINS = PRE_LOOKUP_DOMAINS

internal const val USE_UDP = true

internal const val BLOCK_FIRST = false

internal val EXECUTOR_SUPPLIER = DnsExecutors.ExecutorSupplier { AsyncTask.THREAD_POOL_EXECUTOR }

internal val LOOKED_UP_LISTENER =
    object : ILookedUpListener {

        override fun onPreLookedUp(hostname: String, lookupResult: LookupResult<StatisticsMerge>) {
            DnsLog.d(("Pre looked up for $hostname: ${lookupResult.ipSet}"))
        }

        override fun onLookedUp(hostname: String, lookupResult: LookupResult<StatisticsMerge>) {
            DnsLog.d(("looked up for $hostname: ${lookupResult.ipSet}"))
        }

        override fun onAsyncLookedUp(hostname: String, lookupResult: LookupResult<StatisticsMerge>) {
            DnsLog.d(("Async looked up for $hostname: ${lookupResult.ipSet}"))
        }
    }

internal val LOG_NODE =
    ILogNode { level, tag, msg, tr ->
        when (level) {
            Log.VERBOSE -> DnsLog.v(tr, msg)
            Log.DEBUG -> DnsLog.d(tr, msg)
            Log.INFO -> DnsLog.i(tr, msg)
            Log.WARN -> DnsLog.w(tr, msg)
            Log.ERROR -> DnsLog.e(tr, msg)
            else -> {
            }
        }
    }
