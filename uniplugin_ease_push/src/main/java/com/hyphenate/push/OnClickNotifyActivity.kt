package com.hyphenate.push

import android.app.Activity
import android.os.Bundle
import com.alibaba.fastjson.JSONObject
import com.hyphenate.push.common.PushHelper

class OnClickNotifyActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotifyClick()
    }

    private fun handleNotifyClick(){
        val jsonObject = JSONObject()
        val bundle = intent.extras
        bundle?.let {
            val t: String = it.getString("t","")
            jsonObject["to"] = t
            val f: String = it.getString("f","")
            jsonObject["from"] = f
            val m: String = it.getString("m","")
            jsonObject["msg"] = m
            val g: String = it.getString("g","")
            jsonObject["groupId"] = g
            val e = it.getBundle("e")?.let { bundle ->
                bundle.keySet().associateWith { bundle.getString(it) }
            }?:kotlin.run { "" }
            jsonObject["ext"] = e
        }
        PushHelper.sendNotificationEvent(jsonObject,0)
        PushHelper.saveNotifyData(jsonObject,0)
        PushHelper.launchApp(this)
    }

}