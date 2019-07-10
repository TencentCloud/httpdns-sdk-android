package com.tencent.httpdns.network.base

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.tencent.beacon.event.UserAction
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.DnsService

// NOTE: Sample使用ContentProvider在App初始化时初始化SDK, 仅是为了Sample可以保持架构干净, 避免app module需要了解network相关module的实现细节
class DnsServiceInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        if (!DnsServiceWrapper.useHttpDns) {
            return true
        }

        DnsConfig
            .Builder()
            .logLevel(LOG_LEVEL)
            .apply {
                if (!TextUtils.isEmpty(APP_ID)) {
                    appId(APP_ID)
                }
            }
            .userId(USER_ID)
            // 初始化灯塔
            .initBuiltInReporters()
            .apply {
                if (!TextUtils.isEmpty(DNS_ID)) {
                    dnsId(DNS_ID)
                }
                if (!TextUtils.isEmpty(DNS_KEY)) {
                    dnsKey(DNS_KEY)
                }
            }
            .timeoutMills(TIMEOUT_MILLS)
            .protectedDomains(*PROTECTED_DOMAINS)
            .preLookupDomains(*PRE_LOOKUP_DOMAINS)
            .asyncLookupDomains(*ASYNC_LOOKUP_DOMAINS)
            .apply {
                if (USE_UDP) udp() else http()
                if (BLOCK_FIRST) blockFirst() else nonBlockFirst()
            }
            .executorSupplier(EXECUTOR_SUPPLIER)
            .lookedUpListener(LOOKED_UP_LISTENER)
            .logNode(LOG_NODE)
            .build()
            .let { DnsService.init(context, it) }

        return true
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues): Uri? = null

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ) = INVALID_NUM_OF_ROW

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) =
        INVALID_NUM_OF_ROW

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = null

    companion object {

        const val INVALID_NUM_OF_ROW = -1
    }
}
