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
    const state = reactive({
      leftSourceTags: [],
      rightSourceTags: [],
      leftTags: [],
      rightTags: [],
      cloudHeight: 420,
    });

    let unsubscribe = null;
    const topOffset = 150; // 略微上移，避免被导航栏遮挡
    const bottomOffset = 80;

    const applyVisibleTags = () => {
      // 根据页面可用高度裁剪标签数量，避免短页被截断。
      const maxItemsPerSide = Math.max(10, Math.floor(state.cloudHeight / 27));
      state.leftTags = state.leftSourceTags.slice(0, maxItemsPerSide);
      state.rightTags = state.rightSourceTags.slice(0, maxItemsPerSide);
    };

    const updateCloudHeight = () => {
      // 词云高度跟随页面长度，而不是固定视口高度。
      const pageHeight = Math.max(
          document.body.scrollHeight,
          document.documentElement.scrollHeight
      );
      state.cloudHeight = Math.max(240, pageHeight - topOffset - bottomOffset);
      applyVisibleTags();
    };

    onMounted(() => {
      // 订阅全局标签云任务，所有页面共享同一份数据与刷新节奏。
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
          // 路由切换后等待DOM稳定，再重新计算页面高度。
          setTimeout(updateCloudHeight, 300);
        }
    );

    const goBook = (id) => {
      router.push({ path: `/book/${id}` });
    };

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
  /* 宽度扩展为视口的 40%，最大 520px */
  width: calc(40vw - 20px);
  max-width: 520px;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 8px 12px;
  padding: 16px 14px;
  //z-index: 999; /* 确保在最上层 */
  overflow: hidden;
  pointer-events: auto;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border-radius: 8px;
}

.side-tag-cloud-left {
  left: 0;
  /* 微调避免完全贴边 */
  transform: translateX(10px);
}

.side-tag-cloud-right {
  right: 0;
  /* 微调避免完全贴边 */
  transform: translateX(-10px);
}

.tag-cloud-link {
  display: inline-block;
  font-size: 14px; /* 增大字体 */
  line-height: 1.2;
  padding: 4px 8px; /* 增加内边距 */
  border-radius: 4px; /* 添加圆角 */
  background: rgba(255, 255, 255, 0.15); /* 添加背景色 */
  text-shadow: 0 1px 6px rgba(255, 255, 255, 0.45);
  transition: transform 0.18s ease, opacity 0.18s ease, background 0.18s ease;
}

.tag-cloud-link:hover {
  transform: translateY(-2px) scale(1.06) !important;
  opacity: 1 !important;
  background: rgba(255, 255, 255, 0.25) !important;
}

:global(body.theme-night) .tag-cloud-link {
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.42);
  background: rgba(0, 0, 0, 0.2);
}

:global(body.theme-night) .tag-cloud-link:hover {
  background: rgba(0, 0, 0, 0.3) !important;
}

/* 响应式：小屏时缩小宽度，避免遮挡内容 */
@media (max-width: 1200px) {
  .side-tag-cloud {
    width: calc(35vw - 15px);
    max-width: 420px;
  }
}

@media (max-width: 992px) {
  .side-tag-cloud {
    width: calc(30vw - 10px);
    max-width: 360px;
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