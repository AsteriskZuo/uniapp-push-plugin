package com.hyphenate.push.platform.xiaomi

import android.content.Context
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.hyphenate.push.PushType
import com.hyphenate.push.common.PushHelper
import com.xiaomi.mipush.sdk.ErrorCode
import com.xiaomi.mipush.sdk.MiPushClient
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import com.xiaomi.mipush.sdk.PushMessageReceiver

class MiPushService : PushMessageReceiver() {


    override fun onNotificationMessageClicked(context: Context?, miPushMessage: MiPushMessage?) {
        Log.e("MiPushService","Mi onNotificationMessageClicked $context")
        val jsonObject = JSONObject()
        val extStr = miPushMessage?.content
        val extras = JSONObject.parseObject(extStr)
        Log.e("MiPushService","Mi onNotificationMessageClicked $extras")
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
                        PushHelper.sendRenewTokenEvent(PushType.MIPUSH,cmdArg1)
                    }
                }
            }
        }
    }

}