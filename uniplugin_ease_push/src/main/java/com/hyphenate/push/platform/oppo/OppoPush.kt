package com.hyphenate.push.platform.oppo

import android.content.Context
import android.util.Log
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
            try {
                HeytapPushManager.init(it,true)
                HeytapPushManager.register(it,config.oppoAppKey,config.oppoAppSecret,object : ICallBackResultService{
                    override fun onRegister(responseCode: Int, registerID: String?, packageName: String?, miniPackageName: String?) {
                        if (responseCode == 0) {
                            resultListener?.getPushTokenSuccess(registerID)
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
                        resultListener?.getPushTokenFail(responseCode,error)
                    }
                })
            }catch (e:Exception){
                resultListener?.getPushTokenFail(1111,e.message)
            }

        }?:kotlin.run {
            resultListener?.onError(PushType.OPPOPUSH,-4,"")
        }

    }

    override fun onUnregister(context: Context?) {
        HeytapPushManager.unRegister()
    }

    override fun onGetNotifierName(config: PushConfig): String {
        return config.oppoAppKey
    }

}