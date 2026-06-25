package com.goetypunk.addon.client.render;

import com.goetypunk.addon.GoetyPunkMod;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class CrazedIllagerModel<T extends LivingEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(GoetyPunkMod.MOD_ID, "crazed_illager"), "main");

    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart sawBlade;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public CrazedIllagerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.sawBlade = this.head.getChild("saw_blade");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("hat",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition sawBlade = head.addOrReplaceChild("saw_blade",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -4.0F, -4.0F));

        sawBlade.addOrReplaceChild("blade_center",
                CubeListBuilder.create()
                        .texOffs(48, 32).addBox(-4.0F, -4.0F, -0.5F, 8.0F, 8.0F, 1.0F),
                PartPose.ZERO);

        float toothAngleStep = 45.0F * DEG;
        float toothRadius = 4.5F;
        for (int i = 0; i < 8; i++) {
            float angle = i * toothAngleStep;
            float tx = Mth.cos(angle) * toothRadius;
            float ty = Mth.sin(angle) * toothRadius;
            sawBlade.addOrReplaceChild("tooth_" + i,
                    CubeListBuilder.create()
                            .texOffs(48, 41).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
                    PartPose.offset(tx, ty, 0.0F));
        }

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(44, 22).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * DEG;
        this.head.xRot = headPitch * DEG;

        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        this.sawBlade.zRot = ageInTicks * 0.5F;
        this.sawBlade.yRot = (float) (Math.PI / 2);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
