package com.hyphenate.push

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.alibaba.fastjson.JSONObject
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
    }

    @UniJSMethod(uiThread = true)
    fun initPushModule(callback: UniJSCallback?){
        updatePluginStatus()
        pushConfig.checkPushConfig(uniContext,callback)
    }

    @UniJSMethod(uiThread = true)
    fun clearAllNotification(){
        updatePluginStatus()
        uniContext?.applicationContext?.let {
            val notificationManager = it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll() // 清除所有通知
        }
    }

    @UniJSMethod(uiThread = false)
    fun onRegister(callback: UniJSCallback?){
        updatePluginStatus()
        Log.d(TAG,"onRegister")
        pushClient = PushHelper.getPushClient(pushConfig)
        pushClient?.register(uniContext, pushConfig)
        pushClient?.setTokenResultListener(object : OnTokenResultListener {
            override fun getPushTokenSuccess(pushType: PushType, pushToken: String?) {
                Log.d(TAG,"getPushTokenSuccess:${pushType.name} $pushToken")
                callback?.let {
                    PushHelper.saveRenewToken(pushToken,pushType)
                    PushHelper.onNewTokenCallback[PushConstants.NOTIFICATION_RENEW_TOKEN] = it
                    PushHelper.sendCacheRenewToken()
                }
            }

            override fun getPushTokenFail(pushType: PushType, code: Int, error: String?) {
                Log.e(TAG,"getPushTokenFail:${pushType.name} $code $error")
                callback?.let {
                    PushHelper.saveRenewToken("",pushType,code,error)
                    PushHelper.onNewTokenCallback[PushConstants.NOTIFICATION_RENEW_TOKEN] = it
                    PushHelper.sendCacheRenewToken()
                }
            }

            override fun onError(type: PushType, code: Int, error: String?) {
                Log.e(TAG,"onError: ${type.name} $code $error")
                callback?.let {
                    PushHelper.saveRenewToken("",type,code,error)
                    PushHelper.onNewTokenCallback[PushConstants.NOTIFICATION_RENEW_TOKEN] = it
                    PushHelper.sendCacheRenewToken()
                }
            }
        })
    }

    @UniJSMethod(uiThread = true)
    fun getToken(callback: UniJSCallback?){
        updatePluginStatus()
        val jsonObject = JSONObject()
        val type = PushHelper.getPreferPushType(pushConfig)
        pushClient = PushHelper.getPushClient(pushConfig)
        val token = uniContext?.let { pushClient?.getPushToken(it) }
        token?.let {
            jsonObject[PushConstants.PUSH_TYPE] = type
            jsonObject[PushConstants.CODE] = PushConstants.CODE_SUCCESS
            jsonObject[PushConstants.PUSH_TOKEN] = token
            callback?.invoke(jsonObject)
        }
    }

    @UniJSMethod(uiThread = true)
    fun addNotificationListener(callback: JSCallback?) {
        updatePluginStatus()
        callback?.let {
            Log.d( TAG,"addNotificationListener")
            PushHelper.eventCallback[PushConstants.NOTIFICATION_EVENT] = it
            PushHelper.sendCacheNotify(0)
        }
    }

    override fun destroy() {
        PushHelper.IS_DESTROY = true
        uniContext?.applicationContext?.unregisterReceiver(receiver)
    }
}