# Tags 绯荤粺 + 鍐呭娉ㄥ唽 + DataGen + Stress

## Tags 绯荤粺

Create 娉ㄥ唽浜嗗ぇ閲?Tag锛岄檮灞炲彲閫氳繃鏁版嵁鍖呮垨 CreateRegistrate 鍔犲叆銆?

### Block Tags

| Tag | 璇存槑 |
|---|---|
| `create:brittle` | 杩炴帴澶氭柟鐨勮剢寮辨柟鍧?|
| `create:copycat_allow` / `create:copycat_deny` | Copycat 鏂瑰潡鍏佽/绂佹 |
| `create:fan_transparent` | 涓嶉樆姝㈤鏈烘皵娴?|
| `create:girdable_track` | 杞ㄩ亾涓嬪畨瑁呭ぇ姊?|
| `create:movable_empty_collider` | 纰版挒绠变负绌轰絾鍙Щ鍔?|
| `create:non_movable` | 瑁呯疆鏃犳硶绉诲姩 |
| `create:non_breakable` | 閽诲ご/閿墖鏃犳硶鐮村潖 |
| `create:passive_boiler_heaters` | 閿呯倝琚姩鐑簮 |
| `create:safe_nbt` | 鎵撳嵃淇濈暀 NBT |
| `create:windmill_sails` | 椋庡竼鏈夋晥鏂瑰潡 |
| `create:wrench_pickup` | 鎵虫墜蹇€熸崱璧?|
| `create:chest_mounted_storage` / `create:simple_mounted_storage` | 鎮寕瀛樺偍 |
| `create:fan_processing_catalysts/blasting` | 榧撻鐔旂偧鍌寲鍓?|
| `create:fan_processing_catalysts/haunting` | 榧撻闄勯瓟鍌寲鍓?|
| `create:fan_processing_catalysts/smoking` | 榧撻鐑熺啅鍌寲鍓?|
| `create:fan_processing_catalysts/splashing` | 榧撻娲楁钉鍌寲鍓?|

### Item Tags

| Tag | 璇存槑 |
|---|---|
| `create:blaze_burner_fuel/regular` | 鏅€氱噧鏂?|
| `create:blaze_burner_fuel/special` | 瓒呯骇鐕冩枡 |
| `create:upright_on_belt` | 浼犻€佸甫绔栫珛娓叉煋 |
| `create:casing` | 澶栧３ |
| `create:create_ingots` | Create 閿?|
| `create:crushed_ores` | 绮夌鐭跨煶 |
| `create:sandpaper` | 鐮傜焊 |
| `create:seats` | 搴ф |
| `create:tracks` | 杞ㄩ亾 |

### Fluid Tags

| Tag | 璇存槑 |
|---|---|
| `create:bottomless/allow` | 鍏佽鏃犻檺鎶藉彇 |
| `create:bottomless/deny` | 绂佹鏃犻檺鎶藉彇 |

### Entity Tags

| Tag | 璇存槑 |
|---|---|
| `create:blaze_burner_capturable` | 鍙绗煎瓙鎹曡幏 |
| `create:ignore_seat` | 涓嶅簲鍧愬骇妞?|

---

## 鍐呭娉ㄥ唽锛圕reateRegistrate锛?

### 鍒涘缓鑷繁鐨?Registrate 瀹炰緥

涓嶈浣跨敤 `Create.registrate()`锛堜細妫€鏌ュ寘鍚嶏紝闈?`com.simibubi.create` 鎶涘紓甯革級銆?

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

### 娉ㄥ唽 Block

```java
public static final BlockEntry<MyMachineBlock> MY_MACHINE = REGISTRATE
        .block("my_machine", MyMachineBlock::new)
        .transform(BuilderTransformers.machine())
        .register();
```

甯哥敤 BuilderTransformers锛?

| 鏂规硶 | 鐢ㄩ€?|
|---|---|
| `machine()` | 鏍囧噯鏈哄櫒 |
| `pickaxeOnly()` | 浠呴晲鍙噰 |
| `wooden()` | 鏈ㄧ被 |
| `furnace()` | 鐔旂倝绫?|
| `generator()` | 鍙戠數鏈虹被 |
| `cogwheel()` | 榻胯疆绫?|
| `shaft()` | 浼犲姩杞寸被 |
| `belt()` | 浼犻€佸甫绫?|
| `boiler()` | 閿呯倝绫?|

### 娉ㄥ唽 Item

```java
public static final ItemEntry<MyItem> MY_ITEM = REGISTRATE
        .item("my_item", MyItem::new)
        .properties(p -> p.rarity(Rarity.UNCOMMON))
        .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/my_item")))
        .tag(AllTags.forgeItemTag("ingots"))
        .register();
```

### 娉ㄥ唽 BlockEntity

```java
public static final BlockEntityEntry<MyMachineBlockEntity> MY_MACHINE_BE = REGISTRATE
        .blockEntity("my_machine", MyMachineBlockEntity::new)
        .validBlocks(MyBlocks.MY_MACHINE)
        .register();
```

### 娉ㄥ唽 Fluid

```java
public static final FluidEntry<MyFluid> MY_FLUID = REGISTRATE
        .fluid("my_fluid", MyFluid.Source::new, MyFluid.Flowing::new)
        .attributes(b -> b.viscosity(2000))
        .properties(p -> p.levelDecreasePerBlock(2))
        .register();
```

### CreateRegistrateRegistrationCallback

濡傞渶鍦?Create 鍐呴儴鐨?Registrate 涓婃敞鍐屽洖璋冿細

```java
@SubscribeEvent
public static void onRegister() {
    CreateRegistrateRegistrationCallback.EVENT.register(registrate -> {
        // 鍦?Create 鐨?registrate 涓婃敞鍐?
    });
}
```

### 鑷姩 DataGen

浣跨敤 CreateRegistrate 鍚庯紝鏋勫缓鏃惰嚜鍔ㄧ敓鎴?Blockstates銆丮odels銆丷ecipes銆乀ags銆乴oot銆佽瑷€鏂囦欢銆?

### 鎵嬪姩 Tag 娣诲姞

```java
public static final BlockEntry<MyBlock> MY_BLOCK = REGISTRATE
        .block("my_block", MyBlock::new)
        .transform(BuilderTransformers.machine())
        .tag(AllBlockTags.FAN_TRANSPARENT.tag)
        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .register();
```

---

## Stress 搴斿姏鍊?

浠?Create 6.0.0 璧凤紝搴斿姏鍊奸€氳繃鏁版嵁椹卞姩瀹氫箟锛屾棤闇€浠ｇ爜銆?

`data/create/stress_values/` 涓嬪垱寤?JSON锛?

```json
{
  "GoetyMech:my_machine": 4.0,
  "GoetyMech:my_generator": -256.0
}
```

姝ｅ€?= 娑堣€楋紝璐熷€?= 浜х敓銆?
