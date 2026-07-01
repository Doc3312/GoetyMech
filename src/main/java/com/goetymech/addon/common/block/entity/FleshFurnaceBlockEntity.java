package com.goetymech.addon.common.block.entity;

import com.goetymech.addon.common.block.FleshFurnaceBlock;
import com.goetymech.addon.config.ModConfig;
import com.goetymech.addon.init.ModBlocks;
import com.goetymech.addon.init.ModItems;
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
import com.goetymech.addon.init.ModTags;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 琛€鑲夌啍鐐夋柟鍧楀疄浣?- 绾浆鍖栧櫒
 * - 杈撳叆鎵洸鑳氳儙锛岀児鍒跺悗鐩存帴寮瑰嚭鎴愮啛鑳氳儙
 * - 涓嶅瓨鍌ㄤ换浣曠墿鍝侊紝绫讳技鍘熺増鐔旂倝寮瑰埌婕忔枟
 * - 閫氳繃缁戝畾涓嶇ゥ鐏偓鍐冲畾浜у嚭鍏电绫诲瀷
 */
public class FleshFurnaceBlockEntity extends BlockEntity implements WorldlyContainer {

    // === 鐑瑰埗鐘舵€?===
    private ItemStack cookingStack = ItemStack.EMPTY;
    private int cookingProgress;
    private int cookingTotalTime;

    // === 閾炬帴鏁版嵁 ===
    private int linkedX, linkedY, linkedZ;
    private boolean hasLink;

    // === 鍏冩暟鎹?===
    private String placerName = "";

    // === Create 婕忔枟鍏煎 ===
    private final LazyOptional<IItemHandlerModifiable>[] handlers = SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    public FleshFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FLESH_FURNACE_ENTITY.get(), pos, state);
    }

    // ===== 鏍稿績 Tick =====

    public void tick() {
        if (level == null) return;
        boolean isServer = level instanceof ServerLevel;

        // 鐢ㄥ潗鏍囧搱甯屽垎鏁ｆ壂鎻?tick锛岄伩鍏嶅鐔旂倝鍚屾椂骞跺彂
        int scanOffset = Math.abs(worldPosition.hashCode() % 60);
        if (isServer && level.getGameTime() % 60 == scanOffset) {
            // 鐜╁涓嶅湪 32 鏍煎唴 鈫?璺宠繃鏈疆缁存姢
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

        // LIT 鐘舵€佹洿鏂帮紙姣?5 tick锛?
        if (isServer && level.getGameTime() % 5 == 0) {
            boolean shouldBeLit = isCooking();
            BlockState bs = getBlockState();
            if (bs.getValue(BlockStateProperties.LIT) != shouldBeLit) {
                level.setBlock(worldPosition, bs.setValue(BlockStateProperties.LIT, shouldBeLit), 3);
            }
        }

        // === 鐑瑰埗閫昏緫 ===
        if (!cookingStack.isEmpty() && cookingStack.is(ModItems.TWISTED_EMBRYO.get())) {
            if (isServer) {
                // 妫€鏌ラ摼鎺ユ槸鍚︽湁鏁?
                String entityTypeId = getTargetEntityTypeId();
                if (hasLink && entityTypeId != null) {

                    // ===== 鏍囩妫€娴嬶細闇€瀹屾暣璁粌缁撴瀯鐨勯珮绾у叺绉?鈫?鎷掔粷杞寲 =====
                    if (isEntityRestricted(entityTypeId)) {
                        notifyNearbyPlayers(Component.translatable("info.goetymech.furnace.no_magic_talent"));
                        cookingProgress = 0;
                        cookingTotalTime = 0;
                        tryOutputToContainer(cookingStack);
                        cookingStack = ItemStack.EMPTY;
                        markUpdated();
                        return;
                    }

                    // 棣栨鏀惧叆鎴栬繘搴︿负0鏃跺垵濮嬪寲鎬绘椂闂?
                    if (cookingProgress == 0) {
                        cookingTotalTime = ModConfig.cachedFurnaceCookingTime * 20;
                        if (cookingTotalTime < 20) cookingTotalTime = 20;
                    }

                    // 绮掑瓙鏁堟灉
                    makeCookingParticles();

                    cookingProgress++;

                    // 瀹氭湡鍚屾杩涘害缁欏鎴风锛堟瘡 20 tick锛?
                    if (level.getGameTime() % 20 == 0) {
                        markUpdated();
                    }

                    // 鐑瑰埗瀹屾垚 鈫?浜у嚭锛堜骇鐗╃暀鍦ㄦЫ浣嶄緵婕忔枟鎶藉彇锛?
                    if (cookingProgress >= cookingTotalTime) {
                        cookingProgress = 0;

                        // 鍒涘缓鎴愮啛鑳氳儙
                        ItemStack matureStack = new ItemStack(ModItems.MATURE_EMBRYO.get(), 1);
                        CompoundTag tag = matureStack.getOrCreateTag();
                        tag.putString("EntityTypeId", entityTypeId);
                        matureStack.setTag(tag);

                        // 浜х墿鏀惧叆妲戒綅锛岀瓑寰呮紡鏂椾粠渚ч潰鎶藉彇
                        cookingStack = matureStack;
                        cookingTotalTime = 0;

                        // 鐑瑰埗瀹屾垚闊虫晥
                        level.playSound(null, worldPosition, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 0.6F);

                        markUpdated();
                    }
                    } else {
                        // 鏈摼鎺ユ垨閾炬帴鏃犳晥锛屽脊鍑虹墿鍝佸苟鎻愮ず閿欒
                        cookingProgress = 0;
                        cookingTotalTime = 0;
                        tryOutputToContainer(cookingStack);
                        cookingStack = ItemStack.EMPTY;
                        markUpdated();
                    }
            }
        }
    }

    // ===== 瀹瑰櫒杈撳嚭 =====

    /**
     * 浼樺厛鍚戜笅鏂瑰鍣ㄨ緭鍑猴紝鏃犲鍣ㄦ椂鎵嶅脊鍑虹墿鍝佸疄浣?
     */
    private void tryOutputToContainer(ItemStack stack) {
        if (level == null) return;
        // 妫€鏌ヤ笅鏂规柟鍧楁槸鍚︽湁鐗╁搧澶勭悊鑳藉姏锛堟紡鏂椼€佺瀛愩€佺閬撶瓑锛?
        BlockPos belowPos = worldPosition.below();
        BlockEntity belowBE = level.getBlockEntity(belowPos);
        if (belowBE != null) {
            IItemHandler handler = belowBE.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).resolve().orElse(null);
            if (handler != null) {
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, stack, false);
                if (remaining.isEmpty()) {
                    return; // 鍏ㄩ儴閫佸叆瀹瑰櫒
                }
                // 瀹瑰櫒婊′簡 鈫?寮瑰嚭鍓╀綑閮ㄥ垎
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, remaining);
                return;
            }
        }
        // 鏃犲鍣ㄦ垨瀹瑰櫒涓嶅吋瀹?鈫?鐩存帴寮瑰嚭
        Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, stack);
    }

    // ===== 鐑瑰埗鎺у埗 =====

    /**
     * 鏄惁鍙互鎺ュ彈鏂扮殑鑳氳儙杈撳叆
     * 蹇呴』宸查摼鎺ュ埌鏈夋晥鐨勪笉绁ョ伀鐐笖鍐呴儴涓虹┖
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

    // ===== HUD 鏁版嵁 =====

    public double getCookingProgress() {
        if (cookingTotalTime <= 0) return 0;
        return (double) cookingProgress / cookingTotalTime;
    }

    public int getProgressPercent() {
        return (int) (getCookingProgress() * 100);
    }

    // ===== 閾炬帴绯荤粺 =====

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
     * 浠庝繚瀛樼殑閾炬帴浣嶇疆鑾峰彇鐩爣瀹炰綋绫诲瀷
     */
    @Nullable
    public String getTargetEntityTypeId() {
        if (!hasLink || level == null) return null;
        BlockPos pyrePos = getLinkPosition();
        if (pyrePos == null || !level.isLoaded(pyrePos)) return null;

        BlockEntity be = level.getBlockEntity(pyrePos);
        if (be instanceof com.Polarice3.Goety.common.blocks.entities.OminousPyreBlockEntity pyre) {
            String id = pyre.getEntityTrainTo().getString("id");
            // 鐏偓鏃犺缁冪洰鏍囨椂杩斿洖 null锛岄樆姝㈢┖ ID 鐑瑰埗
            if (id.isEmpty()) return null;
            return id;
        }
        return null;
    }

    /**
     * 鑾峰彇鐩爣鍏电鐨勬樉绀哄悕绉帮紙鐢ㄤ簬 HUD锛?
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
     * 鑷姩鎵弿闄勮繎 xz 卤8 鏍煎唴鐨勪笉绁ョ伀鐐?
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

    // ===== 鍏电闄愬埗妫€娴?=====

    private static final Map<EntityType<?>, Boolean> spellcasterCache = new HashMap<>();

    /**
     * 鍔ㄦ€佸垽鏂疄浣撶被鍨嬫槸鍚﹂渶瑕佸畬鏁磋缁冪粨鏋?
     * 1. 娉曟湳绯诲叺绉嶏紙SpellcasterIllagerServant锛夐渶瑕佽缁?
     * 2. 鏍囩琛ラ泦澶勭悊闈炴硶鏈絾闇€闄愬埗鐨勭壒渚嬶紙濡?StormCaster锛?
     * 瀵瑰簲 Neollager.canTrain() 涓殑 instanceof SpellcasterIllagerServant 閫昏緫
     * 缁撴灉缂撳瓨鍒伴潤鎬?map锛岄伩鍏嶆瘡 tick 瀹炰緥鍖栧疄浣?
     */
    private boolean isEntityRestricted(String entityTypeId) {
        ResourceLocation rl = ResourceLocation.tryParse(entityTypeId);
        if (rl == null) return false;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);
        if (type == null || level == null) return false;

        // 1. 鏍囩琛ラ泦锛氬鐞嗛潪 Spellcaster 浣嗕粛闇€闄愬埗鐨勭壒渚嬶紙濡?StormCaster锛?
        if (type.is(ModTags.REQUIRES_TRAINING_STRUCTURE)) return true;

        // 2. 娉曟湳绯诲叺绉嶉渶瑕佸畬鏁磋缁冪粨鏋勶紙缁撴灉缂撳瓨锛?
        return spellcasterCache.computeIfAbsent(type, t -> {
            Entity e = t.create(level);
            boolean isSpellcaster = e instanceof SpellcasterIllagerServant;
            if (e != null) e.discard();
            return isSpellcaster;
        });
    }

    /**
     * 閫氱煡闄勮繎 8 鏍煎唴鐨勭帺瀹舵秷鎭紙action bar锛?
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

    // ===== 鎷ユ湁鑰?=====

    public String getPlacerName() {
        return placerName;
    }

    public void setPlacerName(String name) {
        this.placerName = name;
        setChanged();
    }

    // ===== 鏁版嵁鎸佷箙鍖?=====

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        // 鐑瑰埗鏁版嵁
        if (!cookingStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            cookingStack.save(stackTag);
            tag.put("CookingStack", stackTag);
        }
        tag.putInt("CookingProgress", cookingProgress);
        tag.putInt("CookingTotalTime", cookingTotalTime);

        // 閾炬帴鏁版嵁
        tag.putBoolean("HasLink", hasLink);
        if (hasLink) {
            tag.putInt("LinkedX", linkedX);
            tag.putInt("LinkedY", linkedY);
            tag.putInt("LinkedZ", linkedZ);
        }

        // 鍏冩暟鎹?
        tag.putString("PlacerName", placerName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // 鐑瑰埗鏁版嵁
        if (tag.contains("CookingStack")) {
            cookingStack = ItemStack.of(tag.getCompound("CookingStack"));
        } else {
            cookingStack = ItemStack.EMPTY;
        }
        cookingProgress = tag.getInt("CookingProgress");
        cookingTotalTime = tag.getInt("CookingTotalTime");

        // 閾炬帴鏁版嵁
        hasLink = tag.getBoolean("HasLink");
        if (hasLink) {
            linkedX = tag.getInt("LinkedX");
            linkedY = tag.getInt("LinkedY");
            linkedZ = tag.getInt("LinkedZ");
        }

        // 鍏冩暟鎹?
        placerName = tag.getString("PlacerName");
    }

    // ===== 缃戠粶鍚屾 =====

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

    // ===== 绮掑瓙鏁堟灉 =====

    private void makeCookingParticles() {
        if (level == null) return;
        // 鍙湪鏈嶅姟绔瘡 5 tick 鐢熸垚涓€娆★紙閰嶅悎 LIT 鏇存柊棰戠巼锛?
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

    // ===== WorldlyContainer 瀹炵幇锛堜緵 Create 婕忔枟鍏煎锛?=====

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

    // ===== Create 婕忔枟 IItemHandler 鑳藉姏 =====

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