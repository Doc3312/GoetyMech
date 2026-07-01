---
name: "entity-render-dev"
description: "瀹炰綋娓叉煋寮€鍙戝弬鑰冦€傛兜鐩?ModModelLayer 妯″紡銆乁V 鍙樻崲 VertexConsumer 鍖呰鍣ㄣ€丳oseStack 鍙樻崲瑙勫垯銆佸姩鐢?setupAnim銆佽皟璇曟鏌ユ竻鍗曘€傚綋闇€瑕佹覆鏌撹嚜瀹氫箟瀹炰綋銆佹柟鍧楀疄浣撴垨妯″瀷鏃惰皟鐢ㄦ湰 skill銆?
---

# Entity Render 鍙傝€?
## ModModelLayer 妯″紡

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

## UV Transform VertexConsumer 鍖呰鍣?
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

## PoseStack 鍙樻崲

### 瑙勫垯
- **Entity position is at FEET** when render starts (`EntityRenderer.render()` translates to entity pos)
- **BlockBench Y=16 = 1 block in Minecraft** (16 model units = 1 block)
- **Do NOT** `translate(0, -1, 0)` 鈥?model center already at correct height
- **Last rotation = first on vertices** (matrix stack order)

### 甯哥敤鍙樻崲
```java
// 杞瓙/鍦嗙洏骞虫斁鈫掔洿绔?poseStack.mulPose(Axis.XP.rotationDegrees(-90));

// 闈㈠悜杩愬姩鏂瑰悜
Vec3 dir = entity.getDirVec();
float yaw = (float) Math.atan2(dir.x, dir.z);
poseStack.mulPose(Axis.YP.rotation(yaw));

// 鍨傜洿鍒嗛噺鐨勪刊浠拌
float pitch = (float) -Math.asin(dir.y);
poseStack.mulPose(Axis.XP.rotation(pitch));

// 鎸佺画鏃嬭浆
float spin = (entity.tickCount + partialTicks) * speed;
poseStack.mulPose(Axis.ZP.rotation(spin));
```

## Animation in setupAnim()

```java
@Override
public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                      float ageInTicks, float netHeadYaw, float headPitch) {
    body.yRot = ageInTicks * 0.5F;            // 鎸佺画鏃嬭浆
    part.zRot = Mth.cos(ageInTicks * 0.1F) * 0.2F;  // 鎽嗗姩
}
```

## 璋冭瘯妫€鏌ユ竻鍗?
1. Model 娉ㄥ唽浜嗗悧锛焋latest.log` 鈫?妫€鏌?renderer log: `model=missing:true/false`
2. 绾圭悊璺緞姝ｇ‘鍚楋紵`assets/<modid>/textures/entity/<name>.png`
3. JSON 涓?namespace 姝ｇ‘鍚楋紵`"1": "modid:path"` 涓嶆槸 `"1": "path"`
4. Render type 姝ｇ‘鍚楋紵`entityCutout` 涓嶆槸 `cutout`
5. Atlas stitching 閿欒锛熷湪 `latest.log` 涓悳绱㈢汗鐞嗗悕绉?6. 鐢ㄤ簡 `OverlayTexture.NO_OVERLAY` 鍚楋紵
7. `model.renderToBuffer()` 鍦ㄥ彉鎹箣鍚庤皟鐢ㄤ簡鍚楋紵