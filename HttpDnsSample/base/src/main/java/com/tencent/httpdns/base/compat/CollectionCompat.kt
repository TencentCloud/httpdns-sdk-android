package com.tencent.httpdns.base.compat

import android.os.Build
import android.util.ArrayMap
import android.util.ArraySet

object CollectionCompat {

    // NOTE: HashMap/HashSet有参数和无参数两个版本的构造方法实现不同

    fun <K, V> createMap() =
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) ArrayMap<K, V>()
        else HashMap<K, V>()

    fun <K, V> createMap(capacity: Int) =
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) ArrayMap<K, V>(capacity)
        else HashMap<K, V>(capacity)

    fun <E> createSet() =
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) ArraySet<E>()
        else HashSet<E>()

    fun <E> createSet(capacity: Int) =
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) ArraySet<E>(capacity)
        else HashSet<E>(capacity)
}
