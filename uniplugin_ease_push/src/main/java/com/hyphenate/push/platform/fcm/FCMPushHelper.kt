package com.hyphenate.push.platform.fcm

import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability

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
        fun isGoogleServiceAvailable(context: Context): Boolean {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            return resultCode == com.google.android.gms.common.ConnectionResult.SUCCESS
        }
    }
}