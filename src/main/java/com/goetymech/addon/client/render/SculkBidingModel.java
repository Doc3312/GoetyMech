package com.goetymech.addon.client.render;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class SculkBidingModel<T extends Entity> extends HierarchicalModel<T> {
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart root;
    private final ModelPart spike_north;
    private final ModelPart spike_east;
    private final ModelPart spike_south;
    private final ModelPart spike_west;
    private final ModelPart center_cross;

    public SculkBidingModel(ModelPart root) {
        this.root = root.getChild("root");
        this.spike_north = this.root.getChild("spike_north");
        this.spike_east = this.root.getChild("spike_east");
        this.spike_south = this.root.getChild("spike_south");
        this.spike_west = this.root.getChild("spike_west");
        this.center_cross = this.root.getChild("center_cross");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition spike_north = root.addOrReplaceChild("spike_north",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 20.0F * DEG, 0.0F, 0.0F));

        spike_north.addOrReplaceChild("north_base",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        spike_north.addOrReplaceChild("north_mid",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        spike_north.addOrReplaceChild("north_tip",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 8.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition spike_east = root.addOrReplaceChild("spike_east",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 20.0F * DEG));

        spike_east.addOrReplaceChild("east_base",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        spike_east.addOrReplaceChild("east_mid",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        spike_east.addOrReplaceChild("east_tip",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 8.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition spike_south = root.addOrReplaceChild("spike_south",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -20.0F * DEG, 0.0F, 0.0F));

        spike_south.addOrReplaceChild("south_base",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        spike_south.addOrReplaceChild("south_mid",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        spike_south.addOrReplaceChild("south_tip",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 8.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        PartDefinition spike_west = root.addOrReplaceChild("spike_west",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, 0.0F, -20.0F * DEG));

        spike_west.addOrReplaceChild("west_base",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        spike_west.addOrReplaceChild("west_mid",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        spike_west.addOrReplaceChild("west_tip",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 8.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        root.addOrReplaceChild("center_cross",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, 8.0F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-3.0F, 8.0F, -1.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
