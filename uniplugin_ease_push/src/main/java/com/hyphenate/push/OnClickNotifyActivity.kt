package com.hyphenate.push

import android.app.Activity
import android.os.Bundle
import android.util.Log
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
            jsonObject["msgId"] = m
            val g: String = it.getString("g","")
            jsonObject["groupId"] = g
            val bundles = it.getBundle("e")
            bundles?.let {
                val map =  it.keySet()?.associateWith { bundle.get(it) }
                map?.let {
                    Log.d("OnClickNotifyActivity","ext: $it")
                    jsonObject["ext"] = it
                }
            }
        }
        PushHelper.sendNotificationEvent(jsonObject,1)
        PushHelper.saveNotifyData(jsonObject,1)
        PushHelper.launchApp(this)
        this@OnClickNotifyActivity.finish()
    }

}