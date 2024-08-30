package com.hyphenate.push.platform.huawei

import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.hyphenate.push.PushType
import com.hyphenate.push.common.PushHelper

class HMSPushService: HmsMessageService() {

    override fun onNewToken(token: String?) {
        if (token.isNullOrEmpty().not()) {
            //没有失败回调，假定token失败时token为null
            Log.d("HWHMSPush", "service register huawei hms push token success token:$token")
            PushHelper.sendRenewTokenEvent(PushType.HMSPUSH,token)
        } else {
            Log.e("HWHMSPush", "service register huawei hms push token fail!")
        }
    }

}