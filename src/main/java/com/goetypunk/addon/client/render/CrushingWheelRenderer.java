package com.goetypunk.addon.client.render;

import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.common.entity.CrushingWheelEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;

public class CrushingWheelRenderer extends EntityRenderer<CrushingWheelEntity> {

    private static final ResourceLocation MODEL_ID =
            new ResourceLocation(GoetyPunkMod.MOD_ID, "entity/crushing_wheel");

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(GoetyPunkMod.MOD_ID, "textures/entity/crushing_wheel.png");

    public CrushingWheelRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(CrushingWheelEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        BakedModel bakedModel = modelManager.getModel(MODEL_ID);
        boolean missing = bakedModel == modelManager.getMissingModel();

        GoetyPunkMod.LOGGER.debug("WheelRenderer: entity={}, model=missing:{}",
                entity.getId(), missing);

        if (missing) return;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(MODEL_ID);
        float uScale = sprite.getU1() - sprite.getU0();
        float vScale = sprite.getV1() - sprite.getV0();

        poseStack.pushPose();

        // pivot to wheel center (BlockBench [0,16,8] = Minecraft [0,1,0.5] from entity feet)
        poseStack.translate(0, 1.0, 0.5);

        // wheel face toward summoner
        float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
        poseStack.mulPose(Axis.YP.rotation(yaw));
        // spin around Z axle
        float spin = (entity.tickCount + partialTicks) * 0.5F;
        poseStack.mulPose(Axis.ZP.rotation(spin));

        poseStack.translate(0, -1.0, -0.5);

        VertexConsumer baseVc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        VertexConsumer vc = new VertexConsumer() {
            @Override
            public VertexConsumer vertex(double x, double y, double z) {
                baseVc.vertex(x, y, z); return this;
            }

            @Override
            public VertexConsumer color(int r, int g, int b, int a) {
                baseVc.color(r, g, b, a); return this;
            }

            @Override
            public VertexConsumer uv(float u, float v) {
                if (uScale > 0 && vScale > 0) {
                    baseVc.uv((u - sprite.getU0()) / uScale, (v - sprite.getV0()) / vScale);
                } else {
                    baseVc.uv(u, v);
                }
                return this;
            }

            @Override
            public VertexConsumer overlayCoords(int u, int v) {
                baseVc.overlayCoords(u, v); return this;
            }

            @Override
            public VertexConsumer uv2(int u, int v) {
                baseVc.uv2(u, v); return this;
            }

            @Override
            public VertexConsumer normal(float x, float y, float z) {
                baseVc.normal(x, y, z); return this;
            }

            @Override
            public void endVertex() { baseVc.endVertex(); }

            @Override
            public void defaultColor(int r, int g, int b, int a) { baseVc.defaultColor(r, g, b, a); }

            @Override
            public void unsetDefaultColor() { baseVc.unsetDefaultColor(); }
        };

        RandomSource random = RandomSource.create();
        for (Direction dir2 : Direction.values()) {
            random.setSeed(42L);
            for (BakedQuad quad : bakedModel.getQuads(null, dir2, random)) {
                vc.putBulkData(poseStack.last(), quad, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
            }
        }

        random.setSeed(42L);
        for (BakedQuad quad : bakedModel.getQuads(null, null, random)) {
            vc.putBulkData(poseStack.last(), quad, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(CrushingWheelEntity entity) {
        return TEXTURE;
    }
}
