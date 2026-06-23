---
name: "goety-block-anim-and-particle"
description: "block anim / particle / camera shake"
---

# Goety 方块动画 & 粒子系统指南

> 前置模组源码路径见 `.opencode/skills/_shared/libcode-paths.md`。
> 快速排除：当前任务如果不涉及 ModFallingBlock 动画、AoE 粒子指示器、爆炸环或镜头震动，请跳过本 skill。

## ModFallingBlock — 方块升起/飞散动画

### 两种模式

`ModFallingBlock.FallingBlockMode` 决定方块行为：

| 模式 | 用途 | 行为 |
|------|------|------|
| `MOBILE` | 飞散碎块 | 受重力+初始速度影响，向外飞出后坠落 |
| `POPUP_ANIM` | 原地升起后落下 | `animY += popUp; popUp -= 0.1F` 每 tick；先升后降，不横向移动 |

### 构造方法

```java
// MOBILE 模式
new ModFallingBlock(Level level, int lifetime, BlockState state)

// POPUP_ANIM 模式
new ModFallingBlock(Level level, Vec3 pos, BlockState state, float popUp)
```

### POPUP_ANIM 物理公式

- `popUp` 是 **初始向上速度**，**不是目标高度**
- 每 tick: `animY += popUp; popUp -= 0.1F`
- 峰值高度 = `popUp² / 0.2` （因为 `popUp` → 0 用时 `popUp/0.1` tick，平均速度 `popUp/2`）
- 反向：`popUp = sqrt(desiredHeight * 0.2f)`

| 目标高度 | popUp 值 |
|----------|----------|
| 0.5 | 0.316 |
| 1.0 | 0.447 |
| 2.0 | 0.632 |
| 3.0 | 0.775 |
| 4.0 | 0.894 |

Goety 原版使徒 roar 使用 `0.2 + gaussian*0.15`（≈0.02~0.5），对应 ≤1 格升起。

### MOBILE 模式常用

```java
ModFallingBlock debris = new ModFallingBlock(level, 40, state);
debris.setPos(x, y, z);
debris.setDeltaMovement(hSpeed, vSpeed, hSpeed);
debris.setMode(ModFallingBlock.FallingBlockMode.MOBILE);
level.addFreshEntity(debris);
```

`lifetime=40` → 2 秒后消失。

---

## AoEParticleOption — 地面半透明圆形范围

### 构造参数

```java
new AoEParticleOption(float radius, int duration)
// 或附带 owner（使追踪目标颜色）:
new AoEParticleOption(float radius, int duration, int ownerId)
```

- `radius`: 圆形半径
- `duration`: 持续 tick（40 = 2 秒）
- `ownerId`: 实体 ID，省略则灰色；传入后按阵营着色

### 使徒红色圆形 pattern（重点）

```java
level.sendParticles(
    new AoEParticleOption(radius, 40),  // 不带 ownerId
    x, y + 0.25, z,
    0,          // count = 0 → dx/dy/dz 变为 RGB
    red,        // 0.0 ~ 1.0
    green,
    blue,
    1.0);       // speed 无效，但需要占位
```

**关键机制**：
- `count = 0`：粒子只生成一次，位于精确位置（无随机分布）
- `count = 0` 时 `dx/dy/dz` 参数被 `AoEParticle.Provider.createParticle()` 转换为 `(float)p_234024_` 并存储为 `rCol/gCol/bCol`
- `count > 0` 时 `dx/dy/dz` 被解释为粒子散布范围，颜色为默认灰色

**红色值参考**：
```java
// 鲜艳红
level.sendParticles(new AoEParticleOption(radius, 40), x, y+0.25, z, 0, 0.8, 0.0, 0.0, 1.0);
// 淡红
level.sendParticles(new AoEParticleOption(radius, 40), x, y+0.25, z, 0, 0.5, 0.0, 0.0, 1.0);
```

**与使徒 RoarSpellGoal 对齐**：`level.sendParticles(new AoEParticleOption(radius, 40, entity.getId()), x, y+0.25, z, 0, red, green, blue, 1.0)` — 带 owner 时颜色根据阵营自动调整。

---

## CircleExplodeParticleOption — 爆炸环（外圈边界）

```java
new CircleExplodeParticleOption(float rCol, float gCol, float bCol, float radius, int count)
```

- 生成一圈粒子环，沿圆形边界分布
- `rCol/gCol/bCol`：颜色（0~1）
- `radius`：环半径
- `count`: 环上粒子数量

示例（红色爆炸环）：
```java
level.sendParticles(
    new CircleExplodeParticleOption(1.0f, 0.0f, 0.0f, radius, 2),
    x, y + 0.3, z, 1, 0, 0, 0, 0);
```

---

## CameraShake — 镜头震动

```java
CameraShake.cameraShake(Level level, Vec3 position, float radius, float intensity, int duration, int falloff)
```

- `radius`: 影响范围
- `intensity`: 震动强度
- `duration`: 持续 tick
- `falloff`: 随距离衰减程度

---

## 常见陷阱

1. **popUp 是速度不是高度**：`new ModFallingBlock(level, pos, state, popUp)` — `popUp` 传入过大（如 2.0）会导致方块飞到 20 格高；正确值 0.2~1.0 对应 0.2~5 格峰值
2. **AoEParticleOption count 参数**：`sendParticles(..., count, ...)` — 设 0 使 dx/dy/dz 变为 RGB；设 >0 使 dx/dy/dz 变为随机散布范围
3. **不传 ownerId 的 AoEParticleOption**：圆形呈灰色；传入 `player.getId()` 后根据玩家阵营着色（红色=敌对，绿色=友方）
4. **sendParticles 在 ClientLevel 不可用**：仅在 `ServerLevel` 调用
5. **findGroundPos 向下搜索**：`BlockFinder.moveDownToGround(entity)` 或手动 `findGroundPos(level, x, startY, z)` 限制搜索深度（通常 3 格）以避免穿透
