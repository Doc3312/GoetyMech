# 血肉魔匣 — goetypunk

## 身份

- 类型：curio
- 注册类：ModItems.FLESH_CASKET_A / B / C
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

### 2026-06-23 — 三个 Bug 修复

- 改动：
  1. AutoCastDummyWand 排除 FROST/STORM/WILD/NETHER/ABYSS → 召唤不再强制变冰系，回退群系判定
  2. `tryAutoCast` 中 tier >= 2 加 +2 potency，tier >= 3 加 +4 potency + +1 duration → 高级匣子不再全出低级怪
  3. `findFirstCurio` → `findAllFleshCaskets` 遍历全部 Curios 槽 → 多个匣子同时生效
- 原因：玩家反馈三个问题
- 关联：commit 7a632bf
