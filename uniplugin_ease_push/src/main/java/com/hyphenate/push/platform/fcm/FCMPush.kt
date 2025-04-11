package com.hyphenate.push.platform.fcm

import android.content.Context
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.platform.IPush
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging


class FCMPush : IPush() {

  override fun getPushType(): PushType {
    return PushType.FCM
  }

  override fun getPushToken(context: Context): String? {
    return pushToken
  }

  override fun onRegister(context: Context?, config: PushConfig) {
    context?.let {
      try {
        FirebaseMessaging.getInstance().isNotificationDelegationEnabled = true

        if (pushToken.isNullOrEmpty()) {
          FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
              val errorMessage = task.exception?.message ?: "Unknown error"
              Log.e(TAG, "Failed to get FCM token: $errorMessage")
              resultListener?.getPushTokenFail(PushType.FCM, 2004, errorMessage)
            } else {
              pushToken = task.result
              Log.d(TAG, "FCM token obtained: $pushToken")
              resultListener?.getPushTokenSuccess(PushType.FCM, pushToken)
            }
          }
        } else {
          resultListener?.getPushTokenSuccess(PushType.FCM, pushToken)
        }
      } catch (e: Exception) {
        Log.e(TAG, "FCM registration error: ${e.message}")
        resultListener?.getPushTokenFail(PushType.FCM, 2003, e.message ?: "isSupportPush false")
      }
    } ?: run {
      Log.e(TAG, "Context is null, cannot register FCM")
      resultListener?.onError(PushType.FCM, 2000, "push plugin context is null.")
    }
  }

  override fun onUnregister(context: Context?, config: PushConfig) {
    context?.let {
      try {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
          if (!task.isSuccessful) {
            val errorMessage = task.exception?.message ?: "Unknown error"
            Log.e(TAG, "Failed to delete FCM token: $errorMessage")
          } else {
            pushToken = null
            Log.d(TAG, "FCM token deleted successfully")
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "FCM unregistration error: ${e.message}")
      }
    }
  }

  override fun onGetNotifierName(config: PushConfig): String {
    return config.fcmSenderId
  }
}