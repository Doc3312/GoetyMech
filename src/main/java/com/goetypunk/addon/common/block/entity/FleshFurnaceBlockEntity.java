package com.goetypunk.addon.common.block.entity;

import com.goetypunk.addon.common.block.FleshFurnaceBlock;
import com.goetypunk.addon.config.ModConfig;
import com.goetypunk.addon.init.ModBlocks;
import com.goetypunk.addon.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import com.Polarice3.Goety.common.entities.ally.illager.SpellcasterIllagerServant;
import com.goetypunk.addon.init.ModTags;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 血肉熔炉方块实体 - 纯转化器
 * - 输入扭曲胚胎，烹制后直接弹出成熟胚胎
 * - 不存储任何物品，类似原版熔炉弹到漏斗
 * - 通过绑定不祥火炬决定产出兵种类型
 */
public class FleshFurnaceBlockEntity extends BlockEntity implements WorldlyContainer {

    // === 烹制状态 ===
    private ItemStack cookingStack = ItemStack.EMPTY;
    private int cookingProgress;
    private int cookingTotalTime;

    // === 链接数据 ===
    private int linkedX, linkedY, linkedZ;
    private boolean hasLink;

    // === 元数据 ===
    private String placerName = "";

    // === Create 漏斗兼容 ===
    private final LazyOptional<IItemHandlerModifiable>[] handlers = SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public FleshFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FLESH_FURNACE_ENTITY.get(), pos, state);
    }

    // ===== 核心 Tick =====

    public void tick() {
        if (level == null) return;
        boolean isServer = level instanceof ServerLevel;

        // 用坐标哈希分散扫描 tick，避免多熔炉同时并发
        int scanOffset = Math.abs(worldPosition.hashCode() % 60);
        if (isServer && level.getGameTime() % 60 == scanOffset) {
            // 玩家不在 32 格内 → 跳过本轮维护
            if (level.players().stream().anyMatch(p ->
                    p.distanceToSqr(Vec3.atCenterOf(worldPosition)) < 1024.0)) {
                if (hasLink) {
                    BlockPos pyrePos = getLinkPosition();
                    if (pyrePos != null) {
                        BlockEntity be = level.getBlockEntity(pyrePos);
                        if (!(be instanceof com.Polarice3.Goety.common.blocks.entities.OminousPyreBlockEntity)) {
                            hasLink = false;
                            linkedX = linkedY = linkedZ = 0;
                            markUpdated();
                        }
                    }
                } else {
                    scanAndLink();
                }
            }
        }

        // LIT 状态更新（每 5 tick）
        if (isServer && level.getGameTime() % 5 == 0) {
            boolean shouldBeLit = isCooking();
            BlockState bs = getBlockState();
            if (bs.getValue(BlockStateProperties.LIT) != shouldBeLit) {
                level.setBlock(worldPosition, bs.setValue(BlockStateProperties.LIT, shouldBeLit), 3);
            }
        }

        // === 烹制逻辑 ===
        if (!cookingStack.isEmpty() && cookingStack.is(ModItems.TWISTED_EMBRYO.get())) {
            if (isServer) {
                // 检查链接是否有效
                String entityTypeId = getTargetEntityTypeId();
                if (hasLink && entityTypeId != null) {

                    // ===== 标签检测：需完整训练结构的高级兵种 → 拒绝转化 =====
                    if (isEntityRestricted(entityTypeId)) {
                        notifyNearbyPlayers(Component.translatable("info.goetypunk.furnace.no_magic_talent"));
                        cookingProgress = 0;
                        cookingTotalTime = 0;
                        tryOutputToContainer(cookingStack);
                        cookingStack = ItemStack.EMPTY;
                        markUpdated();
                        return;
                    }

                    // 首次放入或进度为0时初始化总时间
                    if (cookingProgress == 0) {
                        cookingTotalTime = ModConfig.cachedFurnaceCookingTime * 20;
                        if (cookingTotalTime < 20) cookingTotalTime = 20;
                    }

                    // 粒子效果
                    makeCookingParticles();

                    cookingProgress++;

                    // 定期同步进度给客户端（每 20 tick）
                    if (level.getGameTime() % 20 == 0) {
                        markUpdated();
                    }

                    // 烹制完成 → 产出（产物留在槽位供漏斗抽取）
                    if (cookingProgress >= cookingTotalTime) {
                        cookingProgress = 0;

                        // 创建成熟胚胎
                        ItemStack matureStack = new ItemStack(ModItems.MATURE_EMBRYO.get(), 1);
                        CompoundTag tag = matureStack.getOrCreateTag();
                        tag.putString("EntityTypeId", entityTypeId);
                        matureStack.setTag(tag);

                        // 产物放入槽位，等待漏斗从侧面抽取
                        cookingStack = matureStack;
                        cookingTotalTime = 0;

                        // 烹制完成音效
                        level.playSound(null, worldPosition, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 0.6F);

                        markUpdated();
                    }
                    } else {
                        // 未链接或链接无效，弹出物品并提示错误
                        cookingProgress = 0;
                        cookingTotalTime = 0;
                        tryOutputToContainer(cookingStack);
                        cookingStack = ItemStack.EMPTY;
                        markUpdated();
                    }
            }
        }
    }

    // ===== 容器输出 =====

    /**
     * 优先向下方容器输出，无容器时才弹出物品实体
     */
    private void tryOutputToContainer(ItemStack stack) {
        if (level == null) return;
        // 检查下方方块是否有物品处理能力（漏斗、箱子、管道等）
        BlockPos belowPos = worldPosition.below();
        BlockEntity belowBE = level.getBlockEntity(belowPos);
        if (belowBE != null) {
            IItemHandler handler = belowBE.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).resolve().orElse(null);
            if (handler != null) {
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, stack, false);
                if (remaining.isEmpty()) {
                    return; // 全部送入容器
                }
                // 容器满了 → 弹出剩余部分
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, remaining);
                return;
            }
        }
        // 无容器或容器不兼容 → 直接弹出
        Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, stack);
    }

    // ===== 烹制控制 =====

    /**
     * 是否可以接受新的胚胎输入
     * 必须已链接到有效的不祥火炬且内部为空
     */
    public boolean canAcceptInput() {
        return cookingStack.isEmpty() && hasLink;
    }

    public boolean isCooking() {
        return !cookingStack.isEmpty() && cookingProgress > 0;
    }

    public void setCookingStack(ItemStack stack) {
        this.cookingStack = stack.copyWithCount(1);
        this.cookingProgress = 0;
        this.cookingTotalTime = 0;
        markUpdated();
    }

    public ItemStack getCookingStack() {
        return cookingStack;
    }

    public void dropCookingStack() {
        if (!cookingStack.isEmpty() && level != null) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, cookingStack);
            cookingStack = ItemStack.EMPTY;
            cookingProgress = 0;
        }
    }

    // ===== HUD 数据 =====

    public double getCookingProgress() {
        if (cookingTotalTime <= 0) return 0;
        return (double) cookingProgress / cookingTotalTime;
    }

    public int getProgressPercent() {
        return (int) (getCookingProgress() * 100);
    }

    // ===== 链接系统 =====

    public void setLinkPosition(BlockPos pos) {
        this.linkedX = pos.getX();
        this.linkedY = pos.getY();
        this.linkedZ = pos.getZ();
        this.hasLink = true;
        markUpdated();
    }

    public boolean hasLink() {
        return hasLink;
    }

    @Nullable
    public BlockPos getLinkPosition() {
        return hasLink ? new BlockPos(linkedX, linkedY, linkedZ) : null;
    }

    /**
     * 从保存的链接位置获取目标实体类型
     */
    @Nullable
    public String getTargetEntityTypeId() {
        if (!hasLink || level == null) return null;
        BlockPos pyrePos = getLinkPosition();
        if (pyrePos == null || !level.isLoaded(pyrePos)) return null;

        BlockEntity be = level.getBlockEntity(pyrePos);
        if (be instanceof com.Polarice3.Goety.common.blocks.entities.OminousPyreBlockEntity pyre) {
            String id = pyre.getEntityTrainTo().getString("id");
            // 火炬无训练目标时返回 null，阻止空 ID 烹制
            if (id.isEmpty()) return null;
            return id;
        }
        return null;
    }

    /**
     * 获取目标兵种的显示名称（用于 HUD）
     */
    public String getTargetEntityName() {
        String id = getTargetEntityTypeId();
        if (id == null || id.isEmpty()) return "";
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(id);
        if (rl != null) {
            var entityType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(rl);
            if (entityType != null) {
                return entityType.getDescription().getString();
            }
        }
        return id;
    }

    /**
     * 自动扫描附近 xz ±8 格内的不祥火炬
     */
    public boolean scanAndLink() {
        if (level == null) return false;
        BlockPos current = getBlockPos();
        int radius = ModConfig.cachedLinkRange;
        Vec3i area = new Vec3i(radius, 4, radius);
        for (BlockPos candidate : BlockPos.betweenClosed(current.subtract(area), current.offset(area))) {
            BlockEntity be = level.getBlockEntity(candidate);
            if (be instanceof com.Polarice3.Goety.common.blocks.entities.OminousPyreBlockEntity) {
                setLinkPosition(candidate.immutable());
                return true;
            }
        }
        return false;
    }

    // ===== 兵种限制检测 =====

    private static final Map<EntityType<?>, Boolean> spellcasterCache = new HashMap<>();

    /**
     * 动态判断实体类型是否需要完整训练结构
     * 1. 法术系兵种（SpellcasterIllagerServant）需要训练
     * 2. 标签补集处理非法术但需限制的特例（如 StormCaster）
     * 对应 Neollager.canTrain() 中的 instanceof SpellcasterIllagerServant 逻辑
     * 结果缓存到静态 map，避免每 tick 实例化实体
     */
    private boolean isEntityRestricted(String entityTypeId) {
        ResourceLocation rl = ResourceLocation.tryParse(entityTypeId);
        if (rl == null) return false;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);
        if (type == null || level == null) return false;

        // 1. 标签补集：处理非 Spellcaster 但仍需限制的特例（如 StormCaster）
        if (type.is(ModTags.REQUIRES_TRAINING_STRUCTURE)) return true;

        // 2. 法术系兵种需要完整训练结构（结果缓存）
        return spellcasterCache.computeIfAbsent(type, t -> {
            Entity e = t.create(level);
            boolean isSpellcaster = e instanceof SpellcasterIllagerServant;
            if (e != null) e.discard();
            return isSpellcaster;
        });
    }

    /**
     * 通知附近 8 格内的玩家消息（action bar）
     */
    private void notifyNearbyPlayers(Component message) {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 0.5D;
            double y = worldPosition.getY() + 0.5D;
            double z = worldPosition.getZ() + 0.5D;
            serverLevel.players().stream()
                    .filter(p -> p.distanceToSqr(x, y, z) <= 64.0D)
                    .forEach(p -> p.displayClientMessage(message, true));
        }
    }

    // ===== 拥有者 =====

    public String getPlacerName() {
        return placerName;
    }

    public void setPlacerName(String name) {
        this.placerName = name;
        setChanged();
    }

    // ===== 数据持久化 =====

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        // 烹制数据
        if (!cookingStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            cookingStack.save(stackTag);
            tag.put("CookingStack", stackTag);
        }
        tag.putInt("CookingProgress", cookingProgress);
        tag.putInt("CookingTotalTime", cookingTotalTime);

        // 链接数据
        tag.putBoolean("HasLink", hasLink);
        if (hasLink) {
            tag.putInt("LinkedX", linkedX);
            tag.putInt("LinkedY", linkedY);
            tag.putInt("LinkedZ", linkedZ);
        }

        // 元数据
        tag.putString("PlacerName", placerName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // 烹制数据
        if (tag.contains("CookingStack")) {
            cookingStack = ItemStack.of(tag.getCompound("CookingStack"));
        } else {
            cookingStack = ItemStack.EMPTY;
        }
        cookingProgress = tag.getInt("CookingProgress");
        cookingTotalTime = tag.getInt("CookingTotalTime");

        // 链接数据
        hasLink = tag.getBoolean("HasLink");
        if (hasLink) {
            linkedX = tag.getInt("LinkedX");
            linkedY = tag.getInt("LinkedY");
            linkedZ = tag.getInt("LinkedZ");
        }

        // 元数据
        placerName = tag.getString("PlacerName");
    }

    // ===== 网络同步 =====

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ===== 粒子效果 =====

    private void makeCookingParticles() {
        if (level == null) return;
        // 只在服务端每 5 tick 生成一次（配合 LIT 更新频率）
        if (level.getGameTime() % 5 != 0) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.6D;
        double z = worldPosition.getZ() + 0.5D;

        serverLevel.sendParticles(ParticleTypes.FLAME,
                x, y, z,
                2, 0.2D, 0.1D, 0.2D, 0.02D);
        serverLevel.sendParticles(ParticleTypes.SMOKE,
                x, y, z,
                3, 0.3D, 0.1D, 0.3D, 0.01D);
    }

    // ===== WorldlyContainer 实现（供 Create 漏斗兼容） =====

    private static final int[] SLOTS = {0};

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return stack.is(ModItems.TWISTED_EMBRYO.get()) && canAcceptInput();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (direction == Direction.UP || direction == Direction.DOWN) return false;
        return stack.is(ModItems.MATURE_EMBRYO.get());
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return cookingStack.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? cookingStack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index == 0 && !cookingStack.isEmpty()) {
            ItemStack result = cookingStack.split(count);
            if (cookingStack.isEmpty()) {
                cookingProgress = 0;
                cookingTotalTime = 0;
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index == 0) {
            ItemStack result = cookingStack;
            cookingStack = ItemStack.EMPTY;
            cookingProgress = 0;
            cookingTotalTime = 0;
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            cookingStack = stack.copyWithCount(1);
            cookingProgress = 0;
            cookingTotalTime = 0;
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        cookingStack = ItemStack.EMPTY;
        cookingProgress = 0;
        cookingTotalTime = 0;
    }

    // ===== Create 漏斗 IItemHandler 能力 =====

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null && side != Direction.DOWN) {
            LazyOptional<IItemHandlerModifiable> handler = handlers[side.get3DDataValue()];
            if (handler != null) {
                return handler.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onChunkUnloaded() {
        for (LazyOptional<IItemHandlerModifiable> h : handlers) {
            if (h != null) h.invalidate();
        }
        super.onChunkUnloaded();
    }

    @Override
    public void invalidateCaps() {
        for (LazyOptional<IItemHandlerModifiable> h : handlers) {
            if (h != null) h.invalidate();
        }
        super.invalidateCaps();
    }
}