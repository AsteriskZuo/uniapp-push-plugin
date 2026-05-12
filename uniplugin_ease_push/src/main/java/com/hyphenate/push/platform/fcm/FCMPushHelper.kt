package com.hyphenate.push.platform.fcm

import android.content.Context
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.hyphenate.push.platform.IPush

class FCMPushHelper {
    companion object {
        private const val TAG = "FCMPushHelper"

        @Volatile
        private var fcmDependencyChecked = false

        @Volatile
        private var fcmDependencyAvailable = false

        /**
         * 运行时探测 FCM / Google Play Services 相关 class 是否打入了当前 APK。
         *
         * 云打包场景下，使用方未在 manifest.json 中声明 firebase-messaging 等依赖，
         * 这些 class 不会被打入 APK。此时再去调用任何 FCM API 会抛
         * NoClassDefFoundError（Error 而非 Exception，无法被 catch (Exception) 捕获）。
         *
         * 通过 Class.forName 提前探测，可以让插件在缺失 FCM 依赖时安全降级到厂商通道。
         *
         * 结果会缓存，避免每次调用都触发反射。
         */
        @JvmStatic
        fun isFcmDependencyAvailable(): Boolean {
            if (fcmDependencyChecked) return fcmDependencyAvailable
            fcmDependencyAvailable = try {
                Class.forName("com.google.android.gms.common.GoogleApiAvailability")
                Class.forName("com.google.firebase.FirebaseApp")
                Class.forName("com.google.firebase.messaging.FirebaseMessaging")
                true
            } catch (t: Throwable) {
                Log.w(
                    TAG,
                    "FCM/GMS classes are not present in this APK, FCM channel will be disabled. " +
                            "If you want FCM, please use offline packaging and add firebase-messaging " +
                            "and play-services-base dependencies. Detail: ${t.javaClass.simpleName}: ${t.message}"
                )
                false
            }
            fcmDependencyChecked = true
            return fcmDependencyAvailable
        }

        /**
         * Check if Google Play Services is available
         *
         * @param context Application context
         * @return true if Google Play Services is available, false otherwise
         */
        private fun isGoogleServiceAvailable(context: Context?): Boolean {
            if (context == null) return false
            return try {
                val googleApiAvailability = GoogleApiAvailability.getInstance()
                val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
                resultCode == com.google.android.gms.common.ConnectionResult.SUCCESS
            } catch (t: Throwable) {
                Log.e(TAG, "isGoogleServiceAvailable error: ${t.message}")
                false
            }
        }

        private fun initFirebase(context: Context?): Boolean {
            if (context == null) return false
            return try {
                if (!isGoogleServiceAvailable(context)) {
                    Log.e(TAG, "Google Play Services is not available.")
                    return false
                }
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
                true
            } catch (t: Throwable) {
                Log.e(IPush.TAG, "Firebase initialization error: ${t.message}")
                false
            }
        }

        @JvmStatic
        fun getFCMSenderId(context: Context?): String? {
            if (!isFcmDependencyAvailable()) return null
            return try {
                if (!initFirebase(context)) {
                    Log.e(TAG, "Firebase initialization error.")
                    null
                } else {
                    val app = FirebaseApp.getInstance()
                    app.options.gcmSenderId
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error getting FCM Sender ID: ${t.message}")
                null
            }
        }
    }
}
