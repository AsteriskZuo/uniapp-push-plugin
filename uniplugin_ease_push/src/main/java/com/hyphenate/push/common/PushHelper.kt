package com.hyphenate.push.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.hihonor.push.sdk.common.data.ApiException
import com.huawei.hms.aaid.HmsInstanceId
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.platform.IPush
import com.hyphenate.push.platform.honor.HonorPush
import com.hyphenate.push.platform.huawei.HMSPush
import com.hyphenate.push.platform.meizu.MzPush
import com.hyphenate.push.platform.normal.NormalPush
import com.hyphenate.push.platform.oppo.OppoPush
import com.hyphenate.push.platform.vivo.ViVoPush
import com.hyphenate.push.platform.xiaomi.MiPush
import com.taobao.weex.bridge.JSCallback
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object PushHelper {
    const val TAG = "PushHelper"

    var isAppForeground = false
    // 标识当前插件是否被销毁
    var IS_DESTROY = true

    const val FCM = 1
    const val HMSPUSH = 2
    const val MIPUSH = 3
    const val MEIZUPUSH = 4
    const val VIVOPUSH = 5
    const val OPPOPUSH = 6
    const val HONORPUSH = 7
    const val NORMAL = 8

    var notifyCallback = mutableMapOf<String, JSCallback>()
    var onNewTokenCallback = mutableMapOf<String,JSCallback>()
    var RENEW_TOKEN: JSONObject? = null
    var NOTIFICATION_DATA: JSONObject? = null
    var NOTIFICATION_TYPE: Int = 0

    private fun sendEvent(params: JSONObject?,jsCallback: JSCallback?) {
        try {
            params?.let {
                Log.e("sendEvent", "params:$it")
                jsCallback?.let { callback->
                    Log.e(TAG,"sendEvent success")
                    callback.invokeAndKeepAlive(it)
                    return
                }
                Log.e(TAG,"sendEvent failed")
            }?:kotlin.run {
                Log.e(TAG,"sendEvent error: params is null")
            }
        } catch (throwable: Throwable) {
            Log.e(TAG,"sendEvent error:" + throwable.message)
        }
    }

    fun sendNotificationEvent(params: JSONObject?, notificationType: Int) {
        if (notificationType == 0) {
            Log.e(TAG, "sendEvent eventName:${PushConstants.NOTIFICATION_RENEW_TOKEN}")
            sendEvent(params, onNewTokenCallback[PushConstants.NOTIFICATION_RENEW_TOKEN])
        } else {
            Log.e(TAG, "sendEvent eventName:${PushConstants.NOTIFICATION_EVENT}")
            sendEvent(params, notifyCallback[PushConstants.NOTIFICATION_EVENT])
        }
    }

    /**
     * 缓存通知点击信息，再用户注册监听后返回给用户
     *
     * @param jsonObject
     */
    fun saveNotifyData(jsonObject: JSONObject, type: Int) {
        if (IS_DESTROY) {
            Log.d(TAG,"saveOpenNotifyData:$jsonObject")
            if (type == 0){
                RENEW_TOKEN = jsonObject
            }else{
                NOTIFICATION_DATA = jsonObject
            }
            NOTIFICATION_TYPE = type
        }
    }

    fun sendCacheNotify(type: Int) {
        if (!IS_DESTROY && NOTIFICATION_DATA.isNullOrEmpty().not()) {
            Log.d(TAG,"sendCacheOpenNotify: $NOTIFICATION_DATA" )
            if (type == 0){
                sendNotificationEvent(RENEW_TOKEN, NOTIFICATION_TYPE)
                RENEW_TOKEN = null
            }else{
                sendNotificationEvent(NOTIFICATION_DATA, NOTIFICATION_TYPE)
                NOTIFICATION_DATA = null
            }
        }
    }

    fun assemblyData(
        token:String?,
        pushType: PushType? = PushType.NORMAL,
        code:Int = 200,
        error:String? = ""
    ):JSONObject{
        val jsonObject = JSONObject()
        jsonObject[PushConstants.PUSH_TOKEN] = token
        jsonObject[PushConstants.PUSH_TYPE] = checkPushType(pushType)
        jsonObject[PushConstants.CODE] = code
        jsonObject[PushConstants.ERROR] = error
        return  jsonObject
    }

    fun sendRenewTokenEvent(pushType: PushType? = PushType.NORMAL, token: String?){
        token?.let {
            val jsonObject = JSONObject()
            jsonObject[PushConstants.PUSH_TOKEN] = token
            jsonObject[PushConstants.PUSH_TYPE] = checkPushType(pushType)
            sendNotificationEvent(jsonObject,0)
        }
    }

    fun checkPushType(pushType: PushType?):Int{
        return when(pushType){
            PushType.FCM -> { FCM }
            PushType.HMSPUSH -> { HMSPUSH }
            PushType.MIPUSH -> { MIPUSH }
            PushType.MEIZUPUSH -> { MEIZUPUSH }
            PushType.VIVOPUSH -> { VIVOPUSH }
            PushType.OPPOPUSH -> { OPPOPUSH }
            PushType.HONORPUSH -> { HONORPUSH }
            PushType.NORMAL -> { NORMAL }
            else -> { NORMAL }
        }
    }

    fun getPreferPushType(pushConfig: PushConfig): PushType {
        val supportedPushTypes: Array<PushType> = arrayOf<PushType>(
            PushType.FCM,
            PushType.MIPUSH,
            PushType.HMSPUSH,
            PushType.MEIZUPUSH,
            PushType.OPPOPUSH,
            PushType.VIVOPUSH,
            PushType.HONORPUSH
        )
        val enabledPushTypes: MutableList<PushType> = pushConfig.pushConfigTypes
        for (pushType in supportedPushTypes) {
            if (enabledPushTypes.contains(pushType) && PushUtils.isSupportPush(
                    pushType,
                    pushConfig
                )
            ) {
                return pushType
            }
        }
        return PushType.NORMAL
    }

    fun getPushClient(config: PushConfig):IPush{
        Log.e(TAG,"register: pushType:${getPreferPushType(config)}")
        return when(getPreferPushType(config)){
            PushType.MIPUSH -> MiPush()
            PushType.OPPOPUSH -> OppoPush()
            PushType.VIVOPUSH -> ViVoPush()
            PushType.HONORPUSH -> HonorPush()
            PushType.MEIZUPUSH -> MzPush()
            PushType.HMSPUSH -> HMSPush()
            PushType.NORMAL -> NormalPush()
            else -> NormalPush()
        }
    }

    /**
     * 申请华为Push Token
     * 1、getToken接口只有在AppGallery Connect平台开通服务后申请token才会返回成功。
     *
     * 2、EMUI10.0及以上版本的华为设备上，getToken接口直接返回token。如果当次调用失败Push会缓存申请，之后会自动重试申请，成功后则以onNewToken接口返回。
     *
     * 3、低于EMUI10.0的华为设备上，getToken接口如果返回为空，确保Push服务开通的情况下，结果后续以onNewToken接口返回。
     *
     * 4、服务端识别token过期后刷新token，以onNewToken接口返回。
     */
    fun getHMSToken(context: Context): String{
        var hmsToken = ""
        try {
            if (Class.forName("com.huawei.hms.api.HuaweiApiClient") != null) {
                val classType = Class.forName("android.os.SystemProperties")
                val getMethod = classType.getDeclaredMethod(
                    "get", *arrayOf<Class<*>>(
                        String::class.java
                    )
                )
                val buildVersion =
                    getMethod.invoke(classType, *arrayOf<Any>("ro.build.version.emui")) as String
                //在某些手机上，invoke方法不报错
                if (!TextUtils.isEmpty(buildVersion)) {
                    Log.d(TAG, "huawei hms push is available!")
                    object : Thread() {
                        override fun run() {
                            try {
                                val appId = getHMSAppId(context)
                                Log.d(TAG,"appId:$appId")
                                // 申请华为推送token
                                val token = HmsInstanceId.getInstance(context).getToken(appId, "HCM")
                                Log.d(TAG, "get huawei hms push token:$token")
                                if (token.isNullOrEmpty().not()){
                                    Log.d(TAG, "register huawei hms push token success token:$token")
                                    hmsToken = token
                                }else{
                                    Log.d(TAG,"register huawei hms push token fail!")
                                }
                            } catch (e: ApiException) {
                                Log.d(TAG, "get huawei hms push token failed, $e")
                            }
                        }
                    }.start()
                } else {
                    Log.d(TAG, "huawei hms push is unavailable!")
                }
            } else {
                Log.d(TAG, "no huawei hms push sdk or mobile is not a huawei phone")
            }
        } catch (e: Exception) {
            Log.d(TAG, "no huawei hms push sdk or mobile is not a huawei phone")
        }
        return hmsToken
    }

    fun getHMSAppId(context: Context): String? {
        var appId: String? = null
        try {
            // 获取 AssetManager
            val assetManager: AssetManager = context.assets
            // 打开 assets 目录下的 agconnect-services.json 文件
            val inputStream: InputStream = assetManager.open("agconnect-services.json")
            // 读取文件内容
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            appId = stringBuilder.toString()
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return appId?.let {
            val jsonObject: JSONObject = JSONObject.parseObject(it)
            Log.e(TAG,"jsonObject: $jsonObject")
            val appInfo: JSONObject = jsonObject.getJSONObject("app_info")
            appInfo.getString("app_id")
        }
    }

    fun launchApp(context: Context) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            if (!isAppForeground){
                context.startActivity(intent)
            }
        } catch (throwable: Throwable) {
            Log.e(TAG,"error:${throwable.message}")
        }
    }

    //*****************************应用前后台状态监听*****************************
    fun registerActivityLifecycle(application: Application?) {
        application?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                Log.d(TAG,"onActivityCreated")
            }

            override fun onActivityStarted(activity: Activity) {
                Log.d(TAG,"onActivityStarted")
            }

            override fun onActivityResumed(activity: Activity) {
                Log.d(TAG,"onActivityResumed")
                isAppForeground = true
            }

            override fun onActivityPaused(activity: Activity) {
                Log.d(TAG,"onActivityPaused")
                isAppForeground = false
            }

            override fun onActivityStopped(activity: Activity) {
                Log.d(TAG,"onActivityStopped")
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
                Log.d(TAG,"onActivitySaveInstanceState")
            }

            override fun onActivityDestroyed(activity: Activity) {
                Log.d(TAG,"onActivityDestroyed")
            }
        })
    }
}