package com.tencent.httpdns.network

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

internal object VolleyHelper {

    lateinit var requestQueue: RequestQueue

    fun init(context: Context) {
        requestQueue = Volley.newRequestQueue(context.applicationContext, CompatHttpStack())
    }
}
