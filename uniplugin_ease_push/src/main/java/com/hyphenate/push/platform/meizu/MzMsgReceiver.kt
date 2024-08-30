package com.hyphenate.push.platform.meizu

import android.content.Context
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.hyphenate.push.PushType
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
                PushHelper.sendRenewTokenEvent(PushType.MEIZUPUSH,token)
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

    override fun onNotificationClicked(context: Context?, mzPushMessage: MzPushMessage?) {
        val jsonObject = JSONObject()
        val extStr = mzPushMessage?.content
        val extras = JSONObject.parseObject(extStr)
        if (extras != null) {
            val t: String = extras.getString("t")
            jsonObject["to"] = t
            val f: String = extras.getString("f")
            jsonObject["from"] = f
            val m: String = extras.getString("m")
            jsonObject["msgId"] = m
            val g: String = extras.getString("g")
            jsonObject["groupId"] = g
            val e: Any = extras.getJSONObject("e")
            jsonObject["ext"] = e
            PushHelper.sendNotificationEvent(jsonObject,1)
            PushHelper.saveNotifyData(jsonObject,1)
            context?.let { PushHelper.launchApp(it) }
        }
    }

}