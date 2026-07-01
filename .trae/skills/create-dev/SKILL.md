---
name: "create-dev"
description: "Create 妯＄粍闄勫睘寮€鍙戞寚鍗椼€傛兜鐩栦緷璧栭厤缃€並inetic 杩愯鏃剁敓鍛藉懆鏈熴€丳rocessingRecipe/Ponder API銆丷egistrate 娉ㄥ唽銆丅ESR 娓叉煋绠＄嚎涓?Flywheel銆傚綋闇€瑕佸紑鍙?Create 鏈哄櫒銆佸姩鍔涘鏂瑰潡銆侀厤鏂圭郴缁熸椂璋冪敤鏈?skill銆?
---

# Create 妯＄粍寮€鍙戞寚鍗?
## 渚濊禆閰嶇疆

```groovy
repositories {
    maven { url = "https://maven.createmod.net" }
    maven { url = "https://maven.ithundxr.dev/mirror" }
}

dependencies {
    implementation(fg.deobf("com.simibubi.create:create-${minecraft_version}:${create_version}:slim") { transitive = false })
    implementation(fg.deobf("net.createmod.ponder:Ponder-Forge-${minecraft_version}:${ponder_version}"))
    compileOnly(fg.deobf("dev.engine-room.flywheel:flywheel-forge-api-${minecraft_version}:${flywheel_version}"))
    runtimeOnly(fg.deobf("dev.engine-room.flywheel:flywheel-forge-${minecraft_version}:${flywheel_version}"))
    implementation(fg.deobf("com.tterrag.registrate:Registrate:${registrate_version}"))
}
```

```properties
minecraft_version = 1.20.1
create_version = 6.0.8-289
ponder_version = 1.0.91
flywheel_version = 1.0.5
registrate_version = MC1.20-1.3.3
```

---

## Kinetic 杩愯鏃剁敓鍛藉懆鏈?
### KineticBlockEntity 鏍稿績鏈哄埗

#### tick() 鎵ц娴佺▼
- `tick()`: 姣?tick 璋冪敤銆傚厛妫€鏌?`!level.isClientSide && needsSpeedUpdate()` 鈫?`attachKinetics()`銆傝皟鐢?`super.tick()`锛岀劧鍚?`effects.tick()`銆侰lient 绔蛋 `tickAudio()` 鍚?return銆係erver 绔瘡 `kineticValidationFrequency` ticks 鎵ц `validateKinetics()`銆?- `validateKinetics()`: 妫€鏌?`hasSource()` 鐨?BE 鐨?source 鏄惁浠嶇劧鏈夋晥銆傛棤鏁堝垯 `removeSource()` + `detachKinetics()`銆?
#### speed 绠＄悊
| 鏂规硶 | 杩斿洖鍊?| 璇存槑 |
|---|---|---|
| `getSpeed()` | `overStressed ? 0 : getTheoreticalSpeed()` | 瀵瑰鏆撮湶鐨勭湡瀹炶浆閫燂紝杩囪浇鏃朵负 0 |
| `getTheoreticalSpeed()` | `speed` 瀛楁 | 鐞嗚杞€燂紝涓嶅彈杩囪浇褰卞搷 |
| `getGeneratedSpeed()` | 0锛堝熀绫伙級 | 浠?GeneratingKineticBlockEntity 鏈夋剰涔?|
| `setSpeed(float)` | 鈥?| 鐩存帴鍐?`this.speed` 瀛楁 |

**鍏抽敭瑙勫垯**: `getSpeed()` 鍦?`overStressed` 鏃惰繑鍥?0锛屼絾鍐呴儴 `speed` 瀛楁淇濈暀鍘熷€笺€?
#### stress 绠＄悊
- `calculateStressApplied()`: 閫氳繃 `BlockStressValues.getImpact()` 鑾峰彇搴斿姏娑堣€楀熀鏁帮紙鏁版嵁椹卞姩锛?- `calculateAddedStressCapacity()`: 閫氳繃 `BlockStressValues.getCapacity()` 鑾峰彇搴斿姏瀹归噺鍩烘暟
- `updateFromNetwork()`: 鐢?KineticNetwork 璋冪敤锛屾洿鏂?`capacity`/`stress`/`networkSize`/`overStressed`

### GeneratingKineticBlockEntity 鍙戠數鏈?
#### updateGeneratedRotation() 瀹屾暣娴佺▼
1. 鑾峰彇 `getGeneratedSpeed()` 涓?`prevSpeed` 姣旇緝
2. 濡傛灉閫熷害鍙樺寲 鈫?`applyNewSpeed(prevSpeed, speed)`
3. 濡傛灉鏈?network 涓?speed 鈮?0 鈫?鏇存柊 capacity + stress 鈫?`network.updateStress()`
4. 璋冪敤 `onSpeedChanged(prevSpeed)` 鈫?`sendData()`

### 鍏抽敭闄烽槺

#### notifyStressCapacityChange 蹇呴』鍦?network != null 鏃惰皟鐢?鍦ㄨ嚜瀹氫箟 tick 涓皟鐢?`notifyStressCapacityChange(capacity)` 鏃讹紝濡傛灉 BE 宸蹭粠缃戠粶涓劚绂伙紝`getOrCreateNetwork()` 鍙兘杩斿洖 null 瀵艰嚧 NPE銆?
```java
// 鉂?閿欒
notifyStressCapacityChange(cachedAvailableStress);

// 鉁?姝ｇ‘
if (network != null)
    notifyStressCapacityChange(cachedAvailableStress);
```

#### calculateAddedStressCapacity 杩斿洖鐨勬槸鍩烘暟锛孋reate 浼氳嚜鍔ㄤ箻浠?speed
`calculateAddedStressCapacity()` 鐨勮繑鍥炲€间細琚?Create 鍦?`updateGeneratedRotation()` 涓嚜鍔ㄤ箻浠?`|speed|`銆?
```java
// 鉂?閿欒锛氳繑鍥炵洰鏍囧閲忥紝瀹為檯瀹归噺 = 鐩爣 脳 speed锛堣繃澶э級
@Override public float calculateAddedStressCapacity() { return cachedAvailableStress; }

// 鉁?姝ｇ‘锛氳繑鍥炲熀鏁帮紝璁?Create 鐨?speed 涔樻硶绠楀嚭鐩爣瀹归噺
@Override public float calculateAddedStressCapacity() { return ratio; }
```

#### 璺ㄦ柟鍧楀簲鍔涜拷韪紙consumedRemotely 妯″紡锛?褰撲竴涓柟鍧椾粠鍙︿竴涓柟鍧楃殑缃戠粶"娑堣€?搴斿姏鏃讹紝鍗曠函鐨勮鍊间細瀵艰嚧璇诲彇鍣ㄦ案杩滄樉绀烘湭娑堣€楃殑搴斿姏鍊笺€?
瑙ｅ喅鏂规锛氬紩鎿庡繀椤昏鍙?`capacity - stress`锛堝師濮嬪€硷級锛屼笉鑳界敤 `getAvailableStress()`锛堣皟鏁村悗鐨勫€硷級锛屽惁鍒欎骇鐢?feedback loop銆?
---

## ProcessingRecipe + Ponder + API

### ProcessingRecipe 閰嶆柟绯荤粺
```java
public class MyProcessingRecipe extends ProcessingRecipe<MyProcessingRecipe> {
    public MyProcessingRecipe(ProcessingRecipeParams params) {
        super(MyRecipeTypes.MY_TYPE, params);
    }
    @Override protected int getMaxInputCount()       { return 2; }
    @Override protected int getMaxOutputCount()      { return 2; }
    @Override protected int getMaxFluidInputCount()  { return 0; }
    @Override protected int getMaxFluidOutputCount() { return 0; }
}
```

### Serializer
```java
public static final ProcessingRecipeSerializer<MyProcessingRecipe> SERIALIZER =
        new ProcessingRecipeSerializer<>(MyProcessingRecipe::new);
public static final RecipeType<MyProcessingRecipe> TYPE = RecipeType.simple(
        new ResourceLocation(MOD_ID, "my_processing"));
```

---

## Tags 绯荤粺 + 鍐呭娉ㄥ唽 + DataGen + Stress

### Tags 绯荤粺
Create 娉ㄥ唽浜嗗ぇ閲?Tag锛岄檮灞炲彲閫氳繃鏁版嵁鍖呮垨 CreateRegistrate 鍔犲叆銆?
| Tag | 璇存槑 |
|---|---|
| `create:brittle` | 杩炴帴澶氭柟鐨勮剢寮辨柟鍧?|
| `create:non_movable` | 瑁呯疆鏃犳硶绉诲姩 |
| `create:non_breakable` | 閽诲ご/閿墖鏃犳硶鐮村潖 |
| `create:passive_boiler_heaters` | 閿呯倝琚姩鐑簮 |
| `create:windmill_sails` | 椋庡竼鏈夋晥鏂瑰潡 |
| `create:wrench_pickup` | 鎵虫墜蹇€熸崱璧?|
| `create:fan_processing_catalysts/blasting` | 榧撻鐔旂偧鍌寲鍓?|
| `create:fan_processing_catalysts/haunting` | 榧撻闄勯瓟鍌寲鍓?|
| `create:fan_processing_catalysts/smoking` | 榧撻鐑熺啅鍌寲鍓?|
| `create:fan_processing_catalysts/splashing` | 榧撻娲楁钉鍌寲鍓?|

### 鍐呭娉ㄥ唽锛圕reateRegistrate锛?涓嶈浣跨敤 `Create.registrate()`锛堜細妫€鏌ュ寘鍚嶏級銆?
```java
public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
        .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
```

### 娉ㄥ唽 Block
```java
public static final BlockEntry<MyMachineBlock> MY_MACHINE = REGISTRATE
        .block("my_machine", MyMachineBlock::new)
        .transform(BuilderTransformers.machine())
        .register();
```

### Stress 搴斿姏鍊?浠?Create 6.0.0 璧凤紝搴斿姏鍊奸€氳繃鏁版嵁椹卞姩瀹氫箟锛?`data/create/stress_values/` 涓嬪垱寤?JSON锛?```json
{ "GoetyMech:my_machine": 4.0, "GoetyMech:my_generator": -256.0 }
```
姝ｅ€?= 娑堣€楋紝璐熷€?= 浜х敓銆?
---

## BESR 娓叉煋绠＄嚎涓?Flywheel

### SafeBlockEntityRenderer
- `render()` 鏄?`final`鈥斺€旂姝㈠瓙绫?override锛?- 鏀逛负瀹炵幇 `renderSafe()` 鎶借薄鏂规硶
- `render()` 鍏堟鏌?`isInvalid(be)`锛屾棤鏁堢洿鎺?return

### KineticBlockEntityRenderer
鏍囧噯娓叉煋娴佺▼锛?```java
protected void renderSafe(T be, float partialTicks, PoseStack ms,
    MultiBufferSource buffer, int light, int overlay) {
    if (VisualizationManager.supportsVisualization(be.getLevel())) return; // Flywheel 鎺ョ
    BlockState state = getRenderedBlockState(be);
    RenderType type = getRenderType(be, state);
    renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);
}
```

### CachedBuffers API
| 鏂规硶 | 鐢ㄩ€?|
|---|---|
| `CachedBuffers.block(compartment, state)` | 瀹屾暣 block 妯″瀷 |
| `CachedBuffers.partialFacing(partial, state, direction)` | PartialModel + facing |
| `CachedBuffers.partialFacingVertical(partial, state, direction)` | 鍨傜洿鏈濆悜 partial |
| `CachedBuffers.partial(partial, state)` | 鏃犳湞鍚戝彉鎹?|

### 鏍囧噯娓叉煋妯″紡瀵圭収
| 妯″紡 | getRotatedModel 杩斿洖 | 绀轰緥绫?|
|---|---|---|
| 绠€鏄擄紙浠?shaft锛?| 鈥?| CreativeMotorRenderer |
| 缁忓吀锛坰haftless + shaft锛?| shaftless partial | EncasedCogRenderer |
| 澶ч娇杞姞杞?| shaftless partial + shaft | BracketedKineticBlockEntityRenderer |
| 榛樿锛堝叏 block锛?| block 妯″瀷 | KineticBlockEntityRenderer |
| 鑷畾涔夐儴鍒?| 鑷畾涔?partial | DrillRenderer |