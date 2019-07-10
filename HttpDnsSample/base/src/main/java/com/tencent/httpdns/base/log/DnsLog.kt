package com.tencent.httpdns.base.log

import java.util.Locale

import android.util.Log.DEBUG
import android.util.Log.ERROR
import android.util.Log.INFO
import android.util.Log.VERBOSE
import android.util.Log.WARN
import android.util.Log.isLoggable

// 保留懒拼接msg的能力
object DnsLog {

    private const val TAG = "DnsSample"

    private var logLevel: Int

    init {
        // 初始情况只考虑系统设置
        logLevel = ERROR + 1
        for (level in ERROR downTo VERBOSE) {
            if (!isLoggable(TAG, level)) {
                break
            }
            logLevel = level
        }
    }

    fun setLogLevel(logLevel: Int) {
        // 以最宽松的日志层级为准
        DnsLog.logLevel = Math.min(logLevel, DnsLog.logLevel)
    }

    fun addLogNode(logNode: ILogNode) {
        Log.addLogNode(logNode)
    }

    fun v(msg: String, vararg args: Any) {
        v(null, msg, *args)
    }

    fun v(tr: Throwable?, msg: String, vararg args: Any) {
        tryLog(VERBOSE, tr, msg, *args)
    }

    fun d(msg: String, vararg args: Any) {
        d(null, msg, *args)
    }

    fun d(tr: Throwable?, msg: String, vararg args: Any) {
        tryLog(DEBUG, tr, msg, *args)
    }

    fun i(msg: String, vararg args: Any) {
        i(null, msg, *args)
    }

    fun i(tr: Throwable?, msg: String, vararg args: Any) {
        tryLog(INFO, tr, msg, *args)
    }

    fun w(msg: String, vararg args: Any) {
        w(null, msg, *args)
    }

    fun w(tr: Throwable?, msg: String, vararg args: Any) {
        tryLog(WARN, tr, msg, *args)
    }

    fun e(msg: String, vararg args: Any) {
        e(null, msg, *args)
    }

    fun e(tr: Throwable?, msg: String, vararg args: Any) {
        tryLog(ERROR, tr, msg, *args)
    }

    private fun tryLog(priority: Int, tr: Throwable?, msg: String, vararg args: Any) {
        if (priority >= logLevel) {
            if (args.isEmpty()) {
                Log.println(priority, TAG, msg, tr)
            } else {
                Log.println(priority, TAG, String.format(Locale.US, msg, *args), tr)
            }
        }
    }
}
