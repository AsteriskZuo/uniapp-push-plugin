package com.hyphenate.push.platform.normal

import android.content.Context
import com.hyphenate.push.PushConfig
import com.hyphenate.push.PushType
import com.hyphenate.push.platform.IPush

class NormalPush: IPush() {
    override fun getPushType(): PushType {
        return PushType.NORMAL
    }

    override fun getPushToken(context: Context): String? {
       return pushToken
    }

    override fun onRegister(context: Context?, config: PushConfig) {

    }

    override fun onUnregister(context: Context?) {

    }

    override fun onGetNotifierName(config: PushConfig): String {
       return ""
    }
}