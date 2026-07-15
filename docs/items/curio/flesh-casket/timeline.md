# Flesh Casket Timeline

## curio/flesh-casket.md

﻿# 血肉魔匣 — GoetyMech

## 身份

- 类型：curio
- 注册类：ModItems.FLESH_CASKET_1 / 2 / 3
- Java 类：FleshCasketItem (extends Item, implements ICurioItem)
- 首次创建：2026-06-23

## 设计意图

Curios 饰品槽位的自动施法容器。内置 1/3/5 格（按阶位）存储 Focus，周期自动施法。三阶循环升级：A→B→C→A，配方在 `data/goety/recipes/` 下。

## 演化记录

### 2026-06-23 — 初始设计与注册

- 改动：创建 FleshCasketItem + FleshCasketItemCapability + FleshCasketItemHandler + FleshCasketContainer + FleshCasketEvents + AutoCastDummyWand + TargetingRegistry + TriggerType + ModItems 注册 + 三阶模型 + 三阶循环升级配方 + ModConfig
- 原因：需要一个可以自动施法的 Curios 容器饰品，解放手持槽位
- 关联：14 new files, 4 modified (commit 0ec1b5d)

### 2026-06-23 — 修复 TargetingRegistry 无效字段引用

- 改动：修复 3 个无效的字段引用
- 原因：初始代码中引用还未注册的字段
- 关联：commit 6824236

### 2026-06-23 — GUI 系统 + 装备交互重做

- 改动：添加 FleshCasketContainer/Screen、ModMenuTypes、ModClientEvents；`use()` 打开 GUI；`canEquipFromUse` 从 `true` 改为 `isCrouching()`
- 原因：右击打开 GUI（不潜行）/ 潜行+右击装备到 Curios 栏，匹配 Goety FocusBag 交互模式
- 关联：GUI 纹理复制自 Goety focus_bag.png

### 2026-06-23 — Curios back tag 修复 + 中文本地化

- 改动：`back.json` 补全 flesh_casket_a/b/c 条目（之前只写了 prototype）；重写 zh_cn.json（UTF-8 修复，补全条目）；en_us.json 添加 container title
- 原因：back tag 缺失导致匣子无法装备到背部 Curios 槽；zh_cn.json 编码损坏
- 更新中文名：反击→盈蓄、命理守护→平衡、绝望→濒死

### 2026-06-23 — 自动施法修复：Tier 1 EMPTY 魔杖问题

- 改动：所有 tier 统一使用 `AutoCastDummyWand`，不再为 tier 1 传 `ItemStack.EMPTY`
- 原因：`rightStaff()` 要求 staff 实现 IWand 且含法术类型；EMPTY 不符合，所有法术直接中断不执行
- 关联：commit aade8a1

### 2026-06-23 — 冷却系统重构 + 自由移除

- 改动：per-focus 独立冷却 → 单物品全局冷却（以最长法术冷却为准）；添加 damage bar 可视化；`canUnequip` 始终返回 true
- 原因：玩家反馈无法直观看到冷却、取不下饰品；只有全部聚晶冷却完才能再次触发
- 关联：commit 0548c30, f48d756

### 2026-06-29 — 重构为 3 阶线性升级 + NBT 触发类型切换

- 改动：9 个变体（A1~C3）→ 3 个阶位物品（flesh_casket_1/2/3）；触发类型从 enum 类重构为 NBT 字段；添加诅咒注入器循环配方（A→B→C→A，`FleshCasketInfuserRecipe`）；创建自定义仪式 `UpgradeCasketRitual` 复制 NBT；删除 12 个旧模型 JSON + 9 个旧配方 + 2 个旧物品引用
- 原因：9 个变体太冗余，线性升级 + NBT 切换更简洁；NBT 保留保证升级不掉聚晶
- 关联：FleshCasketInfuserRecipe.java, FleshCasketInfuserRecipeSerializer.java, UpgradeCasketRitual.java, ModRituals.java, ModItems 去 9→3；新增 6 个配方 JSON；commit b10c2c8

### 2026-06-29 — 仪式配方修复：Goety 物品 ID 纠正 + 占位纹理

- 改动：修正 3 个 Upgrade 配方 JSON 中 6 个错误 Goety 物品 ID（pale_metal_ingot→pale_steel_ingot, dark_metal_ingot→dark_ingot, night_core→heart_of_the_night, unholy_cloth→unholy_fabric, large_focus_bag→focus_pack, pure_blood→unholy_blood）；创建 3 个占位 PNG 纹理（flesh_casket_1/2/3.png）
- 原因：配方中 item ID 不存在导致 JEI 不显示；缺失纹理导致物品栏显示紫黑块
- 关联：commit [current session]

### 2026-07-02 — 触发类型视觉变体 + 物品升级保留内容修复

- 改动：
  1. 新增 9 个触发类型子模型 JSON + 9 张贴图（flesh_casket_{1/2/3}_{fullness/balance/deathbed}.png），根据 NBT trigger_type 切换物品外观
  2. FleshCasketItem 覆盖 `getName()` 返回 `item.GoetyMech.flesh_casket_{tier}.{suffix}` 翻译键，物品名随触发类型变化
  3. 添加 ItemProperty 在 client 侧根据 trigger_type 选择子模型
  4. FleshCasketInfuserRecipe.assemble()：用 ITEM_HANDLER capability 逐槽复制聚晶栈，替代仅复制 NBT tag 的旧逻辑，避免升级时清空内容
  5. 覆写 getResultItem() 返回已循环触发类型的 output，让 JEI 显示正确的升级产物图标
- 原因：触发类型仅有 NBT 无视觉反馈；升级仪式会清空已存放的聚晶；JEI 显示未循环的产物（始终显示 A 类型）
- 关联：commit 3a79aa6, 6b4b32d, d3064d1, 86a72e3

### 2026-06-23 — 三个 Bug 修复

- 改动：
  1. AutoCastDummyWand 排除 FROST/STORM/WILD/NETHER/ABYSS → 召唤不再强制变冰系，回退群系判定
  2. `tryAutoCast` 中 tier >= 2 加 +2 potency，tier >= 3 加 +4 potency + +1 duration → 高级匣子不再全出低级怪
  3. `findFirstCurio` → `findAllFleshCaskets` 遍历全部 Curios 槽 → 多个匣子同时生效
- 原因：玩家反馈三个问题
- 关联：commit 7a632bf

### 2026-07-02 — JEI 三变换显示 + 重复条目修复

- 改动：JEI 诅咒注入器分类中注册 6 条显示配方（3 tier × 2 变换：B→C, C→A），配合 Goety 自动注册的 A→B 条目形成完整循环。移除冗余的 A→B 显示配方。
- 原因：玩家反馈只显示满盈→平衡（A→B）一项，缺少平衡→濒死（B→C）和濒死→满盈（C→A）
- 关联：commit 14dfb1a, 86a72e3

### 2026-07-02 — Tier 分级真法杖 + Offhand fix + Z 键 GUI

- 改动：
  1. AutoCastDummyWand 重构：无参构造全类型（VOID, NONE, NECROMANCY, ILL, GEOMANCY, WIND），新增有参构造。注册 AUTO_CAST_DUMMY_WAND_T1（NONE-only）。
  2. `tryAutoCast()` 按 tier 选法杖：tier 1 用 T1（NONE-only），tier 2/3 用全类型法杖。
  3. Offhand fix：将聚晶装入真法杖 handler，临时放副手，SpellResult 后恢复。解决 `WandUtil.findFocus()` 找不到聚晶导致 SquadFocus/StressFocus 等 silent return 的问题。
  4. Z 键绑定（`key.GoetyMech.open_flesh_casket`）+ C2SOpenFleshCasketPacket：手持匣子按 Z 打开 GUI。
- 原因：低阶匣子不应召唤过多暗卫（rightStaff 奖励）；SquadFocus/StressFocus 自动施法被 instanceof 守卫阻断；手持无法快速开 GUI。
- 关联：commit 56b1c30, 97a5ebe, a1d0b11

# 晶匣系列 — GoetyMech

## 身份

- 类型：curio
- 注册类：`ModItems.FLESH_CASKET_1/2/3`
- Java 类：`FleshCasketItem`（三阶共用，tier 字段区分）
- 核心事件：`FleshCasketEvents`
- 首次创建：项目早期

## 设计意图

装备在 Curios 槽位的自动施法容器。玩家受伤时自动施放晶匣内存储的聚晶（IFocus）中的法术。三阶差异：插槽数、巫妖限制、威力/持续时间加成。

## 演化记录

### 初始版本
- 三阶：tier 1 (1槽)、tier 2 (3槽)、tier 3 (5槽)
- 触发模式 A（受伤即发）、B（半血）、C（濒死阈值）
- 冷却使用 damage bar（耐久条）伪装
- 手动 tick 递减冷却

### 2026-07-03 — Grill: 修复 + 冷却重做
- **问题**：tier 2/3 的 lich check 翻译键多下划线（`goety_mech`→`goetymech`），但实际代码正确
- **根因**：升级仪式 `UpgradeCasketRitual` 复制了整个 NBT，包括 `flesh_casket_cooldown`，导致新晶匣继承旧冷却 → `isOnCooldown` 阻止施法
- **修复**：升级时清除冷却 NBT；冷却显示从 damage bar 改为 vanilla `ItemCooldowns`（灰色覆盖层）；移除 `tickCooldowns` 系统
- **设计变更**：`tryAutoCast` 移除第一个聚晶施放后的 `return`，改为**一次触发施放所有有效聚晶**
- **配置**：新增 `flesh_casket.cooldown_override`（默认 60 ticks / 3s），覆盖法术原始冷却

### 2026-07-03 — Tier 差异化：耗魂 + 冷却倍率
- tier 2：灵魂消耗 ×0.5、冷却 ×0.8
- tier 3：灵魂消耗 ×0.1、冷却 ×0.4

### 2026-07-03 — 通用自动施法：调用全部4个生命周期方法 + 定时器
- 对每个 focus 在一个 tick 内依次调 `startSpell/useSpell/SpellResult/stopSpell`（覆写哪个就执行哪个）
- 壁垒（`SpellResult`）→ 直接加盾
- 星辰（`useSpell` + castTime=60）→ 一次性射出一轮 VoidShock
- 撕裂（`startSpell`）→ 创建 VoidRift 裂隙实体
- `IChargingSpell`（腐化光束）→ 注册 `ChannelTask` 定时器，`startUsingItem` 保持 3s 引导，每 tick 调 `useSpell` + 按 `Cooldown()` 间隔调 `SpellResult`
- 玩家已有活跃通道时新触发跳过，已注册通道的 focus 中断后续循环
- `AutoCastDummyWand`：新增 `getUseDuration()` 返回 72000 以支持 `startUsingItem`

### 2026-07-03 — 移除副手占用 + 修复壁垒不工作
- 根因：`AutoCastDummyWand` 缺少 `ForgeCapabilities.ITEM_HANDLER` → `SoulUsingItemHandler.get()` 抛异常被吞
- `AutoCastDummyWand`：新增 `initCapabilities()` 返回 `SoulUsingItemHandler`
- `FleshCasketEvents`：完全移除所有副手替换代码（`oldOffhand`、set/restore、try-finally）
- `ChannelTask`：移除 `startUsingItem`/`stopUsingItem`；以 `staffStack` 内存引用调用 `SpellResult`
- `ChannelTask.tick()`：`SpellResult` 后扫描 `AbstractBeam` 实体，`setItemBase(false)` 使其不依赖 `isUsingItem()`
- `ChannelTask.stop()`：清理所有托管的光束实体

### 2026-07-03 — PhantomWandManager + Mixin 替代副手，保留附魔加成
- 新建 `PhantomWandManager`：`ConcurrentHashMap<UUID, ItemStack>`，用于在服务端注入虚拟 wand
- 新建 `WandUtilMixin`：在 `WandUtil.findWand()` RETURN 时注入，检查 PhantomWandManager
- 完全移除所有 `player.getInventory().offhand` 操作
- `tryAutoCast` 用 `PhantomWandManager.set/clear` 管理 wand 注入，try-finally 保护清除
- 通道期间 phantom wand 持续存在，`enchantedFocus()` 在通道 tick 中正常返回
- 通道结束后 `ChannelTask.stop()` 清除 phantom wand

### 2026-07-04 — tick() 崩溃修复 + 清除实体打补丁代码 + 日志清理
- `PhantomWandManager.tick()` 修复：`removeIf` 内 `entry.setValue()` 对 `SimpleImmutableEntry` 抛 `UnsupportedOperationException`，改为 snapshot 迭代 + `PENDING_CLEAR.put()`
- 移除 `CastSpellTrapMixin` 和 `tryAutoCast` 中的调试日志
- 移除 `FleshCasketEvents` 中旧的实体打补丁代码（`CastSpellTrap.setActivated(true)` + `broadcastEntityEvent`）及其 import
- 日志确认 Mixin 在崩溃前正常工作了 4 tick，证明 Mixin 方案有效

- **2026-07-16** — 配方 craftType 改为 mechanical + Patchouli 条目创建
  - `flesh_casket_1.json`：craftType forge → mechanical（机械仪式）
  - Patchouli 条目（goetymech_focuses sortnum 11，3页：lore + 三阶触发条件 + 仪式配方）
  - extra_recipe_mappings：flesh_casket_2/3 → 第2页
  - 关联：20260716.patchouli-percent-format-error（20%% 正确转义）

