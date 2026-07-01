# 时间聚晶袋（Time Focus Pouch）架构说明

> **本物品的时停代码参考自 [EndLibrary](https://github.com/meglevel/ending_library) 和梦幻终焉。**  
> 服务器端时停引擎（`TimeStopUtils`、`TimeStopSavedData`、S2C 广播协议）、  
> 客户端 Mixin 层（`MinecraftMixin` HEAD 注入、`ClientLevelMixin` tick 取消、`Blaze3DMixin` 时间替换）、  
> 以及 `TimeContext.Client.timer` 独立时钟机制均源自 EndLibrary。  
> 本项目在此基础上实现了 GUI 冻结流程、`InputEvent.Key` 松键检测、`freezePacketDelay` 动画延迟等原创逻辑。

## 概述

时间聚晶袋是一个特殊的聚晶袋（继承 Goety 的 `FocusPack`），持有它时打开 Goety 的径向菜单/聚晶袋/聚晶包 GUI 会**触发全局时停**，让玩家在静止世界中安全切换聚晶。

| 物品 | 类 | 触发方式 |
|------|-----|---------|
| 时间聚晶袋 | `TimeFocusPouch extends FocusPack` | 打开 `FocusRadialMenuScreen`（径向菜单） |
| 时间聚晶袋 | `TimeFocusPouch extends FocusPack` | 打开 `FocusPackScreen`（聚晶袋） |
| 时间聚晶袋 | `TimeFocusPouch extends FocusPack` | 打开 `FocusBagScreen`（聚晶包） |

**核心需求**：
- 打开 GUI 时冻结所有实体/粒子/声音/时间
- 玩家仍能操作 GUI（选择聚晶）
- 关闭 GUI 时恢复一切
- 径向菜单的打开动画（2.5 ticks）要有足够时间完成才冻结

---

## 架构分层

### 1. 物品层（Item）

**`TimeFocusPouch.java`** — 最简单的类，仅继承 `FocusPack` 并重写名字。全部逻辑在事件系统中。

### 2. 客户端事件层（Forge Event Bus）

**`ModClientForgeEvents.java`** — 核心协调者，处理以下事件：

#### 2.1 打开 GUI — `onScreenOpen(ScreenEvent.Opening)`

```
玩家打开 GUI
  ├─ hasTimeFocusPouch()?    → 检查背包/主手/副手/Curios 腰带槽
  │   └─ 缓存 20 ticks，避免高频遍历
  ├─ FocusRadialMenuScreen?  → radMenuOpen=true, freezePacketDelay=3
  └─ FocusPack/BagScreen?    → anyPouchOpen=true, C2S冻结包立即发送
```

**径向菜单延迟 3 ticks 的原因**：`FocusRadialMenuScreen` 的打开动画需要 2.5 ticks（`OPEN_ANIMATION_LENGTH`），需要等动画完成后才冻结，否则 `isReady()` 一直返回 false，`processClick()` 无法触发关屏。

#### 2.2 冻结倒计时 — `onClientTick(TickEvent.ClientTickEvent.END)`

```
每个正常 tick（冻结未激活时）：
  freezePacketDelay > 0?
    ├─ freezePacketDelay-- (3→2→1→0)
    └─ ==0 → anyPouchOpen=true, C2STimeStopPacket(true) 发送到服务器
```

此后 `anyPouchOpen=true` 标记冻结已激活。

#### 2.3 松键检测 — `onKeyInput(InputEvent.Key)`

> **为什么不用 `ClientTickEvent`？** 冻结后 HEAD 注入将 `msPerTick=1.0e32`，游戏循环跑 0 次，`tick()` 不执行，`ClientTickEvent.END` 不会触发。

`InputEvent.Key` 是 GLFW 回调，在 `runTick()` 最开头的 `glfwPollEvents()` 中触发，**完全独立于游戏循环**，冻结期间也能正常工作。

```
X 键释放
  ├─ anyPouchOpen=false? (SHORT PRESS)
  │   ├─ radMenuOpen=false, freezePacketDelay=0   ← 取消冻结倒计时
  │   └─ mc.setScreen(null)                        ← 直接关屏，不触发冻结
  │
  └─ anyPouchOpen=true? (LONG PRESS / 冻结中)
      └─ TimeStopUtilsWrapped.disable()            ← 解冻客户端，让 processClick() 自然执行
```

**SHORT PRESS 关屏但不发冻结包** → `onScreenClosed` 中 `!anyPouchOpen && freezePacketDelay==0` → 直接 return，不发送任何 C2S 包。

**LONG PRESS 只做 `disable()`** → 不关屏、不发解冻包 → `screen.tick()` 在自定义循环中执行 `processClick()` → 选中聚晶 + 关屏 → `onScreenClosed` 发 C2S 解冻包 + `disable()`。

#### 2.4 关屏清理 — `onScreenClosed(ScreenEvent.Closing)`

```
屏幕关闭（无论何种方式）
  ├─ !anyPouchOpen && freezePacketDelay==0? → return (SHORT PRESS 未冻结)
  ├─ anyPouchOpen=true? → C2STimeStopPacket(false) 发送到服务器
  ├─ TimeStopUtilsWrapped.disable()                ← 客户端解冻
  └─ 重置所有状态: anyPouchOpen=false, radMenuOpen=false, freezePacketDelay=0
```

#### 2.5 杂项

- **`onScreenRenderPre`** — 在径向菜单打开时渲染半透明遮罩（`0xBB333333`）
- **`hasTimeFocusPouch()`** — 缓存 20 ticks 的背包检测，遍历背包 + 主副手 + Curios。

### 3. 客户端冻结管理

**`TimeStopUtilsWrapped.java`** — 客户端侧的冻结/解冻包装器：

```java
enable(int id):
  ├─ mc.getSoundManager().pause()
  ├─ TimeStopUtils.isTimeStop = true                ← 全局冻结标志
  ├─ ClientContext.isTimeStop_andSameDimension = true ← HEAD 注入的关键条件
  ├─ ClientLevelExpandedContext.currentTimeStopDimension = 当前维度
  └─ TimeContext.Client.timeStopGLFW = glfwGetTime()  ← Blaze3D 时间锚点

disable():
  ├─ TimeStopUtils.isTimeStop = false
  ├─ mc.getSoundManager().resume()
  ├─ ClientContext.isTimeStop_andSameDimension = false
  └─ ClientLevelExpandedContext.currentTimeStopDimension = null
```

### 4. 网络层

#### C2S（客户端→服务器）

**`C2STimeStopPacket`** — 发送时机：

| 方向 | 触发 | action |
|------|------|--------|
| C2S | `FreezePacketDelay==0` (径向菜单) | `send(true)` |
| C2S | `onScreenOpen` (FocusPack/Bag) | `send(true)` |
| C2S | `onScreenClosed` | `send(false)` |

服务器端处理调用 `TimeStopUtils.use(activate, player, force=true, duration=180, sound=false)`。

#### S2C（服务器→客户端，广播）

| 包 | 作用 |
|---|------|
| `S2CTimeStopStatePacket` | 广播冻结/解冻状态 → 调用 `TimeStopUtilsWrapped.enable/disable` |
| `S2CTimeStopDimensionPacket` | 广播冻结维度变更 → 设置 `currentTimeStopDimension` |

### 5. 服务端冻结管理

**`TimeStopUtils.java`** — 服务端侧的冻结核心：

```java
use(boolean z, LivingEntity source, boolean force, int time, boolean playSound):
  ├─ if (z && !ModConfig.TIME_STOP_ENABLED.get()) return
  ├─ if (source.level().isClientSide) return         ← 拒绝客户端调用
  ├─ isTimeStop = z                                  ← 全局标志
  ├─ TimeStopSavedData 记录/清除冻结维度
  ├─ broadcast S2CTimeStopStatePacket                ← 通知所有客户端冻结状态
  ├─ broadcast S2CTimeStopDimensionPacket            ← 通知所有客户端冻结维度
  └─ TimeStopEntityData 管理倒计时（可选）
```

**`TimeStopSavedData.java`** — 持久化冻结状态（维度集合），存于 `GoetyMech_time_stop_data`。

### 6. Mixin 层 — 实现冻结效果

#### 6.1 游戏循环控制

**`MinecraftMixin.java` — 两个注入点：**

**HEAD 注入**（`runTick` 的最开头）：

```java
if (isTimeStop AND isTimeStop_andSameDimension)
    Minecraft.timer.msPerTick = 1.0e32
```

效果：`timer.advancerCallable()` 返回 false → 游戏循环跑 0 次 → `ClientLevel.tick()`、`ClientPacketListener.tick()` 等全部跳过 → 游戏世界完全冻结。

**二号注入**（`ForgeEventFactory.onRenderTickStart` 处，使用独立 `TimeContext.Client.timer`）：

```java
long l = TimeContext.Client.timer.advanceTime(Util.getMillis());
for (int i = 0; i < l; i++) {
    // keybindings 处理（松开 X 键时的 isKeyDown 检测）
    // screen.tick() ← 这是径向菜单正常工作的关键！
    // keyboard handler
    // gameMode tick（如果玩家可以移动）
}
```

这个独立 Timer（`TimeContext.Client.timer`，默认 50ms/tick）**不受 `msPerTick=1.0e32` 影响**，所以游戏循环停转后屏幕仍然能 tick。

#### 6.2 时间冻结

**`ClientLevelMixin.java`** — `ClientLevel` 的三个方法被取消：

| 方法 | 作用 | 取消后果 |
|------|------|---------|
| `tick()` | 世界 tick | 实体、方块实体不 tick |
| `tickTime()` | 游戏时间推进 | `gameTime` 冻结 |
| `animateTick()` | 粒子效果 | 无粒子 |

**`ClientLevelExpandedContext.java`** — `tickHead()` 在 `ClientLevel.tick()` 被执行前设置 `ClientContext.isTimeStop_andSameDimension` 并取消 tick。

#### 6.3 Blaze3D 时间替换

**`Blaze3DMixin.java`** — 替换 `Blaze3D.getTime()` 返回值为冻结时刻的 GLFW 时间。影响粒子、动画等基于 `Blaze3D.getTime()` 的视觉效果。

#### 6.4 Timer 访问器

**`TimerAccessor.java`** — Mixin Accessor，提供对 `Timer.msPerTick` 私有字段的读写。

---

## 关键设计决策

### 1. 双布尔冻结检查

```java
TimeStopUtils.isTimeStop                    // 全局冻结标志（客户端+服务端共享）
ClientContext.isTimeStop_andSameDimension   // 客户端：当前维度是否在冻结维度列表中
```

HEAD 注入要求**两者都为 true** 才设置 `msPerTick=1.0e32`。这使得 `disable()` 可以独立设置 `isTimeStop_andSameDimension=false` 来绕过 HEAD 注入，即使 S2C 冻结包晚于 `disable()` 到达导致 `isTimeStop=true`。

### 2. 为什么用 `InputEvent.Key` 而不是 `ClientTickEvent`

冻结期间 `MinecraftMixin.HEAD` 注入让游戏循环跑 0 次 → `tick()` 不执行 → `ClientTickEvent.Post` 不触发。但 GLFW 回调（`glfwPollEvents`）在 `runTick()` 最开头执行，独立于游戏循环。`InputEvent.Key` 因此是冻结期间唯一可靠的键检测机制。

### 3. 为什么 `freezePacketDelay=3`

径向菜单打开动画 `OPEN_ANIMATION_LENGTH=2.5 ticks`。如果立即冻结，`isReady()` 返回 false，`processClick()` 无法触发，屏幕卡死。延迟 3 ticks 确保动画完成。

### 4. 关闭流程中 `disable()` 的时机

LONG PRESS 时在 `InputEvent.Key` 中先调 `disable()` 再等 `screen.tick()` 执行 `processClick()`。如果不先 `disable()`，`processClick()` 中的 `menu.clickItem()` 可能因为冻结状态而异常。

### 5. `onScreenClosed` 的二次清理

`InputEvent.Key`（LONG PRESS）只调 `disable()` 不调 `mc.setScreen(null)`。`processClick()` 关屏 → `onScreenClosed` 发 C2S 解冻包 + 再次 `disable()`（幂等）。SHORT PRESS 时 `onScreenClosed` 直接 return（`!anyPouchOpen && freezePacketDelay==0`）。

---

## 完整时序图

### SHORT PRESS（松键时冻结未激活）

```
时间线
│
├─ runTick N: 玩家按 X 打开径向菜单
│   ├─ onScreenOpen: radMenuOpen=true, freezePacketDelay=3
│   └─ 动画开始（OPENING）
│
├─ runTick N+1:
│   ├─ onKeyInput(InputEvent.Key): X 已松开
│   │   └─ anyPouchOpen=false → SHORT PRESS
│   │       ├─ radMenuOpen=false
│   │       ├─ freezePacketDelay=0  ← 取消冻结倒计时
│   │       └─ mc.setScreen(null)
│   ├─ onScreenClosed: !anyPouchOpen && freezePacketDelay==0 → return（不发送任何包）
│   └─ 正常游戏，冻结从未激活 ✓
```

### LONG PRESS（按住 X 直到冻结激活后松开）

```
时间线
│
├─ runTick N: 按 X 打开径向菜单
│   ├─ onScreenOpen: radMenuOpen=true, freezePacketDelay=3
│   └─ 动画开始（OPENING）
│
├─ runTick N+1: onClientTick → freezePacketDelay=2
├─ runTick N+2: onClientTick → freezePacketDelay=1
│
├─ runTick N+3: freezePacketDelay→0
│   ├─ onClientTick: anyPouchOpen=true, C2S冻结包→服务器
│   │   ├─ 服务端: isTimeStop=true, 广播 S2C 冻结
│   │   └─ 客户端: enable(0) → isTimeStop=true, isTimeStop_andSameDimension=true
│   └─ HEAD 注入生效 → 此后游戏循环跑 0 次
│
├─ runTick N+4..M: 玩家还按着 X，游戏冻结，自定义循环中 screen.tick() 继续执行
│
├─ runTick M: 玩家松开 X
│   ├─ onKeyInput(InputEvent.Key): anyPouchOpen=true → LONG PRESS
│   │   └─ disable() → isTimeStop=false, isTimeStop_andSameDimension=false
│   ├─ 自定义循环: screen.tick() → processClick()
│   │   ├─ menu.clickItem() ← 选中聚晶！
│   │   ├─ mc.setScreen(null)
│   │   └─ onScreenClosed: C2S解冻包 + disable()（幂等）
│   ├─ 服务端 tick: 处理 C2S解冻包 → isTimeStop=false → 广播 S2C解冻
│   └─ 正常游戏恢复 ✓
```

---

## 涉及的源文件清单

| 路径 | 作用 |
|------|------|
| `common/item/TimeFocusPouch.java` | 物品定义（继承 FocusPack） |
| `client/ModClientForgeEvents.java` | 客户端事件协调器 |
| `client/TimeStopUtilsWrapped.java` | 客户端冻结/解冻 |
| `client/ClientContext.java` | `isTimeStop_andSameDimension` |
| `client/ClientLevelExpandedContext.java` | 当前冻结维度管理 |
| `common/time/TimeStopUtils.java` | 服务端冻结/解冻核心 |
| `common/time/TimeContext.java` | 独立 Timer + GLFW 时间锚点 |
| `common/time/LevelExpandedContext.java` | 服务端 tick 取消 |
| `common/data/TimeStopSavedData.java` | 冻结维度持久化 |
| `common/net/c2s/C2STimeStopPacket.java` | C2S 冻结包 |
| `common/net/s2c/S2CTimeStopStatePacket.java` | S2C 状态同步 |
| `common/net/s2c/S2CTimeStopDimensionPacket.java` | S2C 维度同步 |
| `config/ModConfig.java` | 时停开关、倒计时等配置 |
| `mixin/MinecraftMixin.java` | 游戏循环控制 + 自定义循环 |
| `mixin/ClientLevelMixin.java` | 客户端世界 tick 取消 |
| `mixin/Blaze3DMixin.java` | Blaze3D 时间替换 |
| `mixin/TimerAccessor.java` | Timer.msPerTick 访问器 |
| `mixin/ServerLevelMixin.java` | 服务端 tick 取消 |
