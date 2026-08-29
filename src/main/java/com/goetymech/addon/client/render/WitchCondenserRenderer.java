package com.goetymech.addon.client.render;

import com.goetymech.addon.common.block.entity.WitchCondenserBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fluids.FluidStack;

/**
 * 巫术冷凝机渲染：传动轴（FACING 反面）+ 3 槽独立液面。
 */
public class WitchCondenserRenderer extends KineticBlockEntityRenderer<WitchCondenserBlockEntity> {

    public WitchCondenserRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(WitchCondenserBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) return;

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        Direction shaftDir = facing.getOpposite();

        SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, shaftDir);
        // SHAFT_HALF 为 8px，缩短到 2px 避免穿透玻璃：沿轴 scale(0.25)，正向侧再平移 12px
        if (shaftDir.getAxis() == Direction.Axis.X) {
            shaft.scale(0.25f, 1, 1);
            if (shaftDir.getStepX() > 0) shaft.translate(0.75, 0, 0);
        } else if (shaftDir.getAxis() == Direction.Axis.Z) {
            shaft.scale(1, 1, 0.25f);
            if (shaftDir.getStepZ() > 0) shaft.translate(0, 0, 0.75);
        } else {
            shaft.scale(1, 0.25f, 1);
            if (shaftDir.getStepY() > 0) shaft.translate(0, 0.75, 0);
        }
        standardKineticRotationTransform(shaft, be, light);
        shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));

        SmartFluidTankBehaviour fluidBehaviour = be.getFluidBehaviour();
        if (fluidBehaviour != null) {
            renderFluids(fluidBehaviour, partialTicks, ms, buffer, light);
        }
    }

    /** 3 槽独立液面：每槽占 1/3 高度，从底部按填充比例上升 */
    private void renderFluids(SmartFluidTankBehaviour fluidBehaviour, float partialTicks,
                              PoseStack ms, MultiBufferSource buffer, int light) {
        float layerH = 1f / WitchCondenserBlockEntity.TANK_COUNT;
        float xMin = 2 / 16f;
        float zMin = 2 / 16f;
        float xMax = 14 / 16f;
        float zMax = 14 / 16f;
        SmartFluidTankBehaviour.TankSegment[] tanks = fluidBehaviour.getTanks();
        for (int i = 0; i < tanks.length; i++) {
            SmartFluidTankBehaviour.TankSegment segment = tanks[i];
            if (segment.isEmpty(partialTicks)) {
                continue;
            }
            FluidStack stack = segment.getRenderedFluid();
            if (stack.isEmpty()) {
                continue;
            }
            float yMin = i * layerH;
            float yMax = yMin + segment.getTotalUnits(partialTicks) / WitchCondenserBlockEntity.TANK_CAPACITY * layerH;
            if (yMax <= yMin + 1 / 32f) {
                continue;
            }
            ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(stack, xMin, yMin, zMin, xMax, yMax, zMax,
                    buffer, ms, light, false, true);
        }
    }
}
