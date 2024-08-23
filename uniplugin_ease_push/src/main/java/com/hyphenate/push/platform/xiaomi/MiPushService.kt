package com.hyphenate.push.platform.xiaomi

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hyphenate.push.common.PushConstants
import com.hyphenate.push.common.PushHelper
import com.xiaomi.mipush.sdk.ErrorCode
import com.xiaomi.mipush.sdk.MiPushClient
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import com.xiaomi.mipush.sdk.PushMessageReceiver

class MiPushService : PushMessageReceiver() {


    override fun onNotificationMessageClicked(context: Context?, msg: MiPushMessage?) {

    }

    override fun onReceiveRegisterResult(context: Context?, msg: MiPushCommandMessage?) {
        Log.e("MiPushService","Mi onReceiveRegisterResult")
        context?.let {
            val command = msg?.command
            val arguments = msg?.commandArguments
            val cmdArg1 = if (arguments != null && arguments.size > 0) arguments[0] as String else null
            Log.e("MiPushService","Mi onReceiveRegisterResult $cmdArg1")
            if (MiPushClient.COMMAND_REGISTER == command) {
                if (msg.resultCode == ErrorCode.SUCCESS.toLong()) {
                    if (cmdArg1.isNullOrEmpty().not()){
                        PushHelper.saveRenewToken(cmdArg1)
                        PushHelper.sendCacheRenewToken()
//                        val intent = Intent(PushConstants.ACTION_SERVICE_ON_NEW_TOKEN)
//                        intent.putExtra(PushConstants.PUSH_TOKEN, cmdArg1)
//                        LocalBroadcastManager.getInstance(it).sendBroadcast(intent)
                    }
                }
            }
        }
    }

}