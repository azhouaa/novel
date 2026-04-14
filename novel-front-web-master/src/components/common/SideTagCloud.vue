<template>
  <div
      v-if="leftTags.length"
      class="side-tag-cloud side-tag-cloud-left"
      :style="cloudStyle"
  >
    <a
        v-for="item in leftTags"
        :key="item.id"
        href="javascript:void(0)"
        class="tag-cloud-link"
        :style="item.style"
        @click="goBook(item.id)"
    >
      {{ item.bookName }}
    </a>
  </div>

  <div
      v-if="rightTags.length"
      class="side-tag-cloud side-tag-cloud-right"
      :style="cloudStyle"
  >
    <a
        v-for="item in rightTags"
        :key="item.id"
        href="javascript:void(0)"
        class="tag-cloud-link"
        :style="item.style"
        @click="goBook(item.id)"
    >
      {{ item.bookName }}
    </a>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted, onUnmounted, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { tagCloudTask } from "@/utils/tagCloudTask";

export default {
  name: "SideTagCloud",
  setup() {
    const route = useRoute();
    const router = useRouter();
    // 组件状态：保存左右标签原始数据、裁剪后数据与实际可用高度。
    const state = reactive({
      leftSourceTags: [],
      rightSourceTags: [],
      leftTags: [],
      rightTags: [],
      cloudHeight: 420,
    });

    let unsubscribe = null;
    // 词云整体下移，尽量利用下方空白区域。
    const topOffset = 188;
    // 底部预留，避免贴到底部影响视觉呼吸感。
    const bottomOffset = 72;

    // 根据页面高度动态裁剪标签数量，确保短页面也能完整展示。
    const applyVisibleTags = () => {
      const maxItemsPerSide = Math.max(8, Math.floor(state.cloudHeight / 62));
      state.leftTags = state.leftSourceTags.slice(0, maxItemsPerSide);
      state.rightTags = state.rightSourceTags.slice(0, maxItemsPerSide);
    };

    // 词云高度跟随文档总高度，滚动时与页面内容一起移动。
    const updateCloudHeight = () => {
      const pageHeight = Math.max(
          document.body.scrollHeight,
          document.documentElement.scrollHeight
      );
      state.cloudHeight = Math.max(280, pageHeight - topOffset - bottomOffset);
      applyVisibleTags();
    };

    onMounted(() => {
      // 订阅全局任务：保证多个页面共用同一份标签与刷新节奏。
      unsubscribe = tagCloudTask.subscribe((snap) => {
        state.leftSourceTags = snap.leftTags;
        state.rightSourceTags = snap.rightTags;
        applyVisibleTags();
      });
      updateCloudHeight();
      window.addEventListener("resize", updateCloudHeight);
    });

    onUnmounted(() => {
      if (unsubscribe) {
        unsubscribe();
      }
      window.removeEventListener("resize", updateCloudHeight);
    });

    watch(
        () => route.fullPath,
        () => {
          // 路由切换后等待 DOM 稳定，再重算标签云高度。
          setTimeout(updateCloudHeight, 300);
        }
    );

    // 点击标签后跳转到对应小说详情页。
    const goBook = (id) => {
      router.push({ path: `/book/${id}` });
    };

    // 将可配置的 top 与 height 注入到行内样式。
    const cloudStyle = computed(() => ({
      top: `${topOffset}px`,
      height: `${state.cloudHeight}px`,
    }));

    return {
      ...toRefs(state),
      goBook,
      cloudStyle,
    };
  },
};
</script>

<style scoped>
.side-tag-cloud {
  position: absolute;
  /* 每侧固定单列：一行一个标签。 */
  width: clamp(250px, 20vw, 340px);
  min-width: 250px;
  display: grid;
  grid-template-columns: 1fr;
  align-content: flex-start;
  /* 上下空间充足时，拉大标签纵向间距。 */
  gap: 22px;
  padding: 10px 8px;
  overflow: hidden;
  z-index: 30;
  pointer-events: auto;
  /* 仅展示文字标签，不展示外层块状底板。 */
  background: transparent;
  border: none;
  border-radius: 0;
}

.side-tag-cloud-left {
  /* 向主内容区适度靠拢，减少词云与正文之间的大空白。 */
  left: clamp(8px, calc((100vw - 1140px) / 2 - 260px), 56px);
  transform: none;
}

.side-tag-cloud-right {
  /* 向主内容区适度靠拢，减少词云与正文之间的大空白。 */
  right: clamp(8px, calc((100vw - 1140px) / 2 - 260px), 56px);
  transform: translateX(-20px);
}

.tag-cloud-link {
  display: block;
  width: 100%;
  cursor: pointer;
  letter-spacing: 0.5px;
  line-height: 1.35;
  padding: 2px 2px;
  border-radius: 4px;
  background: transparent;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.16);
  transition: transform 0.2s ease, opacity 0.2s ease, color 0.2s ease;
  /* 尽量完整显示标签，减少省略号。 */
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
  word-break: break-all;
}

.tag-cloud-link:hover {
  transform: translateY(-1px) scale(1.04) !important;
  opacity: 1 !important;
  color: #1f67d8 !important;
}

:global(body.theme-night) .tag-cloud-link {
  /* 夜间模式压暗标签颜色，避免出现刺眼亮点。 */
  color: #92a7c9 !important;
  opacity: 0.72 !important;
  text-shadow: none !important;
  background: transparent !important;
  box-shadow: none !important;
  filter: none !important;
}

:global(body.theme-night) .tag-cloud-link:hover {
  color: #b6c7e4 !important;
  opacity: 0.9 !important;
}

/* 桌面端优先，窄屏时逐步收缩宽度。 */
@media (max-width: 1200px) {
  .side-tag-cloud {
    width: clamp(190px, 16vw, 250px);
    min-width: 190px;
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 992px) {
  .side-tag-cloud {
    width: clamp(130px, 13vw, 170px);
    min-width: 130px;
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .side-tag-cloud {
    width: 300px;
    max-width: 300px;
    left: 50% !important;
    right: auto !important;
    transform: translateX(-50%) !important;
    margin: 0 auto;
    position: relative;
  }
}

@media (max-width: 480px) {
  .side-tag-cloud {
    width: 280px;
    padding: 12px 10px;
  }

  .tag-cloud-link {
    font-size: 13px;
    padding: 3px 6px;
  }
}
</style>
