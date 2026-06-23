# 调试经验 — 跳跃高度测量方法论

> 本文件记录实际调参过程中经历的多次 build-run 循环，以及从中总结的测量技术。

## 问题：公式高度 vs 实际高度

目标：验证 `velocityForHeight(h)` 能否让玩家精确跳到 `h` 格。

### 首次尝试：起落 Y 差（失败）

```
actualH = startY - player.getY()  // 落地时计算
```

**结果：全部 `0.0`**

```
*** DEBUG ChargeJump: charge=30, calcH=24.8, actualH=0.0
*** DEBUG ChargeJump: charge=60, calcH=50.0, actualH=0.0
```

**根因**：`PlayerTickEvent.START` 阶段 `onGround()` 是**上一 tick** 的地面状态。`applyChargedJump` 在下 tick 的 START 时 `onGround()` 仍为 `true` → 代码在玩家离地前就触发了。`startY == player.getY()` → `actualH=0`。且平面起落 Y 相同，即使正确时机也会得 0。

### 第二次：加 WAS_AIRBORNE 状态机（仍失败）

```java
if (contains(TAG_DBG_CHARGE)) {
    if (!player.onGround()) {
        put(TAG_DBG_WAS_AIRBORNE, true);  // 离地标记
    } else if (get(TAG_DBG_WAS_AIRBORNE)) {
        // 落地时算高度
        actualH = startY - player.getY();
    }
}
```

**结果：仍然是 `0.0`**

**根因**：即使延迟到了正确时机，`startY - landingY` 在平面上等于 `0`。问题不在于时序，在于**计算方式本身**。

### 第三次：追踪 peakY（成功）

```java
// 离地时更新峰值
if (!onGround()) {
    double peak = Math.max(peakY, player.getY());
    put(TAG_DBG_PEAK_Y, peak);
}
// 落地时
actualH = peakY - startY;
```

**结果：终于拿到正确数据**

```
*** DEBUG ChargeJump: charge=50, calcH=50.0, actualH=50.7
*** DEBUG ChargeJump: charge=25, calcH=20.6, actualH=16.5
*** DEBUG ChargeJump: charge=60, calcH=50.0, actualH=36.0
```

### 第四次：修正公式后验证

将 `sqrt(2gh)` 替换为含 drag 的 `velocityForHeight()`：

```
*** DEBUG ChargeJump: charge=50, calcH=41.6, actualH=42.2  ✓
*** DEBUG ChargeJump: charge=60, calcH=50.0, actualH=50.7  ✓（+1.4%）
*** DEBUG ChargeJump: charge=15, calcH=12.2, actualH=12.4  ✓
```

## 最终实测偏差

| 理论高度 | 实际高度 | 偏差 |
|---------|---------|------|
| 3.0 | 3.0~3.1 | ~0% |
| 12.2 | 12.4 | +2% |
| 29.0 | 29.4 | +1.4% |
| 41.6 | 42.2 | +1.4% |
| 50.0 | 50.7 | +1.4% |
| 80.0 | ~60.0 | **-25%** |

5~50 格区间误差 <2%，公式可靠。80 格偏差大，说明当前二分法在高速段有未考虑的物理因素（可能 Entity.move() 迭代限制或地面摩擦干扰）。

## 标准测量流程

给跳跃系统加 debug 日志时的推荐步骤：

1. **起跳时**：在 `setDeltaMovement` 处记录 `charge`、`calcHeight`、`startY`
2. **飞行中**：在 `PlayerTickEvent.START` 中更新 `peakY = max(peakY, player.getY())`
3. **落地时**：用 `WAS_AIRBORNE` 状态机过滤（防止跳前触发）：`!onGround() → 标记 | onGround() && 已标记 → 计算`
4. **清理**：落地后清除所有 debug tag，避免干扰下一跳
5. **Config 更改后**：必须删除 `run/config/*.toml` 强制再生（否则旧值覆盖新默认值）

## 关键教训

| 序号 | 教训 |
|------|------|
| 1 | 不要用 `startY - landingY` 算高度——它们在平面上相等 |
| 2 | 必须追踪飞行中的 `peakY` |
| 3 | `PlayerTickEvent.START` 的 `onGround()` 是延迟一 tick 的——用状态机区分"跳前"和"落地后" |
| 4 | 一个 build-run 循环只改一个变量，否则不知道哪个变化导致了偏差 |
| 5 | 首次加 debug log 时，确认 log 确实输出了预期的信息（否则可能根本没加载新代码） |
