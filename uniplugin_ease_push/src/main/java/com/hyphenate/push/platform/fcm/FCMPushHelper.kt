package com.hyphenate.push.platform.fcm

import android.content.Context
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.hyphenate.push.platform.IPush
import com.hyphenate.push.platform.IPush.Companion

class FCMPushHelper {
    companion object {
        private const val TAG = "FCMPushHelper"
        private const val META_FCM_SENDER_ID = "FCM_SENDER_ID"
        
        /**
         * Check if Google Play Services is available
         *
         * @param context Application context
         * @return true if Google Play Services is available, false otherwise
         */
        private fun isGoogleServiceAvailable(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            return resultCode == com.google.android.gms.common.ConnectionResult.SUCCESS
        }

        private fun initFirebase(context: Context?): Boolean {
            try {
                if (context == null) {
                    return false
                }
                if (!isGoogleServiceAvailable(context)) {
                    Log.e(TAG, "Google Play Services is not available.")
                    return false
                }
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
            } catch (e: Exception) {
                Log.e(IPush.TAG, "Firebase initialization error: ${e.message}")
                return false
            }
            return true
        }

        fun getFCMSenderId(context: Context?): String? {
            try {
                if (!initFirebase(context)) {
                    Log.e(TAG, "Firebase initialization error.")
                    return null
                }
                val app = FirebaseApp.getInstance()
                val options = app.options
                return options.gcmSenderId
            } catch (e: Exception) {
                Log.e(TAG, "Error getting FCM Sender ID: ${e.message}")
                return null
            }
        }
    }
}