package com.hyphenate.push.platform.vivo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hyphenate.push.common.PushConstants
import com.hyphenate.push.common.PushHelper
import com.vivo.push.model.UPSNotificationMessage
import com.vivo.push.sdk.OpenClientPushMessageReceiver

class ViVoMsgReceiver: OpenClientPushMessageReceiver() {

    override fun onNotificationMessageClicked(context: Context?, msg: UPSNotificationMessage?) {

    }

    override fun onReceiveRegId(context: Context?, regId: String?) {
        context?.let {
            if (regId.isNullOrEmpty().not()) {
                //没有失败回调，假定token失败时token为null
                Log.d("ViVoMsgReceiver", "service register vivo push token success token:$regId")
                PushHelper.saveRenewToken(regId)
                PushHelper.sendCacheRenewToken()
//                val intent = Intent(PushConstants.ACTION_SERVICE_ON_NEW_TOKEN)
//                intent.putExtra(PushConstants.PUSH_TOKEN, regId)
//                LocalBroadcastManager.getInstance(it).sendBroadcast(intent)
            } else {
                Log.e("ViVoMsgReceiver", "service register honor push token fail!")
            }
        }
    }

}