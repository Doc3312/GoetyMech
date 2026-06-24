package com.goetypunk.addon.client.render;

import com.goetypunk.addon.GoetyPunkMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class CrazedIllagerRenderer extends MobRenderer<LivingEntity, CrazedIllagerModel<LivingEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            GoetyPunkMod.MOD_ID, "textures/entity/crazed_illager.png");

    public CrazedIllagerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CrazedIllagerModel<>(ctx.bakeLayer(CrazedIllagerModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntity entity) {
        return TEXTURE;
    }
}
