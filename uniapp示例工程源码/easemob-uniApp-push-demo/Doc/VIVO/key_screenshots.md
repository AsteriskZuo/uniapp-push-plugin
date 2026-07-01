# VIVO 推送关键截图说明

## 1. VIVO 推送常见问题截图

### 1.1 Token 获取失败（错误码 10003）

![VIVO Token获取失败10003](/static/demo/vivo/iShot_2026-04-29_13.40.10.png)

**关键信息**:
- 错误日志：`getPushTokenFail:VIVOPUSH 10003 state != 0`
- 错误码：`10003`
- 错误原因：VIVO 开放平台申请的证书包名与 uni-app 云打包包名不一致

### 1.2 云打包包名配置

![VIVO 云打包包名配置](/static/demo/vivo/iShot_2026-04-29_13.40.50.png)

**关键信息**:
- Android 包名：`hyphenate.demo.push`
- 必须与 VIVO 开放平台中配置的应用包名完全一致
- 使用云端证书进行打包

### 1.3 Token 获取成功

![VIVO Token获取成功](/static/demo/vivo/iShot_2026-04-29_14.14.11.png)

**关键信息**:
- 成功日志：`getPushTokenSuccess:VIVOPUSH v2-...`
- 返回的 VIVO 推送 Token 格式：`v2-CRy1hHCV_Ldj6S74_...`
- 事件名称：`notification_renew_token`

### 1.4 环信后台证书绑定成功

![VIVO 环信后台证书绑定](/static/demo/vivo/iShot_2026-04-29_14.14.34.png)

**关键信息**:
- 证书名称：`105792633`（VIVO AppID）
- 推送 Token：与设备获取的 Token 一致
- 状态：绑定成功

### 1.5 用户推送信息详情

![VIVO 用户推送信息](/static/demo/vivo/iShot_2026-04-29_14.14.51.png)

**关键信息**:
- `device_token`: `v2-CRy1hHCV_Ldj6S74_EeUs2z5A6mbnEyecSBI8lcC_fJEu6hUP0-J7jsq`
- `notifier_name`: `105792633`（VIVO AppID）
- 推送信息已正确绑定到用户

### 1.6 VIVO 推送失败（错误码 10045）

![VIVO 推送失败10045](/static/demo/vivo/iShot_2026-04-29_14.32.49.png)

**关键信息**:
- 错误日志：`vivo push failed | pushMsg`
- 错误详情：`notifier=Notifier(name=105792633#bf9b5941615f32b60ab906966b3b9755)`
- 根本原因：应用未上架 VIVO 应用商店，返回 `{"result":10045,"desc":"应用审核中不可发送正式消息"}`
- **解决方案**：必须将应用上架到 VIVO 应用商店并通过审核

## 2. 截图命名规范

所有 VIVO 相关截图文件按照以下规范命名：
- `vivo_[功能]_[状态]_[时间戳].png`
- 例如：`vivo_token_success_20260429141411.png`
- 错误截图：`vivo_error_[错误码]_[时间戳].png`

## 3. 截图存储位置

将相关截图文件保存到：
- `/static/demo/vivo/` 目录下
- 确保截图清晰可读
- 敏感信息需要打码处理（如 AppKey 等）
