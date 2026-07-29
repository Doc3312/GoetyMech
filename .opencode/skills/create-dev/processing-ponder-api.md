# ProcessingRecipe + Ponder + API 扩展点

## ProcessingRecipe 配方系统

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

### DataGen

```java
public class MyProcessingRecipeGen extends ProcessingRecipeGen {

    GeneratedRecipe MY_RECIPE = create("my_recipe", b -> b
            .require(Ingredient.of(Items.DIRT))
            .output(Items.GRASS_BLOCK)
            .duration(200)
    );

    @Override
    public String getName() { return "My Processing Recipes"; }
}
```

---

## Ponder 场景

### 注册

```java
public class MyPonderIndex {
    public static final PonderChapter CHAPTER =
            PonderChapter.of(new ResourceLocation(MOD_ID, "my_chapter"));

    public static void register() {
        PonderRegistrationHelper helper = new PonderRegistrationHelper(MOD_ID);
        helper.forComponents(MyBlocks.MY_MACHINE)
                .addStoryBoard("my_machine", MyPonderScenes::machineScene, CHAPTER);
    }
}
```

### 场景

```java
public class MyPonderScenes {
    public static void machineScene(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("my_machine", "如何使用我的机器");
        scene.configureBasePlate(0, 0, 5);
        scene.worldCycle(util.grid.at(2, 1, 2));
        scene.idle(10);
        scene.overlay.showText(60)
                .text("这就是我的机器")
                .pointAt(util.vector.topOf(2, 2, 2));
        scene.markAsFinished();
    }
}
```

---

## API 扩展点

位于 `com.simibubi.create.api.*`，附属可实现并注册自定义行为。

| 接口 | 注册方式 | 用途 |
|---|---|---|
| `BlockSpoutingBehaviour` | `AllBlockSpoutingBehaviours.registerDefaults()` | 流体喷溅 |
| `MovementBehaviour` | `AllMovementBehaviours.registerDefaults()` | 运动件特殊行为 |
| `MovingInteractionBehaviour` | `AllInteractionBehaviours.registerDefaults()` | 玩家与运动件交互 |
| `DisplaySource` | `AllDisplaySources.register()` | 显示屏数据源 |
| `DisplayTarget` | `AllDisplayTargets.register()` | 显示屏数据目标 |
| `ContraptionMovementSetting` | `AllContraptionMovementSettings.registerDefaults()` | 装置移动设置 |
| `BoilerHeater` | `AllBoilerHeaters.register()` | 锅炉加热器 |
| `PortalTrackProvider` | `AllPortalTrackProviders.register()` | 传送带轨道提供者 |
| `BlockMovementChecks` | `BlockMovementChecks.register()` | 方块移动检查 |
| `ContraptionType` | `AllContraptionTypes.init()` | 自定义装置类型 |
| `MountedDispenseBehavior` | `AllMountedDispenseItemBehaviors.registerDefaults()` | 挂载分发器 |
| `BlockStressValues` | — | 数据驱动应力值 |
| `OpenPipeEffectHandler` | `AllOpenPipeEffectHandlers.registerDefaults()` | 开管效果 |

### 注册示例

```java
AllMovementBehaviours.registerDefaults(
    () -> AllMovementBehaviours.register(MyBlocks.MY_BLOCK.get(), new MyMovementBehaviour())
);
```
