package com.tencent.httpdns.base.log

internal object Log {

    private val LOG_NODE_LIST = mutableListOf<ILogNode>()

    init {
        LOG_NODE_LIST.add(AndroidLogNode())
    }

    fun addLogNode(logNode: ILogNode) {
        LOG_NODE_LIST.add(logNode)
    }

    fun println(priority: Int, tag: String, msg: String, tr: Throwable?) {
        for (logNode in LOG_NODE_LIST) {
            logNode.println(priority, tag, msg, tr)
        }
    }
}
