package com.hyphenate.push.platform.meizu

import android.content.Context
import android.util.Log
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.platform.IPush
import com.meizu.cloud.pushsdk.PushManager
import com.meizu.cloud.pushsdk.util.MzSystemUtils

class MzPush: IPush() {

    override fun getPushType(): PushType {
        return PushType.MEIZUPUSH
    }

    override fun getPushToken(context: Context): String? {
        pushToken = PushManager.getPushId(context)
        return pushToken
    }

    override fun onRegister(context: Context?, config: PushConfig) {
        context?.let {
            val support = MzSystemUtils.isBrandMeizu(it)
            if (support){
                pushToken = PushManager.getPushId(context)
                Log.e(TAG,"pushToken:$pushToken ${config.mzAppId} - ${config.mzAppKey}")
                if (pushToken.isNullOrEmpty()){
                    PushManager.register(context, config.mzAppId, config.mzAppKey)
                }else{
                    resultListener?.getPushTokenSuccess(PushType.MEIZUPUSH, pushToken)
                }
            }else{
                resultListener?.onError(PushType.MEIZUPUSH,2003,"isSupportPush false")
            }
        }?:kotlin.run {
            resultListener?.onError(PushType.MEIZUPUSH,2000,"push plugin context is null.")
        }
    }

    override fun onUnregister(context: Context?, config: PushConfig) {
        if (config.mzAppId.isNotEmpty() && config.mzAppKey.isNotEmpty()){
            PushManager.unRegister(context,config.mzAppId,config.mzAppKey)
        }
    }

    override fun onGetNotifierName(config: PushConfig): String {
        return config.mzAppId
    }
}
