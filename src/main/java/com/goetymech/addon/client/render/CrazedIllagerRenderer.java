package com.goetymech.addon.client.render;

import com.goetymech.addon.GoetyMechMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class CrazedIllagerRenderer extends MobRenderer<Mob, CrazedIllagerModel<Mob>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            GoetyMechMod.MOD_ID, "textures/entity/crazed_illager.png");

    public CrazedIllagerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CrazedIllagerModel<>(ctx.bakeLayer(CrazedIllagerModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Mob entity) {
        return TEXTURE;
    }
}
