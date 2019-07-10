package com.tencent.httpdns.network

import android.annotation.SuppressLint
import android.os.Build
import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.tencent.httpdns.network.httpclient.HttpClientHelper

internal class CompatHttpStack : BaseHttpStack() {

    @SuppressLint("ObsoleteSdkInt")
    private val realHttpStack =
        // NOTE: Sample的minSdk是14, 这里是为了演示minSdk 9以下的情况
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) HurlStackWrapper()
        else AdaptedHttpStack(HttpClientStackWrapper(HttpClientHelper.httpClient))

    override fun executeRequest(request: Request<*>?, additionalHeaders: MutableMap<String, String>?) =
            realHttpStack.executeRequest(request, additionalHeaders)
}
