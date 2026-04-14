const THEME_KEY = "novel-theme-mode";

// 读取本地主题配置，默认白天模式。
export function getThemeMode() {
  return localStorage.getItem(THEME_KEY) || "day";
}

// 通过 body class 控制全局主题，避免逐页分别维护。
export function applyThemeMode(mode) {
  const resolved = mode === "night" ? "night" : "day";
  document.body.classList.remove("theme-day", "theme-night");
  document.body.classList.add(`theme-${resolved}`);
  localStorage.setItem(THEME_KEY, resolved);
  return resolved;
}

// 昼夜模式互斥切换。
export function toggleThemeMode() {
  const current = getThemeMode();
  const next = current === "night" ? "day" : "night";
  return applyThemeMode(next);
}
