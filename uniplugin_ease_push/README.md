# Android 平台推送集成指南

## 目录

- [Android 平台推送集成指南](#android-平台推送集成指南)
  - [目录](#目录)
  - [支持的推送平台](#支持的推送平台)
    - [推送路径说明](#推送路径说明)
  - [集成方式概述](#集成方式概述)
    - [1. 离线打包](#1-离线打包)
    - [2. 云打包](#2-云打包)
  - [离线打包集成 FCM](#离线打包集成-fcm)
    - [配置权限](#配置权限)
    - [FCM 配置](#fcm-配置)
    - [项目配置](#项目配置)
    - [打包与验证](#打包与验证)
  - [云打包集成](#云打包集成)
    - [AAR 配置](#aar-配置)
    - [权限配置](#权限配置)
    - [推送参数配置](#推送参数配置)
    - [构建与安装](#构建与安装)

## 支持的推送平台

本插件支持以下推送平台：

- 小米
- OPPO
- vivo
- 荣耀
- 魅族
- 华为
- FCM (Firebase Cloud Messaging)

### 推送路径说明

- 当国内厂商手机安装了谷歌服务并且配置了 FCM 推送时，将优先使用 FCM 推送；否则使用厂商自身的推送服务
- 其他手机厂商的推送路径：
  - 一加、真我：使用 OPPO 推送服务
  - PTAC：使用华为推送服务
  - 其他厂商：如已安装谷歌服务并配置 FCM，则使用 FCM 推送；否则无推送服务

## 集成方式概述

### 1. 离线打包

将 UniApp 打包成资源，放在 Android 项目中，通过 Android Studio 编译运行

### 2. 云打包

将 AAR 放在 UniApp 的指定位置，通过 HBuilder 进行云打包

**相关文档：**

- [离线打包说明文档](https://nativesupport.dcloud.net.cn/AppDocs/download/android.html)
- [云打包说明文档](https://doc.dcloud.net.cn/uniCloud/)

> **注意**：FCM 集成必须使用离线打包方式，其他厂商推送既可以离线打包也可以云打包。

## 离线打包集成 FCM

### 配置权限

Android 应用接收通知需要配置以下权限，修改`AndroidManifest.xml`文件：

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 基本网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>

    <!-- Android 13 通知运行时权限 -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

    <!-- Android 13 媒体权限 -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>

    <!-- Android 14 权限 -->
    <uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED"/>

    <!-- 存储权限（Android 12及以下） -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="32"/>
</manifest>
```

### FCM 配置

1. 进入[Firebase 控制台](https://console.firebase.google.com/)，创建项目并申请应用配置
2. 参考[FCM 集成文档](https://firebase.google.com/docs/cloud-messaging/android/client?hl=zh-cn)完成基本配置

![FCM配置](./docs/res/fcm_android_config.png)

### 项目配置

1. **添加 FCM 依赖**

   修改 app 文件夹下的`build.gradle`文件：

   ```gradle
   apply plugin: 'com.google.gms.google-services'

   // ...

   dependencies {
       // FCM配置
       implementation 'com.google.firebase:firebase-messaging:24.1.1'
       implementation 'com.google.android.gms:play-services-base:18.6.0'
   }
   ```

2. **添加配置文件**

   将`google-services.json`放在 app 文件夹下

   ![FCM配置文件](./docs/res/native-app_fcm_config.png)

3. **打包 App 资源**

   使用 HBuilder 将 UniApp 打包成 App 资源，放在`app/src/main/assets/apps`目录中

   ![构建资源](./docs/res/build_native_app_res.png)

4. **配置 App 资源**

   修改`app/src/main/assets/data/dcloud_control.xml`文件，设置 appid 为打包的文件夹名

   ![配置资源](./docs/res/native_config_app_res.png)

5. **配置 DCloud Key**

   修改`stand_alone/app/src/main/AndroidManifest.xml`文件，设置 dcloud_appkey

   [获取 AppKey](https://nativesupport.dcloud.net.cn/AppDocs/usesdk/appkey.html)

### 打包与验证

1. 使用 Android Studio 打开项目，安装应用到移动设备
2. 通过 FCM 控制台发送测试消息验证推送功能

   ![测试消息](./docs/res/fcm_send_test_message.png)

## 云打包集成

### AAR 配置

将 Android 依赖通过 AAR 方式引入，放在`nativeplugins/EMPushUniPlugin`目录下

![AAR配置](./docs/res/uni-app_naitve_aar.png)

### 权限配置

修改`manifest.json`文件配置权限：

```json
{
  "app-plus": {
    "distribute": {
      "android": {
        "permissions": [
          "<uses-permission android:name=\"android.permission.CHANGE_NETWORK_STATE\"/>",
          "<uses-permission android:name=\"android.permission.MOUNT_UNMOUNT_FILESYSTEMS\"/>",
          "<uses-permission android:name=\"android.permission.VIBRATE\"/>",
          "<uses-permission android:name=\"android.permission.READ_LOGS\"/>",
          "<uses-permission android:name=\"android.permission.ACCESS_WIFI_STATE\"/>",
          "<uses-feature android:name=\"android.hardware.camera.autofocus\"/>",
          "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\"/>",
          "<uses-permission android:name=\"android.permission.CAMERA\"/>",
          "<uses-permission android:name=\"android.permission.GET_ACCOUNTS\"/>",
          "<uses-permission android:name=\"android.permission.READ_PHONE_STATE\"/>",
          "<uses-permission android:name=\"android.permission.CHANGE_WIFI_STATE\"/>",
          "<uses-permission android:name=\"android.permission.WAKE_LOCK\"/>",
          "<uses-permission android:name=\"android.permission.FLASHLIGHT\"/>",
          "<uses-feature android:name=\"android.hardware.camera\"/>",
          "<uses-permission android:name=\"android.permission.WRITE_SETTINGS\"/>"
        ],
        "minSdkVersion": 21
      }
    }
  }
}
```

![权限配置](./docs/res/uni-app_native_permission.png)

### 推送参数配置

在`manifest.json`中配置各平台的推送参数：

```json
{
  "app-plus": {
    "nativePlugins": {
      "EMPushUniPlugin": {
        "hihonor_app_id": "YOUR_HONOR_APP_ID",
        "oppo_app_key": "YOUR_OPPO_APP_KEY",
        "oppo_app_secret": "YOUR_OPPO_APP_SECRET",
        "com.vivo.push.app_id": "YOUR_VIVO_APP_ID",
        "com.vivo.push.api_key": "YOUR_VIVO_API_KEY",
        "xiaomi_app_id": "YOUR_XIAOMI_APP_ID",
        "xiaomi_app_key": "YOUR_XIAOMI_APP_KEY",
        "meizu_app_id": "YOUR_MEIZU_APP_ID",
        "meizu_app_key": "YOUR_MEIZU_APP_KEY",
        "__plugin_info__": {
          // 插件配置信息
        }
      }
    }
  }
}
```

![推送配置](./docs/res/uni-app_native_config.png)

### 构建与安装

1. 使用 HBuilder 进行云构建（免费版可能需要排队等待）

   ![云构建](./docs/res/dcloud_mobile_app_build.png)

2. 构建完成后安装应用

   ![安装步骤1](./docs/res/uni-app_run_1.png)
   ![安装步骤2](./docs/res/uni-app_run_2.png)
