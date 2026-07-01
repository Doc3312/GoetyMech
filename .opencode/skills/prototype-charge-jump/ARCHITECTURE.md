# 系统架构 — 蓄力跳跃全链路

## 设计决策：Release-Based

蓄力系统**不在按住时立即起跳**，而是**在松开时执行跳**：

```
按住空格 (action=1) → 每 tick 累加蓄力值（NBT: GoetyMech.jump_charge_tick）
松开空格 (action=0) → 根据总蓄力值计算速度并执行跳
```

### 为什么不用 Client-Predictive

- 服务端权威：所有跳跃决策在服务端做，client 只管发 action
- 避免反作弊冲突
- 简化同步逻辑

## 数据流

```
Client ←→ Server
  │
  ├── C2SJumpChargePacket(action)
  │     ├── action > 0: 蓄力中 → PrototypeItem.setJumpChargeTick(player, charge)
  │     └── action == 0: 释放 → PrototypeEvents.applyChargedJump(player, charge)
  │
  └── PrototypeEvents.onPlayerTick(PlayerTickEvent)
        ├── START phase: 蓄力累加 / 地面起飞 / 落地检测 / 飞行启动
        └── END phase: 飞行加速 / 壁面移动
```

### 关键文件

| 文件 | 职责 |
|------|------|
| `common/events/PrototypeEvents.java` | 核心逻辑：蓄力处理、跳跃执行、物理公式 |
| `common/item/PrototypeItem.java` | NBT 存取方法：get/set/clearJumpCharge, hasAnyPrototype |
| `mixin/PrototypePlayerMixin.java` | 蓄力时压制 `jumpFromGround()` 防止冲突 |
| `common/net/c2s/C2SJumpChargePacket.java` | 网络包 encode/decode/handle |
| `common/net/ModNetwork.java` | SimpleChannel 注册 |
| `common/utils/CuriosHelper.java` | Curios 槽位检测 |
| `config/ModConfig.java` | JUMP_* 配置项 |
| `client/PrototypeRenderEvents.java` | 客户端蓄力进度渲染 |

## 时序细节

### PlayerTickEvent.START 阶段陷阱

START phase 的事件顺序：

```
上一个 tick 结束时的 onGround = false/true
        ↓
当前 tick START phase fires
        ↓
  → 此时 player.onGround() 仍反映上一 tick 的落地状态
  → 不是当前 tick 的落地状态！← 关键
        ↓
aiStep() → travel() → move()（更新 onGround）
        ↓
PlayerTickEvent.END phase fires
```

**影响**：
- 落地检测：不能在 START 直接检查 `onGround()`，要用 `WAS_AIRBORNE` 状态机
- 地面起飞：同样 START 时 `onGround()` 为 true，`zza > 0` 时会无条件覆写速度

### 地面起飞覆盖陷阱

`onPlayerTick` START phase 中有如下代码（`PrototypeEvents.java` ~line 238）：

```java
if (player.onGround() && player.zza > 0.0F && charge == 0) {
    player.setDeltaMovement(m.x, 2.0, m.z);  // ← 覆盖蓄力跳速度！
}
```

`charge == 0` 在 `clearJumpCharge()` 后始终成立。如果玩家按 W，`zza > 0` → 速度被覆写为 `2.0`（约 15 格高度）。

**修复**：给这段代码加 `&& !hasPendingChargeJump` 条件，或在蓄力跳后设一个临时标志。

## Curios 槽位检测

必须同时查 chest 和 Curios 两个槽：

```java
public static boolean hasAnyPrototype(LivingEntity entity) {
    if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PrototypeItem)
        return true;
    if (entity instanceof Player player) {
        return CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.isEquipped(s -> s.getItem() instanceof PrototypeItem))
            .orElse(false);
    }
    return false;
}
```

仅查 chest 会漏掉 Curios 槽位的原型。

## Mixin 跳跃压制

```java
@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
private void gp$cancelJumpWhileCharging(CallbackInfo ci) {
    if (PrototypeItem.getJumpChargeTick((Player)(Object)this) > 0) {
        ci.cancel();
    }
}
```

原版的 `jumpFromGround()` 会给 0.42 速度，会与蓄力跳冲突。蓄力中必须取消它。

## Config 配置项

| 配置 | 默认 | 说明 |
|------|------|------|
| `jump_tap_height` | 3.0 | 短按跳跃高度（低于 threshold 时） |
| `jump_charge_threshold` | 6 | 蓄力门槛 tick（≥此值走蓄力公式） |
| `jump_charge_min_height` | 8.0 | threshold 时的高度 |
| `jump_charge_max_height` | 80.0 | 最大蓄力时的高度（实测 ~60） |
| `jump_max_charge_ticks` | 60 | 最大蓄力时间（tick） |

**注意**：修改配置默认值后，必须删除 `run/config/GoetyMech-common.toml` 强制再生，否则 TOML 中的旧值覆盖新默认值。
