# 血肉晶匣 仪式升级系统 — 规范文档

## 概述

将 9 个血肉晶匣变种（A1/A2/A3, B1/B2/B3, C1/C2/C3）合并为 3 个线性升级物品：

| 物品 | 旧对应 | 插槽 | 触发器类型机制 |
|------|--------|------|---------------|
| `flesh_casket_1` | A1/B1/C1 | 1 | 可切换 A/B/C |
| `flesh_casket_2` | A2/B2/C2 | 3 | 可切换 A/B/C |
| `flesh_casket_3` | A3/B3/C3 | 5 | 可切换 A/B/C |

触发器类型 A（满盈）/ B（平衡）/ C（濒死）作为物品 NBT 属性存储，通过诅咒注入器配方无消耗循环切换（A→B→C→A），保留内部物品。

## 仪式升级路径

线性升级，每次升级保留内部所有聚晶/NBT：

```
[原材料] ──► 血肉晶匣 I ──► 血肉晶匣 II ──► 血肉晶匣 III
```

## 三级仪式明细

### I 级：材料 → 血肉晶匣 I

| 项目 | 值 |
|------|-----|
| 仪式类型 | `goety:craft` |
| craftType | `forge` |
| 中心祭坛 | 黑暗魔杖（`goety:dark_wand`）→ 消耗 |
| 材料 | 苍白钢锭（`goety:pale_steel_ingot`）×2, 黑暗合金锭（`goety:dark_ingot`）×2, 灵质（`goety:soul_powder`）×1, 任意生肉 ×1（`#forge:raw_meat`） |
| 启动条件 | 献祭村民（`#goety:villagers`） |
| 灵魂消耗 | 111/秒（JSON `soulCost: 111`） |
| 持续时间 | 6 秒（JSON `duration: 6`） |
| 产出 | `GoetyMech:flesh_casket_1` |

### II 级：血肉晶匣 I → 血肉晶匣 II

| 项目 | 值 |
|------|-----|
| 仪式类型 | `GoetyMech:upgrade_casket`（自定义，NBT 保留）|
| craftType | `forge` |
| 中心祭坛 | 血肉晶匣 I → 消耗，NBT 复制到产出 |
| 材料 | 不洁之血（`goety:unholy_blood`）×1, 聚晶袋（`goety:focus_pack`）×1, 暗夜之心（`goety:heart_of_the_night`）×1, 不洁布料（`goety:unholy_fabric`）×2, 灵质（`goety:soul_powder`）×2 |
| 启动条件 | 献祭村民（`#goety:villagers`） |
| 灵魂消耗 | 111/秒（JSON `soulCost: 111`） |
| 持续时间 | 6 秒（JSON `duration: 6`） |
| 产出 | `GoetyMech:flesh_casket_2`（继承 I 级的内部物品和触发类型） |

### III 级：血肉晶匣 II → 血肉晶匣 III

| 项目 | 值 |
|------|-----|
| 仪式类型 | `GoetyMech:upgrade_casket`（自定义，NBT 保留）|
| craftType | `forge` |
| 中心祭坛 | 血肉晶匣 II → 消耗，NBT 复制到产出 |
| 材料 | 不洁之血（`goety:unholy_blood`）×1, 多晶大袋（`goety:focus_pack`）×1, 下界合金锭 ×2, 不洁布料（`goety:unholy_fabric`）×2 |
| 启动条件 | 献祭村民（`#goety:villagers`） |
| 灵魂消耗 | 111/秒（JSON `soulCost: 111`） |
| 持续时间 | 6 秒（JSON `duration: 6`） |
| 产出 | `GoetyMech:flesh_casket_3`（继承 II 级的内部物品和触发类型） |

## Goety 仪式系统关键机制

### JSON 配方位置

```
src/main/resources/data/GoetyMech/recipes/<name>.json
```

配方 type 为 `goety:ritual`（标准仪式），注册在 `data/GoetyMech/` 下而非 `data/goety/`，但 Goety 能跨 mod 读取。**注意**：item ID 必须使用 Goety 的注册名（`goety:pale_steel_ingot` 而非 `pale_metal_ingot`），错误 ID 不会报错但 JEI 不显示配方。

### JSON 通用结构（craft 类型）

```json
{
  "type": "goety:ritual",
  "ritual_type": "goety:craft",
  "activation_item": { "item": "goety:dark_wand" },
  "craftType": "forge",
  "soulCost": 111,
  "duration": 6,
  "entity_to_sacrifice": {
    "tag": "goety:villagers",
    "display_name": "entity.minecraft.villager"
  },
  "ingredients": [
    { "item": "goety:pale_metal_ingot" },
    { "tag": "forge:raw_meat" }
  ],
  "result": {
    "item": "GoetyMech:flesh_casket_1"
  }
}
```

### 关键 JSON 字段说明

| 字段 | 必填 | 说明 |
|------|------|------|
| `type` | 是 | 固定 `"goety:ritual"` |
| `ritual_type` | 是 | `"goety:craft"`（物品制作） |
| `activation_item` | 是 | 放在暗黑祭坛上的物品。支持 `"item"` 或 `"tag"` |
| `craftType` | 是 | 结构要求标签。可选值见下方「craftType 可选值」 |
| `soulCost` | 是 | 每 tick 灵魂消耗量。**实际机制：每 20 tick (1 秒) 消耗一次** |
| `duration` | 是 | 仪式持续 tick 数。`duration: 6` = 6 tick ≈ 0.3 秒（但材料消耗按秒分配） |
| `entity_to_sacrifice` | 否 | 启动条件：必须在这个标签内的实体死亡 |
| `ingredients` | 否 | 材料数组，放祭品台上。最多 9 项 |
| `result` | 否 | 产出物品。对 craft 仪式必须 |

### craftType 可选值

来自 Goety `RitualTypes.java`：

| 值 | 结构要求 | 适用场景 |
|----|---------|---------|
| `forge` | 锻造相关方块 | 物品制作、升级 |
| `magic` | 魔法相关方块 | 通用 |
| `necroturgy` | 亡灵相关 | 不死生物相关 |
| `end` | 末地方块 | 高级物品 |
| `storm` | 风暴相关 | 中级物品 |
| `sky` | 天空相关 | 召唤 |
| `sabbath` | 献祭相关 | 强力仪式 |
| `geoturgy` | 大地相关 | 矿石 |
| `frost` | 冰霜相关 | 冰系 |
| `deep` | 深暗相关 | 古城 |
| `overgrown` | 自然相关 | 自然系 |
| `divination` | 预言相关 | 定位 |
| `adept_nether` | 地狱中级 | 下界 |
| `expert_nether` | 地狱高级 | 下界高级 |
| `animation` | 动画相关 | 构造 |

### 灵魂消耗机制

- 每 20 game tick（1 秒）扣除一次 `soulCost` 值
- 灵魂来自暗黑祭坛正下方的 `CursedCageBlockEntity`
- 如果灵魂不足，仪式失败

### 牺牲检测机制

- 通过监听 `GameEvent.ENTITY_DIE` 触发
- 检测范围内死亡的实体是否匹配 `entity_to_sacrifice` 标签
- 必须是在仪式开始**后**死亡的实体才有效
- **必须在配方中配置**：`"entity_to_sacrifice": { "tag": "goety:villagers", "display_name": "..." }`

### 材料消耗机制

- 材料消耗速率：`durationPerIngredient = duration / (ingredients.size() + 1)`
- 桶类物品返还空桶
- 有合成遗留物的物品弹出遗留物
- 材料放在祭品台（Pedestal）上，自动扫描范围内

## NBT 保留方案

### 现有限制

`CraftItemRitual.finish()` 默认不保留激活物品的 NBT。仅 Wand→Wand 时复制物品容量，且受配置控制。

### 实现方式（实际方案）

自定义 `Ritual` 子类 `UpgradeCasketRitual`，关键点：**必须先调 `super.finish()` 再 shrink**（否则激活物品提前消失导致 super 内 NPE）：

```java
public class UpgradeCasketRitual extends Ritual {
    public UpgradeCasketRitual(RitualRecipe recipe) {
        super(recipe);
    }

    @Override
    public void finish(Level world, BlockPos blockPos, DarkAltarBlockEntity tileEntity,
                       Player castingPlayer, ItemStack activationItem) {
        super.finish(world, blockPos, tileEntity, castingPlayer, activationItem);
        ItemStack result = this.recipe.getResultItem(world.registryAccess()).copy();
        if (activationItem.hasTag()) {
            result.setTag(activationItem.getTag().copy());
        }
        activationItem.shrink(1);
        result.onCraftedBy(world, castingPlayer, 1);
        IItemHandler handler = tileEntity.itemStackHandler.orElseThrow(RuntimeException::new);
        handler.insertItem(0, result, false);
    }
}
```

### 注册新 ritual_type（实际方案）

Goety 将 `ritual_factory` 注册为 Forge 原版注册表，可直接用 `DeferredRegister` 绑定到 `goety:ritual_factory`：

```java
public class ModRituals {
    public static final ResourceLocation RITUAL_FACTORY_REGISTRY =
            new ResourceLocation("goety", "ritual_factory");

    public static final DeferredRegister<ModRitualFactory> RITUALS =
            DeferredRegister.create(RITUAL_FACTORY_REGISTRY, GoetyMechMod.MOD_ID);

    public static final RegistryObject<ModRitualFactory> UPGRADE_CASKET =
            RITUALS.register("upgrade_casket",
                    () -> new ModRitualFactory(UpgradeCasketRitual::new));
}
```

然后在主类注册到 mod 事件总线：
```java
ModRituals.RITUALS.register(modBus);
```

> JSON 中 `ritual_type` 写为 `"GoetyMech:upgrade_casket"`（命名空间 = 注册时用的 mod id）。

## 触发类型切换系统（诅咒注入器循环）

触发类型 A（满盈）/ B（平衡）/ C（濒死）作为 NBT key `"GoetyMech.triggerType"` 存储，通过诅咒注入器配方循环切换。不消耗材料，不损失物品。

### 配方 JSON（`data/GoetyMech/recipes/flesh_casket_cycle_{1,2,3}.json`）

```json
{
  "type": "goety:cursed_infuser",
  "cookingtime": 100,
  "experience": 0.0,
  "ingredient": { "item": "GoetyMech:flesh_casket_1" },
  "result": "GoetyMech:flesh_casket_1",
  "ritual_type": "GoetyMech:flesh_casket_infuser",
  "update": true,
  "updateAndCheckNBT": { "item": "GoetyMech:flesh_casket_1", "nbt_match": false },
  "wanderingTraderRarity": "RARE"
}
```

| 字段 | 说明 |
|------|------|
| `type` | `"goety:cursed_infuser"`（使用 Goety 内置 recipe type） |
| `ritual_type` | `"GoetyMech:flesh_casket_infuser"`（自定义 serializer 处理 NBT） |
| `updateAndCheckNBT` | 避免 JEI 配方冲突，`nbt_match: false` 保证显示且不匹配 NBT |

### 自定义 Serializer

`FleshCasketInfuserRecipeSerializer` 读取标准 cursed infuser 字段并注册为 `GoetyMech:flesh_casket_infuser`。`assemble()` 负责复制输入物品的 Tag 并调用 `FleshCasketItem.cycleTriggerType()`。

## 新增/修改文件清单

### 新增 Java 类

| 路径 | 说明 |
|------|------|
| `common/ritual/UpgradeCasketRitual.java` | 自定义仪式，保留激活物品 NBT |
| `init/ModRituals.java` | 注册 `upgrade_casket` ritual_type（`goety:ritual_factory` 注册表）|
| `common/recipe/FleshCasketInfuserRecipe.java` | 诅咒注入器配方，NBT 复制 + 触发类型循环 |
| `common/recipe/FleshCasketInfuserRecipeSerializer.java` | 注入器配方序列化器 |

### 新增 JSON 配方

| 路径 | 说明 |
|------|------|
| `data/GoetyMech/recipes/flesh_casket_1.json` | I 级仪式配方（`goety:craft`）|
| `data/GoetyMech/recipes/flesh_casket_1_to_2.json` | II 级升级配方（`GoetyMech:upgrade_casket`）|
| `data/GoetyMech/recipes/flesh_casket_2_to_3.json` | III 级升级配方（`GoetyMech:upgrade_casket`）|
| `data/GoetyMech/recipes/flesh_casket_cycle_1.json` | 触发类型循环配方（诅咒注入器）|
| `data/GoetyMech/recipes/flesh_casket_cycle_2.json` | 同上 |
| `data/GoetyMech/recipes/flesh_casket_cycle_3.json` | 同上 |

### 新增物品注册

| 注册名 | 对应 |
|--------|------|
| `flesh_casket_1` | 移除 A1/B1/C1，合并为 1 |
| `flesh_casket_2` | 移除 A2/B2/C2，合并为 2 |
| `flesh_casket_3` | 移除 A3/B3/C3，合并为 3 |

### 删除文件

| 文件 | 原因 |
|------|------|
| `flesh_casket_a_1` ~ `flesh_casket_c_3` 共 9 个注册项 | 合为 3 个 |
| 9 个诅咒注入器转化配方 | 改为 NBT 内切换触发类型 |
| 对应模型/纹理（12 个旧模型 JSON + 9 个旧配方 JSON） | 更新为新的 3 个 |

### 修改文件

| 文件 | 内容 |
|------|------|
| `FleshCasketItem.java` | 触发类型改为 NBT 可切换属性，移除 `TriggerType` import |
| `TriggerType.java` | 保留但可能不再直接引用（NBT key 替代）|
| `FleshCasketEvents.java` | 适配新 NBT 触发类型读取 |
| `ModItems.java` | 9 个旧注册 → 3 个新注册 |
| `ModCreativeTab.java` | 更新为 3 个物品 |
| `ModRecipeSerializers.java` | 添加 `FLESH_CASKET_INFUSER` |
| `GoetyMechMod.java` | 注册 `ModRituals.RITUALS` |
| 语言文件 `zh_cn.json` / `en_us.json` | 更新物品名称和描述 |
| `back.json` (Curios tag) | 更新为新的 3 个物品 |
| `flesh-casket.md`（文档） | 更新演化记录 |
