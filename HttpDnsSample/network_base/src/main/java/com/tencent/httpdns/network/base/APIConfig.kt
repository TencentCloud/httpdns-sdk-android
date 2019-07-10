package com.tencent.httpdns.network.base

const val TENCENT_WEBSITE_HOSTNAME = "www.tencent.com"
const val TENCENT_WEBSITE_URL = "https://$TENCENT_WEBSITE_HOSTNAME/"

const val QQ_WEBSITE_HOSTNAME = "im.qq.com"
const val QQ_WEBSITE_URL = "https://$QQ_WEBSITE_HOSTNAME/"

const val WECHAT_WEBSITE_HOSTNAME = "weixin.qq.com"
const val WECHAT_WEBSITE_URL = "https://$WECHAT_WEBSITE_HOSTNAME/"

const val TENCENT_SPORTS_WEBSITE_HOSTNAME = "sports.qq.com"
const val TENCENT_SPORTS_WEBSITE_URL = "https://$TENCENT_SPORTS_WEBSITE_HOSTNAME/"

const val TENCENT_VEDIO_WEBSITE_HOSTNAME = "v.qq.com"
const val TENCENT_VEDIO_WEBSITE_URL = "https://$TENCENT_VEDIO_WEBSITE_HOSTNAME/"

val TENCENT_DOMAINS = arrayOf("*.tencent.com", "*.qq.com")
val WEBSITES = arrayOf(
    TENCENT_WEBSITE_URL,
    QQ_WEBSITE_URL,
    WECHAT_WEBSITE_URL,
    TENCENT_SPORTS_WEBSITE_URL,
    TENCENT_VEDIO_WEBSITE_URL
)
