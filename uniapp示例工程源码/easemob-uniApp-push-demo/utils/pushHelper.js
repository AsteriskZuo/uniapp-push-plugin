/**
 * 推送助手模块
 * 
 * 提供在线推送和离线推送的统一管理
 * 
 * 【在线推送】应用运行时收到消息，使用 uniApp 系统通知栏 API 显示本地通知
 * - 使用 plus.push.createMessage() 创建通知
 * - 应用层完全控制通知内容和样式
 * 
 * 【离线推送】应用被杀死时收到消息，通过厂商通道推送
 * - EMPushUniPlugin 只负责：获取设备 Token + 绑定到环信服务器
 * - 消息展示由系统通过厂商通道直接完成，应用层不参与解析
 * - 点击通知后才会唤醒应用
 */

import EMClient from "./WebIM.js";

// #ifdef APP-PLUS
const EMPushUniPlugin = uni.requireNativePlugin("EMPushUniPlugin");
// #endif

/**
 * 推送助手对象
 */
const PushHelper = {
  /**
   * 是否已初始化
   */
  _isInitialized: false,

  /**
   * 初始化推送模块
   * 在 App.vue 的 onLaunch 中调用
   */
  init() {
    // #ifdef APP-PLUS
    if (this._isInitialized) {
      console.log("[PushHelper] 已初始化，跳过");
      return;
    }

    console.log("[PushHelper] 开始初始化推送模块");

    // 1. 初始化离线推送插件
    if (EMPushUniPlugin) {
      EMPushUniPlugin.initPushModule();
      this._initOfflinePush();
    }

    // 2. 注册在线消息监听
    this._initOnlinePush();

    this._isInitialized = true;
    console.log("[PushHelper] 推送模块初始化完成");
    // #endif
  },

  /**
   * 初始化离线推送（环信推送插件 + 厂商通道）
   * 
   * 【插件功能说明】
   * EMPushUniPlugin 仅负责以下工作：
   * 1. 初始化各厂商推送 SDK（华为、小米、OPPO等）
   * 2. 获取设备的推送 Token
   * 3. 将 Token 绑定到环信服务器（登录后自动完成）
   * 
   * 【重要】插件不负责消息解析和展示，离线推送消息由厂商服务器
   * 直接推送至系统通知栏，应用被杀死时也能收到。
   * 
   * @private
   */
  _initOfflinePush() {
    const pushOption = {
      emPush: EMPushUniPlugin,
      config: {
        // 小米推送证书名称（在环信后台上传的证书名称）
        MICertificateName: "2882303761517520571",
        // 华为推送证书名称
        HMSCertificateName: "111809475",
        // OPPO推送证书名称
        // OPPOCertificateName: "xxxxxx",
        // VIVO推送证书名称
        VIVOCertificateName: "105792633#bf9b5941615f32b60ab906966b3b9755",
        // 荣耀推送证书名称
        // HONORCertificateName: "xxxxxx",
        // 魅族推送证书名称
        // MEIZUCertificateName: "xxxxxx",
        // APNs推送证书名称（iOS）
        // APNsCertificateName: "xxxxxx",
      },
    };
    // 注册推送插件到 IM SDK
    // 注册后 SDK 会在登录时自动完成：获取 Token -> 上传至环信服务器 -> 绑定证书
    EMClient.usePlugin(pushOption, "push");
    console.log("[PushHelper] 离线推送插件注册完成（负责Token获取与绑定，不负责消息解析）");
  },

  /**
   * 初始化在线推送（应用内本地通知）
   * @private
   */
  _initOnlinePush() {
    // 监听各类消息事件，在应用运行时显示本地通知
    EMClient.addEventHandler("pushHelperHandler", {
      // 文本消息
      onTextMessage: (message) => {
        this._showLocalNotification({
          title: message.from || "新消息",
          content: message.msg || message.data,
          payload: message,
        });
      },
      // 图片消息
      onImageMessage: (message) => {
        this._showLocalNotification({
          title: message.from || "新消息",
          content: "[图片]",
          payload: message,
        });
      },
      // 语音消息
      onAudioMessage: (message) => {
        this._showLocalNotification({
          title: message.from || "新消息",
          content: "[语音]",
          payload: message,
        });
      },
      // 视频消息
      onVideoMessage: (message) => {
        this._showLocalNotification({
          title: message.from || "新消息",
          content: "[视频]",
          payload: message,
        });
      },
      // 文件消息
      onFileMessage: (message) => {
        this._showLocalNotification({
          title: message.from || "新消息",
          content: "[文件]",
          payload: message,
        });
      },
      // 透传消息（通常用于推送）
      onCmdMessage: (message) => {
        console.log("[PushHelper] 收到透传消息:", message);
      },
    });
    console.log("[PushHelper] 在线推送监听注册完成");
  },

  /**
   * 显示本地通知（在线推送）
   * @param {Object} options
   * @param {String} options.title - 通知标题
   * @param {String} options.content - 通知内容
   * @param {Object} options.payload - 附加数据
   * @private
   */
  _showLocalNotification(options) {
    // #ifdef APP-PLUS
    const { title, content, payload } = options;

    // 获取应用运行状态
    const appState = plus.runtime.isBackground ? "background" : "foreground";
    console.log(`[PushHelper] 应用状态: ${appState}, 显示通知:`, title);

    // 创建本地通知
    plus.push.createMessage(content, JSON.stringify(payload), {
      title: title,
      icon: "static/logo.png",
      sound: "system",
      cover: false, // 不覆盖上一次通知，每条都显示
      when: new Date(),
      // 点击通知后的跳转处理
      payload: payload,
    });
    // #endif
  },

  /**
   * 监听通知点击事件
   * 在 App.vue 的 onLaunch 中调用，用于处理用户点击通知后的跳转
   * @param {Function} callback - 点击回调函数，参数为 payload 数据
   */
  onNotificationClick(callback) {
    // #ifdef APP-PLUS
    // 监听点击系统通知栏消息事件
    plus.push.addEventListener("click", (msg) => {
      console.log("[PushHelper] 用户点击通知:", msg);
      let payload = msg.payload;
      try {
        // payload 可能是 JSON 字符串，尝试解析
        if (typeof payload === "string") {
          payload = JSON.parse(payload);
        }
      } catch (e) {
        console.log("[PushHelper] payload 解析失败，使用原始值");
      }
      if (typeof callback === "function") {
        callback(payload, msg);
      }
    });

    // 监听接收消息事件（应用运行时）
    plus.push.addEventListener("receive", (msg) => {
      console.log("[PushHelper] 接收到推送消息:", msg);
    });
    // #endif
  },

  /**
   * 主动解绑推送 Token
   * 通常在退出登录时调用
   */
  unbindPushToken() {
    // #ifdef APP-PLUS
    console.log("[PushHelper] 解绑推送 Token");
    EMClient.unbindPushToken();
    // #endif
  },

  /**
   * 获取推送配置信息（调试用）
   */
  getPushInfo() {
    // #ifdef APP-PLUS
    return {
      EMPushUniPlugin: !!EMPushUniPlugin,
      isInitialized: this._isInitialized,
    };
    // #endif
    return null;
  },
};

export default PushHelper;
