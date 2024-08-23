package com.hyphenate.push.common

import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

object PushUtils {

    fun getDeviceManufacturer(): String {
        var line = ""
        var input: BufferedReader? = null
        val propName = "ro.miui.ui.version.name"
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (var12: IOException) {
            Log.e("DeviceUtils", "Unable to read sysprop $propName")
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (var11: IOException) {
                }
            }
        }
        if (!TextUtils.isEmpty(line)) {
            return "XIAOMI"
        }
        val manufacturer = Build.MANUFACTURER.replace("-", "_")
        return manufacturer.uppercase(Locale.getDefault())
    }

    fun isSupportPush(pushType: PushType?, pushConfig: PushConfig?): Boolean {
        val os: String = getDeviceManufacturer()
        Log.e("PushUtils", "Current device manufacturer: $os")
        return when (pushType) {
            PushType.MIPUSH -> os.contains("XIAOMI")
            PushType.OPPOPUSH -> os.contains("OPPO") || os.contains("ONEPLUS") || os.contains("REALME")
            PushType.VIVOPUSH -> os.contains("VIVO")
            PushType.MEIZUPUSH -> os.contains("MEIZU")
            PushType.HMSPUSH -> os.contains("HUAWEI") || os.contains("PTAC")
            PushType.HONORPUSH -> os.contains("HONOR")
            else -> {
                false
            }
        }
    }

}