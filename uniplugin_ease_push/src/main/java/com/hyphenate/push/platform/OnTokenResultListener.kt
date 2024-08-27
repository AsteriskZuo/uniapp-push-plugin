package com.hyphenate.push.platform

import com.hyphenate.push.PushType

interface OnTokenResultListener {
    fun getPushTokenSuccess(pushType: PushType,pushToken: String?)
    fun getPushTokenFail(pushType: PushType,code: Int, error: String?)
    fun onError(type: PushType, code: Int, error: String?)
}