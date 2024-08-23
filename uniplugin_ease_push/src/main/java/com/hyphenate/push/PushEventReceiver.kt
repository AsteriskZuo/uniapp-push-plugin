package com.hyphenate.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hyphenate.push.common.PushConstants
import io.dcloud.feature.uniapp.AbsSDKInstance

class PushEventReceiver constructor(
    private val mUniSDKInstance : AbsSDKInstance
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("apex","PushEventReceiver")
        val params: MutableMap<String, Any> = HashMap()
        val action = intent?.action
        val extras = intent?.extras
        Log.e("apex","PushEventReceiver action:$action -  extras:$extras")
        if (action == PushConstants.ACTION_SERVICE_ON_NEW_TOKEN){
            extras?.let {
                if (it.containsKey(PushConstants.PUSH_TOKEN)){
                    params[PushConstants.PUSH_TOKEN] = it.getString(PushConstants.PUSH_TOKEN,"")
                    // 注意 globalEvent事件只能通过页面的UniSDKInstance实例给当前页面发送globalEvent事件。其他页面无法接受。
                    mUniSDKInstance.fireGlobalEventCallback("onNewToken", params)
                }
            }
        }else if (action == PushConstants.ACTION_SERVICE_ON_CLICK){
            extras?.let {
                if (it.containsKey(PushConstants.PUSH_EVENT)){
                    params[PushConstants.PUSH_EVENT] = it.getString(PushConstants.PUSH_EVENT,"")
                    // 注意 globalEvent事件只能通过页面的UniSDKInstance实例给当前页面发送globalEvent事件。其他页面无法接受。
                    mUniSDKInstance.fireGlobalEventCallback("onNotificationClick", params)
                }
            }
        }

    }


}