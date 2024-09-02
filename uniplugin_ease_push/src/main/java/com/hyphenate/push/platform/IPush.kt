package com.hyphenate.push.platform

import android.content.Context
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType

abstract class IPush {
    companion object{
        const val TAG = "IPush"
    }

    var notifierName: String = ""
    var pushToken: String? = ""
    var resultListener: OnTokenResultListener? = null

    open fun register(
        context: Context?,
        config: PushConfig
    ) {
        notifierName = onGetNotifierName(config)
        onRegister(context, config)
    }

    open fun unregister(context: Context?,config: PushConfig) {
        onUnregister(context, config)
    }

    fun setTokenResultListener(listener: OnTokenResultListener){
        this.resultListener = listener
    }

    abstract fun getPushType(): PushType?

    abstract fun getPushToken(context: Context): String?

    abstract fun onRegister(context: Context?, config: PushConfig)

    abstract fun onUnregister(context: Context?,config: PushConfig)

    abstract fun onGetNotifierName(config: PushConfig): String
}