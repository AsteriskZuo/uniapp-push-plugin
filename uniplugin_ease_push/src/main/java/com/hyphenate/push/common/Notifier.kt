/************************************************************
 * * Hyphenate CONFIDENTIAL
 * __________________
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package com.hyphenate.push.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.Ringtone
import android.os.Build
import android.os.Vibrator
import android.text.TextUtils
import androidx.core.app.NotificationCompat

/**
 * new message notifier class
 *
 * this class is subject to be inherited and implement the relative APIs
 *
 * On devices prior to Android 8.0:
 * The sound and vibration of notifications in the notification bar can be controlled by the'sound' and'vibration' switches in the demo settings
 * On Android 8.0 devices:
 * The sound and vibration of notifications in the notification bar are not controlled by the'sound' and'vibration' switches in the demo settings
 */
open class Notifier(context: Context) {
    protected val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    protected var fromUsers = HashSet<String>()
    protected var notificationNum = 0
    protected var appContext: Context
    protected var packageName: String
    protected var msg: String
    protected var lastNotifyTime: Long = 0
    protected var ringtone: Ringtone? = null
    protected var audioManager: AudioManager
    protected var vibrator: Vibrator

    init {
        appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= 26) {
            // Create the notification channel for Android 8.0
            val channel = NotificationChannel(
                CHANNEL_ID,
                "hyphenate chatuidemo message default channel.",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.vibrationPattern = VIBRATION_PATTERN
            notificationManager.createNotificationChannel(channel)
        }
        packageName = appContext.applicationInfo.packageName
        msg = "你有一条新消息"
        audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * this function can be override
     */
    fun reset() {
        resetNotificationCount()
        cancelNotification()
    }

    fun resetNotificationCount() {
        notificationNum = 0
        fromUsers.clear()
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFY_ID)
    }

    @Synchronized
    fun notify(content: String?) {
        try {
            val builder = generateBaseBuilder(content)
            val notification = builder.build()
            notificationManager.notify(NOTIFY_ID, notification)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Applicable to Android10, the limitation of starting Activity from the background
     * @param fullScreenIntent
     * @param title
     * @param content
     */
    @Synchronized
    fun notify(fullScreenIntent: Intent, title: String?, content: String) {
        try {
            val builder = generateBaseFullIntentBuilder(fullScreenIntent, content)
            if (!TextUtils.isEmpty(title)) {
                builder.setContentTitle(title)
            }
            val notification = builder.build()
            notificationManager.notify(NOTIFY_ID, notification)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Generate a base Notification#Builder, contains:
     * 1.Use the app icon as default icon
     * 2.Use the app name as default title
     * 3.This notification would be sent immediately
     * 4.Can be cancelled by user
     * 5.Would launch the default activity when be clicked
     *
     * @return
     */
    private fun generateBaseBuilder(content: String?): NotificationCompat.Builder {
        val pm: PackageManager = appContext.packageManager
        val title: String = pm.getApplicationLabel(appContext.applicationInfo).toString()
        val i: Intent? = appContext.packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(appContext, NOTIFY_ID, i, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(appContext.applicationInfo.icon)
            .setContentTitle(title)
            .setTicker(content)
            .setContentText(content)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    /**
     * Generate a base Notification#Builder to replace of start background activity.
     * @param fullScreenIntent
     * @param content
     * @return
     */
    private fun generateBaseFullIntentBuilder(
        fullScreenIntent: Intent,
        content: String
    ): NotificationCompat.Builder {
        val pm: PackageManager = appContext.packageManager
        val title: String = pm.getApplicationLabel(appContext.applicationInfo).toString()
        val fullScreenPendingIntent: PendingIntent = PendingIntent.getActivity(
            appContext, NOTIFY_ID,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(appContext.applicationInfo.icon)
            .setContentTitle(title)
            .setTicker(content)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
    }


    companion object {
        private const val TAG = "Notifier"
        protected var NOTIFY_ID = 341 // start notification id
        protected const val CHANNEL_ID = "hyphenate_chatuidemo_notification"
        protected val VIBRATION_PATTERN = longArrayOf(0, 180, 80, 120)
    }
}