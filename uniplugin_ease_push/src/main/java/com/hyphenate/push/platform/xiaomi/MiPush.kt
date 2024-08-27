package com.hyphenate.push.platform.xiaomi

import android.content.Context
import android.util.Log
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.platform.IPush
import com.xiaomi.mipush.sdk.MiPushClient


class MiPush: IPush() {

    override fun getPushType(): PushType {
        return PushType.MIPUSH
    }

    override fun getPushToken(context: Context): String? {
        return MiPushClient.getRegId(context)
    }

    override fun onRegister(context: Context?, config: PushConfig) {
        Log.d("MiPush","MiPush onRegister $context  ${config.xiaomiAppId} - ${config.xiaomiAppKey}")
        context?.let {
            pushToken = MiPushClient.getRegId(context)
            Log.d("MiPush","MiPush onRegister pushToken:$pushToken - ${config.xiaomiAppId} - ${config.xiaomiAppKey}")
            if (pushToken.isNullOrEmpty()){
                MiPushClient.registerPush(context, config.xiaomiAppId, config.xiaomiAppKey)
                Log.e("MiPush","registerPush end")
                pushToken = MiPushClient.getRegId(context)
            }else{
                resultListener?.getPushTokenSuccess(PushType.MIPUSH, pushToken)
            }
        }?:kotlin.run {
            resultListener?.onError(PushType.HONORPUSH,2000,"push plugin context is null.")
        }
    }

    override fun onUnregister(context: Context?) {
        context?.let {
            MiPushClient.unregisterPush(it)
        }
    }

    override fun onGetNotifierName(config: PushConfig): String {
        return config.xiaomiAppId
    }
}
