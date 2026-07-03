# 1.1.2

- 修复集成插件后，uni-app 图片选择器可打开但无法显示相册图片的问题
  - 原因：插件 AAR 中声明了与推送无关的相册/存储权限、`FileProvider` 和 `file_paths.xml`，
    并携带了宿主级 `application` 属性，参与宿主 APK manifest 合并后可能影响图片选择器行为。
  - 修复：移除插件自身对相册/存储权限、`FileProvider`、`file_paths.xml root-path` 的声明，
    清理不必要的宿主级 `application` 属性，保持推送插件的 manifest 声明最小化。

# 1.1.1

- 修复云打包（或离线打包未引入 FCM 依赖）场景下，调用 `onRegister` 时崩溃的问题
  - 原因：插件在选择推送通道前会探测 FCM 是否可用，但当宿主 APK 中不存在
    `com.google.android.gms.common.GoogleApiAvailability` 等类时，会抛
    `java.lang.NoClassDefFoundError`（`Error` 而非 `Exception`，原有 `catch (Exception)` 无法捕获）。
  - 修复：在调用任何 FCM / Google Play Services API 之前，先通过反射探测相关类是否存在；
    所有 FCM 相关入口（`FCMPushHelper`、`PushConfig.fcmAvailable`、`PushHelper.getPushClient`、
    `FCMPush`）一律改为 `catch (Throwable)`，缺失依赖时安全降级到厂商通道。

# 1.1.0

- 增加 对 fcm 推送的支持
- 优化细节，提升程序的稳定性

# 1.0.0

- 创建 uni-app 原生插件
