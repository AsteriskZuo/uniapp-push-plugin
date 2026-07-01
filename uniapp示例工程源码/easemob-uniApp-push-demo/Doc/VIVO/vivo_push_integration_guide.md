# VIVO 推送集成指南

## 概述

本指南详细说明如何在 uni-app 项目中集成 VIVO 推送服务，确保应用能够在 VIVO 设备上正常接收推送消息。

## 核心集成步骤

### 1. VIVO 推送服务配置

#### 1.1 注册 VIVO 开发者账号

- 访问 [VIVO 开放平台](https://dev.vivo.com.cn/)
- 注册并登录开发者账号
- 创建应用并获取以下关键信息：
  - **AppID**: VIVO 分配的应用唯一标识（纯数字，如 `105792633`）
  - **AppKey**: 应用密钥，用于客户端验证（32 位字符串，如 `bf9b5941615f32b60ab906966b3b9755`）

#### 1.2 配置应用信息

在 VIVO 开放平台中配置以下信息：

- **应用包名**：必须与 uni-app 云打包时的包名**完全一致**
- **推送权限**：申请推送权限并配置推送图标

> **⚠️ 重要提示**：VIVO 开放平台填写的包名必须与 uni-app 云打包时填写的 Android 包名完全一致，否则会导致 `10003` 错误，Token 获取失败。

### 2. 环信后台上传 VIVO 推送证书

- 登录环信控制台
- 找到应用，点击应用详情
- 找到即时推送，选择证书管理，点击添加证书
- 上传 VIVO 推送证书

> **⚠️ 证书名称格式**：VIVO 的证书名称必须是 `appid#appkey` 的格式，用 `#` 号拼接。
>
> 例如：`105792633#bf9b5941615f32b60ab906966b3b9755`
>
> 如果证书名称格式不正确，会导致环信后台绑定成功，但杀掉应用后推送失败，报错 `notifier is null`。

### 3. uni-app 项目配置

#### 3.1 原生插件必要配置

- 打开 `manifest.json` 文件
- 在 `nativePlugins` -> `EMPushUniPlugin` 中填写以下字段：
  - `com.vivo.push.app_id`: VIVO 推送 AppID
  - `com.vivo.push.api_key`: VIVO 推送 AppKey

```json
"nativePlugins": {
    "EMPushUniPlugin": {
        "com.vivo.push.app_id": "105792633",
        "com.vivo.push.api_key": "bf9b5941615f32b60ab906966b3b9755"
    }
}
```

### 4. 关键代码集成

#### 4.1 初始化推送服务

```javascript
const pushOption = {
  // @ts-ignore
  emPush: EMPushUniPlugin,
  // 配置需要推送的证书名称
  config: {
    VIVOCertificateName: "105792633#bf9b5941615f32b60ab906966b3b9755", // VIVO 推送证书名称（appid#appkey）
  },
};
// 完整代码见 README.md 中示例
```

### 5. 常见问题及解决方案

#### 5.1 Token 获取失败（错误码 10003）

- **问题现象**：日志显示 `getPushTokenFail:VIVOPUSH 10003 state != 0`
- **核心原因**：VIVO 开放平台申请的证书包名与 uni-app 云打包时的包名不一致
- **解决方案**：
  1. 检查 VIVO 开放平台中的应用包名
  2. 检查 uni-app 云打包配置中的 Android 包名
  3. 确保两者**完全一致**
  4. 重新云打包后测试

#### 5.2 杀掉应用后推送失败（notifier is null）

- **问题现象**：环信管理后台显示绑定成功，但杀掉应用后收不到推送
- **核心原因**：VIVO 推送证书名称格式不正确
- **解决方案**：
  1. 确保证书名称格式为 `appid#appkey`
  2. 例如：`105792633#bf9b5941615f32b60ab906966b3b9755`
  3. 在 `pushHelper.js` 的 `VIVOCertificateName` 中配置正确的证书名称

#### 5.3 应用审核中不可发送正式消息（错误码 10045）

- **问题现象**：日志显示 `vivo push failed`，返回 `{"result":10045,"desc":"应用审核中不可发送正式消息"}`
- **核心原因**：VIVO 推送要求应用必须上架应用商店后才能发送正式推送消息
- **解决方案**：
  1. 将应用上架到 VIVO 应用商店
  2. 上架审核通过后，即可正常接收推送
  3. 在审核期间，推送功能受限，无法发送正式消息

#### 5.4 通知权限已开启但仍收不到推送

- 检查设备系统设置中是否允许该应用显示通知
- 确认 VIVO 推送服务已启用
- 验证服务端推送参数是否正确
- 检查应用是否已通过 VIVO 应用商店审核
