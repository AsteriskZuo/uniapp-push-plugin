package com.hyphenate.push


enum class PushType(val value:String) {
    /** \~chinese 谷歌推送。  \~english The FCM push.  */
    FCM("FCM"),
    /** \~chinese 小米推送。  \~english The Mi push.  */
    MIPUSH("MIPUSH"),
    /** \~chinese 华为推送。  \~english The HUAWEI push.  */
    HMSPUSH("HMSPUSH"),
    /** \~chinese 魅族推送。  \~english The MEIZU push.  */
    MEIZUPUSH("MEIZUPUSH"),
    /** \~chinese OPPO 推送。  \~english The OPPO push.  */
    OPPOPUSH("OPPOPUSH"),
    /** \~chinese VIVO 推送。  \~english The VIVO push.  */
    VIVOPUSH("VIVOPUSH"),
    /** \~chinese 荣耀推送。  \~english The HONOR push.  */
    HONORPUSH("HONORPUSH"),
    NORMAL("NORMAL");

    /**
     * \~chinese
     * 获取推送类型。
     * @return  推送类型。
     *
     * \~english
     * Gets the push type.
     * @return  The push type.
     */
    companion object {
        fun from(value: String): PushType {
            val types = PushType.values()
            val length = types.size
            for (i in 0 until length) {
                val type = types[i]
                if (type.value == value) {
                    return type
                }
            }
            return NORMAL
        }
    }
}