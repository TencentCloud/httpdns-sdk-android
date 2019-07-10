package com.tencent.httpdns.base.log

import android.os.Build
import android.util.Log
import com.tencent.httpdns.base.BuildConfig

internal class AndroidLogNode : ILogNode {

    override fun println(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        when (priority) {
            Log.VERBOSE, Log.DEBUG, Log.INFO ->
                if (BuildConfig.DEBUG) log(Log.INFO, tag, msg, tr) else log(priority, tag, msg, tr)
            Log.WARN, Log.ERROR -> log(priority, tag, msg, tr)
        }
    }

    private fun log(priority: Int, tag: String?, msg: String?, tr: Throwable?) {
        val realTag = getTag(tag)
        val realMsg = "${msg ?: ""}${if (null != tr) "\n" + Log.getStackTraceString(tr) else ""}"
        if (MAX_LOG_LENGTH >= realMsg.length) {
            Log.println(priority, realTag, realMsg)
            return
        }
        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val len = realMsg.length
        while (i < len) {
            var newline = realMsg.indexOf('\n', i)
            newline = if (-1 != newline) newline else len
            do {
                val end = Math.min(newline, i + MAX_LOG_LENGTH)
                val part = realMsg.substring(i, end)
                Log.println(priority, realTag, part)
                i = end
            } while (i < newline)
            i++
        }
    }

    private fun getTag(tag: String?): String {
        val candidateTag = tag ?: ""
        return if (MAX_TAG_LENGTH >= candidateTag.length || Build.VERSION_CODES.N <= Build.VERSION.SDK_INT) candidateTag
        else candidateTag.substring(0, MAX_TAG_LENGTH)
    }

    companion object {

        private const val MAX_TAG_LENGTH = 23
        private const val MAX_LOG_LENGTH = 4000
    }
}
