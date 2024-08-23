package com.hyphenate.push.platform

import com.hyphenate.push.PushType

interface OnTokenResultListener {
    fun getPushTokenSuccess(pushToken: String?)
    fun getPushTokenFail(code: Int, error: String?)
    fun onError(type: PushType, code: Int, error: String?)
}