---
name: "GoetyMech-architecture"
description: "package / convention / register"
---

<!-- 本技能无详细实现文件，所有内容均在 SKILL.md 中 -->

# GoetyMech 项目架构规范

> ⚠ 本技能最后更新于 2026-06。部分包结构和注册模式可能与当前代码存在偏差。
> 前置模组源码路径见 `.opencode/skills/_shared/libcode-paths.md`。
> 快速排除：当前任务如果不涉及新建类、决定包路径、理解注册模式或评估架构设计，请跳过本 skill。

## 适用场景

- 添加新物品/方块/实体/功能 — 确定代码放哪个包
- 新建 Java 类 — 确定类名、包路径、继承关系
- 评估设计决策是否符合项目规范
- 理解当前代码布局

---

## 一、项目标识

| 项 | 值 |
|----|-----|
| **模组 ID** | `GoetyMech` |
| **主包** | `com.GoetyMech.addon` |
| **主入口** | `com.GoetyMech.addon.GoetyMechMod` |
| **MC版本** | 1.20.1 / Forge 47.4.0 / Java 17 |
| **映射** | Parchment 2023.09.03 |
| **前置** | Curios 5.3.4, Goety 2.x |

---

## 二、包结构（当前项目实际布局）

```
com.GoetyMech.addon/
├── GoetyMechMod.java              # 主入口
├── config/
│   └── ModConfig.java             # ForgeConfigSpec
├── init/                          # 注册层
│   ├── ModBlocks.java             # 方块 + 方块实体
│   ├── ModItems.java              # 物品 + Focus
│   ├── ModEntities.java           # 实体类型
│   ├── ModCreativeTab.java        # 创造标签页
│   └── ModTags.java               # 自定义标签
├── common/                        # 逻辑层
│   ├── item/
│   │   ├── SculkBindFocus.java
│   │   ├── TwistedEmbryoItem.java
│   │   ├── MatureEmbryoItem.java
│   │   └── SquadFocus.java
│   ├── block/
│   │   ├── FleshFurnaceBlock.java
│   │   ├── SquadBarracksBlock.java
│   │   ├── Tier1SquadBarracksBlock.java
│   │   ├── Tier2SquadBarracksBlock.java
│   │   ├── Tier3SquadBarracksBlock.java
│   │   └── entity/
│   │       ├── FleshFurnaceBlockEntity.java
│   │       └── SquadBarracksBlockEntity.java
│   ├── entity/
│   │   └── SculkSpikeEntity.java
│   ├── magic/spell/
│   │   ├── SculkBindSpell.java
│   │   └── SquadSpell.java
│   ├── events/
│   │   ├── SculkBindEvents.java
│   │   └── SquadFocusEvents.java
│   ├── menu/                       # 容器 GUI 逻辑
│   │   ├── ModMenuTypes.java
│   │   └── SquadBarracksMenu.java
│   ├── net/                        # 网络包
│   │   ├── ModNetwork.java
│   │   └── SquadConfigC2SPacket.java
│   └── utils/
│       ├── MinionSpawnHelper.java
│       └── SquadChunkTickets.java
└── client/                        # 客户端专用
    ├── ModClientEvents.java
    ├── FleshFurnaceHUD.java
    ├── gui/
    │   └── SquadBarracksScreen.java
    └── render/
        ├── SculkBidingModel.java
        └── SculkBidingRenderer.java
```

---

## 三、分层约定

| 包 | 职责 | 禁止 |
|----|------|------|
| `init/` | DeferredRegister + RegistryObject | 业务逻辑、匿名内部类 |
| `common/item/` | 物品交互逻辑（use/useOn） | 注册逻辑 |
| `common/block/` | 方块行为 + entity/ 方块实体 tick | 客户端代码 |
| `common/entity/` | 实体 AI、动画状态机 | 渲染代码 |
| `common/magic/` | Spell → spell/ 子包 | 物品注册 |
| `common/events/` | `@Mod.EventBusSubscriber` | 注册逻辑 |
| `common/menu/` | AbstractContainerMenu 实现 | 注册逻辑、渲染 |
| `common/net/` | 网络包 encode/decode/handle | 业务逻辑 |
| `common/utils/` | 纯静态工具方法 | 实例状态 |
| `client/` | 渲染/HUD/GUI | 服务端逻辑 |
| `client/gui/` | Screen 实现 | 服务端逻辑 |
| `client/render/` | 实体模型 + 渲染器 | 服务端逻辑 |

**新功能添加流程：**
1. 需要新注册项 → `init/` 下新建注册类或扩展现有类
2. 自定义行为 → `common/` 对应子包下创建类
3. 公共静态逻辑（3行+）→ `common/utils/` 提取
4. 客户端渲染/GUI → `client/` 下创建
5. 可配置数值 → `config/ModConfig.java` 新增配置项

---

## 四、项目特有设计决策

### 4.1 血肉熔炉（Flesh Furnace）输出策略
- 扫描 8 格半径内的不祥火炬（Ominous Pyre）来确定兵种
- 优先输出到下方容器的逻辑（漏斗/管道）
- 通过 `ModTags.REQUIRES_TRAINING_STRUCTURE` 标签过滤高级兵种
- 客户端 HUD 进度条同步

### 4.2 扭曲胚胎 → 成熟胚胎 转化链路
```
TwistedEmbryoItem (右键生成随机 Neollager)
         ↓
   FleshFurnace (烹制)
         ↓
MatureEmbryoItem (NBT 存实体类型, 右键生成指定兵种)
```

### 4.3 幽匿束缚法术（SculkBindSpell）流程

```
Phase 1: 锁定目标 → 强制原地（PersistentData 存坐标）
Phase 2: 召唤 SculkSpike（视觉实体）+ 声波粒子 + 尖啸音效
Phase 3: 召唤原版 Warden，通过 Brain 记忆操作控制出土动画、禁遁地、注入愤怒值
```

**架构决策**：使用原版 Warden + Brain 操作，而非自定义 Warden 子类。详情见 `goety-addon-dev` 技能第七部分。

### 4.4 SculkBind 事件处理

- **定身**：`SculkBindEvents.handleSnare()` — 每 tick 从 PersistentData 读取锁定坐标，强制 `setPos` 归位
- **Warden 可见性**：`SculkBindEvents.handleWardenVisibility()` — 延迟 15 tick 恢复可见，防止出土动画首帧闪现

---

### 4.5 点将台（Squad Barracks）系统

#### 4.5.1 核心架构

```
SquadBarracksBlock (abstract, BaseEntityBlock)
├── Tier1SquadBarracksBlock (6 config slots)
├── Tier2SquadBarracksBlock (12 config slots, 50% soul discount)
└── Tier3SquadBarracksBlock (18 config slots, 90% soul discount)
         ↓ owns
SquadBarracksBlockEntity — 配置槽(virtual) + 储备槽(ItemStack)
         ↓ opens
SquadBarracksMenu (AbstractContainerMenu, virtual config slots)
         ↓ renders
SquadBarracksScreen (AbstractContainerScreen, tiers use different textures)
```

#### 4.5.2 兵团聚晶编队召唤流程

```
SquadFocus (右键绑定点将台 → NBT 存 bound pos/dim/tier/count)
         ↓
SquadSpell.SpellResult() → 验证绑定 + 维度 + 所有权 + 灵魂
         ↓
barracks.consumeSquad() → 消费储备槽成熟胚胎
         ↓
MinionSpawnHelper.spawnTimedMinion() → 有寿命仆从
         ↓
SquadFocus.TAG_SUMMONER → 用于 dismiss-all 召回
```

**架构决策**：
- 配置槽是虚拟槽（仅存 `ResourceLocation`），不可交互；储备槽是真实 `ItemStack` 槽位
- C2S 网络包 (`SquadConfigC2SPacket`) 实时同步 GUI 操作到服务端
- `SquadChunkTickets` 保证跨维度点将台区块保持加载（用于远程召唤）
- 三阶折扣通过 `ModConfig.SQUAD_TIER2_DISCOUNT` 和 `SQUAD_TIER3_DISCOUNT` 配置
- 按 Shift + 右键施法触发 dismiss-all（`SquadSpell.isShifting` 分支）

---

### 4.6 GUI 渲染关键经验 (SquadBarracks)

适用于点将台 GUI 或其他 `AbstractContainerScreen` 子类。

#### `blit()` 必须传纹理实际尺寸

分段截取时使用完整重载：
```java
gui.blit(TEXTURE, x, y, destWidth, destHeight, uOffset, vOffset, uvWidth, uvHeight, texWidth, texHeight);
```

#### `imageHeight` 变更后更新 `inventoryLabelY`

```java
this.imageWidth = 194;
this.imageHeight = 168;
this.inventoryLabelY = this.imageHeight - 94;  // ← 容易遗漏
```

#### 用 `leftPos`/`topPos` 而非重复计算

`AbstractContainerScreen.init()` 已在 `super.init()` 算好，直接引用。

#### `render()` 必须调用 `renderBackground()`

```java
@Override
public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
    this.renderBackground(gui);
    super.render(gui, mouseX, mouseY, partialTick);
    this.renderTooltip(gui, mouseX, mouseY);
}
```

SquadBarracks 曾因未传纹理尺寸 + inventoryLabelY 未同步 → UV 映射错误、背包标签偏移 2px。

---

### 4.7 Prototype 步高系统 — Mixin 覆写 `maxUpStep()`

#### 问题

原方案在 `PrototypeEvents.java` 中每 tick 调用 `player.setMaxUpStep(1.5f)`（冲刺时）和 `player.setMaxUpStep(0.6f)`（非冲刺时）。这导致步高在 1.5 和 0.6 之间频繁翻转，引起速度异常。

#### 根因

`Entity.maxUpStep()` 有两个调用链，且 `Player` 有特殊绕过：

| 代码路径 | 调用方式 | 说明 |
|---------|---------|------|
| `Entity.collide()` (Forge) | → `getStepHeight()` → `maxUpStep()` + `STEP_HEIGHT_ADDITION` | Forge 推荐路径 |
| `Player.maybeBackOffFromEdge()` | **直接调用 `this.maxUpStep()`** | 明确注释不使用 `getStepHeight()` (forge#8922) |
| `Player.isAboveGround()` | **直接调用 `this.maxUpStep()`** | 同上 (forge#9376) |

仅用 `setMaxUpStep()` 或操作 `STEP_HEIGHT_ADDITION` 属性无法覆盖 `maybeBackOffFromEdge` 和 `isAboveGround`。

#### 解决方案：Mixin 注入 `Entity.maxUpStep()`

**新建 Mixin** `PrototypeStepHeightMixin.java`，`@Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)`，当 `PrototypeItem.hasAnyPrototype(player)` 时返回 `1.5f`。

```java
@Mixin(Entity.class)
public abstract class PrototypeStepHeightMixin {
    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void gp$overrideMaxUpStep(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player player && PrototypeItem.hasAnyPrototype(player)) {
            cir.setReturnValue(1.5f);
        }
    }
}
```

#### 效果

- **两个调用链都覆盖**：Player 的直接调用和 Forge 的 `getStepHeight()` 均经过 `maxUpStep()`
- **无需 per-tick 写入**：移除 `PrototypeEvents.java` 中两个 `setMaxUpStep()` 调用
- **步高恒定 1.5**：不随 sprint 状态翻转，消除速度异常
- **即时生效**：穿上/脱下 prototype 时 Mixin 实时判断 `hasAnyPrototype`，无延迟

#### 注意事项

- Mixin 注册在 `mixins` 数组（服务端+客户端），因为 `maxUpStep()` 在两侧都使用
- `PrototypeItem.hasAnyPrototype()` 检查胸甲格 + Curios 槽，纳入热路径性能可接受
- 若其他模组同时注入 `maxUpStep()`，按 Mixin 优先级执行；`cancellable=true` 保证不被覆盖

---

## 五、禁止事项

| 禁止 | 正确做法 |
|------|----------|
| 主类中写业务逻辑 | 放入 `common/` 对应包 |
| 注册类中写匿名内部类业务 | 创建独立业务类，注册类仅引用 |
| 工具方法放在业务包根目录 | 放入 `common/utils/` |
| 手动绑仆从主人 | 必须用 `MobUtil.summonTame()` |
| Goety 已有接口/工具时自行重写 | 优先复用 Goety |
| 修改 build.gradle 依赖配置 | 保持现有配置 |
| 创建空预留事件类 | 需要时再创建 |