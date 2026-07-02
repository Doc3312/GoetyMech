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
