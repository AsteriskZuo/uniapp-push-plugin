package com.hyphenate.push.platform.meizu

import android.content.Context
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
                if (pushToken.isNullOrEmpty()){
                    PushManager.register(context, config.mzAppId, config.mzAppKey)
                }else{
                    resultListener?.getPushTokenSuccess(PushType.MEIZUPUSH, pushToken)
                }
            }else{
                resultListener?.onError(PushType.MEIZUPUSH,-9,"support is false")
            }
        }
    }

    override fun onUnregister(context: Context?) {

    }

    override fun onGetNotifierName(config: PushConfig): String {
        return config.mzAppId
    }
}
