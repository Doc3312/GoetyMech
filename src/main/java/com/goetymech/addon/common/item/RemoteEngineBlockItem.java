package com.goetymech.addon.common.item;

import com.goetymech.addon.common.block.entity.RemoteEngineBlockEntity;
import com.goetymech.addon.common.block.entity.StressReaderBlockEntity;
import com.goetymech.addon.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteEngineBlockItem extends BlockItem {

    private static final Logger LOGGER = LoggerFactory.getLogger("RemoteEngineItem");

    public static final String TAG_BOUND = "goetymech.bound";
    public static final String TAG_X = "goetymech.bound_x";
    public static final String TAG_Y = "goetymech.bound_y";
    public static final String TAG_Z = "goetymech.bound_z";
    public static final String TAG_DIM = "goetymech.bound_dim";
    public static final String TAG_ENGINE_X = "goetymech.engine_x";
    public static final String TAG_ENGINE_Y = "goetymech.engine_y";
    public static final String TAG_ENGINE_Z = "goetymech.engine_z";

    public RemoteEngineBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        BlockPos clickedPos = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();

        if (player != null) {
            if (player.isShiftKeyDown()) {
                LOGGER.debug("[RemoteEngineItem] sneak+right-click at {} | isBound={} isReader={} boundPos={}",
                        clickedPos, isBound(stack), level.getBlockState(clickedPos).is(ModBlocks.STRESS_READER.get()),
                        isBound(stack) ? getBoundPos(stack) : "N/A");
                if (isBound(stack) && level.getBlockState(clickedPos).is(ModBlocks.STRESS_READER.get())) {
                    BlockPos boundPos = getBoundPos(stack);
                    if (boundPos.equals(clickedPos)) {
                        if (!level.isClientSide) {
                            LOGGER.debug("[RemoteEngineItem] UNBIND at {}", clickedPos);
                            BlockEntity be = level.getBlockEntity(clickedPos);
                            if (be instanceof StressReaderBlockEntity reader) {
                                BlockPos enginePos = getEnginePos(stack);
                                if (enginePos != null && level.getBlockEntity(enginePos) instanceof RemoteEngineBlockEntity engine) {
                                    engine.clearLinkedPos();
                                }
                            }
                            clearBinding(stack);
                            player.displayClientMessage(
                                    Component.translatable("info.goetymech.remote_engine.unbound"), true);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                return super.useOn(ctx);
            }

            if (level.getBlockState(clickedPos).is(ModBlocks.STRESS_READER.get())) {
                if (!level.isClientSide) {
                    writeBinding(stack, clickedPos, level.dimension());
                    player.displayClientMessage(
                            Component.translatable("info.goetymech.remote_engine.bound"),
                            true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(ctx);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (isBound(stack)) {
            BlockPos pos = getBoundPos(stack);
            tooltip.add(Component.translatable("info.goetymech.remote_engine.bound_to",
                    pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    public static boolean isBound(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_BOUND);
    }

    public static void writeBinding(ItemStack stack, BlockPos pos, ResourceKey<Level> dim) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_BOUND, true);
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        tag.putString(TAG_DIM, dim.location().toString());
    }

    public static void writeEnginePos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_ENGINE_X, pos.getX());
        tag.putInt(TAG_ENGINE_Y, pos.getY());
        tag.putInt(TAG_ENGINE_Z, pos.getZ());
    }

    public static BlockPos getEnginePos(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ENGINE_X)) return null;
        return new BlockPos(tag.getInt(TAG_ENGINE_X), tag.getInt(TAG_ENGINE_Y), tag.getInt(TAG_ENGINE_Z));
    }

    public static BlockPos getBoundPos(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
    }

    public static ResourceKey<Level> getBoundDim(ItemStack stack) {
        return ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.tryParse(stack.getTag().getString(TAG_DIM)));
    }

    public static void clearBinding(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            tag.remove(TAG_BOUND);
            tag.remove(TAG_X);
            tag.remove(TAG_Y);
            tag.remove(TAG_Z);
            tag.remove(TAG_DIM);
            tag.remove(TAG_ENGINE_X);
            tag.remove(TAG_ENGINE_Y);
            tag.remove(TAG_ENGINE_Z);
            if (tag.isEmpty()) stack.setTag(null);
        }
    }
}
