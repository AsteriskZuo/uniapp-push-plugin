package com.hyphenate.push.platform.vivo

import android.content.Context
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.platform.IPush
import com.vivo.push.PushClient
import com.vivo.push.listener.IPushQueryActionListener
import com.vivo.push.util.VivoPushException

class ViVoPush : IPush() {

    override fun getPushType(): PushType {
        return PushType.VIVOPUSH
    }

    override fun getPushToken(context: Context): String? {
        return pushToken
    }

    override fun onRegister(context: Context?, config: PushConfig) {
        context?.let {
            try {
                PushClient.getInstance(it).checkManifest()
                val build = com.vivo.push.PushConfig.Builder().agreePrivacyStatement(true).build()
                PushClient.getInstance(it).initialize(build)
                PushClient.getInstance(it).turnOnPush{ state->
                    if (state == 0) {
                        PushClient.getInstance(it).getRegId(object : IPushQueryActionListener{
                            override fun onSuccess(regId: String?) {
                                resultListener?.getPushTokenSuccess(PushType.VIVOPUSH, regId)
                            }

                            override fun onFail(error: Int) {
                                resultListener?.getPushTokenFail(PushType.VIVOPUSH, error, "")
                            }
                        })
                    }else{
                        resultListener?.getPushTokenFail(PushType.VIVOPUSH, state, " state != 0")
                    }
                }
            }catch (e: VivoPushException){
                resultListener?.onError(PushType.VIVOPUSH, e.code, e.message)
            }
        }?:kotlin.run {
            resultListener?.onError(PushType.VIVOPUSH,2000,"push plugin context is null.")
        }
    }

    override fun onUnregister(context: Context?, config: PushConfig) {

    }

    override fun onGetNotifierName(config: PushConfig): String {
        return "${config.vivoAppId}#${config.vivoAppKey}"
    }
}