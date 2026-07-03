# case-demo Android 16 图片选择器问题 - 插件排除验证

日期：2026-07-01

## 1. 问题回顾

- 云打包 APK：无法打开图片选择器（选择器打开但图片列表为空）
- 离线打包 APK：可以正常打开图片选择器
- 初步怀疑：推送插件可能导致问题

## 2. 验证过程

### 2.1 去掉插件云打包

在 `case-demo/manifest.json` 中注释掉插件配置：

```json
"nativePlugins" : {
  // "EMPushUniPlugin": {}  // 注释掉
}
```

云打包后得到 APK：`__UNI__32EB66E_0701195515.apk`

### 2.2 APK 内容验证

**带插件云包** (`__UNI__32EB66E_0701175059.apk`)：
- ✅ 包含 `com/hyphenate/push/*` 相关类
- ❌ 相册空列表

**去掉插件云包** (`__UNI__32EB66E_0701195515.apk`)：
- ❌ 不包含 `com/hyphenate/push/*` 相关类
- ❌ 相册仍然空列表

### 2.3 插件打包方式确认

插件通过 AAR 方式集成：
- AAR 文件：`uniplugin_ease_push-release.aar`
- 位置：`case-demo/nativeplugins/EMPushUniPlugin/android/`
- 云打包时会被合并到 APK 的 dex 文件中

## 3. 结论

**推送插件不是导致图片选择器问题的原因。**

无论是否包含推送插件，云打包 APK 在 Android 16 上都无法正常显示图片列表。

## 4. 问题定位

问题更可能来自以下差异：

| 项目 | 云打包 | 离线打包 |
|------|--------|----------|
| targetSdkVersion | 30 | 34 |
| compileSdkVersion | 35 | 34 |
| DCloud runtime | 云打包版本 | 离线打包版本 |

## 5. 建议解决方案

### 5.1 提升 targetSdkVersion

在 `case-demo/manifest.json` 中修改：

```json
"android" : {
    "targetSdkVersion" : 34,  // 从 30 改为 34
    "minSdkVersion" : 21
}
```

重新云打包测试。

### 5.2 升级 HBuilderX

升级到最新版本的 HBuilderX，确保 DCloud Android 云打包 SDK 支持 Android 16。

### 5.3 联系 DCloud 技术支持

如果上述方法无效，联系 DCloud 技术支持确认 Android 16 兼容性问题。

## 6. 相关文档

- [case-demo-android16-image-picker-investigation.md](./case-demo-android16-image-picker-investigation.md) - 详细的对比调查记录
