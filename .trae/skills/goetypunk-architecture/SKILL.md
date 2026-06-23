---
name: "goetypunk-architecture"
description: "GoetyPunk 项目特有架构规范。定义本项目的包结构、分层边界、注册约定和设计决策。当添加新功能、新建类、调整项目结构时调用本 skill。通用 Forge/Goety 开发知识在 goety-addon-dev 中。"
---

# GoetyPunk 项目架构规范

## 适用场景

- 添加新物品/方块/实体/功能 — 确定代码放哪个包
- 新建 Java 类 — 确定类名、包路径、继承关系
- 评估设计决策是否符合项目规范
- 理解当前代码布局

---

## 一、项目标识

| 项 | 值 |
|----|-----|
| **模组 ID** | `goetypunk` |
| **主包** | `com.goetypunk.addon` |
| **主入口** | `com.goetypunk.addon.GoetyPunkMod` |
| **MC版本** | 1.20.1 / Forge 47.4.0 / Java 17 |
| **映射** | Parchment 2023.09.03 |
| **前置** | Curios 5.3.4, Goety 2.x |

---

## 二、包结构（当前项目实际布局）

```
com.goetypunk.addon/
├── GoetyPunkMod.java              # 主入口
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
│   │   └── MatureEmbryoItem.java
│   ├── block/
│   │   ├── FleshFurnaceBlock.java
│   │   └── entity/FleshFurnaceBlockEntity.java
│   ├── entity/
│   │   └── SculkSpikeEntity.java
│   ├── magic/spell/
│   │   └── SculkBindSpell.java
│   ├── events/
│   │   └── SculkBindEvents.java
│   └── utils/
│       └── MinionSpawnHelper.java
└── client/                        # 客户端专用
    ├── ModClientEvents.java
    ├── FleshFurnaceHUD.java
    └── entity/SculkSpikeRenderer.java
```

---

## 三、分层约定

| 包 | 职责 | 禁止 |
|----|------|------|
| `init/` | DeferredRegister + RegistryObject | 业务逻辑、匿名内部类 |
| `common/item/` | 物品交互逻辑（use/useOn） | 注册逻辑 |
| `common/block/` | 方块行为 + entity/ 方块实体 tick | 客户端代码 |
| `common/entity/` | 实体 AI、动画状态机 | 渲染代码 |
| `common/magic/` | Spell → spelle/ 子包 | 物品注册 |
| `common/events/` | `@Mod.EventBusSubscriber` | 注册逻辑 |
| `common/utils/` | 纯静态工具方法 | 实例状态 |
| `client/` | 渲染/HUD/GUI | 服务端逻辑 |

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