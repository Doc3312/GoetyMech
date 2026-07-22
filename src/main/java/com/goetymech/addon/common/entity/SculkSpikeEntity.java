package com.goetymech.addon.common.entity;

import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SRepositionPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 幽匿尖刺 + 定身锚点实体
 * 视觉效果：从地面升起后消失
 * 定身：每 tick 将锁定目标强制归位（仿 Goety TangleEntity 机制）
 */
public class SculkSpikeEntity extends Entity {

    private static final EntityDataAccessor<Float> RISE =
            SynchedEntityData.defineId(SculkSpikeEntity.class, EntityDataSerializers.FLOAT);

    private static final int RISE_DURATION = 40;  // 升起阶段 2 秒
    private static final int DEFAULT_LIFE = 60;   // 默认存活（尖刺视觉）

    private int life = DEFAULT_LIFE;
    private int lifeSpan = DEFAULT_LIFE;

    @Nullable
    private LivingEntity target;
    @Nullable
    private UUID targetUUID;

    // 首次绑定后的 tick 计数，用于限制发包频率
    private int bindTick;

    public SculkSpikeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;   // 始终渲染
    }

    // ==== 设置定身目标与存活时间 ====

    public void setBindTarget(@Nullable LivingEntity target) {
        this.target = target;
        this.targetUUID = target == null ? null : target.getUUID();
    }

    @Nullable
    public LivingEntity getBindTarget() {
        if (this.target == null && this.targetUUID != null && this.level() instanceof ServerLevel) {
            Entity entity = ((ServerLevel) this.level()).getEntity(this.targetUUID);
            if (entity instanceof LivingEntity) {
                this.target = (LivingEntity) entity;
            }
        }
        return this.target;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
        this.life = lifeSpan;
    }

    // ==== Tick ====

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        this.bindTarget();

        if (life > lifeSpan - RISE_DURATION) {
            float progress = 1.0F - (float)(life - (lifeSpan - RISE_DURATION)) / RISE_DURATION;
            this.entityData.set(RISE, Math.min(progress, 1.0F));
        }

        if (--life <= 0) {
            this.discard();
        }
    }

    /**
     * 定身逻辑 — 仿 Goety TangleEntity.tangleTarget()
     * 尖刺实体作为独立实体，tick 在目标之后执行，可覆盖 AI 移动
     */
    private void bindTarget() {
        LivingEntity t = this.getBindTarget();
        if (t == null || !t.isAlive()) return;
        if (this.life <= 0) return;

        t.setPos(this.position());
        t.setDeltaMovement(Vec3.ZERO);
        t.move(MoverType.SELF, Vec3.ZERO);
        t.moveRelative(0.0F, Vec3.ZERO);

        // 客户端同步：前 10 tick 每 tick 发包，之后依赖 setPos 同步
        if (this.bindTick < 10) {
            ModNetwork.sentToTrackingEntityAndPlayer(t,
                    new SRepositionPacket(t.getId(), this.position()));
        }
        this.bindTick++;
    }

    /** 客户端读取 rising 值用于渲染偏移 */
    public float getRise() {
        return this.entityData.get(RISE);
    }

    // ==== NBT ====

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Life")) this.life = tag.getInt("Life");
        if (tag.contains("LifeSpan")) this.lifeSpan = tag.getInt("LifeSpan");
        if (tag.contains("Rise")) this.entityData.set(RISE, tag.getFloat("Rise"));
        if (tag.hasUUID("BindTarget")) this.targetUUID = tag.getUUID("BindTarget");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", this.life);
        tag.putInt("LifeSpan", this.lifeSpan);
        tag.putFloat("Rise", this.getRise());
        if (this.targetUUID != null) tag.putUUID("BindTarget", this.targetUUID);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RISE, 0.0F);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}