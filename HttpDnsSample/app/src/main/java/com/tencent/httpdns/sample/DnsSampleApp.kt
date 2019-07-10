package com.tencent.httpdns.sample

import android.app.Application
import android.content.Context
import android.util.Log
import com.tencent.httpdns.base.log.DnsLog

class DnsSampleApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        DnsLog.setLogLevel(Log.VERBOSE)
    }
}
