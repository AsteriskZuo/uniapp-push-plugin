package com.hyphenate.push.platform.honor

import android.content.Context
import com.hihonor.push.sdk.HonorPushCallback
import com.hihonor.push.sdk.HonorPushClient
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.platform.IPush

class HonorPush: IPush() {

    override fun getPushType(): PushType {
        return PushType.HONORPUSH
    }

    override fun onRegister(context: Context?, config: PushConfig) {
        context?.let {
            val isSupportHonor = HonorPushClient.getInstance().checkSupportHonorPush(it)
            if (isSupportHonor) {
                // true，调用初始化接口时SDK会同时进行异步请求PushToken。会触发HonorMessageService.onNewToken(String)回调。
                // false，不会异步请求PushToken，需要应用主动请求获取PushToken。
                HonorPushClient.getInstance().init(it, false)
                HonorPushClient.getInstance().getPushToken(object : HonorPushCallback<String> {
                    override fun onSuccess(token: String?) {
                        pushToken = token
                        resultListener?.getPushTokenSuccess(PushType.HONORPUSH, token)
                    }

                    override fun onFailure(code: Int, error: String?) {
                        resultListener?.getPushTokenFail(PushType.HONORPUSH, code, error)
                    }
                } )
            }else{
                resultListener?.onError(PushType.HONORPUSH,2003,"isSupportPush false")
            }
        }?:kotlin.run {
            resultListener?.onError(PushType.HONORPUSH,2000,"push plugin context is null.")
        }

    }

    override fun onUnregister(context: Context?, config: PushConfig) {

    }

    override fun onGetNotifierName(config: PushConfig): String {
        return config.honorAppId
    }

    override fun getPushToken(context: Context): String? {
        return pushToken
    }

}