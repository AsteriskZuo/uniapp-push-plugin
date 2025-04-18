package com.hyphenate.push.platform.oppo

import android.content.Context
import com.heytap.msp.push.HeytapPushManager
import com.heytap.msp.push.callback.ICallBackResultService
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.platform.IPush

class OppoPush: IPush() {

    override fun getPushType(): PushType {
        return PushType.OPPOPUSH
    }

    override fun getPushToken(context: Context): String? {
        return HeytapPushManager.getRegisterID()
    }

    override fun onRegister(context: Context?, config: PushConfig) {
        context?.let {
            HeytapPushManager.init(it,true)
            HeytapPushManager.register(it,config.oppoAppKey,config.oppoAppSecret,object : ICallBackResultService{
                override fun onRegister(responseCode: Int, registerID: String?, packageName: String?, miniPackageName: String?) {
                    if (responseCode == 0) {
                        resultListener?.getPushTokenSuccess(PushType.OPPOPUSH, registerID)
                    }
                }

                override fun onUnRegister(responseCode: Int, packageName: String?, miniPackageName: String?) {

                }

                override fun onSetPushTime(responseCode: Int, pushTime: String?) {

                }

                override fun onGetPushStatus(responseCode: Int, status: Int) {

                }

                override fun onGetNotificationStatus(responseCode: Int, status: Int) {

                }

                override fun onError(responseCode: Int, error: String?, packageName: String?, miniPackageName: String?) {
                    resultListener?.getPushTokenFail(PushType.OPPOPUSH, responseCode, error)
                }
            })
        }?:kotlin.run {
            resultListener?.onError(PushType.OPPOPUSH,2000,"push plugin context is null.")
        }

    }

    override fun onUnregister(context: Context?, config: PushConfig) {
        context.let {
            HeytapPushManager.unRegister()
        }
    }

    override fun onGetNotifierName(config: PushConfig): String {
        return config.oppoAppKey
    }

}