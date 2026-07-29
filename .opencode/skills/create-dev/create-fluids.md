# Create 娴佷綋寮€鍙戞寚鍗楋紙Forge 1.20.1 / Create 6.0.8锛?
> 鑷寘鍚弬鑰冿紝AI agent 鍙粠澶磋鍒板熬寤虹珛瀹屾暣蹇冩櫤妯″瀷锛屼篃鍙寜鐩綍璺宠穬鏌ユ壘銆?
---

## 鐩綍

1. [Forge 娴佷綋鍩虹](#1-forge-娴佷綋鍩虹)
2. [Create 娴佷綋鍩虹璁炬柦](#2-create-娴佷綋鍩虹璁炬柦)
3. [Create 绠￠亾涓庝紶杈揮(#3-create-绠￠亾涓庝紶杈?
4. [Create 娴佷綋閰嶆柟](#4-create-娴佷綋閰嶆柟)
5. [API 鎵╁睍鐐筣(#5-api-鎵╁睍鐐?
6. [DataGen](#6-datagen)
7. [Ponder 鍦烘櫙](#7-ponder-鍦烘櫙)
8. [娴佷綋娓叉煋](#8-娴佷綋娓叉煋)

---

## 1. Forge 娴佷綋鍩虹

### 1.1 FluidStack 鈥?娴佷綋鏍?
```java
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;

// 鏋勯€?FluidStack stack = new FluidStack(fluid, amount);            // mb 鍗曚綅
FluidStack stack = new FluidStack(fluid, amount, nbtTag);    // 甯?NBT

// 甯哥敤鏂规硶
stack.getFluid();                    // -> Fluid
stack.getAmount();                   // -> int (mb)
stack.getDisplayName();             // -> Component
stack.getFluid().getFluidType();    // -> FluidType锛堝惈灞炴€э級
stack.isFluidEqual(other);          // 绫诲瀷+NBT 鐩哥瓑
stack.getFluid().isSame(fluid);     // 浠呯被鍨嬬浉绛?stack.copy();                       // 娣辨嫹璐?stack.setAmount(n);                 // 淇敼閲忥紙璋ㄦ厧锛?FluidStack.EMPTY                    // 绌烘爤甯搁噺
```

### 1.2 IFluidHandler 鈥?娴佷綋鑳藉姏鎺ュ彛

```java
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public interface IFluidHandler {
    int getTanks();                                            // 缃愭暟
    FluidStack getFluidInTank(int tank);                       // 鏌愮綈娴佷綋
    int getTankCapacity(int tank);                             // 鏌愮綈瀹归噺
    boolean isFluidValid(int tank, FluidStack stack);          // 鑳藉惁瀛樺叆

    int fill(FluidStack resource, FluidAction action);         // 娉ㄥ叆
    FluidStack drain(FluidStack resource, FluidAction action); // 鎸夌被鍨嬫娊鍙?    FluidStack drain(int maxDrain, FluidAction action);        // 鎸夐噺鎶藉彇

    enum FluidAction {
        SIMULATE,  // 妯℃嫙锛堜笉淇敼锛?        EXECUTE    // 鎵ц
    }
}
```

**閫氳繃鑳藉姏鑾峰彇**锛?```java
LazyOptional<IFluidHandler> capability = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
IFluidHandler handler = capability.orElse(null);
```

### 1.3 ForgeFlowingFluid 鈥?娴佷綋娉ㄥ唽

```java
import net.minecraftforge.fluids.ForgeFlowingFluid;

// 娴佷綋瀹氫箟锛堥€氬父鐢?DeferredRegister锛?public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, MOD_ID);

    public static final RegistryObject<ForgeFlowingFluid.Source> MY_FLUID =
            FLUIDS.register("my_fluid", () -> new ForgeFlowingFluid.Source(ModFluidTypes.MY_PROPERTIES));

    public static final RegistryObject<ForgeFlowingFluid.Flowing> MY_FLUID_FLOWING =
            FLUIDS.register("my_fluid_flowing", () -> new ForgeFlowingFluid.Flowing(ModFluidTypes.MY_PROPERTIES));

    // 妗剁墿鍝?    public static final RegistryObject<Item> MY_FLUID_BUCKET =
            ITEMS.register("my_fluid_bucket", () -> new BucketItem(ModFluids.MY_FLUID, new Item.Properties()));

    // 娴佷綋灞炴€ф瀯寤哄櫒
    static final ForgeFlowingFluid.Properties MY_PROPERTIES =
            new ForgeFlowingFluid.Properties(
                    ModFluidTypes.MY_TYPE,
                    ModFluids.MY_FLUID,
                    ModFluids.MY_FLUID_FLOWING
            )
            .bucket(ModFluids.MY_FLUID_BUCKET)
            .block(ModBlocks.MY_FLUID_BLOCK);
}
```

### 1.4 FluidType 鈥?娴佷綋绫诲瀷灞炴€?
```java
import net.minecraftforge.fluids.FluidType;

public class MyFluidType extends FluidType {
    public MyFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL =
                    new ResourceLocation(MOD_ID, "block/my_fluid_still");
            private static final ResourceLocation FLOW =
                    new ResourceLocation(MOD_ID, "block/my_fluid_flow");

            @Override
            public ResourceLocation getStillTexture() { return STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return FLOW; }
            @Override
            public int getTintColor() { return 0xFF88FF88; }
        });
    }
}

// 娉ㄥ唽
public static final RegistryObject<FluidType> MY_TYPE =
        FLUID_TYPES.register("my_fluid",
                () -> new MyFluidType(FluidType.Properties.create()
                        .viscosity(2000)
                        .density(1500)
                        .temperature(400)
                        .canSwim(false)
                        .canDrown(true)
                        .lightLevel(8)
                ));
```

### 1.5 GoetyMech 閫氱敤娴佷綋妗嗘灦

GoetyMech 椤圭洰涓嶄緷璧?Registrate锛屾墍鏈夋祦浣撻€氳繃 `DeferredRegister` + 鑷畾涔夋鏋剁粺涓€绠＄悊銆傛鏋舵彁渚涘彲閫夌殑璁℃椂/娑堥€€/绮掑瓙/鎺ヨЕ鏁堟灉绛夊唴缃涓猴紝鏂版祦浣撳彧闇€鍐欐渶灏戠殑浠ｇ爜銆?
**妗嗘灦鏂囦欢璺緞**锛歚src/main/java/com/GoetyMech/addon/common/fluid/frame/`

| 绫?| 瑙掕壊 | 蹇呴€夛紵 |
|----|------|--------|
| `AbstractEphemeralFluid` | 娴佷綋鍩虹被锛堢户鎵?`FlowingFluid`锛?| **蹇呴』** |
| `AbstractEphemeralFluidType` | FluidType 鍩虹被锛堝鐢ㄥ師鐗堟按绾圭悊锛?| 鍙€夛紙涔熷彲鐩存帴缁ф壙 `FluidType`锛?|
| `EphemeralFluidConfig` | 閰嶇疆 Builder锛堢矑瀛?鎺ヨЕ鏁堟灉锛?| 鍙彉閫氾紙鐩存帴浼犲弬鏁颁篃琛岋級 |
| `EphemeralLiquidBlock` | 鍙鐢?Block锛堣鏃?娑堥€€/绮掑瓙/鎺ヨЕ锛?| **蹇呴』** |

瀹屾暣鐨勬敞鍐屽叆鍙ｅ湪 `ModFluids.java`锛屾枃浠讹細`src/main/java/com/GoetyMech/addon/init/ModFluids.java`

### 1.6 妗嗘灦绫昏瑙?
#### AbstractEphemeralFluid

```java
public abstract class AbstractEphemeralFluid extends FlowingFluid {
    // 瀛愮被蹇呴』瀹炵幇锛?    public abstract int getDecayRate();
    protected abstract Supplier<? extends LiquidBlock> getFluidBlock();

    // 宸叉湁榛樿瀹炵幇锛岄€氬父涓嶉渶瑕佽鍐欙細
    // isSame, getSlopeFindDistance(4), getDropOff(1), getTickDelay(5),
    // getExplosionResistance(100), canBeReplacedWith, beforeDestroyingBlock,
    // canConvertToSource(false), createLegacyBlock
}
```

**Source/Flowing 鍐呴儴绫?*锛堝繀椤诲啓鍦ㄥ叿浣撴祦浣撶被涓紝鑰岄潪妗嗘灦灞傦級锛?
```java
// 鍏蜂綋娴佷綋绫诲唴閮?public static class Source extends SoulFluid {
    @Override public int getAmount(FluidState s) { return 8; }
    @Override public boolean isSource(FluidState s) { return true; }
}

public static class Flowing extends SoulFluid {
    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> b) {
        super.createFluidStateDefinition(b);
        b.add(LEVEL);        // 鈫?娴佸姩鍙樹綋蹇呴』娉ㄥ唽 LEVEL
    }
    @Override public int getAmount(FluidState s) { return s.getValue(LEVEL); }
    @Override public boolean isSource(FluidState s) { return false; }
}
```

**娉ㄦ剰**锛歚getFluidType()` 蹇呴』澹版槑涓?`public`锛堝洜涓?`IForgeFluid` 鎺ュ彛涓鏂规硶涓?`public`锛夈€?
#### AbstractEphemeralFluidType

澶嶇敤鍘熺増姘寸殑 still/flow 绾圭悊锛岃鍐?`getFluidTintColor()` 杩斿洖 ARGB 鐫€鑹插€煎嵆鍙娇鐢ㄨ嚜瀹氫箟棰滆壊銆?
```java
public class SoulFluidType extends AbstractEphemeralFluidType {
    private static final int TINT_COLOR = 0xCC7CFCFF;
    @Override protected int getFluidTintColor() { return TINT_COLOR; }
}
```

#### EphemeralFluidConfig

Builder 妯″紡锛屽彲閫夐摼寮忛厤缃細

```java
EphemeralFluidConfig.builder()
    .particle(ParticleTypes.SCULK_SOUL)           // 绮掑瓙绫诲瀷锛坣ull = 鏃狅級
    .particleProbability(0.025f)                   // 姣?tick 绮掑瓙姒傜巼
    .contactEffect(player -> SEHelper.increaseSouls(player, 50))  // 鎺ヨЕ鏁堟灉
    .build();
```

#### EphemeralLiquidBlock

**鍏抽敭鏂规硶瑕嗗啓绾﹀畾**锛堥伩鍏嶈俯鍧戯級锛?
| 鏂规硶 | 绾﹀畾 |
|------|------|
| `onPlace` | **蹇呴』璋冨害娴佷綋 spread tick**锛歚level.scheduleTick(pos, fluid.get(), fluid.get().getTickDelay(level))`锛岀劧鍚庡啀璋冨害鑷畾涔夎鏃跺櫒 |
| `tick` | 鏂规硶鍚嶆槸 `tick(BlockState, ServerLevel, BlockPos, RandomSource)` **涓嶆槸** `scheduledTick` |
| `neighborChanged` | **蹇呴』閲嶆柊璋冨害 fluid spread tick**锛屽惁鍒欓偦灞呭彉鍖栧悗娴佷綋涓嶆祦鍔?|
| `entityInside` | 棰戠巼鎺у埗鍙敤 `level.getGameTime() % N`锛?*绂佹鍙犵敤** `player.tickCount % N`锛堜袱涓鏁板櫒鍋忕Щ涓嶅悓姝ヤ細瀵艰嚧鏉′欢姘歌繙涓嶆垚绔嬶級 |
| `animateTick` | 瀹㈡埛绔晶锛屾按婧愭柟鍧楅殢鏈虹敓鎴愮矑瀛?|

鏋勯€犲弬鏁帮細

```java
new EphemeralLiquidBlock(
    () -> (FlowingFluid) SOUL_LIQUID_SOURCE.get(),  // fluid Supplier
    BlockBehaviour.Properties.of()
        .noCollission()
        .randomTicks()
        .strength(100.0F)
        .noLootTable(),                              // 鏂瑰潡灞炴€?    config,                                          // EphemeralFluidConfig
    timerDuration,                                   // 姘存簮璁℃椂锛坱ick锛?    decayRate                                        // 娑堥€€閫熷害锛堟瘡 tick 闄?N 绾э級
);
```

### 1.7 娣诲姞鏂版祦浣撶殑 Step-by-Step 妯℃澘

浠ヤ笅浠ユ坊鍔犱竴涓悕涓?`magic_fluid` 鐨勬祦浣撲负渚嬨€?
#### 鈶?FluidType 绫?
```java
package com.GoetyMech.addon.common.fluid.magic;

import com.GoetyMech.addon.common.fluid.frame.AbstractEphemeralFluidType;

public class MagicFluidType extends AbstractEphemeralFluidType {
    private static final int TINT_COLOR = 0xFFFF00FF;
    @Override protected int getFluidTintColor() { return TINT_COLOR; }
}
```

#### 鈶?Fluid 绫?
```java
package com.GoetyMech.addon.common.fluid.magic;

import com.GoetyMech.addon.common.fluid.frame.AbstractEphemeralFluid;
import com.GoetyMech.addon.init.ModFluids;

public abstract class MagicFluid extends AbstractEphemeralFluid {

    @Override public int getDecayRate() { return 1; }  // 鏍囧噯娑堥€€閫熷害

    @Override public Fluid getFlowing() { return ModFluids.MAGIC_FLUID_FLOWING.get(); }
    @Override public Fluid getSource()  { return ModFluids.MAGIC_FLUID_SOURCE.get(); }
    @Override public Item getBucket()   { return ModFluids.MAGIC_FLUID_BUCKET.get(); }
    @Override protected Supplier<? extends LiquidBlock> getFluidBlock() { return ModFluids.MAGIC_FLUID_BLOCK; }
    @Override public FluidType getFluidType() { return ModFluids.MAGIC_FLUID_TYPE.get(); }

    public static class Source extends MagicFluid {
        @Override public int getAmount(FluidState s) { return 8; }
        @Override public boolean isSource(FluidState s) { return true; }
    }

    public static class Flowing extends MagicFluid {
        @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> b) {
            super.createFluidStateDefinition(b); b.add(LEVEL);
        }
        @Override public int getAmount(FluidState s) { return s.getValue(LEVEL); }
        @Override public boolean isSource(FluidState s) { return false; }
    }
}
```

#### 鈶?娉ㄥ唽锛圡odFluids.java锛?
鍙傜収 `SOUL_LIQUID_*` 鐨勬ā寮忥紝鍦ㄦ瘡涓?`DeferredRegister` 涓敞鍐岋細

```java
// FluidType
public static final RegistryObject<FluidType> MAGIC_FLUID_TYPE =
        FLUID_TYPES.register("magic_fluid", MagicFluidType::new);

// Fluids
public static final RegistryObject<FlowingFluid> MAGIC_FLUID_SOURCE =
        FLUIDS.register("magic_fluid", () -> new MagicFluid.Source());
public static final RegistryObject<FlowingFluid> MAGIC_FLUID_FLOWING =
        FLUIDS.register("magic_fluid_flowing", () -> new MagicFluid.Flowing());

// Block
private static final EphemeralFluidConfig MAGIC_FLUID_CONFIG = EphemeralFluidConfig.builder()
        .particle(ParticleTypes.WITCH)
        .particleProbability(0.03f)
        .build();

public static final RegistryObject<EphemeralLiquidBlock> MAGIC_FLUID_BLOCK =
        BLOCKS.register("magic_fluid", () -> new EphemeralLiquidBlock(
                () -> (FlowingFluid) MAGIC_FLUID_SOURCE.get(),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_MAGENTA)
                        .noCollission().randomTicks().strength(100.0F).noLootTable(),
                MAGIC_FLUID_CONFIG,
                100,  // timerDuration锛堜笉鍚敤璁℃椂璁句负 Integer.MAX_VALUE 鎴栫渷鐣ユ瀯閫狅級
                1     // decayRate
        ));

// Bucket
public static final RegistryObject<BucketItem> MAGIC_FLUID_BUCKET =
        ITEMS.register("magic_fluid_bucket", () -> new BucketItem(
                MAGIC_FLUID_SOURCE,
                new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
        ));
```

#### 鈶?鍔犲叆鍒涢€犵墿鍝佹爮

鍦?`ModCreativeTab.java` 鐨?`displayItems` 涓坊鍔狅細
```java
output.accept(ModFluids.MAGIC_FLUID_BUCKET.get());
```

#### 鈶?Client 娓叉煋娉ㄥ唽

鍦?`ModClientEvents.java` 涓坊鍔狅細
```java
ItemBlockRenderTypes.setRenderLayer(ModFluids.MAGIC_FLUID_SOURCE.get(), RenderType.translucent());
ItemBlockRenderTypes.setRenderLayer(ModFluids.MAGIC_FLUID_FLOWING.get(), RenderType.translucent());
```

#### 鈶?Bucket Item Model

`assets/GoetyMech/models/item/magic_fluid_bucket.json`锛?```json
{
  "loader": "forge:item/bucket_drip",
  "parent": "forge:item/bucket_drip",
  "fluid": "GoetyMech:magic_fluid"
}
```

#### 鈶?Lang

`assets/GoetyMech/lang/en_us.json`锛?```json
{
  "fluid.GoetyMech.magic_fluid": "Magic Fluid",
  "fluid.GoetyMech.magic_fluid_flowing": "Magic Fluid",
  "item.GoetyMech.magic_fluid_bucket": "Magic Fluid Bucket"
}
```

---

## 2. Create 娴佷綋鍩虹璁炬柦

### 2.1 SmartFluidTank

`com.simibubi.create.foundation.fluid.SmartFluidTank`

缁ф壙 `net.minecraftforge.fluids.capability.templates.FluidTank`锛屾坊鍔犲唴瀹瑰彉鏇村洖璋冦€?
```java
import com.simibubi.create.foundation.fluid.SmartFluidTank;

// 鏋勯€?SmartFluidTank tank = new SmartFluidTank(capacity, fluidStack -> {
    // 娴佷綋鍙樺寲鏃剁殑鍥炶皟
    be.sendData();
    be.setChanged();
});

// 缁ф壙鑷?FluidTank 鐨勬柟娉?tank.getFluid();           // -> FluidStack
tank.getCapacity();        // -> int
tank.getFluidAmount();     // -> int
tank.isEmpty();
tank.fill(stack, action);  // -> int
tank.drain(max, action);   // -> FluidStack
tank.setFluid(stack);      // 寮哄埗璁惧€硷紙瑙﹀彂鍥炶皟锛?tank.writeToNBT(tag);
tank.readFromNBT(tag);
```

### 2.2 SmartFluidTankBehaviour 鈥?瀹屾暣绀轰緥锛團ULL锛?
`com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour`

涓?`SmartBlockEntity` 闄勫姞娴佷綋鍌ㄧ綈鑳藉姏銆傝嚜鍔ㄥ鐞?NBT 璇诲啓銆佸鎴风鍚屾銆乣LerpedFloat` 骞虫粦鍔ㄧ敾銆?
```java
// ===== 宸ュ巶鏂规硶 =====

// 鍗曠綈锛堟渶甯哥敤锛?SmartFluidTankBehaviour.single(be, capacity);

// 澶氱綈鏋勯€?new SmartFluidTankBehaviour(
    SmartFluidTankBehaviour.TYPE,  // 琛屼负绫诲瀷
    be,                            // SmartBlockEntity
    tankCount,                     // 缃愭暟
    tankCapacity,                  // 姣忕綈瀹归噺
    enforceVariety                 // 鏄惁绂佹鍚岀綈瑁呭娴佷綋
);

// ===== 閾惧紡閰嶇疆 =====
behaviour
    .allowInsertion()              // 鍏佽娉ㄥ叆锛堥粯璁?true锛?    .allowExtraction()             // 鍏佽鎶藉彇锛堥粯璁?true锛?    .forbidInsertion()
    .forbidExtraction()
    .whenFluidUpdates(() -> {      // 娴佷綋鍙樻洿鏃跺洖璋?        // 渚嬪鏇存柊鏂瑰潡鐘舵€?    });

// ===== 鍏抽敭鏂规硶 =====
behaviour.getPrimaryHandler();        // -> SmartFluidTank锛堜富缃愶紝绱㈠紩 0锛?behaviour.getPrimaryTank();           // -> TankSegment锛堜富缃愭锛?behaviour.getTanks();                 // -> TankSegment[]
behaviour.isEmpty();                  // -> boolean
behaviour.getCapability();            // -> LazyOptional<? extends IFluidHandler>
behaviour.sendDataImmediately();      // 绔嬪嵆鍚屾
behaviour.sendDataLazily();           // 寤惰繜鍚屾锛堟渶澶氭瘡 8tick 涓€娆★級

// ===== TankSegment =====
// 姣忎釜 TankSegment 鎸佹湁涓€涓?SmartFluidTank + LerpedFloat
tankSegment.tank;                  // -> SmartFluidTank
tankSegment.getFluidLevel();       // -> LerpedFloat锛?~1锛屽鎴风骞虫粦鍔ㄧ敾锛?tankSegment.getRenderedFluid();    // -> FluidStack锛堝鎴风娓叉煋鐢級
tankSegment.getTotalUnits(partialTicks); // -> float 褰撳墠娓叉煋鍗曚綅鏁?
// ===== InternalFluidHandler =====
// 閫氳繃 behaviour.getCapability() 鑾峰彇鐨勬槸 InternalFluidHandler
// 鍐呴儴瀹炵幇涓?CombinedTankWrapper锛屾敞鍏?鎶藉彇鍙?insertionAllowed/extractionAllowed 鎺у埗
// forceFill(resource, action) 鍙粫杩囨彃鍏ラ檺鍒?```

#### 瀹屾暣 BE 绀轰緥

```java
// 鈹€鈹€ 鏂瑰潡 鈹€鈹€
public class MyFluidTankBlock extends Block implements EntityBlock {
    public MyFluidTankBlock(Properties props) { super(props); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MyFluidTankBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) { return state; }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) { return state; }
}

// 鈹€鈹€ BE锛堟牳蹇冿級 鈹€鈹€
public class MyFluidTankBlockEntity extends SmartBlockEntity {
    private static final int TANK_CAPACITY = 16000;

    protected SmartFluidTankBehaviour fluidBehaviour;

    public MyFluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        fluidBehaviour = SmartFluidTankBehaviour.single(this, TANK_CAPACITY);
        fluidBehaviour.whenFluidUpdates(this::onFluidChanged);
        behaviours.add(fluidBehaviour);
    }

    private void onFluidChanged() {
        SyncBehaviourUtil.sendData(this);
    }

    public SmartFluidTankBehaviour getFluidBehaviour() {
        return fluidBehaviour;
    }

    // ===== 鐗╁搧浜や簰锛堟《/鐡惰濉?娓呯┖锛?=====
    public InteractionResult onWrenched(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, held, this))
            return InteractionResult.SUCCESS;
        if (FluidHelper.tryFillItemFromBE(level, player, hand, held, this))
            return InteractionResult.SUCCESS;
        return InteractionResult.PASS;
    }

    // ===== 绠￠亾浜や簰锛氶€氳繃 capability 鏆撮湶 =====
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluidBehaviour.getCapability().cast();
        return super.getCapability(cap, side);
    }

    // ===== 鍙抽敭鐩存帴浜や簰 =====
    public InteractionResult onUse(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown())
            return InteractionResult.PASS;
        return onWrenched(player, hand);
    }
}

// 鈹€鈹€ 娉ㄥ唽 鈹€鈹€
// Block
public static final BlockEntry<MyFluidTankBlock> MY_FLUID_TANK = REGISTRATE
        .block("my_fluid_tank", MyFluidTankBlock::new)
        .transform(BuilderTransformers.machine())
        .register();

// BlockEntity
public static final BlockEntityEntry<MyFluidTankBlockEntity> MY_FLUID_TANK_BE = REGISTRATE
        .blockEntity("my_fluid_tank", MyFluidTankBlockEntity::new)
        .validBlocks(ModBlocks.MY_FLUID_TANK)
        .register();
```

### 2.3 CombinedTankWrapper

`com.simibubi.create.foundation.fluid.CombinedTankWrapper`

灏嗗涓?`IFluidHandler` 缁勫悎涓哄崟涓帴鍙ｃ€俙fill()` 绛栫暐锛氫紭鍏堝～鍏呭凡鏈夊悓绉嶆祦浣撶殑缃愶紝鍐嶅～绌虹綈銆俙enforceVariety()` 妯″紡涓嬫瘡缃愬彧鑳藉瓨涓嶅悓娴佷綋銆?
```java
IFluidHandler[] handlers = { tank1, tank2, tank3 };
CombinedTankWrapper combined = new CombinedTankWrapper(handlers);
combined.enforceVariety();

combined.fill(resource, action);       // 杩斿洖瀹為檯娉ㄥ叆閲?combined.drain(max, action);           // 鎸夐噺鎶藉彇
combined.drain(resource, action);      // 鎸夌被鍨嬫娊鍙?combined.getTanks();                   // 鎬荤綈鏁?combined.getFluidInTank(index);        // 鏌愮綈娴佷綋
```

`SmartFluidTankBehaviour` 鍐呴儴浣跨敤 `InternalFluidHandler extends CombinedTankWrapper`銆?
### 2.4 FluidHelper 鈥?宸ュ叿鏂规硶

`com.simibubi.create.foundation.fluid.FluidHelper`

```java
FluidHelper.isWater(fluid);                     // 鏄惁鏄按
FluidHelper.isLava(fluid);                      // 鏄惁鏄博娴?FluidHelper.isSame(stackA, stackB);             // 绫诲瀷鐩哥瓑
FluidHelper.isSame(stack, fluid);               // 鏍堜笌绫诲瀷
FluidHelper.isTag(fluid, tag);                  // 娴佷綋鏍囩妫€娴?FluidHelper.isTag(stack, tag);
FluidHelper.isTag(state, tag);
FluidHelper.hasBlockState(fluid);               // 鏄惁鏈夊搴旀柟鍧楃姸鎬?FluidHelper.convertToStill(fluid);              // 闈欐鐗堟湰
FluidHelper.convertToFlowing(fluid);            // 娴佸姩鐗堟湰
FluidHelper.copyStackWithAmount(stack, amount); // 澶嶅埗骞舵敼閲?FluidHelper.getFillSound(stack);                // 鐏屽叆闊虫晥
FluidHelper.getEmptySound(stack);               // 鍊掑嚭闊虫晥
FluidHelper.serializeFluidStack(stack);         // -> JsonElement
FluidHelper.deserializeFluidStack(json);        // -> FluidStack

// 鐗╁搧 鈫?鍌ㄧ綈浜や簰
FluidHelper.tryEmptyItemIntoBE(world, player, hand, heldItem, be);  // 鐗╁搧鈫掔綈
FluidHelper.tryFillItemFromBE(world, player, hand, heldItem, be);   // 缃愨啋鐗╁搧

// 娴佷綋浜ゆ崲锛堣嚜鍔ㄩ€夋嫨鏂瑰悜锛?FluidHelper.exchange(tank, itemTank, preferred, maxAmount);
FluidHelper.exchangeAll(tank, itemTank, preferred);
```

---

## 3. Create 绠￠亾涓庝紶杈?
### 3.1 FluidTransportBehaviour

`com.simibubi.create.content.fluids.FluidTransportBehaviour`

绠￠亾娴佷綋浼犺緭鐨勬牳蹇冭涓恒€傞檮鐫€浜?`SmartBlockEntity`锛岀鐞?`Map<Direction, PipeConnection>`銆?
```java
public abstract class FluidTransportBehaviour extends BlockEntityBehaviour {
    // 涓夐樁娈垫洿鏂?    enum UpdatePhase { WAIT_FOR_PUMPS, FLIP_FLOWS, IDLE }

    Map<Direction, PipeConnection> interfaces;  // 鍚勬柟鍚戠閬撹繛鎺?    UpdatePhase phase;

    // 瀛愮被蹇呴』瀹炵幇
    public abstract boolean canHaveFlowToward(BlockState state, Direction direction);

    // 鍙€夎鍐?    public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
        return true;
    }
}
```

### 3.2 PipeConnection 鈥?绠￠亾杩炴帴

```java
import com.simibubi.create.content.fluids.PipeConnection;

// 姣忎釜鏂瑰悜涓€涓?PipeConnection
connection.getFluid();               // -> FluidStack锛堢閬撳唴娴佷綋锛?connection.getFlow();                // -> LerpedFloat锛?~1 娴侀€熷姩鐢伙級
connection.getPressure();            // -> LerpedFloat锛堝帇鍔涘姩鐢伙級
connection.serialize();
connection.deserialize(nbt);
```

### 3.3 FluidNetwork 鈥?娴佷綋缃戠粶

`com.simibubi.create.content.fluids.FluidNetwork`

- 姣?tick 鏈€澶?**16 涓惊鐜?* (`CYCLES_PER_TICK = 16`)
- 浠?`BlockFace` 婧愬紑濮?BFS锛屾部绠￠亾浼犳挱娴佷綋
- `FluidPropagator.propagateChangedPipe(world, pos, state)` 鈥?绠￠亾鍙樺寲鍚庨噸鏂拌绠楃綉缁?
### 3.4 鏈烘娉甸泦鎴?
```java
// PumpBlockEntity 缁ф壙 KineticBlockEntity
// addBehaviours 涓坊鍔?PumpFluidTransferBehaviour
public class MyPumpBlockEntity extends KineticBlockEntity {
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new PumpFluidTransferBehaviour(this));
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        // 閫熷害鍙樺寲鏃舵洿鏂版祦浣撳帇鍔?    }
}
```

### 3.5 闆嗘垚鎸囧崡

**瑕佸皢鑷畾涔夋柟鍧楁帴鍏?Create 绠￠亾缃戠粶**锛?
1. BE 缁ф壙 `SmartBlockEntity`
2. 鍦?`addBehaviours()` 涓坊鍔犱竴涓户鎵?`FluidTransportBehaviour` 鐨勮嚜瀹氫箟琛屼负锛堥櫎闈炰綘鏄仛绠￠亾鏂瑰潡鏈韩锛岄€氬父涓嶉渶瑕佺洿鎺ユ搷浣滅閬擄級
3. 閫氳繃 `ForgeCapabilities.FLUID_HANDLER` 鏆撮湶 `SmartFluidTankBehaviour` 鐨?capability
4. 绠￠亾鑷姩鍙戠幇鍛ㄥ洿鐨?`IFluidHandler` 骞朵笌涔嬩氦浜?
```java
// 鏍囧噯鍋氭硶锛氬偍缃?BE 鍙渶鏆撮湶 capability
@Override
public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER)
        return fluidBehaviour.getCapability().cast();
    return super.getCapability(cap, side);
}
```

---

## 4. Create 娴佷綋閰嶆柟

### 4.1 FluidIngredient

`com.simibubi.create.foundation.fluid.FluidIngredient`

缁ф壙 `Predicate<FluidStack>`锛屾敮鎸?JSON 搴忓垪鍖?鍙嶅簭鍒楀寲銆?
```java
import com.simibubi.create.foundation.fluid.FluidIngredient;

// 宸ュ巶鏂规硶
FluidIngredient.fromFluid(fluid, amount);             // 鎸夌壒瀹氭祦浣?FluidIngredient.fromFluidStack(fluidStack);           // 鎸夋祦浣撴爤锛堝惈 NBT锛?FluidIngredient.fromTag(fluidTag, amount);            // 鎸?Tag

// 鏂规硶
ingredient.test(fluidStack);                          // -> boolean
ingredient.getRequiredAmount();                       // -> int
ingredient.getMatchingFluidStacks();                  // -> List<FluidStack>
ingredient.serialize();                               // -> JsonObject

// 鍙嶅簭鍒楀寲
FluidIngredient.deserialize(jsonElement);             // -> FluidIngredient

// JSON 鏍煎紡锛?// 鎸夋祦浣?{ "fluid": "minecraft:water", "amount": 1000 }
// 鎸?tag
{ "fluidTag": "minecraft:water", "amount": 500 }
// 甯?NBT
{ "fluid": "mod:custom_fluid", "amount": 250, "nbt": "{...}" }
```

### 4.2 ProcessingRecipe + 娴佷綋 鈥?瀹屾暣绀轰緥锛團ULL锛?
```java
// 鈹€鈹€ 1. 閰嶆柟绫?鈹€鈹€
public class MyProcessingRecipe extends ProcessingRecipe<MyProcessingRecipe> {

    public MyProcessingRecipe(ProcessingRecipeParams params) {
        super(MyRecipeTypes.MY_TYPE, params);
    }

    @Override protected int getMaxInputCount()       { return 1; }
    @Override protected int getMaxOutputCount()      { return 2; }
    @Override protected int getMaxFluidInputCount()  { return 1; }
    @Override protected int getMaxFluidOutputCount() { return 1; }
}

// 鈹€鈹€ 2. Serializer 鈹€鈹€
public static final ProcessingRecipeSerializer<MyProcessingRecipe> SERIALIZER =
        new ProcessingRecipeSerializer<>(MyProcessingRecipe::new);

public static final RecipeType<MyProcessingRecipe> TYPE =
        RecipeType.simple(new ResourceLocation(MOD_ID, "my_processing"));

// 鈹€鈹€ 3. 绫诲瀷淇℃伅鏋氫妇 鈹€鈹€
public enum MyRecipeTypes implements IRecipeTypeInfo {
    MY_TYPE {
        @Override public RecipeSerializer<?> getSerializer() { return MyProcessingRecipe.SERIALIZER; }
        @Override public RecipeType<?> getType()            { return MyProcessingRecipe.TYPE; }
        @Override public ResourceLocation getId()           { return MyProcessingRecipe.TYPE.toString(); }
        @Override public Supplier<RecipeSerializer<?>> getSerializerSupplier()     { return () -> SERIALIZER; }
        @Override public Supplier<RecipeType<?>> getTypeSupplier()                 { return () -> TYPE; }
    }
}

// 鈹€鈹€ 4. 閰嶆柟鐢熸垚锛圖ataGen锛?鈹€鈹€
public class MyProcessingRecipeGen extends ProcessingRecipeGen {
    public MyProcessingRecipeGen(PackOutput output) {
        super(output, MOD_ID);
    }

    GeneratedRecipe MY_FLUID_RECIPE = create("my_fluid_recipe", b -> b
            .require(Items.DIRT)
            .require(FluidIngredient.fromFluid(ModFluids.MY_FLUID.getSource(), 500))
            .output(Items.GRASS_BLOCK)
            .output(ModFluids.MY_FLUID.getSource(), 250)
            .duration(100)
    );

    GeneratedRecipe MY_TAG_RECIPE = create("my_tag_recipe", b -> b
            .require(Items.COBBLESTONE)
            .require(FluidTags.WATER, 1000)
            .output(Items.STONE)
    );

    @Override
    protected IRecipeTypeInfo getRecipeType() { return MyRecipeTypes.MY_TYPE; }

    @Override
    public String getName() { return "My Processing Recipes"; }
}

// 鈹€鈹€ 5. ProcessingRecipeBuilder 鐨勬祦浣撶浉鍏冲揩鎹锋柟娉?鈹€鈹€
// require(Fluid fluid, int amount)         => FluidIngredient.fromFluid
// require(TagKey<Fluid> fluidTag, int amount) => FluidIngredient.fromTag
// require(FluidIngredient ingredient)
// output(Fluid fluid, int amount)           => 鑷姩 convertToStill
// output(FluidStack fluidStack)
```

### 4.3 FillingRecipe / EmptyingRecipe

Create 鍐呯疆鐨勬祦浣撳～鍏?娓呯┖閰嶆柟绫诲瀷锛屽垎鍒搴斿柗澶达紙Spout锛夊拰鎺掓按鍣紙Item Drain锛夈€?
```java
// FillingRecipe 鈥?鍠峰ご閰嶆柟锛堟祦浣?+ 鐗╁搧 鈫?鏂扮墿鍝侊級
// 娉ㄥ唽鑷畾涔夊～鍏呴厤鏂?public class MyFillingRecipeGen extends ProcessingRecipeGen {
    public MyFillingRecipeGen(PackOutput output) {
        super(output, MOD_ID);
    }

    GeneratedRecipe FILL = create("my_fill", b -> b
            .require(Items.GLASS_BOTTLE)
            .require(ModFluids.MY_FLUID.getSource(), 250)
            .output(Items.POTION) // 绀轰緥
            .duration(20)
    );

    @Override
    protected IRecipeTypeInfo getRecipeType() { return AllRecipeTypes.FILLING; }
}

// EmptyingRecipe 鈥?鎺掓按鍣ㄩ厤鏂癸紙鐗╁搧 鈫?娴佷綋 + 绌哄鍣級
```

**FillingRecipe 鍏抽敭鏂规硶**锛?```java
public class FillingRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {
    public FluidIngredient getRequiredFluid();  // 鑾峰彇鎵€闇€娴佷綋
}
```

---

## 5. API 鎵╁睍鐐?
### 5.1 BlockSpoutingBehaviour 鈥?瀹屾暣绀轰緥锛團ULL锛?
`com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour`

鍠峰ご锛圫pout锛夊悜涓嬫柟鏂瑰潡鍠锋簠娴佷綋鏃剁殑鑷畾涔夎涓烘帴鍙ｃ€?
```java
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.api.behaviour.spouting.StateChangingBehavior;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

// 鈹€鈹€ 娉ㄥ唽鏂瑰紡 鈹€鈹€
// BY_BLOCK锛氭寜鏂瑰潡娉ㄥ唽
BlockSpoutingBehaviour.BY_BLOCK.register(MyBlocks.MY_BLOCK.get(), myBehaviour);
// BY_BLOCK_ENTITY锛氭寜 BE 绫诲瀷娉ㄥ唽
BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(MyBlocks.MY_BE.get(), myBehaviour);

// 鈹€鈹€ 鎺ュ彛瀹氫箟 鈹€鈹€
@FunctionalInterface
public interface BlockSpoutingBehaviour {
    /**
     * 姣?tick 闈炴ā鎷熻皟鐢ㄣ€傝繑鍥炲疄闄呭～鍏呴噺锛坢b锛夛紝杩斿洖 0 琛ㄧず闂茬疆/鍙栨秷銆?     */
    int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout,
                  FluidStack availableFluid, boolean simulate);
}

// 鈹€鈹€ 瀹屾暣瀹炵幇绀轰緥 鈹€鈹€
// 鍋囪鑷畾涔夊厖鑳芥柟鍧楋紝娉ㄥ叆鍐嶇敓娴佷綋灏卞厖鑳?public class MyChargingSpoutingBehaviour implements BlockSpoutingBehaviour {
    private static final int FLUID_PER_CHARGE = 250;

    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout,
                         FluidStack availableFluid, boolean simulate) {
        // 1. 妫€鏌ユ祦浣撶被鍨?        if (!availableFluid.getFluity().isSame(ModFluids.RECHARGE_FLUID.get()))
            return 0;

        // 2. 鑾峰彇鏂瑰潡鐘舵€?        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MyChargingBlock))
            return 0;

        int currentCharge = state.getValue(MyChargingBlock.CHARGE);
        int maxCharge = MyChargingBlock.MAX_CHARGE;
        if (currentCharge >= maxCharge)
            return 0;

        // 3. 璁＄畻闇€瑕佸灏戞祦浣?        int needed = (maxCharge - currentCharge) * FLUID_PER_CHARGE;
        int toFill = Math.min(needed, availableFluid.getAmount());

        if (!simulate) {
            level.setBlockAndUpdate(pos,
                    state.setValue(MyChargingBlock.CHARGE,
                            currentCharge + toFill / FLUID_PER_CHARGE));
        }
        return toFill;
    }
}

// 鈹€鈹€ 娉ㄥ唽 鈹€鈹€
@SubscribeEvent
public static void onRegister() {
    // 鍦?mod 鏋勯€犳湡璋冪敤
    BlockSpoutingBehaviour.BY_BLOCK.register(
        ModBlocks.MY_CHARGING_BLOCK.get(),
        new MyChargingSpoutingBehaviour()
    );
}

// 鈹€鈹€ 鍐呯疆瀹炵幇鍙傝€?鈹€鈹€
// Create 鍐呯疆浜嗕互涓嬭涓猴細
// 1. StateChangingBehavior.setTo(amount, fluidPredicate, targetBlock)
//    鈫?婊¤冻鏉′欢鏃剁洿鎺ユ洿鎹㈡柟鍧?// 2. StateChangingBehavior.incrementingState(amount, fluidPredicate, property)
//    鈫?閫愮骇閫掑 IntegerProperty
// 3. CauldronSpoutingBehavior.INSTANCE
//    鈫?鐐艰嵂閿呭疄鐜?//
// 浣跨敤绀轰緥锛?BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.DIRT,
    StateChangingBehavior.setTo(250, isWater, Blocks.MUD));

BlockSpoutingBehaviour.BY_BLOCK.register(Blocks.FARMLAND,
    StateChangingBehavior.incrementingState(100, isWater, FarmBlock.MOISTURE));
```

### 5.2 OpenPipeEffectHandler

`com.simibubi.create.api.effect.OpenPipeEffectHandler`

绠￠亾寮€鍙ｇ鍚戜笘鐣屾帓鏀炬祦浣撴椂鐨勯檮鍔犳晥鏋溿€?
```java
import com.simibubi.create.api.effect.OpenPipeEffectHandler;

@FunctionalInterface
public interface OpenPipeEffectHandler {
    void apply(Level level, AABB area, FluidStack fluid);
}

// 鈹€鈹€ 娉ㄥ唽 鈹€鈹€
// 鎸?Fluid 娉ㄥ唽
OpenPipeEffectHandler.REGISTRY.register(ModFluids.MY_FLUID.getSource(), (level, area, fluid) -> {
    // 鍦?area 鑼冨洿鍐呭簲鐢ㄦ晥鏋?    level.getEntitiesOfClass(LivingEntity.class, area).forEach(entity -> {
        entity.addEffect(new MobEffectInstance(...));
    });
});

// 鎸?FluidTag 娉ㄥ唽
OpenPipeEffectHandler.REGISTRY.registerProvider(
    SimpleRegistry.Provider.forFluidTag(
        FluidTags.WATER,
        (level, area, fluid) -> {
            // 鏁堟灉閫昏緫
        }
    )
);

// 鈹€鈹€ Create 鍐呯疆娉ㄥ唽鏃舵満 鈹€鈹€
// 瑙?AllOpenPipeEffectHandlers.registerDefaults()
```

### 5.3 BoilerHeater

`com.simibubi.create.api.boiler.BoilerHeater`

閿呯倝鍔犵儹鍣ㄦ帴鍙ｏ紝瀹炵幇鍚庢敞鍐屽埌 `BoilerHeater.REGISTRY`銆?
```java
import com.simibubi.create.api.boiler.BoilerHeater;

@FunctionalInterface
public interface BoilerHeater {
    int PASSIVE_HEAT = 0;    // 琚姩鍔犵儹锛堟爣璁帮級
    int NO_HEAT = -1;        // 鏃犲姞鐑?
    float getHeat(Level level, BlockPos pos, BlockState state);
}

// 鈿狅笍 娉ㄥ唽鏂瑰紡涓嶅悓锛氫笉鏄?SimpleRegistry锛岃€屾槸鐩存帴 registry
// 瑙?AllBoilerHeaters.register()
// 閫氬父鍦ㄦ瀯閫犳湡璋冪敤
```

### 5.4 PipeCollisionEvent

`com.simibubi.create.api.event.PipeCollisionEvent`

绠￠亾鍐呮祦浣撶鎾炰簨浠讹紝鍙敤浜庤嚜瀹氫箟娴佷綋鍙嶅簲銆?
```java
import com.simibubi.create.api.event.PipeCollisionEvent;

// 鈹€鈹€ 涓ょ纰版挒绫诲瀷 鈹€鈹€
// 1. 绠￠亾鍐呮祦浣撶浉閬?(Flow)
PipeCollisionEvent.Flow event = new PipeCollisionEvent.Flow(
    level, pos, fluid1, fluid2, null);
// 2. 绠￠亾娴佷綋娉勬紡鍒颁笘鐣屾祦浣?(Spill)
PipeCollisionEvent.Spill event = new PipeCollisionEvent.Spill(
    level, pos, worldFluid, pipeFluid, null);

// 鈹€鈹€ 鐢ㄦ硶锛氳闃呬簨浠讹紙闇€瑕佸湪 mod 鏋勯€犳湡娉ㄥ唽浜嬩欢鎬荤嚎锛?鈹€鈹€
@SubscribeEvent(priority = EventPriority.HIGH)
public static void onPipeCollision(PipeCollisionEvent.Flow event) {
    Fluid p1 = event.getFirstFluid();
    Fluid p2 = event.getSecondFluid();

    if (p1 == ModFluids.ACID.get() && p2 == ModFluids.BASE.get()) {
        event.setState(Blocks.OBSIDIAN.defaultBlockState());
    }
}

@SubscribeEvent(priority = EventPriority.HIGH)
public static void onPipeSpill(PipeCollisionEvent.Spill event) {
    if (event.getPipeFluid() == ModFluids.ACID.get()
            && event.getWorldFluid() == Fluids.WATER) {
        event.setState(Blocks.COBBLESTONE.defaultBlockState());
    }
}
```

---

## 6. DataGen

### 6.1 娴佷綋 Tags

```java
// Create 鎻愪緵鐨勬祦浣?Tags
import com.simibubi.create.AllTags;

// create:bottomless/allow 鈥?鍏佽鏃犻檺鎶藉彇锛堣蒋绠℃粦杞級
// create:bottomless/deny  鈥?绂佹鏃犻檺鎶藉彇

// 閫氳繃 CreateRegistrate 娣诲姞
public static final FluidEntry<MyFluid> MY_FLUID = REGISTRATE
        .fluid("my_fluid", MyFluid.Source::new, MyFluid.Flowing::new)
        .attributes(b -> b.viscosity(2000))
        .properties(p -> p.levelDecreasePerBlock(2))
        .tag(AllTags.forgeFluidTag("my_fluids"))
        .tag(AllTags.commonFluidTag("create:bottomless/allow"))
        .register();
```

### 6.2 閰嶆柟 JSON 缁撴瀯

```json
{
  "type": "GoetyMech:my_processing",
  "ingredients": [
    { "item": "minecraft:dirt" }
  ],
  "fluidIngredients": [
    { "fluid": "minecraft:water", "amount": 500 }
  ],
  "results": [
    { "item": "minecraft:grass_block" }
  ],
  "fluidResults": [
    { "fluid": "GoetyMech:my_fluid", "amount": 250 }
  ],
  "processingDuration": 100
}
```

### 6.3 閰嶆柟鐢熸垚閫熸煡

```java
// ProcessingRecipeGen 娴佷綋鐩稿叧蹇嵎鏂规硶锛堝潎鍦?ProcessingRecipeBuilder 涓級锛?b.require(FluidIngredient.fromFluid(fluid, amount));   // 鏂规硶鈶狅細FluidIngredient 瀵硅薄
b.require(FluidIngredient.fromTag(tag, amount));       // 鏂规硶鈶★細FluidIngredient 瀵硅薄
b.require(fluid, amount);                               // 鏂规硶鈶細鐩存帴浼?Fluid
b.require(tag, amount);                                 // 鏂规硶鈶ｏ細鐩存帴浼?TagKey<Fluid>
b.output(fluid, amount);                                // 娴佷綋杈撳嚭锛堣嚜鍔?convertToStill锛?b.output(fluidStack);                                   // 娴佷綋杈撳嚭锛團luidStack锛?b.withFluidIngredients(ingredients...);                 // 鎵归噺璁?b.withFluidOutputs(outputs...);                         // 鎵归噺璁?```

---

## 7. Ponder 鍦烘櫙

娴佷綋鏈哄櫒 Ponder 鏍囧噯妯℃澘锛?
```java
public class MyFluidPonderScenes {
    public static void tankScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("my_fluid_tank", "浣跨敤娴佷綋鍌ㄧ綈");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);

        // 鏀剧疆鏂瑰潡
        BlockPos tankPos = util.grid.at(2, 1, 2);
        scene.world.setBlock(tankPos, ModBlocks.MY_FLUID_TANK.getDefaultState(), false);
        scene.idle(10);

        // 鐢ㄥ柗澶存紨绀烘敞鍏?        BlockPos spoutPos = util.grid.at(2, 3, 2);
        scene.world.setBlock(spoutPos, AllBlocks.SPOUT.getDefaultState(), true);
        scene.idle(5);

        // 娣诲姞娴佷綋鍒板偍缃愶紙妯℃嫙锛?        scene.world.modifyBlockEntityNBT(tankPos, nbt -> {
            // 閫氳繃 NBT 璁剧疆娴佷綋
        });
        scene.idle(10);

        scene.overlay.showText(60)
                .text("娴佷綋鍌ㄧ綈鍙互鍌ㄥ瓨鏈€澶?16000mb 娴佷綋")
                .pointAt(util.vector.topOf(tankPos));
        scene.idle(70);

        // 鏄剧ず绠￠亾杩炴帴
        BlockPos pipePos = util.grid.at(2, 1, 3);
        scene.world.setBlock(pipePos, AllBlocks.FLUID_PIPE.getDefaultState(), true);

        scene.overlay.showText(60)
                .text("鍙帴鍏?Create 绠￠亾缃戠粶")
                .pointAt(util.vector.block(2, 1, 3));
        scene.idle(70);

        scene.markAsFinished();
    }
}
```

娉ㄥ唽 Ponder锛?
```java
public class MyPonderIndex {
    public static final PonderChapter CHAPTER =
            PonderChapter.of(new ResourceLocation(MOD_ID, "fluids"));

    public static void register() {
        PonderRegistrationHelper helper = new PonderRegistrationHelper(MOD_ID);
        helper.forComponents(ModBlocks.MY_FLUID_TANK)
                .addStoryBoard("my_fluid_tank", MyFluidPonderScenes::tankScene, CHAPTER);
    }
}
```

---

## 8. 娴佷綋娓叉煋

### 8.1 FluidRenderer 鈥?娴佷綋娴佹覆鏌?
`com.simibubi.create.foundation.fluid.FluidRenderer`

```java
import com.simibubi.create.foundation.fluid.FluidRenderer;

// 娓叉煋绠￠亾涓殑娴佷綋娴?FluidRenderer.renderFluidStream(
    fluidStack,        // 瑕佹覆鏌撶殑娴佷綋
    direction,         // 娴佸悜
    radius,            // 鍗婂緞锛堥€氬父 0.25~0.5锛?    progress,          // 杩涘害 0~1
    inbound,           // 鏄惁鍚戝唴娴?    buffer, ms, light
);
```

### 8.2 BESR 涓覆鏌撴祦浣撳偍缃?
鍦?BESR 涓幏鍙栨祦浣撹繘搴﹀苟娓叉煋锛?
```java
public class MyFluidTankRenderer extends SafeBlockEntityRenderer<MyFluidTankBlockEntity> {
    @Override
    protected void renderSafe(MyFluidTankBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        SmartFluidTankBehaviour behaviour = be.getFluidBehaviour();
        if (behaviour == null) return;
        TankSegment tank = behaviour.getPrimaryTank();

        FluidStack fluidStack = tank.getRenderedFluid();
        if (fluidStack.isEmpty()) return;

        float level = tank.getFluidLevel().getValue(partialTicks);
        if (level < 0.01f) return;
        float height = level * 0.875f + 0.125f; // 搴曢儴鐣欑┖

        // 浣跨敤 Catnip 鐨?FluidRenderHelper 娓叉煋
        FluidRenderHelper.renderFluidBox(
            fluidStack,           // FluidStack
            0.125, 0.125, 0.125,  // minX, minY, minZ
            0.875, height, 0.875, // maxX, maxY, maxZ
            buffer, ms, light, false
        );
    }
}
```

### 8.3 鑷畾涔夋祦浣撶汗鐞?
绾圭悊瑕佹眰锛堣矾寰勫熀浜?`assets/<modid>/textures/block/`锛夛細

```
block/my_fluid_still.png     鈥?闈欐绾圭悊
block/my_fluid_flow.png      鈥?娴佸姩绾圭悊
```

`FluidType` 鐨?`IClientFluidTypeExtensions` 涓寚瀹氾細

```java
consumer.accept(new IClientFluidTypeExtensions() {
    @Override
    public ResourceLocation getStillTexture() {
        return new ResourceLocation(MOD_ID, "block/my_fluid_still");
    }
    @Override
    public ResourceLocation getFlowingTexture() {
        return new ResourceLocation(MOD_ID, "block/my_fluid_flow");
    }
    @Override
    public int getTintColor() { return 0xFF88FF88; }   // ARGB 鐫€鑹?});
```

### 8.4 娴佷綋鏂瑰潡妯″瀷

```java
// 閫氳繃 CreateRegistrate 娉ㄥ唽鏃惰嚜鍔ㄧ敓鎴?fluid block model
// 濡傞渶鑷畾涔夛紝鍦?.model() 涓寚瀹?.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry()))
```

---

## 闄勫綍锛氱被璺緞閫熸煡琛?
| 绫?| 璺緞 |
|---|---|
| SmartFluidTank | `com.simibubi.create.foundation.fluid.SmartFluidTank` |
| SmartFluidTankBehaviour | `com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour` |
| CombinedTankWrapper | `com.simibubi.create.foundation.fluid.CombinedTankWrapper` |
| FluidIngredient | `com.simibubi.create.foundation.fluid.FluidIngredient` |
| FluidHelper | `com.simibubi.create.foundation.fluid.FluidHelper` |
| FluidRenderer | `com.simibubi.create.foundation.fluid.FluidRenderer` |
| FluidTransportBehaviour | `com.simibubi.create.content.fluids.FluidTransportBehaviour` |
| FluidNetwork | `com.simibubi.create.content.fluids.FluidNetwork` |
| FluidPropagator | `com.simibubi.create.content.fluids.FluidPropagator` |
| FluidReactions | `com.simibubi.create.content.fluids.FluidReactions` |
| OpenEndedPipe | `com.simibubi.create.content.fluids.OpenEndedPipe` |
| BlockSpoutingBehaviour | `com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour` |
| OpenPipeEffectHandler | `com.simibubi.create.api.effect.OpenPipeEffectHandler` |
| BoilerHeater | `com.simibubi.create.api.boiler.BoilerHeater` |
| PipeCollisionEvent | `com.simibubi.create.api.event.PipeCollisionEvent` |
| ProcessingRecipe | `com.simibubi.create.content.processing.recipe.ProcessingRecipe` |
| ProcessingRecipeBuilder | `com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder` |
| ProcessingRecipeGen | `com.simibubi.create.api.data.recipe.ProcessingRecipeGen` |
| FillingRecipe | `com.simibubi.create.content.fluids.transfer.FillingRecipe` |
| EmptyingRecipe | `com.simibubi.create.content.fluids.transfer.EmptyingRecipe` |
| GenericItemFilling | `com.simibubi.create.content.fluids.transfer.GenericItemFilling` |
| GenericItemEmptying | `com.simibubi.create.content.fluids.transfer.GenericItemEmptying` |
| FluidSplashPacket | `com.simibubi.create.content.fluids.transfer.FluidSplashPacket` |
