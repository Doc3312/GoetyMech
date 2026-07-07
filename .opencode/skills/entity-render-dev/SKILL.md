---
name: entity-render-dev
description: entity render / collision / invisible
---

# Entity Render Development

> 前置模组源码路径见 `.opencode/skills/_shared/libcode-paths.md`。
> 快速排除：当前任务如果不涉及 entity renderer 注册、model 创建、collision AABB 或 texture/UV 修复，请跳过本 skill。

## Quick Start — Goety Pattern (Recommended)

```java
// 1. Model class
public class MyEntityModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    public MyEntityModel(ModelPart part) { this.root = part; }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
    @Override public void setupAnim(T e, float l, float l2, float age, float y, float p) {}
    @Override public ModelPart root() { return this.root; }
}

// 2. Register (in ModClientEvents):
// event.registerLayerDefinition(ModModelLayer.MY_ENTITY, MyEntityModel::createBodyLayer);
// event.registerEntityRenderer(ModEntities.MY_ENTITY.get(), MyEntityRenderer::new);

// 3. Renderer:
// public class MyEntityRenderer extends EntityRenderer<MyEntity> {
//     private final MyEntityModel<MyEntity> model;
//     public MyEntityRenderer(Context ctx) {
//         super(ctx);
//         this.model = new MyEntityModel<>(ctx.bakeLayer(ModModelLayer.MY_ENTITY));
//     }
//     public void render(...) {
//         poseStack.pushPose();
//         // ... transforms (see REFERENCE.md) ...
//         VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
//         model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
//         poseStack.popPose();
//     }
//     public ResourceLocation getTextureLocation(MyEntity e) {
//         return new ResourceLocation(MOD_ID, "textures/entity/my_entity.png");
//     }
// }
```

## Registration Pipeline

| Step | Event | Purpose |
|------|-------|---------|
| 1 | `RegisterLayerDefinitions` | Connect `ModelLayerLocation` → `createBodyLayer()` |
| 2 | `RegisterRenderers` | Connect `EntityType` → `Renderer::new` |
| (alt) | `ModelEvent.RegisterAdditional` | Only for block/item model JSONs, NOT entities |

## Three Approaches

**A. Java HierarchicalModel** — New entity with custom shape. Most common.
`HierarchicalModel<T>` + `createBodyLayer()` + `CubeListBuilder`. Independent PNG texture, `RenderType.entityCutout(TEXTURE)`.

**B. Block Model JSON + UV Transform** — Reuse existing BlockBench model.
Keep JSON + `RegisterAdditional`. In renderer, intercept `putBulkData` UVs via `VertexConsumer` wrapper converting atlas UV → entity texture UV. Use `entityCutout()` not `cutout()`. See [REFERENCE.md](REFERENCE.md) for wrapper code.

**C. Direct Vertex** — Simple shapes (beams, trails). No model needed.

## Key Pitfalls

- **Block model JSON** textures get stitched into block atlas; baked quads have atlas-remapped UVs
- `RenderType.cutout()` → block atlas → UVs wrong for entity texture
- `RenderType.entityCutout(TEXTURE)` → expects [0,1] on entity texture → white/garbled
- **Fix**: UV transform wrapper (B) or switch to Java model (A)
- **Texture path in JSON**: `"1": "modid:entity/name"` (no `textures/` prefix, no `.png`)

## Texture Conventions

| Aspect | Convention |
|--------|-----------|
| Path | `textures/entity/<name>.png` |
| Namespace | Always prefix `modid:` in model JSON |
| RenderType | `entityCutout` for opaque, `entityTranslucent` for alpha |
| Size | Power of 2 (16, 32, 64, 128) |

See [REFERENCE.md](REFERENCE.md) for PoseStack transforms, animation, debug checklist, and ModModelLayer pattern.

---

## ★ 最高优先级：实体朝向 + 碰撞箱标准流程

**只要涉及朝向调整，必须按此流程：① 调整实体朝向 → ② 随即调整碰撞箱。**

### 原则

| 事项 | 做法 |
|------|------|
| 朝向 | 在召唤时（Spell/Item）使用 `entity.setYRot()`，**禁止**在渲染器中计算朝向 |
| 碰撞箱 | 覆写 `makeBoundingBox()`，根据 `getYRot()` 实时计算旋转后的投影范围 |
| 自旋动画 | 叠加在朝向之上（`Axis.ZP.rotation()`），与朝向无关 |

### ① 召唤时设置实体朝向

```java
// yRot 计算公式：270 - summonerYaw 使模型面朝召唤者
// (若 90° 偏差则调整为 180 - yaw 或 270 - yaw)
entity.setYRot(Mth.wrapDegrees(270 - caster.getYRot()));
```

### ② 渲染器使用 entity.getYRot()

```java
// 朝向
float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
poseStack.mulPose(Axis.YP.rotation(yaw));
// 自旋（独立于朝向）
float spin = (entity.tickCount + partialTicks) * 0.5F;
poseStack.mulPose(Axis.ZP.rotation(spin));
```

`entity.getYRot()` 返回度，`Axis.YP.rotation()` 需弧度。BakedModel 实体需手动旋转（EntityRenderer 不自动应用 yRot）。

### ③ 碰撞箱 — 旋转感知 AABB（最优方案）

模型在 XZ 平面的范围：**宽 W（X方向）、深 D（Z方向）**，中心偏移量 **Z_offset**（模型中心在实体 Z 方向的偏移）。

```java
@Override
protected AABB makeBoundingBox() {
    float rad = this.getYRot() * Mth.DEG_TO_RAD;
    double c = Math.abs(Mth.cos(rad));
    double s = Math.abs(Mth.sin(rad));
    // W = X方向宽度, D = Z方向深度, Z_OFFSET = 模型中心在实体Z方向的偏移
    double rx = (W / 2.0) * c + (D / 2.0) * s;
    double rz = (W / 2.0) * s + (D / 2.0) * c;
    double cx = this.getX();
    double cz = this.getZ() + Z_OFFSET;
    return new AABB(cx - rx, this.getY(), cz - rz,
                    cx + rx, this.getY() + H, cz + rz);
}
```

**CrushingWheel 实例参数**：W=2, D=1, Z_OFFSET=0.5, H=2

| yRot | rx | rz | 说明 |
|------|----|----|------|
| 0° | 1 | 0.5 | X方向2块，Z方向1块 |
| 90° | 0.5 | 1 | X方向1块，Z方向2块 |
| 45° | 1.06 | 1.06 | 对角线方向扩到 ~2.12块 |

**验证方法**：F3+B 显示碰撞箱，从多角度召唤目测是否贴合模型。不贴合 → 检查 W/D/Z_OFFSET/H 值。歪 90° → 调 yRot 公式。

---

## 碰撞检测 — getEntities() 漏检排查

适用：自定义 `Entity` 中用 `level().getEntities(this, getBoundingBox(), predicate)` 做碰撞检测时 targets 始终为 0。

### 根因：`getBoundingBox()` ≠ 视觉位置

`getBoundingBox()` 返回 `makeBoundingBox()` 的 AABB，若包含了偏移量（如 `cz = getZ() + 0.5`），检测中心偏离视觉中心 → 漏检。

### Entity push 物理分离

`move(MoverType.SELF, vel)` 会把接触实体弹开，即使视觉重叠，AABB 间已有微小间隙。

### 修复：`.inflate(margin)` 补偿

```java
// ✅ 保留原始 makeBoundingBox()，检测时膨胀
AABB box = getBoundingBox();
List<Entity> targets = level().getEntities(this, box.inflate(0.5),
        e -> e instanceof LivingEntity && !e.getUUID().equals(ownerUUID));
```

`inflate(0.5)` 在六面各扩展 0.5 格，补偿 entity push 和 box 偏移。

### 排查步骤

1. 日志输出 box + target count — 确认 `targets = 0`
2. 日志输出 target 信息 — 确认 predicate（`instanceof`, UUID 过滤）
3. 日志输出 inflate 后的 box — 对比目标实体 AABB
4. `canBeCollidedWith()` / `isPushable()` 不影响 getEntities() 但影响物理分离程度

## 相关技能

| 技能 | 典型切换场景 |
|------|-------------|
| `item-ister-flat-quads` | 渲染的是物品图标/叠加层而非实体时 |
| `goety-addon-dev` | 需要了解 Goety 实体注册模式和召唤 API 时 |
