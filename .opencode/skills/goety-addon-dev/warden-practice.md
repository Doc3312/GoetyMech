# Warden（坚守者）开发实战经验

> 来自 SculkBindSpell 开发过程，涵盖召唤原版 Warden、Brain 记忆操作、出土动画、音效冲突等。

## Warden Brain 活动优先级

| 优先级 | 活动 | 说明 |
|--------|------|------|
| 0 | EMERGE | 出土动画 |
| 0 | DIG | 遁地 |
| 1 | ROAR | 怒吼 |
| 2 | FIGHT | 战斗 |
| 3 | INVESTIGATE | 调查 |
| 4 | SNIFF | 嗅探 |
| 6 | IDLE | 空闲 |

**核心陷阱**：DIG 和 EMERGE 优先级相同且都高于 FIGHT。即使设置 ATTACK_TARGET，如果 Warden 有遁地条件，Brain 会优先 DIG。

**正确做法**：召唤时设置 DIG_COOLDOWN 禁止遁地：

```java
warden.getBrain().setMemoryWithExpiry(
    MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200); // 60s 冷却
```

## 激怒 Warden 的方式

| 方式 | 效果 | 评价 |
|------|------|------|
| 手动设置 ANGRY_AT 记忆 | 不触发 AngerManagement | 不稳定 |
| 手动设置 ATTACK_TARGET | 直接指定目标 | 不稳定 |
| 循环 `increaseAngerAt(target)` | 每次 +35，5 次 = 175 > 80 阈值 | ✅ 可用 |
| `increaseAngerAt(target, 100, false)` | 单次 100 = 80 + 20，直接超 FIGHT 阈值 | **推荐** |
| 发射 `GameEvent.SHRIEK` | 振动检测 → 间接索敌 | 辅助手段 |

**推荐方案**：使用 3 参数重载直接指定愤怒值（Goety 官方做法）：

```java
// 100 = AngerLevel.ANGRY.getMinimumAnger() (80) + 20
// false = 不触发 Sculk Shrieker 连锁召唤
warden.increaseAngerAt(target, 100, false);
```

**3 参数重载签名**（vanilla `Warden` 类）：
```java
public void increaseAngerAt(@Nullable LivingEntity target, int angerIncrease, boolean listen)
```
- `target` — 愤怒目标
- `angerIncrease` — 直接指定愤怒增加值
- `listen` — `true` 时触发附近 Sculk Shrieker 振动监听链（可能再召 Warden），`false` 静默注入

**Goety 中该 API 的使用场景**（均传 `false` 防连锁召 Warden）：

| 来源 | 愤怒值 | 场景 |
|------|--------|------|
| `ModEvents.java` (Apostle 无敌阶段) | 80+20=100 | 打 Apostle 的 mob 被拉到 ObsidianMonolith |
| `AbstractObsidianMonolith.java` (柱子死亡) | 80+20=100 | 击杀柱子的 mob 转火 Apostle |
| `SkullLord.java` (挨打时) | 80+100=180 | 攻击 SkullLord 的 mob 转火 BoneLord |

## Warden 目标死后转火（LivingDeathEvent 重定向）

**场景**：召唤 Warden 击杀束缚目标后，立即对施法者愤怒。

**实现方案**：

1. **Warden 生成时打标签**（PersistentData）：
```java
warden.getPersistentData().putUUID("goetypunk.bind_caster", caster.getUUID());
warden.getPersistentData().putUUID("goetypunk.bind_target", target.getUUID());
```

2. **LivingDeathEvent 中转火**（仅击杀者自身转火）：
```java
if (!(event.getSource().getEntity() instanceof Warden warden)) return;
CompoundTag tag = warden.getPersistentData();
if (!tag.hasUUID("goetypunk.bind_caster")) return;
if (!dead.getUUID().equals(tag.getUUID("goetypunk.bind_target"))) return;

if (warden.level().getPlayerByUUID(tag.getUUID("goetypunk.bind_caster")) instanceof ServerPlayer player) {
    warden.increaseAngerAt(player, 100, false);
}
tag.remove("goetypunk.bind_caster");
tag.remove("goetypunk.bind_target");
```

3. **法术执行期目标意外死亡补救**（SpellResult 中 fallback）：
```java
if (!target.isAlive()) {
    warden.increaseAngerAt(caster, 100, false);
    // 清除标签防重复
    warden.getPersistentData().remove("goetypunk.bind_caster");
    warden.getPersistentData().remove("goetypunk.bind_target");
}
```

**规则**：
- 仅亲手击杀的 Warden 获得转火（各自独立），非击杀者的同批 Warden 不会转火
- 避免使用 `setAttackTarget()` 和 Brain 记忆直接操作（doc 标注为不稳定）
- 仅依赖 `increaseAngerAt()` 三参数重载，和 Goety 官方做法一致
- `false` 禁止 Sculk Shrieker 连锁召唤

## 关键经验总结

| 做法 | 结论 |
|------|------|
| 用循环 `increaseAngerAt` ×N 或单次 `increaseAngerAt(entity, amount, false)` | ✅ 稳定 |
| 直接设 `ATTACK_TARGET` / `ANGRY_AT` Brain 记忆 | ❌ 不稳定 |
| 只有 `increaseAngerAt` 能触发 `AngerManagement` 系统 | ✅ 必须 |
| 转火时传 `listen=false` | ✅ 防连锁召 Warden |

## 出土动画（Emerging）

**错误**：`setMemory` 永久设置 IS_EMERGING → 死循环
**正确**：`setMemoryWithExpiry` 设过期时间：

```java
warden.getBrain().setMemoryWithExpiry(
    MemoryModuleType.IS_EMERGING, Unit.INSTANCE, 100); // 5 秒
```

## 出土动画首帧闪现问题

**现象**：召唤时先出现 0.5s，然后消失再播出土动画。

**根因**：服务端 `setPose(EMERGING)` 确保 spawn packet 包含正确姿态，但客户端 animation state machine 尚未初始化。

**解决方案**：延迟可见性：

```java
// spawn 前
warden.setPose(Pose.EMERGING);
warden.setInvisible(true);
warden.getPersistentData().putLong("goetypunk.visible_at",
    worldIn.getGameTime() + 15);
worldIn.addFreshEntity(warden);
```

```java
// LivingTickEvent 中
if (entity instanceof Warden warden) {
    long visibleAt = entity.getPersistentData().getLong("goetypunk.visible_at");
    if (visibleAt > 0 && entity.level().getGameTime() >= visibleAt) {
        warden.setInvisible(false);
        entity.getPersistentData().remove("goetypunk.visible_at");
    }
}
```

## 音效冲突

**根因**：同一 SoundEvent + 同一 SoundSource.HOSTILE 多实例互相干扰。

**解决方案**：同一音效全局只播一次，适当提高音量：

```java
// ✅ 正确
worldIn.playSound(null, targetPos, SoundEvents.SCULK_SHRIEKER_SHRIEK,
    SoundSource.HOSTILE, 3.0F, 1.0F);
for (int i = 0; i < wardenCount; i++) { /* 创建 Warden，不再播音效 */ }

// ❌ 错误：循环内播放
for (int i = 0; i < wardenCount; i++) {
    worldIn.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, ...);
}
```

## 决策：自定义 Warden vs 原版 Warden

| 方案 | 优点 | 缺点 | 结论 |
|------|------|------|------|
| 自定义 Warden 实体 | 完全控制 | 需重写全部 AI（~3000 行） | **不推荐** |
| 召唤原版 Warden + 操作 Brain | 零维护成本 | 需理解 Brain 优先级 | **推荐** |

## 关键记忆模块速查

| 记忆模块 | 类型 | 用途 |
|----------|------|------|
| `IS_EMERGING` | `Unit` | 触发出土动画，必须 `setMemoryWithExpiry` |
| `DIG_COOLDOWN` | `Unit` | 禁止遁地 |
| `ROAR_TARGET` | `Entity` | 怒吼目标 |
| `ATTACK_TARGET` | `Entity` | 攻击目标 |
| `ANGRY_AT` | `Entity` | 愤怒对象 |
| `IS_SNIFFING` | `Unit` | 正在嗅探 |
| `DISTURBANCE_LOCATION` | `BlockPos` | 振动扰动位置 |

## 开发流程

### 召唤 Warden

```
 1. 创建原版 Warden 实例
 2. 设置位置、持久化
 3. 设置 DIG_COOLDOWN 记忆（禁遁地）
 4. 设置 IS_EMERGING 记忆（带过期时间）
 5. increaseAngerAt(target, 100, false) 注入愤怒（单次三参数版本）
 6. setPose(EMERGING) + setInvisible(true)（防闪现）
 7. PersistentData 存可见性恢复时间戳 + bind_caster/bind_target 标签
 8. addFreshEntity()
 9. [可选] 目标已死 → increaseAngerAt(caster, 100, false) 转火
10. 事件处理器：到期恢复可见 + LivingDeathEvent 转火逻辑
```

### 目标死后转火 Warden

```
 1. LivingDeathEvent → event.getSource() 是带标签的 Warden
 2. 确认 dead.getUUID() == warden.getPersistentData().bind_target
 3. increaseAngerAt(player, 100, false) 注入愤怒
 4. 清标签防重复触发
```
