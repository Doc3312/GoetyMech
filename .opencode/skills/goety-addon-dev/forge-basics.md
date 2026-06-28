# Forge 1.20.x 开发基础

## mods.toml

```toml
modLoader = "javafml"
loaderVersion = "[47,)"
[[mods]]
modId = "your_mod_id"
displayName = "Your Mod"
```

## 主类

```java
@Mod("your_mod_id")
public class YourMod {
    public static final String MOD_ID = "your_mod_id";
    public YourMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    }
}
```

## 注册表（DeferredRegister）

```java
public static final DeferredRegister<Block> BLOCKS =
    DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
public static final RegistryObject<Block> EXAMPLE_BLOCK =
    BLOCKS.register("example", () -> new Block(BlockBehaviour.Properties.of()));
```

注册顺序（强制）：方块 → 方块实体 → 物品(BlockItem) → 创造标签页

## 事件系统

- **模组事件总线**：生命周期、注册事件
- **Forge 事件总线**：运行时事件

```java
@Mod.EventBusSubscriber(modid = MOD_ID)
public class Events {
    @SubscribeEvent
    public static void onEvent(SomeEvent e) {}
}
```

## 端位隔离

- 跨逻辑端通信必须用网络数据包
- 客户端代码隔离在 `client` 包
- 使用 `DistExecutor.safeRunWhenOn(Dist.CLIENT, ...)`

## 方块实体

```java
@Nullable @Override
public <T extends BlockEntity> BlockEntityTicker<T> getTicker(...) {
    return level.isClientSide ? null : (w,p,s,be) -> ((MyBE)be).tick();
}

// 数据同步
getUpdateTag() / handleUpdateTag()   // 区块加载
getUpdatePacket() / onDataPacket()   // 更新时
```

## 常见陷阱：Entity.level 是 private 字段

`Entity.level` 在 1.20.x 是 **private** 字段，必须用 getter `level()`。

```java
// ❌ 错误
if (this.level.isClientSide) return;

// ✅ 正确
if (this.level().isClientSide) return;
((ServerLevel) this.level()).getEntity(uuid);
```

同样适用于 `Entity.position()` 等方法。

## 网络（SimpleImpl）

```java
public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
    new ResourceLocation(MOD_ID, "main"),
    () -> "1", "1"::equals, "1"::equals);
```

发送：`sendToServer()` / `send(PacketDistributor.ALL.noArg())`

## 配置

```java
ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configSpec);
```

## ⚠️ 容器开发陷阱：`setItem` 禁止 `stack.split(n)`

当你的 BlockEntity 实现 `WorldlyContainer` 时，`SidedInvWrapper`（Forge 的 IItemHandler 桥接）在**空槽满额接受**分支中，会将原始 stack **引用**直接传入 `setItem`（不做 `.copy()`）：

```java
// SidedInvWrapper.java 第189-190行（B2 分支，关键代码）
if (!simulate)
    setInventorySlotContents(slot1, stack);  // ← 原始引用！
return ItemStack.EMPTY;
```

### ❌ 错误写法

```java
public void setItem(int index, ItemStack stack) {
    items[index] = stack.split(n);  // ← 修改了原始 stack 的 count！
}
```

`ItemStack.split(n)` 会 `this.shrink(n)`，**直接修改传入的原始引用**。导致：

- **Create 漏斗**：`TransportedItemStack.stack` 被置空，传送带认为物品未消耗
- **其他使用 IItemHandler 的管道**：调用方侧数据损坏，表现诡异

### ✅ 正确写法

```java
public void setItem(int index, ItemStack stack) {
    items[index] = stack.copyWithCount(n);  // 新对象，不修改原引用
}
```

`copyWithCount(n)` 内部调用 `this.copy()` + `setCount(n)`，完全隔离所有权。

### 适用场景

- 任何实现 `WorldlyContainer` / `Container` 的 BlockEntity
- 单槽位有限容量的容器（最大堆叠 < 64 或固定容量 = 1）
- 需要与 Create 漏斗/管道/传送带交互的方块
