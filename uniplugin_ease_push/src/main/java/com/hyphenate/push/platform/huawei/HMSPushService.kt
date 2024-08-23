package com.hyphenate.push.platform.huawei

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.huawei.hms.push.HmsMessageService
import com.hyphenate.push.common.PushConstants
import com.hyphenate.push.common.PushHelper

class HMSPushService: HmsMessageService() {

    override fun onNewToken(token: String?) {
        if (token.isNullOrEmpty().not()) {
            //没有失败回调，假定token失败时token为null
            Log.d("HWHMSPush", "service register huawei hms push token success token:$token")
            PushHelper.saveRenewToken(token)
            PushHelper.sendCacheRenewToken()
//            val intent = Intent(PushConstants.ACTION_SERVICE_ON_NEW_TOKEN)
//            intent.putExtra(PushConstants.PUSH_TOKEN, token)
//            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        } else {
            Log.e("HWHMSPush", "service register huawei hms push token fail!")
        }
    }

}