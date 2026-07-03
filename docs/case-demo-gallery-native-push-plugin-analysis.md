# case-demo 相册选择器与 EMPushUniPlugin 冲突排查记录

## 背景

`case-demo` 是 uni-app 小程序示例工程，使用原生推送插件 `EMPushUniPlugin`。该插件的 Android AAR 由同级源码工程 `uniplugin_ease_push` 构建。

问题现象：

- `case-demo` 集成 native 推送插件后，打开图片选择器可以进入界面，但看不到图片。
- 不使用 native 推送插件时，图片选择器可以正常显示图片。
- 因此初步怀疑 native 推送插件的 Android manifest、AAR 资源或三方厂商 SDK 合并项影响了 uni-app 的相册选择器。

## 排查重点

重点检查了以下内容：

- `uniplugin_ease_push/src/main/AndroidManifest.xml`
- `uniplugin_ease_push/src/main/res/xml/file_paths.xml`
- `case-demo/nativeplugins/EMPushUniPlugin/android/uniplugin_ease_push-release.aar`
- `case-demo/nativeplugins/EMPushUniPlugin/package.json`
- 厂商 AAR：OPPO、vivo、小米、荣耀
- 云打包验证包：`/Users/asterisk/Downloads/__UNI__32EB66E_0702212132.apk`

## 原始可疑点

### 1. 推送插件声明了相册/存储权限

旧版 `uniplugin_ease_push` 的 manifest 中声明了：

```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32"/>
```

推送插件本身不需要访问相册或外部存储。这类权限会参与最终 APK 的 manifest 合并，并可能影响 Android 13/14+ 上图片选择器的权限分支。

### 2. 推送插件曾声明 FileProvider

旧版插件 manifest 中存在类似配置：

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileProvider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

推送插件没有拍照、相册、文件分享职责，不应额外声明 FileProvider。

### 3. `file_paths.xml` 包含 `root-path`

旧版 `file_paths.xml` 包含：

```xml
<root-path
    name="root-path"
    path="/" />
```

`root-path="/"` 范围过大。即使不一定直接导致相册不显示，也属于明显风险点。推送插件不需要该资源，建议删除。

### 4. AAR manifest 修改宿主级 application 属性

旧版插件 manifest 的 `<application>` 上带有：

```xml
android:allowBackup="true"
android:allowClearUserData="true"
android:label="@string/app_name"
android:largeHeap="true"
android:supportsRtl="true"
android:theme="@android:style/Theme.NoTitleBar"
```

库 AAR 不应随意影响宿主 App 的全局 application 属性。推送插件应尽量只声明自己必需的 service、receiver、activity 和 meta-data。

## 三方厂商 AAR 判断

解包检查了以下 AAR 的 manifest：

- `oppo_push_3.5.2.aar`
- `vivo_push_v4.0.4.0_504.aar`
- `MiPush_SDK_Client_6_0_1-C_3rd.aar`
- `HonorPush-7.0.61.303.aar`

结论：

- OPPO/vivo/小米/荣耀 AAR 没有直接声明相册媒体读取权限。
- 小米、荣耀 SDK 有自己的推送组件和权限，但未发现直接访问相册或影响相册选择器的 manifest 项。
- 三方厂商 AAR 的直接嫌疑低于 `uniplugin_ease_push` 自身 manifest 与资源配置。

## 已做修改

### `uniplugin_ease_push/src/main/AndroidManifest.xml`

已处理：

- 注释/移除推送插件自身的相册和存储权限声明。
- 删除插件自己的 `androidx.core.content.FileProvider` 声明。
- 清理 `<application>` 上不必要的宿主级属性，使其保持为简单的组件容器。

当前重新打包后的 AAR manifest 中：

- `READ_MEDIA_IMAGES`、`READ_MEDIA_VIDEO`、`READ_MEDIA_VISUAL_USER_SELECTED`、`READ_EXTERNAL_STORAGE`、`WRITE_EXTERNAL_STORAGE` 均不再作为有效权限声明存在。
- 插件自己的 `FileProvider` 不再存在。
- `<application>` 不再携带 `label/theme/largeHeap/allowBackup` 等全局属性。

### `uniplugin_ease_push/src/main/res/xml/file_paths.xml`

已处理：

- 注释掉 `external-files-path`。
- 注释掉 `root-path path="/"`。

建议后续直接删除该文件，因为 manifest 已不再引用它，继续打进 AAR 没有实际价值。

### 重新构建 AAR

构建命令：

```bash
cd /Users/asterisk/Codes/zuoyu/uniapp-push-plugin
./gradlew :uniplugin_ease_push:assembleRelease
```

产物路径：

```text
/Users/asterisk/Codes/zuoyu/uniapp-push-plugin/uniplugin_ease_push/build/outputs/aar/uniplugin_ease_push-release.aar
```

替换到：

```text
/Users/asterisk/Codes/zuoyu/uniapp-push-plugin/uniapp示例工程源码/case-demo/nativeplugins/EMPushUniPlugin/android/uniplugin_ease_push-release.aar
```

## 云打包验证结果

使用替换后的 AAR 进行云打包，得到 APK：

```text
/Users/asterisk/Downloads/__UNI__32EB66E_0702212132.apk
```

验证结果：

- App 可正常打开相册。
- 图片选择器可以正常显示图片。
- 问题已解决。

使用 `aapt` 检查该 APK：

```bash
/Users/asterisk/Library/Android/sdk/build-tools/36.0.0/aapt dump badging /Users/asterisk/Downloads/__UNI__32EB66E_0702212132.apk
```

关键信息：

```text
package: name='uni.app.UNI32EB66E' versionCode='129' versionName='1.2.9'
targetSdkVersion:'30'
```

注意：最终 APK 中仍能看到部分媒体/存储权限：

```text
android.permission.WRITE_EXTERNAL_STORAGE
android.permission.READ_EXTERNAL_STORAGE
android.permission.READ_MEDIA_IMAGES
android.permission.READ_MEDIA_VIDEO
android.permission.READ_MEDIA_VISUAL_USER_SELECTED
```

因此，根因不能简单归结为“最终 APK 只要包含媒体权限就会导致相册异常”。本次验证更支持以下判断：

- 原插件 AAR 中的相册/存储权限、FileProvider、`file_paths.xml root-path`、`application` 全局属性组合参与 manifest 合并后，触发了 uni-app 图片选择器异常。
- 清理插件 AAR 自身不该声明的 manifest 项后，即使最终 APK 仍由其他模块带入媒体权限，相册也恢复正常。

## 当前结论

本问题已通过云打包实证解决。最可疑且已被验证有效的修复方向是：

1. 推送插件不要声明相册/存储权限。
2. 推送插件不要声明 FileProvider。
3. 推送插件不要携带 `root-path="/"` 的 `file_paths.xml`。
4. 推送插件不要修改宿主 App 的 `<application>` 全局属性。

三方厂商推送 AAR 目前未发现直接导致相册不显示的证据。

## 后续建议

1. 删除 `uniplugin_ease_push/src/main/res/xml/file_paths.xml`，避免无用资源继续进入 AAR。
2. 清理 `case-demo/nativeplugins/EMPushUniPlugin/package.json` 中和推送无关的权限，尤其是：

```json
"android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
"android.permission.WRITE_EXTERNAL_STORAGE",
"android.permission.READ_EXTERNAL_STORAGE"
```

3. 保持 native push plugin 的权限最小化，只保留推送确实需要的权限和组件。
4. 后续每次修改 AAR 后，重新执行：

```bash
./gradlew :uniplugin_ease_push:assembleRelease
```

并替换 `case-demo/nativeplugins/EMPushUniPlugin/android/uniplugin_ease_push-release.aar` 后再云打包验证。

5. 如果需要进一步最小化根因，可以做二分验证：

- 只回加媒体/存储权限。
- 只回加 FileProvider。
- 只回加 `root-path="/"`。
- 只回加 `<application>` 全局属性。

目前从验证结果看，`FileProvider + file_paths root-path + 插件权限/全局属性合并` 组合比单独媒体权限更可疑。
