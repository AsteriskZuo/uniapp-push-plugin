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
                                resultListener?.getPushTokenSuccess(regId)
                            }

                            override fun onFail(error: Int) {
                                resultListener?.getPushTokenFail(error,"")
                            }
                        })
                    }else{
                        resultListener?.getPushTokenFail(state," state != 0")
                    }
                }
            }catch (e: VivoPushException){
                resultListener?.getPushTokenFail(e.code,e.message)
            }
        }
    }

    override fun onUnregister(context: Context?) {
        context?.let {

        }
    }

    override fun onGetNotifierName(config: PushConfig): String {
        return "${config.vivoAppId}#${config.vivoAppKey}"
    }
}