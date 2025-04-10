package com.hyphenate.push

import android.app.NotificationManager
import android.content.Context
import android.util.Log
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

    private fun updatePluginStatus() {
        if (uniContext == null){
            uniContext = mUniSDKInstance.context
        }
        PushHelper.IS_DESTROY = false
    }

    @UniJSMethod(uiThread = true)
    fun initPushModule(callback: UniJSCallback?){
        Log.e(TAG,"initPushModule")
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
        Log.e(TAG,"onRegister")
        callback?.let {
            PushHelper.onNewTokenCallback[PushConstants.NOTIFICATION_RENEW_TOKEN] = it
        }
        pushClient = PushHelper.getPushClient(pushConfig,uniContext)
        Log.e(TAG, "onRegister:pushType:${pushClient?.getPushType()?.name}")
        pushClient?.setTokenResultListener(object : OnTokenResultListener {
            override fun getPushTokenSuccess(pushType: PushType, pushToken: String?) {
                Log.e(TAG,"onRegister:getPushTokenSuccess:${pushType.name} $pushToken")
                PushHelper.sendRenewTokenEvent(pushType,pushToken)
            }

            override fun getPushTokenFail(pushType: PushType, code: Int, error: String?) {
                Log.e(TAG,"onRegister:getPushTokenFail:${pushType.name} $code $error")
                callback?.let {
                    val jsonObject = PushHelper.assemblyData("",pushType,code,error)
                    PushHelper.sendNotificationEvent(jsonObject,0)
                }
            }

            override fun onError(type: PushType, code: Int, error: String?) {
                Log.e(TAG,"onRegister:onError: ${type.name} $code $error")
                callback?.let {
                    val jsonObject = PushHelper.assemblyData("",type,code,error)
                    PushHelper.sendNotificationEvent(jsonObject,0)
                }
            }
        })
        pushClient?.register(uniContext, pushConfig)
    }

    @UniJSMethod(uiThread = true)
    fun unRegister(){
        updatePluginStatus()
        Log.e(TAG,"onRegister:unRegister:")
        pushClient?.unregister(uniContext,pushConfig)
    }

    @UniJSMethod(uiThread = true)
    fun getMateDataInfo(callback: UniJSCallback?){
        updatePluginStatus()
        pushConfig.getMetaDataInfo(uniContext,callback)
    }

    @UniJSMethod(uiThread = true)
    fun addNotificationListener(callback: JSCallback?) {
        updatePluginStatus()
        callback?.let {
            Log.e( TAG,"addNotificationListener")
            PushHelper.notifyCallback[PushConstants.NOTIFICATION_EVENT] = it
            PushHelper.sendCacheNotify(1)
        }
    }

    override fun destroy() {
        PushHelper.IS_DESTROY = true
    }
}