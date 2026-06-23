---
name: "goety-addon-dev"
description: "Goety Focus / Spell / servant / soul"
---

# Goety Addon Dev — 综合开发指南

> 前置模组源码路径见 `.opencode/skills/_shared/libcode-paths.md`。
> 快速排除：当前任务如果不涉及 Goety 物品/方块注册、Focus/Spell 开发、仆从实体或灵魂系统，请跳过本 skill。

## 适用场景

- 开发 Goety 1.20.1 Forge 附属模组
- 注册物品/方块/实体/法术/聚晶
- 实现自定义 Spell 法术逻辑或 Focus 聚晶
- 创建召唤仆从或投射物实体
- 接入 Goety 灵魂能量、Capability 等系统
- 所有涉及代码编写、注册、构建、调试的操作

---

## 项目架构规范

### 最终项目结构

```
com.yourmod.addon/
├── MainMod.java                       # 主入口（仅注册总线 + 配置 + 网络）
├── config/ModConfig.java              # Forge 配置
├── init/                              # 注册层
│   ├── ModBlocks.java                 # 方块 + 方块实体
│   ├── ModItems.java                  # 物品（含 Focus）
│   ├── ModEntities.java               # 实体类型
│   └── ModCreativeTab.java           # 创造标签页
├── common/                            # 通用逻辑层
│   ├── item/                          # 自定义物品
│   ├── block/                         # 自定义方块 + entity/
│   ├── entity/                        # 自定义实体
│   ├── magic/spell/                   # 法术实现
│   ├── events/                        # Forge 事件订阅
│   └── utils/                         # 静态工具类
├── client/                            # 客户端专用
│   ├── entity/                        # 实体渲染器
│   └── gui/                           # HUD/GUI
└── network/                           # 自定义网络包
```

### 分层原则

| 层 | 职责 | 禁止 |
|----|------|------|
| `init/` | DeferredRegister + RegistryObject | 业务逻辑、匿名内部类 |
| `common/` | 业务逻辑实现 | 客户端专用代码 |
| `client/` | HUD/渲染/粒子/按键 | 服务端逻辑 |
| `config/` | ForgeConfigSpec 配置项 | 运行时逻辑 |
| `network/` | 自定义网络包 | 业务逻辑 |

### YAGNI

- 禁止预留空方法、空类、空事件监听器
- 只在真正需要时才添加代码

---

## 强制规则

### 注册顺序

```java
ModBlocks.BLOCKS.register(modEventBus);         // 1. 方块
ModBlocks.BLOCK_ENTITIES.register(modEventBus);  // 2. 方块实体
ModItems.ITEMS.register(modEventBus);            // 3. 物品
TABS.register(modEventBus);                     // 4. 创造标签页
```

### 仆从驯服 — 必须用 MobUtil.summonTame()

```java
Entity entity = type.create(serverLevel);
if (entity instanceof Mob mob) {
    MobUtil.summonTame(mob, player);
    mob.finalizeSpawn(serverLevel, ...);
}
```

禁止手动 setTrueOwner 不调 summonTame。

### 复用 Goety 既有实现（优先级）

1. **继承** Goety 类（`Spell`、`Summoned`、`RitualType`）
2. **调用** Goety 工具方法（`MobUtil.summonTame()`、`SoulUtil`）
3. **实现** Goety 接口（`ISpell`、`IFocus`、`IServant`）
4. 以上不行再自创

### Mixin refmap 配置

```groovy
minecraft.runs.configureEach {
    property 'mixin.env.disableRefMap', 'true'   // ✅ 必须
}
```

### 禁止修改依赖配置

除非用户明确要求，禁止修改 build.gradle、settings.gradle、gradle.properties。

### 构建失败策略

构建失败超过 3 次时停止重试，整理原因汇报，等用户指示后再继续。

---

## 文档索引

| 文件 | 内容 |
|------|------|
| [forge-basics.md](forge-basics.md) | Forge 1.20.x 开发基础（主类/注册/事件/BE/网络/配置） |
| [focus-spell.md](focus-spell.md) | Goety 核心 API + Focus/Spell/Summoned/Projectile 全链路开发 |
| [warden-practice.md](warden-practice.md) | Warden 实战经验（Brain/愤怒/出土/音效） |
| [utility-reference.md](utility-reference.md) | 工具方法速查表 + Goety API 接口速查 |
