---
name: "prototype-flight-mixin"
description: "elytra flight / Mixin / velocity"
---

# Prototype 飞行 Mixin — 探索与实现

> 前置模组源码路径见 `.opencode/skills/_shared/libcode-paths.md`。
> 快速排除：当前任务如果不涉及鞘翅飞行物理、Mixin 注入点选择或客户端-服务端 velocity 同步，请跳过本 skill。

## 问题

为 Prototype 鞘翅实现"按 W 时沿视线方向匀速飞行"（velocity-set 模型），替代原版鞘翅的烟花火箭加速。要求：
- 平飞不下沉
- 仰头沿 y 轴上升
- 俯冲向标俯冲
- 不按 W 时恢复原版鞘翅物理

## 尝试路径

### V4：PlayerTickEvent.END + 加法加速器

```
motion += look × BOOST_FORCE + (0, HOVER_LIFT, 0)
```

**结果：** 完全无效。加法力在仰头时水平分量为 0，垂直分量不足以对抗重力。

**教训：** 加法模型不适合任意方向的匀速飞行。必须用 velocity-set（每 tick 重置速度为目标值）。

---

### V5：Mixin @Inject LivingEntity.travel() HEAD（无 cancel）

```java
@Mixin(LivingEntity.class)
@Inject(method = "travel", at = @At("HEAD"))
void gp$onTravelHead(...) {
    player.setDeltaMovement(look.scale(speed).add(0, 0.08, 0));
}
```

**结果：** 服务端触发，但客户端"所有浮力都消失了"——完全退回原版鞘翅。

**根因：** 客户端 `getFlightForward()` 始终返回 0。`C2SFlightInputPacket` 仅在服务端设 `setFlightForward`，客户端 `LocalPlayer` 的 PersistentData 从未写入。

**教训：** C2S 包的 handler 只跑在服务端。客户端要读同一份数据，必须在发送端也写本端。

---

### V5b：Mixin @Inject Player.travel() HEAD（cancellable = true）

```java
@Mixin(Player.class)
@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
void gp$onTravelHead(Vec3 travelVec, CallbackInfo ci) {
    player.setDeltaMovement(motion);
    player.move(MoverType.SELF, motion);
    ci.cancel();
}
```

**结果：** 服务端 Mixin 触发（forward=1.0），客户端 Mixin 不触发（forward=0）。

**根因：** 同上——客户端 `getFlightForward` 未设。Mixin 仅在 `Server thread` 命中，`Render thread` 上条件不通过。

**教训：** 判断 Mixin 是否生效时，不仅要看服务端日志，还要确认客户端线程是否触发。

---

## 关键技术发现

### 1. Player.travel() 委托链

```java
// Player.travel() 始终调用 super.travel()
public void travel(Vec3 pTravelVector) {
    // 游泳修正、创造飞行 -> 最终都调 super.travel()
    super.travel(pTravelVector);  // → LivingEntity.travel()
    this.checkMovementStatistics(...);
}
```

`super.travel()` 是 `LivingEntity.travel()`，鞘翅物理代码在其中。所以 Mixin 注入 `LivingEntity.travel()` HEAD **理论可行**。

### 2. ServerPlayer.isEffectiveAi() 返回 true

```java
// Entity.java（Player 未覆盖此方法）
public boolean isEffectiveAi() {
    return !this.level().isClientSide;
}
```

服务端非旁观者玩家 → `isEffectiveAi() = true` → `travel()` 体内逻辑（鞘翅物理）执行。

### 3. ServerPlayer.zza 永远为 0

```java
// LivingEntity.aiStep() — 不会从 input 设置 zza
this.zza = this.zza * 0.98f;  // 仅衰减

// LocalPlayer.aiStep() — 客户端才做拷贝
this.zza = this.input.forwardImpulse;  // 按 W -> 1.0
```

`LocalPlayer`（客户端）在 `aiStep()` 中将 `input.forwardImpulse` 拷贝到 `this.zza`。
**服务端 `ServerPlayer` 没有这个逻辑**，`zza` 永远为 0。

**结论：** 用 `player.zza` 判断 W 按下的方法**只在客户端有效**，服务端不行。

### 4. 客户端-服务端 forward 分裂

| 存储方式 | 客户端 | 服务端 |
|----------|--------|--------|
| `player.zza` | ✅ 1.0 (从 input 拷贝) | ❌ 永远 0 |
| `PrototypeItem.getFlightForward()` | ❌ 默认 0 | ✅ C2S packet 设置 |

**修复：** 在 C2S 发送端同时写本端 PersistentData：

```java
// PrototypeRenderEvents.java
float forwardVal = forwardDown ? 1.0f : 0.0f;
PrototypeItem.setFlightForward(player, forwardVal);  // 客户端本端写入
if (tick % 3 == 0) {
    ModNetwork.CHANNEL.sendToServer(new C2SFlightInputPacket(forwardVal)); // 服务端写入
}
```

### 5. cancellable = true 的注意事项

`ci.cancel()` 会跳过 `Player.travel()` 全部逻辑，包括：
- `checkMovementStatistics()` — 可接受（仅统计数据）
- `super.travel()` — 被替代，需手动调 `player.move(MoverType.SELF, motion)`

```java
player.setDeltaMovement(motion);
player.move(MoverType.SELF, motion);
ci.cancel();
```

**cancel 的适用条件：** 仅在已确认需要完全接管该 tick 的运动时使用。对于"仅在 elytra + W 按下"的条件分支，非激活时 return 让原方法正常运行。

## 最终实现

### Mixin（PrototypeTravelMixin.java）

```java
@Mixin(Player.class)
public abstract class PrototypeTravelMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void gp$onTravelHead(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.isFallFlying()) return;
        if (!PrototypeItem.hasAnyPrototype(player)) return;
        float forward = PrototypeItem.getFlightForward(player);
        if (!(forward > 0.01f)) return;

        Vec3 look = player.getLookAngle();
        double speed = ModConfig.FLIGHT_TARGET_SPEED.get();
        Vec3 motion = look.scale(speed).add(0, 0.08, 0);
        player.setDeltaMovement(motion);
        player.move(MoverType.SELF, motion);
        ci.cancel();
    }
}
```

### Config

```java
FLIGHT_TARGET_SPEED = BUILDER
    .defineInRange("flight_target_speed", 0.5, 0.1, 2.0);
```

重力补偿 0.08 硬编码（等于原版鞘翅 `motion.y -= 0.08`）。

## 实战经验

### Mixin 注入点选择

| 目标 | 方法 | 结果 |
|------|------|------|
| `LivingEntity.travel()` | `@At("HEAD")` | 能触发，但 client forward=0 问题同存在 |
| `Player.travel()` | `@At("HEAD")` cancellable | 更可控，可完全接管飞行 |

**推荐：** 对玩家专属功能直接用 `@Mixin(Player.class)` + `cancellable = true`，避免依赖 LivingEntity 内部逻辑分支。

### 客户端-服务端同步陷阱

- `PlayerTickEvent` 的 START/END 在客户端和服务端都触发
- Mixin 编译到两个环境，但 `@Inject` 的条件判断体在两端执行
- PersistentData（`entity.getPersistentData()`）在客户端和服务端是**独立实例**
- C2S 包 handler 只跑服务端，不写客户端
- `player.zza` 在 `LocalPlayer` 有效，`ServerPlayer` 无效

**黄金法则：** 需要在两端读同一份中间状态时——要么用 `player.zza` 这种本来就同步的字段，要么在 C2S 发送端同时写本端。

### 调试方法

1. 加 `GoetyMechMod.LOGGER.info` 看 Mixin 是否触发（区分 `[Server thread]` 和 `[Render thread]`）
2. 日志中看 `forward=` 值是否 > 0.01
3. 如 Mixin 只在 Server thread 触发 → forward 值在客户端为 0 → C2S 或本端写入问题
4. 用 `player.isFallFlying()` + `hasAnyPrototype` 缩小条件范围

### 相关文件

| 路径 | 角色 |
|------|------|
| `mixin/PrototypeTravelMixin.java` | Player.travel() HEAD 注入 |
| `client/PrototypeRenderEvents.java` | C2S 发送 + 本端 forward 写入 |
| `common/item/PrototypeItem.java` | getFlightForward / setFlightForward NBT |
| `config/ModConfig.java` | FLIGHT_TARGET_SPEED |
| `common/net/c2s/C2SFlightInputPacket.java` | 服务端 forward 设置 |
