import { listTagCloudBooks } from "@/api/book";

// 统一3分钟刷新一次，避免切页频繁重算样式导致抖动。
const REFRESH_INTERVAL = 3 * 60 * 1000;
// 亮色/中性色/暗色混合色盘，避免标签云整体偏暗。
const COLOR_POOL = [
  "#1f2a44",
  "#2e3e5c",
  "#374f8a",
  "#4d658d",
  "#5a6c8f",
  "#6f7d95",
  "#2f7bd7",
  "#46a2ff",
  "#38a169",
  "#56c596",
  "#9f7aea",
  "#b794f4",
  "#e67e22",
  "#f6ad55",
  "#db4f83",
  "#f06292",
  "#0ea5a4",
  "#22c1c3",
  "#7a8aa1",
  "#5f6c7b",
];

function mixSeed(id, index, styleSeed) {
  const base = Number(id || 1);
  return base * 131 + index * 97 + styleSeed;
}

function buildTagStyle(id, index, styleSeed) {
  // 同一轮(styleSeed固定)下，标签样式稳定；下一轮刷新再整体变化。
  const seed = mixSeed(id, index, styleSeed);
  const fontSize = 50 + (Math.abs(seed) % 16);            //标签大小
  const rotate = (Math.abs(seed) % 33) - 16;
  const color = COLOR_POOL[Math.abs(seed) % COLOR_POOL.length];
  const opacity = 0.56 + (Math.abs(seed) % 36) / 100;
  const weight = [500, 600, 700][Math.abs(seed) % 3];
  return {
    fontSize: `${fontSize}px`,
    color,
    transform: `rotate(${rotate}deg)`,
    opacity,
    fontWeight: weight,
  };
}

class TagCloudTask {
  constructor() {
    this.leftTags = [];
    this.rightTags = [];
    this.lastRefreshAt = 0;
    this.styleSeed = 1;
    this.loading = false;
    this.timer = null;
    this.listeners = new Set();
  }

  subscribe(listener) {
    this.listeners.add(listener);
    // 订阅后立即推一次快照，保证组件首屏可渲染。
    listener(this.snapshot());
    this.start();
    return () => {
      this.listeners.delete(listener);
      if (!this.listeners.size) {
        this.stop();
      }
    };
  }

  snapshot() {
    return {
      leftTags: this.leftTags,
      rightTags: this.rightTags,
      loading: this.loading,
      lastRefreshAt: this.lastRefreshAt,
    };
  }

  notify() {
    const snap = this.snapshot();
    this.listeners.forEach((listener) => listener(snap));
  }

  start() {
    if (this.timer) {
      return;
    }
    if (!this.leftTags.length && !this.rightTags.length) {
      // 首次启动立即拉一次数据。
      this.refreshNow(false);
    }
    this.timer = setInterval(() => {
      const now = Date.now();
      if (now - this.lastRefreshAt >= REFRESH_INTERVAL) {
        this.refreshNow(true);
      }
    }, 10 * 1000);
  }

  stop() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  async refreshNow(force) {
    if (this.loading) {
      return;
    }
    const now = Date.now();
    if (!force && this.lastRefreshAt && now - this.lastRefreshAt < REFRESH_INTERVAL) {
      // 非强制刷新且未到间隔，直接跳过。
      return;
    }

    this.loading = true;
    this.notify();
    try {
      const { data } = await listTagCloudBooks({ size: 32 });
      const styleSeed = now;
      const left = [];
      const right = [];
      (data || []).forEach((item, index) => {
        const tag = {
          id: item.id,
          bookName: item.bookName,
          style: buildTagStyle(item.id, index, styleSeed),
        };
        // 左右交错分发，视觉更均衡。
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
    } finally {
      this.loading = false;
      this.notify();
    }
  }
}

export const tagCloudTask = new TagCloudTask();