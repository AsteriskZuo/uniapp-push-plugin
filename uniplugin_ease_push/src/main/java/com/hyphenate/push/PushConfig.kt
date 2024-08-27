package com.hyphenate.push

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.hyphenate.push.common.PushConstants
import com.hyphenate.push.common.PushHelper
import com.taobao.weex.bridge.JSCallback


class PushConfig {
    companion object{
        const val TAG = "PushConfig"
    }
    // huawei
    var hwAppId: String = ""
    // honor
    var honorAppId: String = ""
    // oppo
    var oppoAppKey: String = ""
    var oppoAppSecret: String = ""
    //vivo
    var vivoAppId: String = ""
    var vivoAppKey: String = ""
    //xiaomi
    var xiaomiAppId: String = ""
    var xiaomiAppKey: String = ""
    //meizu
    var mzAppId: String = ""
    var mzAppKey: String = ""

    val pushConfigTypes: MutableList<PushType> = mutableListOf()

    val isAgreeAgreement: Boolean = false

    var jsonObject: JSONObject = JSONObject()

    fun checkPushConfig(context:Context?,callback: JSCallback?){
        pushConfigTypes.clear()
        checkPush(context)
        jsonObject[PushConstants.PUSH_ENABLE_TYPES] = pushConfigTypes
        Log.e(TAG,"pushConfigTypes:$pushConfigTypes")
        callback?.invoke(jsonObject)
    }

    private fun checkPush(context:Context?){
        context?.let {
            try {
                it.packageManager.getApplicationInfo(
                    it.packageName, PackageManager.GET_META_DATA
                ).let { appInfo->
                    if (appInfo.metaData.isEmpty){
                        Log.e(TAG,"metaData info is empty")
                        return
                    }
                    try {
                        // xiaomi
                        if (appInfo.metaData.containsKey("XIAO_MI_APP_ID")){
                            when(appInfo.metaData.get("XIAO_MI_APP_ID")){
                                is String -> {
                                    xiaomiAppId = appInfo.metaData.getString("XIAO_MI_APP_ID","")
                                }
                                is Float -> {
                                    val a = appInfo.metaData.getFloat("XIAO_MI_APP_ID",0f)
                                    if (a != 0f){
                                        xiaomiAppId = a.toString()
                                    }
                                }
                                else -> {
                                    Log.e(TAG,"Parameter type exception")
                                }
                            }
                        }
                        if (appInfo.metaData.containsKey("XIAO_MI_APP_KEY")){
                            when(appInfo.metaData.get("XIAO_MI_APP_KEY")){
                                is String -> {
                                    xiaomiAppKey = appInfo.metaData.getString("XIAO_MI_APP_KEY","")
                                }
                                is Float -> {
                                    val a = appInfo.metaData.getFloat("XIAO_MI_APP_KEY",0f)
                                    if (a != 0f){
                                        xiaomiAppKey = a.toString()
                                    }
                                }
                                is Int -> {
                                    val a = appInfo.metaData.getInt("XIAO_MI_APP_KEY",0)
                                    if (a != 0){
                                        xiaomiAppKey = a.toString()
                                    }
                                }
                                else -> {
                                    Log.e(TAG,"Parameter type exception")
                                }
                            }
                        }
                        if (xiaomiAppId.isNotEmpty() && xiaomiAppKey.isNotEmpty()){
                            pushConfigTypes.add(PushType.MIPUSH)
                        }
                    }catch (e: NullPointerException){
                        Log.e( TAG, "mi push config meta-data: not found in AndroidManifest.xml.")
                    }

                    try {
                        // oppo
                        if (appInfo.metaData.containsKey("OPPO_APP_KEY")){
                            oppoAppKey = appInfo.metaData.getString("OPPO_APP_KEY","")
                        }
                        if (appInfo.metaData.containsKey("OPPO_APP_SECRET")){
                            oppoAppSecret = appInfo.metaData.getString("OPPO_APP_SECRET","")
                        }
                        if (oppoAppKey.isNotEmpty() && oppoAppSecret.isNotEmpty()){
                            pushConfigTypes.add(PushType.OPPOPUSH)
                        }
                    }catch (e: NullPointerException){
                        Log.e( TAG, "oppo push config meta-data: not found in AndroidManifest.xml.")
                    }

                    try {
                        // vivo
                        if (appInfo.metaData.containsKey("com.vivo.push.app_id")){
                            when(appInfo.metaData.get("com.vivo.push.app_id")){
                                is Int -> {
                                    val vid = appInfo.metaData.getInt("com.vivo.push.app_id",0)
                                    if (vid != 0){
                                        vivoAppId = vid.toString()
                                    }
                                }
                                is String -> {
                                    vivoAppId = appInfo.metaData.getString("com.vivo.push.app_id","")
                                }
                                is Float -> {
                                    val a = appInfo.metaData.getFloat("com.vivo.push.app_id",0f)
                                    if (a != 0f){
                                        vivoAppId = a.toString()
                                    }
                                }
                                else -> {
                                    Log.e(TAG,"Parameter type exception")
                                }
                            }

                        }
                        if (appInfo.metaData.containsKey("com.vivo.push.api_key")){
                            vivoAppKey = appInfo.metaData.getString("com.vivo.push.api_key","")
                        }
                        if (vivoAppId.isNotEmpty() && vivoAppKey.isNotEmpty()){
                            pushConfigTypes.add(PushType.VIVOPUSH)
                        }

                    }catch (e: NullPointerException){
                        Log.e( TAG, "vivo push config meta-data: not found in AndroidManifest.xml.")
                    }

                    try {
                        // meizu
                        if (appInfo.metaData.containsKey("MEI_ZU_APP_ID")){
                            when(appInfo.metaData.get("MEI_ZU_APP_ID")){
                                is Int -> {
                                    val id = appInfo.metaData.getInt("MEI_ZU_APP_ID",0)
                                    if (id != 0){
                                        mzAppId = id.toString()
                                    }
                                }
                                is String -> {
                                    mzAppId = appInfo.metaData.getString("MEI_ZU_APP_ID","")
                                }
                                is Float -> {
                                    val a = appInfo.metaData.getFloat("MEI_ZU_APP_ID",0f)
                                    if (a != 0f){
                                        mzAppId = a.toString()
                                    }
                                }
                            }
                        }
                        if (appInfo.metaData.containsKey("MEI_ZU_APP_KEY")){
                            mzAppKey = appInfo.metaData.getString("MEI_ZU_APP_KEY","")
                        }
                        if (mzAppId.isNotEmpty() && mzAppKey.isNotEmpty()){
                            pushConfigTypes.add(PushType.MEIZUPUSH)
                        }
                    }catch (e: NullPointerException){
                        Log.e( TAG, "meizu push config meta-data: not found in AndroidManifest.xml.")
                    }

                    try {
                        // honor
                        if (appInfo.metaData.containsKey("com.hihonor.push.app_id")){
                            val id = appInfo.metaData.getInt("com.hihonor.push.app_id",0)
                            if (id != 0){
                                honorAppId = id.toString()
                                pushConfigTypes.add(PushType.HONORPUSH)
                            }
                        }
                    }catch (e: NullPointerException){
                        Log.e( TAG, "honor push config meta-data: not found in AndroidManifest.xml.")
                    }

                    // hms
                    try {
                        if (appInfo.metaData.containsKey("com.huawei.hms.client.appid")){
                            hwAppId = appInfo.metaData.getString("com.huawei.hms.client.appid","")
                            hwAppId = if (hwAppId.contains("=")) {
                                hwAppId.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[1]
                            } else {
                                val id = appInfo.metaData.getInt("com.huawei.hms.client.appid")
                                id.toString()
                            }
                        }else{
                            val id = PushHelper.getHMSAppId(context)
                            id?.let { hwAppId = it }
                            Log.d(TAG,"appId:$hwAppId")
                        }
                        if (hwAppId.isEmpty().not()){
                            pushConfigTypes.add(PushType.HMSPUSH)
                        }else{}
                    }catch (e: NullPointerException){
                        Log.e( TAG, "hms push config meta-data: not found in AndroidManifest.xml.")
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                Log.e(TAG,"${e.message}")
            }
        }?:kotlin.run {
            Log.e( TAG, "context is null.")
        }
    }

    fun getMetaDataInfo(context:Context?,callback: JSCallback?){
        context?.let {
            val appInfo: ApplicationInfo?
            try {
                appInfo = it.packageManager.getApplicationInfo(
                    it.packageName, PackageManager.GET_META_DATA
                )
                val value = appInfo.metaData
                value?.let { v->
                    val jsonObject = JSONObject()
                    jsonObject[PushConstants.CODE] = PushConstants.CODE_SUCCESS
                    for (s in v.keySet()) {
                        var i = 0
                        jsonObject[s] = v.get(s).toString()
                        jsonObject[s+i] = v.get(s)?.javaClass.toString()
                        i++
                    }
                    Log.e(TAG,"getInfo: $jsonObject")
                    callback?.invoke(jsonObject)
                }?:kotlin.run {
                    jsonObject[PushConstants.ERROR] = "value is null."
                    callback?.invoke(jsonObject)
                }
            } catch (e: PackageManager.NameNotFoundException){
                jsonObject[PushConstants.ERROR] = "${e.message}"
                callback?.invoke(jsonObject)
            }
        }
    }


}