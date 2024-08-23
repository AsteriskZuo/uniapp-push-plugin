package com.hyphenate.push

import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.hyphenate.push.common.Notifier
import com.hyphenate.push.common.PushConstants
import com.hyphenate.push.common.PushHelper
import com.hyphenate.push.platform.IPush
import com.hyphenate.push.platform.OnTokenResultListener
import com.taobao.weex.bridge.JSCallback
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback
import io.dcloud.feature.uniapp.common.UniDestroyableModule


class PushModule: UniDestroyableModule() {

    companion object{
        const val TAG = "PushModule"
    }

    val pushConfig by lazy { PushConfig() }
    var uniContext: Context? = null
    var pushClient: IPush? = null
    var receiver: PushEventReceiver? = null

    private fun updatePluginStatus() {
        if (uniContext == null){
            uniContext = mUniSDKInstance.context
        }
        PushHelper.IS_DESTROY = false
        val packageName = uniContext?.packageName
        Log.e(TAG,"packageName $packageName")
    }

    @UniJSMethod(uiThread = true)
    fun initPushModule(callback: UniJSCallback?){
        updatePluginStatus()
        Log.e(TAG,"initPushModule start")
        uniContext?.applicationContext?.let {
            Log.e("apex","create Notifier")
            Notifier(it)
        }
        Log.e(TAG,"initPushModule checkPushConfig")
        pushConfig.checkPushConfig(uniContext,callback)
        Log.e(TAG,"initPushModule end")
    }

    @UniJSMethod(uiThread = true)
    fun getInfo(callback: UniJSCallback?){
        updatePluginStatus()
        pushConfig.getInfo(uniContext,callback)
    }

    @UniJSMethod(uiThread = false)
    fun onRegister(callback: UniJSCallback?){
        updatePluginStatus()
        Log.e(TAG,"onRegister")
        pushClient = PushHelper.getPushClient(pushConfig)
        pushClient?.register(uniContext, pushConfig)
        val jsonObject = JSONObject()
        pushClient?.setTokenResultListener(object : OnTokenResultListener {
            override fun getPushTokenSuccess(pushToken: String?) {
                Log.e(TAG,"getPushTokenSuccess:${pushToken}")
                jsonObject[PushConstants.PUSH_TOKEN] = pushToken
                jsonObject[PushConstants.CODE] = PushConstants.CODE_SUCCESS
                callback?.invoke(jsonObject)
            }

            override fun getPushTokenFail(code: Int, error: String?) {
                Log.e(TAG,"getPushTokenFail:$code $error")
                jsonObject[PushConstants.CODE] = code
                jsonObject[PushConstants.ERROR] = error
                callback?.invoke(jsonObject)
            }

            override fun onError(type: PushType, code: Int, error: String?) {
                Log.e(TAG,"onError: ${type.name} $code $error")
                jsonObject[PushConstants.CODE] = code
                jsonObject[PushConstants.ERROR] = error
                callback?.invoke(jsonObject)
            }
        })
    }

    @UniJSMethod(uiThread = true)
    fun getToken(callback: UniJSCallback?){
        updatePluginStatus()
        val jsonObject = JSONObject()
        pushClient = PushHelper.getPushClient(pushConfig)
        val token = uniContext?.let { pushClient?.getPushToken(it) }
        jsonObject[PushConstants.PUSH_TOKEN] = token?:""
        callback?.invoke(jsonObject)
    }

    @UniJSMethod(uiThread = true)
    fun addNotificationListener(callback: JSCallback?) {
        updatePluginStatus()
        callback?.let {
            Log.d("apex","addNotificationListener")
            PushHelper.eventCallback[PushConstants.NOTIFICATION_EVENT] = it
            PushHelper.sendCacheNotify(0)
        }
    }

    @UniJSMethod(uiThread = true)
    fun addOnNewTokenListener(callback: JSCallback?){
        updatePluginStatus()
        callback?.let {
            PushHelper.onNewTokenCallback[PushConstants.NOTIFICATION_RENEW_TOKEN] = it
            PushHelper.sendCacheRenewToken()
        }
    }

//    @UniJSMethod(uiThread = true)
//    fun clearAllNotifications() {
//        updatePluginStatus()
//        // todo clearAllNotifications clearAllNotifications(mWXSDKInstance.getContext())
//    }
//
//    @UniJSMethod(uiThread = true)
//    fun clearNotificationById(readableMap: JSONObject?) {
//        updatePluginStatus()
//        if (readableMap == null) {
//            Log.w("","PARAMS_NULL")
//            return
//        }
//        if (readableMap.containsKey(PushConstants.NOTIFICATION_ID)) {
//            val id = readableMap.getIntValue(PushConstants.NOTIFICATION_ID)
//            // todo clearNotificationById clearNotificationById(mWXSDKInstance.context, id)
//        } else {
//            Log.w("","there are no id")
//        }
//    }

    //用于页面监听持久性事件，例如定位信息，陀螺仪等的变化。
    @UniJSMethod(uiThread = true)
    fun onNotificationClick(data: String?){
        data?.let {
            val params: MutableMap<String, Any> = HashMap()
            params["result"] = it
            // 注意 globalEvent事件只能通过页面的UniSDKInstance实例给当前页面发送globalEvent事件。其他页面无法接受。
            mUniSDKInstance.fireGlobalEventCallback("onNotificationClick", params)
        }
    }

    override fun destroy() {
        PushHelper.IS_DESTROY = true
        uniContext?.applicationContext?.unregisterReceiver(receiver)
    }
}