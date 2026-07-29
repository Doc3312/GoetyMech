# Tags 系统 + 内容注册 + DataGen + Stress

## Tags 系统

Create 注册了大量 Tag，附属可通过数据包或 CreateRegistrate 加入。

### Block Tags

| Tag | 说明 |
|---|---|
| `create:brittle` | 连接多方的脆弱方块 |
| `create:copycat_allow` / `create:copycat_deny` | Copycat 方块允许/禁止 |
| `create:fan_transparent` | 不阻止风机气流 |
| `create:girdable_track` | 轨道下安装大梁 |
| `create:movable_empty_collider` | 碰撞箱为空但可移动 |
| `create:non_movable` | 装置无法移动 |
| `create:non_breakable` | 钻头/锯片无法破坏 |
| `create:passive_boiler_heaters` | 锅炉被动热源 |
| `create:safe_nbt` | 打印保留 NBT |
| `create:windmill_sails` | 风帆有效方块 |
| `create:wrench_pickup` | 扳手快速捡起 |
| `create:chest_mounted_storage` / `create:simple_mounted_storage` | 悬挂存储 |
| `create:fan_processing_catalysts/blasting` | 鼓风熔炼催化剂 |
| `create:fan_processing_catalysts/haunting` | 鼓风附魔催化剂 |
| `create:fan_processing_catalysts/smoking` | 鼓风烟熏催化剂 |
| `create:fan_processing_catalysts/splashing` | 鼓风洗涤催化剂 |

### Item Tags

| Tag | 说明 |
|---|---|
| `create:blaze_burner_fuel/regular` | 普通燃料 |
| `create:blaze_burner_fuel/special` | 超级燃料 |
| `create:upright_on_belt` | 传送带竖立渲染 |
| `create:casing` | 外壳 |
| `create:create_ingots` | Create 锭 |
| `create:crushed_ores` | 粉碎矿石 |
| `create:sandpaper` | 砂纸 |
| `create:seats` | 座椅 |
| `create:tracks` | 轨道 |

### Fluid Tags

| Tag | 说明 |
|---|---|
| `create:bottomless/allow` | 允许无限抽取 |
| `create:bottomless/deny` | 禁止无限抽取 |

### Entity Tags

| Tag | 说明 |
|---|---|
| `create:blaze_burner_capturable` | 可被笼子捕获 |
| `create:ignore_seat` | 不应坐座椅 |

---

## 内容注册（CreateRegistrate）

### 创建自己的 Registrate 实例

不要使用 `Create.registrate()`（会检查包名，非 `com.simibubi.create` 抛异常）。

```java
public class GoetyMech {
    public static final String MOD_ID = "GoetyMech";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    public GoetyMech(IEventBus modEventBus) {
        REGISTRATE.registerEventListeners(modEventBus);
    }
}
```

### 注册 Block

```java
public static final BlockEntry<MyMachineBlock> MY_MACHINE = REGISTRATE
        .block("my_machine", MyMachineBlock::new)
        .transform(BuilderTransformers.machine())
        .register();
```

常用 BuilderTransformers：

| 方法 | 用途 |
|---|---|
| `machine()` | 标准机器 |
| `pickaxeOnly()` | 仅镐可采 |
| `wooden()` | 木类 |
| `furnace()` | 熔炉类 |
| `generator()` | 发电机类 |
| `cogwheel()` | 齿轮类 |
| `shaft()` | 传动轴类 |
| `belt()` | 传送带类 |
| `boiler()` | 锅炉类 |

### 注册 Item

```java
public static final ItemEntry<MyItem> MY_ITEM = REGISTRATE
        .item("my_item", MyItem::new)
        .properties(p -> p.rarity(Rarity.UNCOMMON))
        .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/my_item")))
        .tag(AllTags.forgeItemTag("ingots"))
        .register();
```

### 注册 BlockEntity

```java
public static final BlockEntityEntry<MyMachineBlockEntity> MY_MACHINE_BE = REGISTRATE
        .blockEntity("my_machine", MyMachineBlockEntity::new)
        .validBlocks(MyBlocks.MY_MACHINE)
        .register();
```

### 注册 Fluid

```java
public static final FluidEntry<MyFluid> MY_FLUID = REGISTRATE
        .fluid("my_fluid", MyFluid.Source::new, MyFluid.Flowing::new)
        .attributes(b -> b.viscosity(2000))
        .properties(p -> p.levelDecreasePerBlock(2))
        .register();
```

### CreateRegistrateRegistrationCallback

如需在 Create 内部的 Registrate 上注册回调：

```java
@SubscribeEvent
public static void onRegister() {
    CreateRegistrateRegistrationCallback.EVENT.register(registrate -> {
        // 在 Create 的 registrate 上注册
    });
}
```

### 自动 DataGen

使用 CreateRegistrate 后，构建时自动生成 Blockstates、Models、Recipes、Tags、loot、语言文件。

### 手动 Tag 添加

```java
public static final BlockEntry<MyBlock> MY_BLOCK = REGISTRATE
        .block("my_block", MyBlock::new)
        .transform(BuilderTransformers.machine())
        .tag(AllBlockTags.FAN_TRANSPARENT.tag)
        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .register();
```

---

## Stress 应力值

从 Create 6.0.0 起，应力值通过数据驱动定义，无需代码。

`data/create/stress_values/` 下创建 JSON：

```json
{
  "GoetyMech:my_machine": 4.0,
  "GoetyMech:my_generator": -256.0
}
```

正值 = 消耗，负值 = 产生。
