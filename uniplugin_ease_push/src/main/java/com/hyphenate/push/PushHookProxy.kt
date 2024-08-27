package com.hyphenate.push

import android.app.Application
import android.util.Log
import com.hyphenate.push.common.Notifier
import com.hyphenate.push.common.PushHelper
import com.taobao.weex.WXSDKEngine
import com.taobao.weex.common.WXException
import io.dcloud.feature.uniapp.UniAppHookProxy

class PushHookProxy: UniAppHookProxy {

    override fun onCreate(application: Application?) {
        Log.e("apex","PushHookProxy onCreate")
        PushHelper.registerActivityLifecycle(application)
        application?.applicationContext?.let { Notifier(it) }
        //可写初始化触发逻辑
        try{
            WXSDKEngine.registerModule("apexPlugin",PushModule::class.java)
        }catch (e: WXException){
            e.printStackTrace();
        }
    }

    override fun onSubProcessCreate(application: Application?) {
        Log.e("apex","onSubProcessCreate")
    }

}