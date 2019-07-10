package com.tencent.httpdns.network

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

// NOTE: Sample使用ContentProvider在App初始化时初始化VolleyHelper, 仅是为了Sample可以保持架构干净, 避免app module需要了解network相关module的实现细节
class VolleyHelperInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        VolleyHelper.init(context)
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
