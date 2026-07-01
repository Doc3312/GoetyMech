# `shouldSprint()` 守卫导致跑墙断冲刺

status: verified

## 症状

跑墙 vaule 翻越后，爬墙冲刺自动中断，需要重新按冲刺键。

## 根因

`PrototypeRenderEvents.java:81-83` 的守卫逻辑：

```java
if (!ClientWallRunState.shouldSprint()) {
    ClientWallRunState.sprintDown = mc.options.keySprint.isDown() || player.isSprinting();
}
```

`shouldSprint()` 在 vault 后返回 true（`sprintRetainTicks > 0`，60 tick 窗口），导致 `sprintDown` **不刷新**——卡在 mixin 设的 `true`。

60 tick 过期后，`shouldSprint()` 降为 false，`sprintDown` 从物理键刷新：

- **toggle sprint**: 键已松(false) || isSprinting 已被 vanilla 清掉(false) → **sprintDown = false** → 断冲刺
- **hold sprint**: 键还按着(true) || isSprinting(false) → sprintDown = true → 正常

## 历史对照

原始代码 (`f9b9ee8`) 每 tick **直接**从物理键读 sprintDown，没有任何 `shouldSprint()` 守卫，从来不跳 sprint。

## 教训

1. **不要加不必要的守卫。** 原始代码每 tick 直接读物理键，vault 后 sprint 不会断。加 `sprintRetainTicks + shouldSprint()` 破坏了原有的简单直连。
2. **"保护"式逻辑容易产生隐藏的过期问题。** 60 tick 的保护窗口一过，`sprintDown` 从被保护状态跌入"按键抬起 + vanilla 清空 isSprinting"的双重 false 陷阱。
3. **恢复方案**：去掉守卫，恢复原始的直接按键轮询：

   ```java
   ClientWallRunState.tickSprintRetain();
   ClientWallRunState.sprintDown = mc.options.keySprint.isDown() || player.isSprinting();
   ```

## 影响范围

仅 `src/main/java/com/GoetyMech/addon/client/PrototypeRenderEvents.java`，删除了 3 行，减为 1 行。

## 关联提交

`0ac0ac8` — `[auto] edit: PrototypeRenderEvents.java | remove shouldSprint() guard, restore direct key polling for sprintDown`
