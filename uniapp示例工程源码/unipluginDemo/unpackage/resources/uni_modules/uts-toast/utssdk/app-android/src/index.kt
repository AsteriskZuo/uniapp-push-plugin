@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "SENSELESS_COMPARISON", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uts.sdk.modules.utsToast
import android.widget.Toast
import io.dcloud.uniapp.*
import io.dcloud.uniapp.extapi.*
import io.dcloud.uts.*
import io.dcloud.uts.Map
import io.dcloud.uts.Set
import io.dcloud.uts.UTSAndroid
import kotlin.properties.Delegates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
open class ToastOptions (
    @JsonNotNull
    open var message: String,
) : UTSObject()
fun showToast(option: ToastOptions): Unit {
    open class MainThreadRunnable : Runnable {
        override fun run() {
            Toast.makeText(UTSAndroid.getUniActivity()!!, option.message, Toast.LENGTH_LONG).show()
        }
    }
    UTSAndroid.getUniActivity()?.runOnUiThread(MainThreadRunnable())
}
open class ToastOptionsJSONObject : UTSJSONObject() {
    open lateinit var message: String
}
fun showToastByJs(option: ToastOptionsJSONObject): Unit {
    return showToast(ToastOptions(message = option.message))
}
