package com.hyphenate.push.platform.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hyphenate.push.PushType
import com.hyphenate.push.common.PushHelper

class FCMPushService: FirebaseMessagingService() {
  override fun onNewToken(token: String) {
    PushHelper.sendRenewTokenEvent(PushType.FCM,token)
  }

  override fun onMessageReceived(message: RemoteMessage) {
    Log.d("FCMPush", "onMessageReceived" + message.data)
  }
}