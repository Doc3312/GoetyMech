# 蓄力跳跃 — 开发经验

来源：prototype-charge-jump skill（等待重构合并至统一 physics skill）

## 关键物理公式

Minecraft 垂直运动带 0.98/tick drag，`sqrt(2 * g * h)` 真空公式不准。
正确做法：用 `velocityForHeight()` 二分法迭代，模拟含 drag 的垂直弹道。

## 高度测量

落地高度差（起落 Y 差）测出来永远 0，因为平面起落 Y 相同。
正确：追踪飞行中最大 Y（peakY）。

## 常见陷阱

- 地面起飞代码（`zza>0 && charge==0`）会覆盖为 2.0，导致蓄力跳没生效
- Curios 原型检测必须同时查 chest + Curios 槽（`hasAnyPrototype`）
- 改配置没生效 → 删 `run/config/GoetyMech-common.toml` 强制再生
