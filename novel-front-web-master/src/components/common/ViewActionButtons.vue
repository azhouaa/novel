<template>
  <div class="view-actions">
    <a
      v-if="showRefresh"
      href="javascript:void(0)"
      class="action-item"
      @click="refreshCloud"
      :class="{ disabled: refreshing }"
      title="刷新标签云"
    >
      <span class="action-circle">↻</span>
      <span class="action-text">{{ refreshing ? "刷新中" : "刷新" }}</span>
    </a>

    <a
      href="javascript:void(0)"
      class="action-item"
      @click="switchTheme"
      :title="themeMode === 'night' ? '切换白天模式' : '切换夜间模式'"
    >
      <span class="action-circle">{{ themeMode === 'night' ? '☀' : '☾' }}</span>
      <span class="action-text">{{ themeMode === "night" ? "白天" : "夜间" }}</span>
    </a>
  </div>
</template>

<script>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { tagCloudTask } from "@/utils/tagCloudTask";
import { getThemeMode, toggleThemeMode } from "@/utils/theme";

export default {
  name: "ViewActionButtons",
  props: {
    showRefresh: {
      type: Boolean,
      default: false,
    },
  },
  setup() {
    const themeMode = ref(getThemeMode());
    const refreshing = ref(false);

    const refreshCloud = async () => {
      if (refreshing.value) {
        return;
      }
      refreshing.value = true;
      // 手动强制刷新：立即拉取新数据并重算随机样式。
      const result = await tagCloudTask.refreshNow(true);
      refreshing.value = false;
      if (result.success) {
        ElMessage.success("标签云已刷新");
      } else if (result.reason === "loading") {
        ElMessage.info("标签云正在刷新中");
      } else if (result.reason === "error") {
        ElMessage.error("标签云刷新失败，请稍后重试");
      }
    };

    const switchTheme = () => {
      // 切换昼夜主题，并同步按钮文案。
      themeMode.value = toggleThemeMode();
    };

    return {
      themeMode,
      refreshing,
      refreshCloud,
      switchTheme,
    };
  },
};
</script>

<style scoped>
.view-actions {
  position: fixed;
  right: 18px;
  top: 36%;
  z-index: 96;
}

.action-item {
  display: block;
  width: 64px;
  margin-bottom: 18px;
  text-align: center;
}

.action-circle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 5px 14px rgba(0, 0, 0, 0.08);
  color: #838ca0;
  font-size: 30px;
  line-height: 1;
}

.action-text {
  display: block;
  margin-top: 6px;
  color: #8f98aa;
  font-size: 30px;
  transform: scale(0.5);
  transform-origin: top center;
}

.action-item:hover .action-circle {
  background: #ffffff;
  color: #4f6287;
}

.action-item:hover .action-text {
  color: #5f708f;
}

:global(body.theme-night) .action-circle {
  background: rgba(35, 44, 63, 0.92);
  color: #c0c9dd;
}

:global(body.theme-night) .action-text {
  color: #bcc5d8;
}

.action-item.disabled {
  pointer-events: none;
  opacity: 0.7;
}
</style>
