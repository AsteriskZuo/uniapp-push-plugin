# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# hms honor
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hihonor.push.**{*;}
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}

#oppo
-keep public class * extends android.app.Service
-keep class com.heytap.** { *;}

#xiaomi
-keep class com.hyphenate.push.platform.xiaomi.MiPushService {*;}

#uniapp
-keep public class * extends io.dcloud.weex.AppHookProxy{*;}
-keep public class * extends io.dcloud.feature.uniapp.UniAppHookProxy{*;}
-keep public class * extends io.dcloud.feature.uniapp.common.UniModule{*;}
-keep public class * extends io.dcloud.feature.uniapp.ui.component.UniComponent{*;}