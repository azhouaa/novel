<template>
  <div class="floating-ai-chat" :style="widgetStyle">
    <transition name="ai-panel-fade">
      <div v-if="expanded" class="ai-chat-panel">
        <div class="ai-chat-panel-title">AI 找书助手</div>
        <div ref="messageBoxRef" class="ai-chat-message-box">
          <div
            v-for="(item, index) in messages"
            :key="index"
            :class="['ai-chat-message-item', `is-${item.role}`]"
          >
            <p class="ai-chat-message-text">{{ item.content }}</p>
            <ul v-if="item.books && item.books.length" class="ai-chat-book-list">
              <li v-for="book in item.books" :key="book.id">
                <a href="javascript:void(0)" @click.prevent="jumpToBook(book.id)">
                  《{{ book.bookName }}》
                  <span v-if="book.authorName"> - {{ book.authorName }}</span>
                  <span v-if="book.categoryName"> ({{ book.categoryName }})</span>
                </a>
              </li>
            </ul>
          </div>
        </div>
        <div class="ai-chat-input-wrap">
          <textarea
            v-model="inputMessage"
            placeholder="例如：推荐男频玄幻爽文，节奏快、字数多一些"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <button :disabled="sending || !inputMessage.trim()" @click="sendMessage">
            {{ sending ? "推荐中..." : "发送" }}
          </button>
        </div>
      </div>
    </transition>

    <button class="ai-chat-fab" @pointerdown.prevent="startDrag" @click="togglePanel">
      {{ expanded ? "收起" : "AI找书" }}
    </button>
  </div>
</template>

<script>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { chatWithAi } from "@/api/ai";

const STORAGE_KEY = "novel_ai_chat_position";
const GAP = 16;
const BUTTON_SIZE = 58;
const PANEL_WIDTH = 360;
const PANEL_HEIGHT = 460;

export default {
  name: "FloatingAiChat",
  setup() {
    const route = useRoute();
    const router = useRouter();
    const messageBoxRef = ref(null);

    const state = reactive({
      expanded: false,
      sending: false,
      inputMessage: "",
      x: 0,
      y: 0,
      suppressClick: false,
      dragInfo: {
        active: false,
        startPointerX: 0,
        startPointerY: 0,
        startX: 0,
        startY: 0,
      },
      messages: [
        {
          role: "assistant",
          content:
            "你好，我是找书助手。告诉我题材、风格或关键词，我会从站内数据库筛选小说并给你可跳转链接。",
          books: [],
        },
      ],
    });

    const widgetStyle = computed(() => ({
      left: `${state.x}px`,
      top: `${state.y}px`,
    }));

    const clamp = (value, min, max) => {
      if (max < min) {
        return min;
      }
      return Math.min(Math.max(value, min), max);
    };

    const getBounds = (expanded) => {
      const vw = window.innerWidth;
      const vh = window.innerHeight;
      const maxX = expanded ? vw - PANEL_WIDTH - GAP : vw - BUTTON_SIZE - GAP;
      const minY = expanded ? PANEL_HEIGHT + GAP : GAP;
      const maxY = vh - BUTTON_SIZE - GAP;
      return {
        minX: GAP,
        maxX,
        minY,
        maxY,
      };
    };

    const normalizePosition = (expanded = state.expanded) => {
      const { minX, maxX, minY, maxY } = getBounds(expanded);
      state.x = clamp(state.x, minX, maxX);
      state.y = clamp(state.y, minY, maxY);
    };

    const savePosition = () => {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          x: state.x,
          y: state.y,
        })
      );
    };

    const initPosition = () => {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        try {
          const pos = JSON.parse(raw);
          if (typeof pos.x === "number" && typeof pos.y === "number") {
            state.x = pos.x;
            state.y = pos.y;
            normalizePosition(false);
            return;
          }
        } catch (error) {
          console.warn("AI 悬浮窗位置读取失败:", error);
        }
      }
      state.x = window.innerWidth - BUTTON_SIZE - GAP;
      state.y = window.innerHeight - BUTTON_SIZE - GAP;
      normalizePosition(false);
    };

    const scrollToBottom = async () => {
      await nextTick();
      if (messageBoxRef.value) {
        messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight;
      }
    };

    const handleResize = () => {
      normalizePosition();
      savePosition();
    };

    const togglePanel = async () => {
      if (state.suppressClick) {
        state.suppressClick = false;
        return;
      }
      state.expanded = !state.expanded;
      normalizePosition(state.expanded);
      savePosition();
      if (state.expanded) {
        await scrollToBottom();
      }
    };

    const onPointerMove = (event) => {
      if (!state.dragInfo.active) {
        return;
      }
      const dx = event.clientX - state.dragInfo.startPointerX;
      const dy = event.clientY - state.dragInfo.startPointerY;
      if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
        state.suppressClick = true;
      }
      state.x = state.dragInfo.startX + dx;
      state.y = state.dragInfo.startY + dy;
      normalizePosition();
    };

    const onPointerUp = () => {
      if (!state.dragInfo.active) {
        return;
      }
      state.dragInfo.active = false;
      window.removeEventListener("pointermove", onPointerMove);
      window.removeEventListener("pointerup", onPointerUp);
      savePosition();
      if (state.suppressClick) {
        window.setTimeout(() => {
          state.suppressClick = false;
        }, 80);
      }
    };

    const startDrag = (event) => {
      if (event.button !== undefined && event.button !== 0) {
        return;
      }
      state.dragInfo.active = true;
      state.dragInfo.startPointerX = event.clientX;
      state.dragInfo.startPointerY = event.clientY;
      state.dragInfo.startX = state.x;
      state.dragInfo.startY = state.y;
      window.addEventListener("pointermove", onPointerMove);
      window.addEventListener("pointerup", onPointerUp);
    };

    const buildHistory = () =>
      state.messages.slice(-8).map((item) => ({
        role: item.role,
        content: item.content,
      }));

    const sendMessage = async () => {
      const content = state.inputMessage.trim();
      if (!content || state.sending) {
        return;
      }
      state.messages.push({
        role: "user",
        content,
        books: [],
      });
      state.inputMessage = "";
      state.sending = true;
      await scrollToBottom();

      try {
        const { data } = await chatWithAi({
          message: content,
          history: buildHistory(),
        });
        state.messages.push({
          role: "assistant",
          content: data?.answer || "我找到了几本相关小说，你可以直接点击查看。",
          books: data?.books || [],
        });
      } catch (error) {
        state.messages.push({
          role: "assistant",
          content: "暂时无法获取推荐，请稍后再试。",
          books: [],
        });
      } finally {
        state.sending = false;
        await scrollToBottom();
      }
    };

    const jumpToBook = (bookId) => {
      if (!bookId) {
        return;
      }
      if (route.name === "book") {
        window.location.assign(
          `${window.location.origin}${window.location.pathname}#/book/${bookId}`
        );
        return;
      }
      router.push({
        path: `/book/${bookId}`,
      });
    };

    onMounted(() => {
      initPosition();
      window.addEventListener("resize", handleResize);
    });

    onBeforeUnmount(() => {
      window.removeEventListener("resize", handleResize);
      window.removeEventListener("pointermove", onPointerMove);
      window.removeEventListener("pointerup", onPointerUp);
    });

    return {
      expanded: computed(() => state.expanded),
      sending: computed(() => state.sending),
      inputMessage: computed({
        get: () => state.inputMessage,
        set: (value) => {
          state.inputMessage = value;
        },
      }),
      messages: computed(() => state.messages),
      widgetStyle,
      messageBoxRef,
      togglePanel,
      startDrag,
      sendMessage,
      jumpToBook,
    };
  },
};
</script>

<style scoped>
.floating-ai-chat {
  position: fixed;
  z-index: 3000;
}

.ai-chat-fab {
  width: 58px;
  height: 58px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #1f8fff, #19b8c9);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 10px 24px rgba(31, 143, 255, 0.35);
  cursor: grab;
}

.ai-chat-fab:active {
  cursor: grabbing;
}

.ai-chat-panel {
  position: absolute;
  left: 0;
  bottom: 72px;
  width: 360px;
  height: 460px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e6edf6;
  box-shadow: 0 16px 36px rgba(18, 29, 51, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-chat-panel-title {
  height: 48px;
  line-height: 48px;
  padding: 0 14px;
  color: #1b2a3b;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid #eef3f9;
}

.ai-chat-message-box {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  background: #f6f9fc;
}

.ai-chat-message-item {
  margin-bottom: 10px;
  max-width: 92%;
}

.ai-chat-message-item.is-user {
  margin-left: auto;
}

.ai-chat-message-item.is-assistant {
  margin-right: auto;
}

.ai-chat-message-text {
  margin: 0;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  color: #1d2735;
  word-break: break-word;
}

.ai-chat-message-item.is-user .ai-chat-message-text {
  background: #1f8fff;
  color: #fff;
}

.ai-chat-message-item.is-assistant .ai-chat-message-text {
  background: #fff;
  border: 1px solid #e4ebf4;
}

.ai-chat-book-list {
  margin: 8px 0 0;
  padding-left: 14px;
}

.ai-chat-book-list li {
  margin-bottom: 6px;
  font-size: 13px;
}

.ai-chat-book-list a {
  color: #1677ff;
}

.ai-chat-input-wrap {
  border-top: 1px solid #eef3f9;
  padding: 10px;
  background: #fff;
}

.ai-chat-input-wrap textarea {
  width: 100%;
  height: 72px;
  border: 1px solid #dbe5f0;
  border-radius: 8px;
  outline: none;
  padding: 10px;
  font-size: 13px;
  resize: none;
  box-sizing: border-box;
}

.ai-chat-input-wrap textarea:focus {
  border-color: #1f8fff;
}

.ai-chat-input-wrap button {
  margin-top: 8px;
  float: right;
  min-width: 86px;
  height: 34px;
  border: none;
  border-radius: 8px;
  background: #1f8fff;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.ai-chat-input-wrap button:disabled {
  background: #adc9ef;
  cursor: not-allowed;
}

.ai-panel-fade-enter-active,
.ai-panel-fade-leave-active {
  transition: all 0.2s ease;
}

.ai-panel-fade-enter-from,
.ai-panel-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 600px) {
  .ai-chat-panel {
    width: 320px;
    height: 420px;
  }
}
</style>
