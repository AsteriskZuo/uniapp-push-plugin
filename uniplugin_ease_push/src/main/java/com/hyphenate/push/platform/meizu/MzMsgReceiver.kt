package com.hyphenate.push.platform.meizu

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hyphenate.push.common.PushConstants
import com.hyphenate.push.common.PushHelper
import com.meizu.cloud.pushsdk.MzPushMessageReceiver
import com.meizu.cloud.pushsdk.handler.MzPushMessage
import com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus
import com.meizu.cloud.pushsdk.platform.message.SubAliasStatus
import com.meizu.cloud.pushsdk.platform.message.SubTagsStatus
import com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus

class MzMsgReceiver: MzPushMessageReceiver() {

    override fun onRegisterStatus(context: Context?, registerStatus: RegisterStatus?) {
        context?.let {
            // token 过期需要重新#register()获取新的Id，目前sdk内部无法知道Id过期事件。
            val token: String? = registerStatus?.pushId
            if (token.isNullOrEmpty().not()) {
                //没有失败回调，假定token失败时token为null
                Log.d("MzMsgReceiver", "service register honor push token success token:$token")
                PushHelper.saveRenewToken(token)
                PushHelper.sendCacheRenewToken()
//                val intent = Intent(PushConstants.ACTION_SERVICE_ON_NEW_TOKEN)
//                intent.putExtra(PushConstants.PUSH_TOKEN, token)
//                LocalBroadcastManager.getInstance(it).sendBroadcast(intent)
            } else {
                Log.e("MzMsgReceiver", "service register honor push token fail!")
            }
        }
    }

    override fun onUnRegisterStatus(context: Context?, status: UnRegisterStatus?) {

    }

    override fun onPushStatus(context: Context?, status: PushSwitchStatus?) {

    }

    override fun onSubTagsStatus(context: Context?, status: SubTagsStatus?) {

    }

    override fun onSubAliasStatus(context: Context?, status: SubAliasStatus?) {

    }

    override fun onNotificationClicked(p0: Context?, p1: MzPushMessage?) {

    }

}