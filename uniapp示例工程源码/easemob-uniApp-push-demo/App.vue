<script>
import PushHelper from "@/utils/pushHelper.js";

export default {
  onLaunch: function () {
    // #ifdef APP-PLUS
    console.log("App Launch");
    
    // 初始化推送模块（包含在线推送 + 离线推送）
    PushHelper.init();
    
    // 监听通知点击事件，处理用户点击通知后的页面跳转
    PushHelper.onNotificationClick((payload, msg) => {
      console.log("通知被点击，payload:", payload);
      // 示例：根据消息类型跳转到对应页面
      if (payload && payload.type === "chat") {
        uni.navigateTo({
          url: `/pages/chat/chat?id=${payload.from}`,
        });
      }
    });
    // #endif
  },
  onShow: function () {
    console.log("App Show");
  },
  onHide: function () {
    console.log("App Hide");
  },
};
</script>

<style>
/* 每个页面公共 css */
</style>
