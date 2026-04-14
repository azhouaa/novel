import { listTagCloudBooks } from "@/api/book";

// 统一 3 分钟刷新一次，避免切页频繁重算样式导致抖动。
const REFRESH_INTERVAL = 3 * 60 * 1000;
// 白天模式下的多彩色盘，提升活力感并覆盖更多色相。
const COLOR_POOL = [
  "#1f5db6",
  "#2f7bd7",
  "#46a2ff",
  "#5b8def",
  "#00a6a6",
  "#2dc6b8",
  "#38a169",
  "#5ac977",
  "#6f86ff",
  "#8d6bff",
  "#b07cf7",
  "#d277ff",
  "#e67e22",
  "#f6ad55",
  "#ff8c66",
  "#ff6b6b",
  "#db4f83",
  "#f06292",
  "#a66f4f",
  "#5f7f9e",
  "#4f647f",
  "#7aa8d8",
];

// 混合种子：确保同一轮刷新内样式稳定，下一轮整体变化。
function mixSeed(id, index, styleSeed) {
  const base = Number(id || 1);
  return base * 131 + index * 97 + styleSeed;
}

// 生成单个标签样式，控制大小、角度、颜色、透明度。
function buildTagStyle(id, index, styleSeed) {
  const seed = mixSeed(id, index, styleSeed);
  // 字号拉开梯度，但避免过大导致重叠过重。
  const fontSize = 42 + (Math.abs(seed) % 22);
  // 角度随机但不倒置，维持可读性。
  const rotate = (Math.abs(seed) % 27) - 13;
  const color = COLOR_POOL[Math.abs(seed) % COLOR_POOL.length];
  // 仅让标签轻微透明，避免发灰和发虚。
  const opacity = 0.62 + (Math.abs(seed) % 22) / 100;
  const weight = [500, 600, 700][Math.abs(seed) % 3];
  const translateY = (Math.abs(seed) % 10) - 5;
  return {
    fontSize: `${fontSize}px`,
    color,
    transform: `translateY(${translateY}px) rotate(${rotate}deg)`,
    opacity,
    fontWeight: weight,
  };
}

/**
 * 标签云共享任务：
 * 1. 全站共享一份标签数据，路由切换不重复抖动。
 * 2. 每 3 分钟自动刷新一次，支持手动刷新。
 */
class TagCloudTask {
  constructor() {
    // 左右两侧标签缓存，供多个页面共享。
    this.leftTags = [];
    this.rightTags = [];
    // 最近一次刷新时间，控制 3 分钟节奏。
    this.lastRefreshAt = 0;
    // 样式随机种子，每轮刷新更新一次。
    this.styleSeed = 1;
    // 刷新状态与错误信息，用于按钮反馈。
    this.loading = false;
    this.errorMessage = "";
    // 定时器与订阅者集合。
    this.timer = null;
    this.listeners = new Set();
  }

  /**
   * 订阅标签云快照，组件挂载后可立即拿到当前数据。
   */
  subscribe(listener) {
    this.listeners.add(listener);
    listener(this.snapshot());
    this.start();
    return () => {
      this.listeners.delete(listener);
      if (!this.listeners.size) {
        this.stop();
      }
    };
  }

  /**
   * 输出只读快照，避免外部直接改写内部状态。
   */
  snapshot() {
    return {
      leftTags: this.leftTags,
      rightTags: this.rightTags,
      loading: this.loading,
      errorMessage: this.errorMessage,
      lastRefreshAt: this.lastRefreshAt,
    };
  }

  /**
   * 广播最新快照给所有订阅者。
   */
  notify() {
    const snap = this.snapshot();
    this.listeners.forEach((listener) => listener(snap));
  }

  /**
   * 启动轮询计时器，并在首次订阅时立即触发一次刷新。
   */
  start() {
    if (this.timer) {
      return;
    }
    if (!this.leftTags.length && !this.rightTags.length) {
      this.refreshNow(false);
    }
    this.timer = setInterval(() => {
      const now = Date.now();
      if (now - this.lastRefreshAt >= REFRESH_INTERVAL) {
        this.refreshNow(true);
      }
    }, 10 * 1000);
  }

  /**
   * 所有页面都取消订阅后，关闭轮询计时器。
   */
  stop() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  /**
   * 立即刷新标签云；force=true 时忽略时间间隔限制。
   */
  async refreshNow(force) {
    if (this.loading) {
      return { success: false, reason: "loading" };
    }
    const now = Date.now();
    if (!force && this.lastRefreshAt && now - this.lastRefreshAt < REFRESH_INTERVAL) {
      return { success: false, reason: "interval" };
    }

    this.loading = true;
    this.errorMessage = "";
    this.notify();
    try {
      // 追加时间戳参数，规避 GET 缓存导致“刷新无变化”。
      // 前端发送的请求标签云个数 size，推荐偶数
      const { data } = await listTagCloudBooks({ size: 22, _t: now });
      const styleSeed = now;
      const left = [];
      const right = [];
      (data || []).forEach((item, index) => {
        const tag = {
          id: item.id,
          bookName: item.bookName,
          style: buildTagStyle(item.id, index, styleSeed),
        };
        // 左右交错分发，保持两侧视觉平衡。
        if (index % 2 === 0) {
          left.push(tag);
        } else {
          right.push(tag);
        }
      });
      this.leftTags = left;
      this.rightTags = right;
      this.styleSeed = styleSeed;
      this.lastRefreshAt = now;
      return { success: true, reason: "updated" };
    } catch (error) {
      this.errorMessage = error?.message || "刷新失败";
      return { success: false, reason: "error", error };
    } finally {
      this.loading = false;
      this.notify();
    }
  }
}

export const tagCloudTask = new TagCloudTask();
