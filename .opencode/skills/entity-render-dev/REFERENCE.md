# Entity Render Reference

## ModModelLayer Pattern

```java
// ModModelLayer.java
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

## UV Transform VertexConsumer Wrapper (Approach B)

```java
// In render() method:
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

// Use vc.putBulkData(...) instead of baseVc.putBulkData(...)
```

## PoseStack Transforms

### Rules
- **Entity position is at FEET** when render starts (`EntityRenderer.render()` translates to entity pos)
- **BlockBench Y=16 = 1 block in Minecraft** (16 model units = 1 block)
- **Do NOT** `translate(0, -1, 0)` — model center already at correct height
- **Last rotation = first on vertices** (matrix stack order)

### Common Fixes
```java
// Wheel/disk lying flat → upright
poseStack.mulPose(Axis.XP.rotationDegrees(-90));

// Face movement direction
Vec3 dir = entity.getDirVec();
float yaw = (float) Math.atan2(dir.x, dir.z);
poseStack.mulPose(Axis.YP.rotation(yaw));

// Pitch for vertical component
float pitch = (float) -Math.asin(dir.y);
poseStack.mulPose(Axis.XP.rotation(pitch));

// Continuous spin
float spin = (entity.tickCount + partialTicks) * speed;
poseStack.mulPose(Axis.ZP.rotation(spin));
```

## Animation in setupAnim()

```java
@Override
public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                      float ageInTicks, float netHeadYaw, float headPitch) {
    body.yRot = ageInTicks * 0.5F;            // continuous spin
    part.zRot = Mth.cos(ageInTicks * 0.1F) * 0.2F;  // wobble
}
```

## Debug Checklist

1. Model registered? `latest.log` → check renderer log: `model=missing:true/false`
2. Texture at correct path? `assets/<modid>/textures/entity/<name>.png`
3. Namespace correct in JSON? `"1": "modid:path"` not `"1": "path"`
4. Render type correct? `entityCutout` not `cutout`
5. Atlas stitching errors in `latest.log` — search for texture name
6. `OverlayTexture.NO_OVERLAY` used?
7. `model.renderToBuffer()` called after transforms?
