# case-demo Android 16 图片选择器问题调研记录

日期：2026-07-01

## 1. 背景

当前问题集中在 `uniapp示例工程源码/case-demo`：

- 同一个 uni-app demo，均包含推送原生插件。
- Android 16 设备上，离线打包 APK 可以正常打开相册并看到图片。
- 云打包 APK 可以打开图片选择器，但图片列表为空，看不到图片。
- 需要判断问题更可能来自项目配置、推送插件、云打包/离线打包差异，还是 Android 运行时权限/系统相册兼容问题。

本次对比覆盖：

- 源码配置对比
- HBuilder/uni-app 构建产物对比
- APK 静态信息对比
- logcat 动态行为对比

## 2. 对比样本

### 2.1 项目源码

目标项目：

```text
/Users/asterisk/Codes/zuoyu/uniapp-push-plugin/uniapp示例工程源码/case-demo
```

图片选择入口：

```text
case-demo/pages/index/index.vue
```

推送插件声明：

```text
case-demo/nativeplugins/EMPushUniPlugin/package.json
```

推送插件 Android 源码：

```text
/Users/asterisk/Codes/zuoyu/uniapp-push-plugin/uniplugin_ease_push/src/main
```

### 2.2 构建产物

uni-app app-plus 构建产物：

```text
case-demo/unpackage/dist/build/app-plus
```

离线打包资源目录：

```text
case-demo/unpackage/resources/__UNI__32EB66E
```

### 2.3 APK

云打包 APK：

```text
/Users/asterisk/Downloads/__UNI__32EB66E_0701175059.apk
```

离线打包 APK：

```text
/Users/asterisk/Codes/zuoyu/uniapp-push-plugin/app/build/intermediates/apk/debug/app-debug.apk
```

### 2.4 运行日志

异常云包日志：

```text
/tmp/cloud-apk-logcat.txt
```

正常离线包日志：

```text
/tmp/cloud-apk-logcat-ok.txt
```

推送相关操作后再打开相册的云包日志：

```text
/tmp/cloud-push-before-picker.txt
```

## 3. 总体结论

当前证据不支持“前端 `uni.chooseImage` 代码不同”或“最终 APK 缺少媒体权限”作为主要原因。

更符合现象的判断是：

1. 云包和离线包都成功启动了 DCloud 的旧图片选择器 `com.dmcbig.mediapicker.PickerActivity`。
2. 异常云包的问题不是“选择器打不开”，而是“选择器打开后媒体列表为空”。
3. 两边最终 APK 都声明了 Android 13+ 媒体权限，包括 `READ_MEDIA_IMAGES`、`READ_MEDIA_VIDEO`、`READ_MEDIA_VISUAL_USER_SELECTED`。
4. 两边日志都出现相同的 `GalleryFeatureImpl` / `EISDIR` 异常，因此该异常不是区分正常/异常的根因。
5. 运行时权限补充对比显示，两个包在 `dumpsys package` 中都显示媒体权限已授权，单独查询 `READ_MEDIA_IMAGES` 时两个包也都是 `allow`。
6. 当前 AppOps 的新差异是：正常离线包的 `READ_MEDIA_IMAGES: allow` 后带有最近访问时间，异常云包只有 `READ_MEDIA_IMAGES: allow`，没有访问时间记录。这更像是云包打开旧 picker 后没有真正执行到被系统记录的图片读取操作，或读取路径没有命中该 AppOp。
7. 目前更可疑的是 DCloud 云打包 runtime 中旧 `com.dmcbig.mediapicker.PickerActivity` 在 Android 16 上对 MediaStore/照片权限模型兼容不完整，而不是简单的权限未授权。

## 4. 源码对比

### 4.1 图片选择代码

`case-demo/pages/index/index.vue` 中图片选择逻辑使用 `uni.chooseImage`，并指定只从相册选择：

```js
uni.chooseImage({
  count: 6,
  sizeType: ['original', 'compressed'],
  sourceType: ['album'],
  success: function (res) {
    console.log(JSON.stringify(res.tempFilePaths));
  }
})
```

这说明当前问题路径是 uni-app 的 Gallery/Album 功能链路，不是相机拍照链路。

### 4.2 manifest 配置

`case-demo/manifest.json` 关键配置：

```json
{
  "name": "爱尚往约到家",
  "appid": "__UNI__32EB66E",
  "versionName": "1.2.9"
}
```

App 模块包含：

```json
{
  "Camera": {},
  "Push": {}
}
```

`plus.nativePlugins` 中声明了：

```json
{
  "EMPushUniPlugin": {}
}
```

### 4.3 推送插件权限声明

`case-demo/nativeplugins/EMPushUniPlugin/package.json` 中 Android 权限包含：

```text
android.permission.READ_MEDIA_VISUAL_USER_SELECTED
android.permission.WRITE_EXTERNAL_STORAGE
android.permission.READ_EXTERNAL_STORAGE
```

插件 Android 源码 `uniplugin_ease_push/src/main/AndroidManifest.xml` 中还包含：

```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32"/>
```

从源码看，插件侧已经声明了 Android 13+ 图片/视频媒体权限。

### 4.4 推送插件生命周期代码

`PushHookProxy.kt` 主要逻辑：

```kotlin
PushHelper.registerActivityLifecycle(application)
Notifier(it)
WXSDKEngine.registerModule("apexPlugin", PushModule::class.java)
```

`PushHelper.registerActivityLifecycle` 注册的是 `Application.ActivityLifecycleCallbacks`，用于记录前后台状态和生命周期日志。

当前未看到插件代码拦截 `onActivityResult`、替换图片选择器 Activity、拦截 MediaStore 查询或修改相册权限请求的逻辑。

因此，从源码层面看，推送插件直接导致图片选择器列表为空的证据不足。

## 5. 构建产物对比

### 5.1 产物目录大小

```text
936K  case-demo/unpackage/dist/build/app-plus
932K  case-demo/unpackage/resources/__UNI__32EB66E
```

两个目录体积接近。

### 5.2 前端关键文件 hash

`app-service.js`：

```text
da2e0d5a292931e53b5a337633192bbef9103315  case-demo/unpackage/dist/build/app-plus/app-service.js
da2e0d5a292931e53b5a337633192bbef9103315  case-demo/unpackage/resources/__UNI__32EB66E/www/app-service.js
```

`__uniapppicker.js`：

```text
34a4a78297e39d828ed1e3430f5713acaa1dd714  case-demo/unpackage/dist/build/app-plus/__uniapppicker.js
34a4a78297e39d828ed1e3430f5713acaa1dd714  case-demo/unpackage/resources/__UNI__32EB66E/www/__uniapppicker.js
```

结论：

- app-plus 构建产物和离线资源目录中的关键前端文件一致。
- 该证据基本排除“离线包和云包使用了不同 JS 图片选择逻辑”的可能。

### 5.3 APK 内前端资源 hash

云包 APK 内：

```text
assets/apps/__UNI__32EB66E/www/app-service.js       da2e0d5a292931e53b5a337633192bbef9103315
assets/apps/__UNI__32EB66E/www/__uniapppicker.js    34a4a78297e39d828ed1e3430f5713acaa1dd714
```

离线包 APK 内：

```text
assets/apps/__UNI__32EB66E/www/app-service.js       da2e0d5a292931e53b5a337633192bbef9103315
assets/apps/__UNI__32EB66E/www/__uniapppicker.js    34a4a78297e39d828ed1e3430f5713acaa1dd714
```

结论：

- 两个 APK 中 `__UNI__32EB66E` 对应的前端资源也是一致的。
- 当前差异更可能发生在 APK 原生层、运行时权限、DCloud runtime 或系统行为上。

## 6. APK 静态对比

使用 Android SDK `aapt` 对 APK 进行静态检查。

### 6.1 包名和版本

云打包 APK：

```text
package: name='uni.app.UNI32EB66E'
versionCode='129'
versionName='1.2.9'
compileSdkVersion='35'
targetSdkVersion:'30'
application-label:'爱尚往约到家'
native-code: 'arm64-v8a'
```

离线打包 APK：

```text
package: name='hyphenate.demo.push'
versionCode='1'
versionName='1.0'
compileSdkVersion='34'
targetSdkVersion:'34'
application-label:'UniApp-Push-Ts'
native-code: 'arm64-v8a' 'armeabi-v7a' 'x86' 'x86_64'
```

关键差异：

| 项目 | 云打包 | 离线打包 |
| --- | --- | --- |
| packageName | `uni.app.UNI32EB66E` | `hyphenate.demo.push` |
| versionName | `1.2.9` | `1.0` |
| targetSdkVersion | 30 | 34 |
| compileSdkVersion | 35 | 34 |
| ABI | arm64-v8a | arm64-v8a、armeabi-v7a、x86、x86_64 |

说明：

- `versionName` 是 APK 应用版本，不是 DCloud 版本、插件版本或 Android 系统版本。
- 包名不同会导致 Android 运行时权限、AppOps、系统相册授权状态完全独立，不能假设两个 App 权限状态一致。

### 6.2 媒体权限声明

云打包 APK 包含：

```text
android.permission.READ_MEDIA_IMAGES
android.permission.READ_MEDIA_VIDEO
android.permission.READ_MEDIA_VISUAL_USER_SELECTED
android.permission.READ_EXTERNAL_STORAGE maxSdkVersion=32
android.permission.WRITE_EXTERNAL_STORAGE maxSdkVersion=32
android.permission.CAMERA
```

离线打包 APK 也包含：

```text
android.permission.READ_MEDIA_IMAGES
android.permission.READ_MEDIA_VIDEO
android.permission.READ_MEDIA_VISUAL_USER_SELECTED
android.permission.READ_EXTERNAL_STORAGE maxSdkVersion=32
android.permission.WRITE_EXTERNAL_STORAGE maxSdkVersion=32
android.permission.CAMERA
```

结论：

- 最终 APK 层面，云包并不缺少 Android 13+ 媒体权限声明。
- 问题更可能是运行时授权状态、AppOps 状态，或 picker/runtime 对权限结果处理不同。

### 6.3 图片选择相关 Activity

云打包 APK manifest 中包含：

```text
io.dcloud.feature.nativeObj.photoview.PhotoActivity
com.dmcbig.mediapicker.PickerActivity
io.dcloud.common.util.DCloud_FileProvider
io.dcloud.feature.gallery.imageedit.IMGEditActivity
uts.sdk.modules.DCloudUniMedia.SystemPickerActivity
```

离线打包 APK manifest 中包含：

```text
io.dcloud.feature.nativeObj.photoview.PhotoActivity
com.dmcbig.mediapicker.PickerActivity
io.dcloud.common.util.DCloud_FileProvider
io.dcloud.feature.gallery.imageedit.IMGEditActivity
```

差异：

- 云包额外包含 `uts.sdk.modules.DCloudUniMedia.SystemPickerActivity`。
- 但从动态日志看，当前 `uni.chooseImage({ sourceType: ['album'] })` 实际启动的仍然是旧选择器 `com.dmcbig.mediapicker.PickerActivity`，不是 `SystemPickerActivity`。

### 6.4 APK 体积和 runtime 差异

```text
31M  /Users/asterisk/Downloads/__UNI__32EB66E_0701175059.apk
42M  /Users/asterisk/Codes/zuoyu/uniapp-push-plugin/app/build/intermediates/apk/debug/app-debug.apk
```

离线包体积更大，且包含多个 appid 的资源和多个 ABI。云包更接近生产包，仅包含 arm64-v8a。

这说明两个 APK 的 DCloud runtime、原生依赖组合和打包环境并不完全一致，即使 `__UNI__32EB66E` 的前端资源一致。

## 7. 动态日志对比

### 7.1 异常云包日志

日志文件：

```text
/tmp/cloud-apk-logcat.txt
```

关键行为：

```text
START ... cmp=uni.app.UNI32EB66E/com.dmcbig.mediapicker.PickerActivity
Displayed uni.app.UNI32EB66E/com.dmcbig.mediapicker.PickerActivity
```

说明：

- 云包已经成功启动图片选择器 Activity。
- 问题不是 Activity 启动失败。

云包同时出现：

```text
java.io.FileNotFoundException:
/storage/emulated/0/Android/data/uni.app.UNI32EB66E/apps/__UNI__32EB66E/doc:
open failed: EISDIR (Is a directory)

at io.dcloud.js.gallery.GalleryFeatureImpl...
Caused by: android.system.ErrnoException: open failed: EISDIR (Is a directory)
```

该异常发生在 DCloud GalleryFeatureImpl 执行期间。

### 7.2 正常离线包日志

日志文件：

```text
/tmp/cloud-apk-logcat-ok.txt
```

关键行为：

```text
START ... cmp=hyphenate.demo.push/com.dmcbig.mediapicker.PickerActivity
Displayed hyphenate.demo.push/com.dmcbig.mediapicker.PickerActivity for user 0: +32ms
```

说明：

- 正常离线包同样启动的是 `com.dmcbig.mediapicker.PickerActivity`。

正常离线包也出现：

```text
java.io.FileNotFoundException:
/storage/emulated/0/Android/data/hyphenate.demo.push/apps/__UNI__32EB66E/doc:
open failed: EISDIR (Is a directory)

at io.dcloud.js.gallery.GalleryFeatureImpl...
```

### 7.3 `EISDIR` 异常判断

异常云包和正常离线包都出现了相同类型的异常：

```text
GalleryFeatureImpl
FileNotFoundException
open failed: EISDIR (Is a directory)
```

因此该异常不能解释“云包没有图片、离线包有图片”的差异。

它更像是 DCloud GalleryFeatureImpl 在访问 app 私有 `doc` 路径时的兼容性/容错日志，而不是导致媒体列表为空的直接根因。

### 7.4 未发现明确崩溃或权限拒绝

两份日志中未看到可以直接定性的错误，例如：

```text
SecurityException
Permission Denial
READ_MEDIA_IMAGES denied
MediaProvider denied
ActivityNotFoundException
FATAL EXCEPTION
```

这说明：

- Picker Activity 没有崩溃。
- 系统没有在 logcat 中显式打印媒体权限拒绝。
- 如果确实是权限/AppOps 导致媒体列表为空，DCloud 旧 picker 很可能是静默处理了空查询结果。

### 7.5 推送插件生命周期日志

两边打开图片选择器时都能看到 `PushHelper` 生命周期日志，例如：

```text
PushHelper: onActivityPaused
PushHelper: onActivityCreated
PushHelper: onActivityStarted
PushHelper: onActivityResumed
```

正常离线包和异常云包行为一致。

结合源码看，推送插件当前只是注册 Activity 生命周期回调，没有看到拦截图片选择结果或媒体查询的行为。

因此目前不支持“推送插件生命周期回调直接导致 picker 空列表”的判断。

### 7.6 运行时权限与 AppOps 补充对比

后续通过 adb 对两个已安装应用做了运行时权限和 AppOps 对比。

云包 `dumpsys package uni.app.UNI32EB66E` 关键结果：

```text
android.permission.READ_MEDIA_VISUAL_USER_SELECTED: granted=true
android.permission.READ_MEDIA_IMAGES: granted=true
android.permission.READ_MEDIA_VIDEO: granted=true
```

离线包 `dumpsys package hyphenate.demo.push` 关键结果：

```text
android.permission.READ_MEDIA_VISUAL_USER_SELECTED: granted=true
android.permission.READ_MEDIA_IMAGES: granted=true
android.permission.READ_MEDIA_VIDEO: granted=true
```

这说明从 Android runtime permission 层面看，两个应用都拿到了图片/视频媒体权限。

但 `appops get` 的结果需要分两层看。

完整查询异常云包 `adb shell appops get uni.app.UNI32EB66E` 时，早先输出中没有看到 `READ_MEDIA_IMAGES: allow`，只看到：

```text
WRITE_MEDIA_IMAGES: deny
MANAGE_EXTERNAL_STORAGE: default
```

但后续单独查询异常云包 `READ_MEDIA_IMAGES`：

```text
adb shell appops get uni.app.UNI32EB66E READ_MEDIA_IMAGES
READ_MEDIA_IMAGES: allow
```

正常离线包单独查询 `READ_MEDIA_IMAGES`：

```text
adb shell appops get hyphenate.demo.push READ_MEDIA_IMAGES
READ_MEDIA_IMAGES: allow; time=+26m9s164ms ago
```

这个差异非常关键：

- `dumpsys package` 只能说明运行时 permission grant 状态。
- `appops` 是 Android 对敏感操作的实际访问控制层之一。
- 对媒体读取来说，permission grant 与 AppOps 状态不完全等价。
- 但单独查询后，云包并不是 AppOps deny，它也是 `READ_MEDIA_IMAGES: allow`。
- 离线包的 allow 后带有 `time=...`，说明该包最近实际触发过图片读取 AppOp。
- 云包只有 `READ_MEDIA_IMAGES: allow`，没有 `time=...`，说明当前没有看到它最近触发过图片读取 AppOp 的记录。

因此，当前证据应从“云包 AppOps 未授权”修正为：“云包 AppOps 处于 allow，但打开 picker 后没有留下图片读取访问时间记录”。这更接近旧 picker 查询路径、MediaStore 查询条件或 DCloud runtime 兼容问题，而不是简单的 AppOps deny。

### 7.7 推送相关操作后再打开相册的日志

后续又采集了云包在推送相关流程后打开相册的日志：

```text
/tmp/cloud-push-before-picker.txt
```

该日志用于验证一个新的假设：

```text
推送相关代码是否先发生 crash，然后影响后续相册操作。
```

关键时间线：

```text
07-01 19:26:17  Force stopping uni.app.UNI32EB66E
07-01 19:26:24  Start proc 4702:uni.app.UNI32EB66E
07-01 19:26:24  PushHookProxy onCreate
07-01 19:26:24  App 主页面启动完成
07-01 19:26:31  GalleryFeatureImpl 出现 EISDIR 日志
07-01 19:26:31  START PickerActivity
07-01 19:26:31  Displayed PickerActivity for user 0: +30ms
```

重点检索了以下崩溃/进程异常标志：

```text
FATAL EXCEPTION
AndroidRuntime
Fatal signal
SIGSEGV
Force finishing
has died
ANR
am_crash
```

未发现 `uni.app.UNI32EB66E` 的崩溃、ANR、进程死亡或被系统杀掉后重启。

进程状态上，日志显示：

```text
Start proc 4702:uni.app.UNI32EB66E
```

之后一直是同一个 pid `4702` 在执行启动、DCloud 页面、GalleryFeatureImpl 和 PickerActivity 流程，没有看到中途 `Killing 4702`、`Process has died` 或新的 `Start proc`。

推送相关日志主要是：

```text
PushHookProxy onCreate
PushHelper: onActivityCreated
PushHelper: onActivityStarted
PushHelper: onActivityResumed
PushHelper: onActivityPaused
```

这些是插件生命周期日志，不是崩溃日志。当前未看到环信/推送 SDK 的致命异常，也未看到 push 初始化后导致 Activity 或进程异常退出。

该日志中仍然出现：

```text
GalleryFeatureImpl
FileNotFoundException ... doc: open failed: EISDIR (Is a directory)
```

但该异常在正常离线包日志中也存在，并且异常后立即启动了 `com.dmcbig.mediapicker.PickerActivity`，因此仍不支持把它作为“云包相册空列表”的直接根因。

该日志没有看到和当前相册问题直接相关的系统拒绝：

```text
MediaProvider
MediaStore
READ_MEDIA_IMAGES denied
Permission Denial
SecurityException 访问图片
```

日志中的 Google Play/Finsky 网络错误、APN settings 权限错误等来自其他系统/应用流程，和当前 App 相册问题没有直接关系。

结论：

- “推送相关代码先 crash，然后影响后续相册操作”的可能性低。
- 仍不能完全排除“推送插件集成带来的非 crash 副作用”，例如 native 依赖、manifest 合并、DCloud runtime 初始化顺序变化。
- 要验证非 crash 副作用，需要构建一版不集成 `EMPushUniPlugin` 的 `case-demo` 云包做 A/B 对比。

## 8. 当前最可能原因

### 8.1 第一优先级：旧 picker 查询链路没有真正读到媒体

两个 APK 都声明了媒体权限，`dumpsys package` 中也都显示媒体权限已授权，并且单独查询 `READ_MEDIA_IMAGES` 时两个包都是 `allow`。

云包包名：

```text
uni.app.UNI32EB66E
```

离线包包名：

```text
hyphenate.demo.push
```

因此正常离线包能读取图片，并不能证明云包也已经获得了完整媒体访问权限。

本次实际对比中，离线包 AppOps 有最近访问时间：

```text
READ_MEDIA_IMAGES: allow; time=+26m9s164ms ago
```

云包 AppOps 是 allow，但没有最近访问时间：

```text
READ_MEDIA_IMAGES: allow
```

这说明两者不是简单的“一个 allow、一个 deny”，而是“正常包发生过被系统记录的图片读取，异常包当前没有看到图片读取访问记录”。

在 Android 13+，尤其 Android 14/15/16 的照片权限模型下，以下状态都可能导致旧 picker 查询不到完整图片：

- `READ_MEDIA_IMAGES` 未授权
- `READ_MEDIA_VIDEO` 未授权
- 只授予了“选择的照片”
- AppOps 对媒体读取是 `ignore`、`default` 或受限状态
- 系统权限 UI 对目标包名没有授予“所有照片和视频”

这类情况不一定在 logcat 中表现为 `SecurityException`，有可能只是 MediaStore 查询返回空。

结合本次结果，优先判断应调整为：

```text
云包媒体权限声明存在
云包 runtime permission grant 存在
云包 READ_MEDIA_IMAGES AppOps 是 allow
但云包没有 READ_MEDIA_IMAGES 最近访问时间记录
```

这比“APK 缺权限”或“前端代码不同”更接近当前现象。问题更可能发生在 DCloud 旧 picker 对 MediaStore 的查询链路中。

### 8.2 第二优先级：DCloud 云打包 runtime / 旧 picker 兼容问题

动态日志显示云包和离线包都走：

```text
com.dmcbig.mediapicker.PickerActivity
```

但云包和离线包的原生 runtime、targetSdk、compileSdk、ABI 组合不同。

云包虽然包含：

```text
uts.sdk.modules.DCloudUniMedia.SystemPickerActivity
```

实际却没有走系统 picker，而是继续走旧的 `com.dmcbig.mediapicker.PickerActivity`。

如果运行时权限确认无误，那么问题很可能落在：

- DCloud 云打包 runtime 版本差异
- 旧 `com.dmcbig.mediapicker.PickerActivity` 在 Android 16 上的 MediaStore 查询兼容问题
- 云包 targetSdk 30 与 Android 16 照片权限策略之间的兼容边界

## 9. 建议验证步骤

### 9.1 手机系统 UI 验证

在 Android 16 设备上检查云包：

```text
系统设置 -> 应用 -> 爱尚往约到家/云包 App -> 权限 -> 照片和视频
```

确认是否为：

```text
允许访问所有照片和视频
```

不要只授予“选择的照片”。

然后重新进入 App，打开图片选择器测试。

### 9.2 adb 验证权限和 AppOps

对云包执行：

```bash
adb shell dumpsys package uni.app.UNI32EB66E | grep -E "READ_MEDIA|READ_EXTERNAL|WRITE_EXTERNAL|CAMERA|granted="
adb shell appops get uni.app.UNI32EB66E
```

对正常离线包执行：

```bash
adb shell dumpsys package hyphenate.demo.push | grep -E "READ_MEDIA|READ_EXTERNAL|WRITE_EXTERNAL|CAMERA|granted="
adb shell appops get hyphenate.demo.push
```

重点对比：

```text
READ_MEDIA_IMAGES
READ_MEDIA_VIDEO
READ_MEDIA_VISUAL_USER_SELECTED
```

如果云包是 `ignore`、`default`、未授权，或只允许 selected photos，而离线包是 `allow`，则可以基本确认根因是运行时权限/AppOps 差异。

本次进一步单独查询后，实际输出应修正为：

```text
云包：READ_MEDIA_IMAGES: allow
离线包：READ_MEDIA_IMAGES: allow; time=+26m9s164ms ago
```

因此下一步应围绕“云包打开 picker 后为什么没有留下 READ_MEDIA_IMAGES 访问时间记录”继续验证。

### 9.3 如果权限一致

如果云包和离线包的运行时权限、AppOps 都一致，而云包仍然空列表，则建议继续验证：

1. 升级 HBuilderX / DCloud Android 云打包 SDK 后重新云打包。
2. 尝试改用系统 Photo Picker，而不是 DCloud 旧的 `com.dmcbig.mediapicker.PickerActivity`。
3. 在原生插件侧为 Android 13+ 单独提供系统图片选择能力，例如系统 Photo Picker 或 `ACTION_PICK_IMAGES`。
4. 对 DCloud GalleryFeatureImpl / PickerActivity 的 MediaStore 查询行为做更细日志定位。

## 10. 目前排除项

基于当前证据，暂时可以降低优先级的方向：

| 方向 | 判断 |
| --- | --- |
| JS 图片选择代码不同 | 不支持。APK 内 `app-service.js` 和 `__uniapppicker.js` hash 一致。 |
| 最终 APK 缺少媒体权限声明 | 不支持。两个 APK 都声明了 Android 13+ 媒体权限。 |
| Picker Activity 无法启动 | 不支持。两边都成功启动并显示 `com.dmcbig.mediapicker.PickerActivity`。 |
| `GalleryFeatureImpl EISDIR` 是根因 | 不支持。正常离线包也有同样异常。 |
| 推送插件直接拦截图片选择结果 | 当前证据不足。源码和日志均未看到相关拦截行为。 |
| 推送相关代码先 crash 导致后续相册异常 | 不支持。`/tmp/cloud-push-before-picker.txt` 中未发现 `FATAL EXCEPTION`、进程死亡、ANR 或重启，pid `4702` 从启动到打开 picker 持续存在。 |
| 云包 AppOps 是 deny | 不支持。单独查询 `READ_MEDIA_IMAGES` 时云包也是 `allow`。 |
| 两个包运行时媒体访问行为完全一致 | 不支持。离线包 `READ_MEDIA_IMAGES: allow` 后带最近访问时间，云包没有访问时间记录。 |

## 11. 当前建议结论

当前最合理的结论是：

云打包 APK 与离线打包 APK 的前端资源和媒体权限声明基本一致，运行时媒体权限也都显示已授权，单独查询 `READ_MEDIA_IMAGES` 时两个包也都是 `allow`。异常现象发生在 DCloud 旧图片选择器已经打开之后；结合 AppOps 访问时间差异，更可能是云包 DCloud runtime 中旧 picker 在 Android 16 上没有正确完成 MediaStore 图片读取，或查询路径没有触发/命中系统记录的 `READ_MEDIA_IMAGES` 访问。

补充的 `/tmp/cloud-push-before-picker.txt` 日志不支持“推送相关代码先 crash，然后影响后续相册操作”。日志中未发现 `uni.app.UNI32EB66E` 的崩溃、ANR、进程死亡或重启；push 相关日志主要是 `PushHookProxy onCreate` 和 `PushHelper` 生命周期回调。

下一步应优先验证为什么 `uni.app.UNI32EB66E` 打开 picker 后没有产生 `READ_MEDIA_IMAGES` 访问时间记录，包括打开 picker 前后连续执行单项 `appops get` 对比、系统照片权限 UI 复核、卸载重装后重新授权，以及检查云打包 runtime 是否正确触发了 MediaStore 图片查询。若 AppOps 访问记录仍不出现或仍然空列表，应转向 DCloud 云打包 runtime/旧 picker 兼容问题，并考虑切换系统 Photo Picker。

如果要继续验证推送插件是否存在非 crash 副作用，最硬的验证方式是打两版云包做 A/B：

```text
case-demo + EMPushUniPlugin
case-demo - EMPushUniPlugin
```

如果去掉插件后云包相册正常，则再回头检查插件依赖、manifest 合并、初始化顺序和 DCloud runtime 的交互；如果去掉插件后云包仍然空列表，则问题更偏向 DCloud 云打包 runtime / 旧 picker / Android 16 兼容。
