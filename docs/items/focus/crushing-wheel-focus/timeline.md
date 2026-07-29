# 粉碎轮聚晶 — Timeline

## 2026-07-13

- **2026-07-13** — 添加合成配方 (crushing_wheel_focus.json)
  - 原因：补全生存获取途径

## 2026-07-15

- **重命名 + 纹理 + 冷却**：`stress_focus` → `crushing_wheel_focus`
  - 注册 ID：`stress_focus` → `crushing_wheel_focus`
  - 纹理：`stress_focus.png` → `crushing_wheel_focus.png`（来自 Desktop `create_crushing_wheel.png`）
  - 配置字段：`STRESS_FOCUS_COST/COOLDOWN` → `CRUSHING_WHEEL_FOCUS_COST/COOLDOWN`
  - 默认冷却：`20t (1s)` → `400t (20s)`，范围扩至 1200
  - lang key：全部从 `stress_focus` → `crushing_wheel_focus`
  - 中文名："应力聚晶" → "粉碎轮聚晶"
