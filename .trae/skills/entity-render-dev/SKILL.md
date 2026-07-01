---
name: "entity-render-dev"
description: "实体渲染开发参考。涵盖 ModModelLayer 模式、UV 变换 VertexConsumer 包装器、PoseStack 变换规则、动画 setupAnim、调试检查清单。当需要渲染自定义实体、方块实体或模型时调用本 skill。"
---

# Entity Render 参考

## ModModelLayer 模式

```java
public class ModModelLayer {
    private static final Set<ModelLayerLocation> ALL = Sets.newHashSet();
    public static final ModelLayerLocation MY_ENTITY = register("my_entity");

    private static ModelLayerLocation register(String name) {
        ModelLayerLocation loc = new ModelLayerLocation(
            new ResourceLocation(GoetyMechMod.MOD_ID, name), "main");
        if (!ALL.add(loc)) throw new IllegalStateException("Duplicate " + name);
        return loc;
    }
}
```

## UV Transform VertexConsumer 包装器

```java
TextureAtlasSprite sprite = Minecraft.getInstance()
    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MODEL_TEXTURE_ID);
float uScale = sprite.getU1() - sprite.getU0();
float vScale = sprite.getV1() - sprite.getV0();

VertexConsumer baseVc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
VertexConsumer vc = new VertexConsumer() {
    @Override public VertexConsumer vertex(double x, double y, double z) { baseVc.vertex(x, y, z); return this; }
    @Override public VertexConsumer color(int r, int g, int b, int a) { baseVc.color(r, g, b, a); return this; }
    @Override public VertexConsumer uv(float u, float v) {
        if (uScale > 0 && vScale > 0)
            baseVc.uv((u - sprite.getU0()) / uScale, (v - sprite.getV0()) / vScale);
        else baseVc.uv(u, v);
        return this;
    }
    @Override public VertexConsumer overlayCoords(int u, int v) { baseVc.overlayCoords(u, v); return this; }
    @Override public VertexConsumer uv2(int u, int v) { baseVc.uv2(u, v); return this; }
    @Override public VertexConsumer normal(float x, float y, float z) { baseVc.normal(x, y, z); return this; }
    @Override public void endVertex() { baseVc.endVertex(); }
    @Override public void defaultColor(int r, int g, int b, int a) { baseVc.defaultColor(r, g, b, a); }
    @Override public void unsetDefaultColor() { baseVc.unsetDefaultColor(); }
};
```

## PoseStack 变换

### 规则
- **Entity position is at FEET** when render starts (`EntityRenderer.render()` translates to entity pos)
- **BlockBench Y=16 = 1 block in Minecraft** (16 model units = 1 block)
- **Do NOT** `translate(0, -1, 0)` — model center already at correct height
- **Last rotation = first on vertices** (matrix stack order)

### 常用变换
```java
// 轮子/圆盘平放→直立
poseStack.mulPose(Axis.XP.rotationDegrees(-90));

// 面向运动方向
Vec3 dir = entity.getDirVec();
float yaw = (float) Math.atan2(dir.x, dir.z);
poseStack.mulPose(Axis.YP.rotation(yaw));

// 垂直分量的俯仰角
float pitch = (float) -Math.asin(dir.y);
poseStack.mulPose(Axis.XP.rotation(pitch));

// 持续旋转
float spin = (entity.tickCount + partialTicks) * speed;
poseStack.mulPose(Axis.ZP.rotation(spin));
```

## Animation in setupAnim()

```java
@Override
public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                      float ageInTicks, float netHeadYaw, float headPitch) {
    body.yRot = ageInTicks * 0.5F;            // 持续旋转
    part.zRot = Mth.cos(ageInTicks * 0.1F) * 0.2F;  // 摆动
}
```

## 调试检查清单

1. Model 注册了吗？`latest.log` → 检查 renderer log: `model=missing:true/false`
2. 纹理路径正确吗？`assets/<modid>/textures/entity/<name>.png`
3. JSON 中 namespace 正确吗？`"1": "modid:path"` 不是 `"1": "path"`
4. Render type 正确吗？`entityCutout` 不是 `cutout`
5. Atlas stitching 错误？在 `latest.log` 中搜索纹理名称
6. 用了 `OverlayTexture.NO_OVERLAY` 吗？
7. `model.renderToBuffer()` 在变换之后调用了吗？