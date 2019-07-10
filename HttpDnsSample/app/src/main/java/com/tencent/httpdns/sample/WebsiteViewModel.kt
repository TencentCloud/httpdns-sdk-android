package com.tencent.httpdns.sample

import androidx.lifecycle.ViewModel
import com.tencent.httpdns.sample.data.WebsiteRepository

class WebsiteViewModel : ViewModel() {

    val websites by lazy {
        Array(WebsiteRepository.size) { i -> WebsiteRepository[i] }
    }
}
