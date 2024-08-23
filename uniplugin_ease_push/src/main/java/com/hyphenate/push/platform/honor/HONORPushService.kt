package com.hyphenate.push.platform.honor

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hihonor.push.sdk.HonorMessageService
import com.hihonor.push.sdk.HonorPushDataMsg
import com.hyphenate.push.common.PushConstants
import com.hyphenate.push.common.PushHelper

class HONORPushService : HonorMessageService() {
    //Token发生变化时，会以onNewToken方法返回
    override fun onNewToken(token: String?) {
        if (token.isNullOrEmpty().not()) {
            //没有失败回调，假定token失败时token为null
            Log.d("HONORPush", "service register honor push token success token:$token")
            PushHelper.saveRenewToken(token)
            PushHelper.sendCacheRenewToken()
//            val intent = Intent(PushConstants.ACTION_SERVICE_ON_NEW_TOKEN)
//            intent.putExtra(PushConstants.PUSH_TOKEN, token)
//            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        } else {
            Log.e("HONORPush", "service register honor push token fail!")
        }
    }

    override fun onMessageReceived(honorPushDataMsg: HonorPushDataMsg) {
        Log.d("HONORPush", "onMessageReceived" + honorPushDataMsg.data)
    }

}