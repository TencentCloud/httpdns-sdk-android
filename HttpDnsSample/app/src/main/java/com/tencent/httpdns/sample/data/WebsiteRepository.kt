package com.tencent.httpdns.sample.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tencent.httpdns.network.base.WEBSITES
import com.tencent.httpdns.sample.data.remote.RemoteWebsiteDataSource
import kotlinx.coroutines.*

object WebsiteRepository {

    private val remoteWebsiteDataSource: WebsiteDataSource by lazy {
        RemoteWebsiteDataSource()
    }

    val size = WEBSITES.size

    operator fun get(i: Int): LiveData<Website> {
        // NOTE: Let it crash if out of index
        val url = WEBSITES[i]
        val websiteLiveData = MutableLiveData<Website>()

        GlobalScope.launch {
            remoteWebsiteDataSource[url]?.also {
                websiteLiveData.postValue(Website(url, it))
            }
        }

        return websiteLiveData
    }

    fun getResource(url: String) =
        remoteWebsiteDataSource.getResource(url)
}

data class Website(val url: String, val content: String)
