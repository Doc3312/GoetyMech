---
name: "goety-ritual-dev"
description: "Goety Ritual / 仪式开发 / ModRitualFactory / IRitualType / recipe JSON / 聚晶合成 / 机械仪式 / 祭坛"
---

# Goety Ritual Dev — 仪式开发速查手册

> 前置模组源码路径见 `.opencode/skills/_shared/libcode-paths.md`。
> 快速排除：当前任务如果不涉及 Goety 仪式（Ritual）开发、聚晶合成配方、或仪式 recipe JSON，请跳过本 skill。

## 适用场景

- 为聚晶/物品创建 Goety 仪式合成配方
- 自定义 `Ritual` 子类（如机械仪式）
- 注册 `ModRitualFactory` 和 `IRitualType`
- 编写 `ritual` 类型的 Patchouli 条目页面

---

## 架构：两套独立注册

**最容易踩的坑**：Goety 仪式需要**两次独立注册**，缺一不可。

| 注册项 | 注册位置 | JSON 字段 | 翻译 key |
|--------|---------|-----------|----------|
| `IRitualType` | `ModRitualTypes.java` — `RitualType.addRitualType("name", new XxxType())` | `craftType` | `jei.goety.craftType.<name>` |
| `ModRitualFactory` | `ModRituals.java` — `DeferredRegister<ModRitualFactory>` + `GoetyMechMod` 注册到 event bus | `ritual_type` | 不需要翻译（内部匹配用） |

**`ritual_type`** — 匹配 `ModRitualFactory` 注册 ID，决定**用哪个 Ritual 子类处理逻辑**。
**`craftType`** — 匹配 `IRitualType.getName()`，决定**柱基种类 + 视觉特效 + JEI/Patchouli 类型名称**。

---

## Recipe JSON 格式

```json
{
  "type": "goety:ritual",
  "ritual_type": "goetymech:mechanical",
  "activation_item": { "item": "goety:empty_focus" },
  "craftType": "mechanical",
  "soulCost": 80,
  "duration": 8,
  "ingredients": [
    { "item": "minecraft:ender_pearl" },
    { "item": "create:stock_link" }
  ],
  "result": { "item": "goetymech:remote_storage_focus" }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `ritual_type` | String | `ModRitualFactory` 注册 ID，带 MOD_ID 命名空间 |
| `craftType` | String | **单字符串**，不是数组。匹配 `IRitualType.getName()` |
| `soulCost` | int | 总灵魂消耗（8s×10/s=80） |
| `duration` | int | **秒**，不是 tick |

---

## Ritual 子类模板

```java
public class MechanicalRitual extends Ritual {
    public MechanicalRitual(RitualRecipe recipe) {
        super(recipe);
    }

    @Override
    public void finish(Level world, BlockPos blockPos, DarkAltarBlockEntity tileEntity,
                       Player castingPlayer, ItemStack activationItem) {
        super.finish(world, blockPos, tileEntity, castingPlayer, activationItem);
        // ★ 必须手动把产物放回祭坛 slot，基类 finish() 不产物
        ItemStack result = this.recipe.getResultItem(world.registryAccess()).copy();
        activationItem.shrink(1);
        result.onCraftedBy(world, castingPlayer, 1);
        IItemHandler handler = tileEntity.itemStackHandler.orElseThrow(RuntimeException::new);
        handler.insertItem(0, result, false);
    }
}
```

**关键注意**：`Ritual.finish()` 只播放音效和特效，**不产出物品**。子类必须 override `finish()` 手动 `handler.insertItem(0, result, false)`。

---

## ModRitualFactory 注册模板

```java
// ModRituals.java
public static final DeferredRegister<ModRitualFactory> RITUALS =
    DeferredRegister.create(new ResourceLocation("goety", "ritual_factory"), GoetyMechMod.MOD_ID);

public static final RegistryObject<ModRitualFactory> MECHANICAL =
    RITUALS.register("mechanical", () -> new ModRitualFactory(MechanicalRitual::new));

// GoetyMechMod.java
ModRituals.RITUALS.register(modEventBus);  // ★ 必须注册到 event bus
```

---

## IRitualType 注册模板

```java
// ModRitualTypes.java
public static void register() {
    RitualType.addRitualType("mechanical", new MechanicalRitualType());
}

// GoetyMechMod.java
modEventBus.addListener((FMLCommonSetupEvent event) -> {
    ModRitualTypes.register();  // ★ 必须在 FMLCommonSetupEvent 中调用
});
```

---

## 翻译 key 速查

| key | zh_cn 值 | en_us 值 | 显示位置 |
|-----|---------|---------|---------|
| `jei.goety.craftType.mechanical` | `机械` | `Mechanical` | JEI 配方页类型标签 + Patchouli 仪式模板 |
| `spell.goety.mechanical` | `§e机械§e` | `§eMechanical§e` | 聚晶 tooltip 法术类型 |

> ⚠️ `jei.goety.craftType.<name>` 是 Goety `RitualRecipeProcessor` + JEI `ModRitualCategory` 的硬编码 key 格式，不是 `ritual.goety.<name>`。

---

## 相关技能

| 技能 | 典型切换场景 |
|------|-------------|
| `goety-addon-dev` | 聚晶物品/Spell 类开发，注册 ID 命名，翻译 key 规则 |
| `patchouli-dev` | 为仪式配方添加 Patchouli 条目页面 |
| `jei-dev` | JEI 配方兼容 ghost drag 等 |

---

## 常见踩坑清单

1. ❌ `craftType: ["mechanical"]` 数组格式 → Goety JSON 解析失败 → 配方不显示
   ✅ `craftType: "mechanical"` 单字符串
2. ❌ 没 override `Ritual.finish()` → 仪式跑完，产物不产出，祭坛上仍是触发物
   ✅ override `finish()` + `handler.insertItem(0, result, false)`
3. ❌ `jei.goety.craftType.mechanical` 翻译 key 写成 `ritual.goety.mechanical`
   ✅ 用 `jei.goety.craftType.<name>` 格式
4. ❌ `SpellType.create("mechanical", "spell.goety.mechanical")` 双重前缀
   ✅ 第二个参数裸名 `"mechanical"`
