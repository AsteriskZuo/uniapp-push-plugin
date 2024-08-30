package com.hyphenate.push

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.hyphenate.push.common.PushConstants
import com.hyphenate.push.common.PushHelper
import com.hyphenate.push.platform.IPush
import com.hyphenate.push.platform.OnTokenResultListener
import com.hyphenate.push.platform.honor.HonorPush
import com.hyphenate.push.platform.huawei.HMSPush
import com.hyphenate.push.platform.meizu.MzPush
import com.hyphenate.push.platform.normal.NormalPush
import com.hyphenate.push.platform.oppo.OppoPush
import com.hyphenate.push.platform.vivo.ViVoPush
import com.hyphenate.push.platform.xiaomi.MiPush
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
        callback?.let {
            PushHelper.onNewTokenCallback[PushConstants.NOTIFICATION_RENEW_TOKEN] = it
        }
        pushClient = when(PushHelper.getPreferPushType(pushConfig)){
            PushType.MIPUSH -> MiPush()
            PushType.OPPOPUSH -> OppoPush()
            PushType.VIVOPUSH -> ViVoPush()
            PushType.HONORPUSH -> HonorPush()
            PushType.MEIZUPUSH -> MzPush()
            PushType.HMSPUSH -> HMSPush()
            PushType.NORMAL -> NormalPush()
            else -> NormalPush()
        }
        pushClient?.setTokenResultListener(object : OnTokenResultListener {
            override fun getPushTokenSuccess(pushType: PushType, pushToken: String?) {
                Log.d(TAG,"getPushTokenSuccess:${pushType.name} $pushToken")
                PushHelper.sendRenewTokenEvent(pushType,pushToken)
            }

            override fun getPushTokenFail(pushType: PushType, code: Int, error: String?) {
                Log.e(TAG,"getPushTokenFail:${pushType.name} $code $error")
                callback?.let {
                    val jsonObject = PushHelper.assemblyData("",pushType,code,error)
                    PushHelper.sendNotificationEvent(jsonObject,0)
                }
            }

            override fun onError(type: PushType, code: Int, error: String?) {
                Log.e(TAG,"onError: ${type.name} $code $error")
                callback?.let {
                    val jsonObject = PushHelper.assemblyData("",type,code,error)
                    PushHelper.sendNotificationEvent(jsonObject,0)
                }
            }
        })
        pushClient?.register(uniContext, pushConfig)
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
            Log.d( TAG,"addNotificationListener")
            PushHelper.notifyCallback[PushConstants.NOTIFICATION_EVENT] = it
            PushHelper.sendCacheNotify(0)
        }
    }

    override fun destroy() {
        PushHelper.IS_DESTROY = true
    }
}