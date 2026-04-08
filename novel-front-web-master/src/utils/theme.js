const THEME_KEY = "novel-theme-mode";

// 读取本地主题，默认白天模式。
export function getThemeMode() {
  return localStorage.getItem(THEME_KEY) || "day";
}

export function applyThemeMode(mode) {
  // 通过 body class 控制全局配色，避免逐页面单独维护。
  const resolved = mode === "night" ? "night" : "day";
  document.body.classList.remove("theme-day", "theme-night");
  document.body.classList.add(`theme-${resolved}`);
  localStorage.setItem(THEME_KEY, resolved);
  return resolved;
}

export function toggleThemeMode() {
  // 昼夜模式互斥切换。
  const current = getThemeMode();
  const next = current === "night" ? "day" : "night";
  return applyThemeMode(next);
}