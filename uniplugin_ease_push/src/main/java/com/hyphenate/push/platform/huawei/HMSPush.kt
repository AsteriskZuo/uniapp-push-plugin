package com.hyphenate.push.platform.huawei

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.hihonor.push.sdk.common.data.ApiException
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.common.PushHelper
import com.hyphenate.push.platform.IPush

class HMSPush: IPush() {

    override fun getPushType(): PushType {
        return PushType.HMSPUSH
    }

    override fun getPushToken(context: Context): String? {
        pushToken = PushHelper.getHMSToken(context)
        return pushToken
    }

    override fun onRegister(context: Context?, config: PushConfig) {
        context?.let {
            getToken(it,config.hwAppId)
        }?:kotlin.run {
            resultListener?.onError(PushType.HMSPUSH,2000,"push plugin context is null.")
        }
    }

    override fun onUnregister(context: Context?) {

    }

    override fun onGetNotifierName(config: PushConfig): String {
        return config.hwAppId
    }

    private fun getToken(context: Context,id:String = "") {
        object : Thread() {
            override fun run() {
                try {
                    val appId = id.ifEmpty {
                        AGConnectOptionsBuilder().build(context).getString("client/app_id")
                    }
                    Log.d(PushHelper.TAG,"appId:$appId")
                    val tokenScope = "HCM"
                    val token = HmsInstanceId.getInstance(context).getToken(appId, tokenScope)
                    Log.i(TAG, "get token success")
                    if (!TextUtils.isEmpty(token)) {
                        pushToken = token
                        resultListener?.getPushTokenSuccess(PushType.HMSPUSH, token)
                    }
                } catch (e: ApiException) {
                    Log.e(TAG, "get token failed, $e")
                    resultListener?.getPushTokenFail(PushType.HMSPUSH, e.errorCode, e.message)
                }
            }
        }.start()
    }
}