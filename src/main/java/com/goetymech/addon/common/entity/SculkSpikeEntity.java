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
 * 骞藉尶灏栧埡 + 瀹氳韩閿氱偣瀹炰綋
 * 瑙嗚鏁堟灉锛氫粠鍦伴潰鍗囪捣鍚庢秷澶? * 瀹氳韩锛氭瘡 tick 灏嗛攣瀹氱洰鏍囧己鍒跺綊浣嶏紙浠?Goety TangleEntity 鏈哄埗锛? */
public class SculkSpikeEntity extends Entity {

    private static final EntityDataAccessor<Float> RISE =
            SynchedEntityData.defineId(SculkSpikeEntity.class, EntityDataSerializers.FLOAT);

    private static final int RISE_DURATION = 40;  // 鍗囪捣闃舵 2 绉?
    private static final int DEFAULT_LIFE = 60;   // 榛樿瀛樻椿锛堝皷鍒鸿瑙夛級

    private int life = DEFAULT_LIFE;
    private int lifeSpan = DEFAULT_LIFE;

    // 瀹氳韩鐩爣
    @Nullable
    private LivingEntity target;
    @Nullable
    private UUID targetUUID;

    // 棣栨缁戝畾鍚庣殑 tick 璁℃暟锛岀敤浜庨檺鍒跺彂鍖呴鐜?
    private int bindTick;

    public SculkSpikeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;   // 濮嬬粓娓叉煋
    }

    // ==== 璁剧疆瀹氳韩鐩爣涓庡瓨娲绘椂闂?====

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

        // 瀹氳韩锛氭瘡 tick 灏嗙洰鏍囧己鍒舵媺鍥為敋鐐逛綅缃?
        this.bindTarget();

        // 鍗囪捣鍔ㄧ敾
        if (life > lifeSpan - RISE_DURATION) {
            float progress = 1.0F - (float)(life - (lifeSpan - RISE_DURATION)) / RISE_DURATION;
            this.entityData.set(RISE, Math.min(progress, 1.0F));
        }

        if (--life <= 0) {
            this.discard();
        }
    }

    /**
     * 瀹氳韩閫昏緫 鈥?浠?Goety TangleEntity.tangleTarget()
     * 灏栧埡瀹炰綋浣滀负鐙珛瀹炰綋锛宼ick 鍦ㄧ洰鏍囦箣鍚庢墽琛岋紝鍙鐩?AI 绉诲姩
     */
    private void bindTarget() {
        LivingEntity t = this.getBindTarget();
        if (t == null || !t.isAlive()) return;
        if (this.life <= 0) return;

        // 寮哄埗褰掍綅
        t.setPos(this.position());
        t.setDeltaMovement(Vec3.ZERO);
        t.move(MoverType.SELF, Vec3.ZERO);
        t.moveRelative(0.0F, Vec3.ZERO);

        // 瀹㈡埛绔悓姝ワ細鍓?10 tick 姣?tick 鍙戝寘锛屼箣鍚庝緷璧?setPos 鍚屾
        if (this.bindTick < 10) {
            ModNetwork.sentToTrackingEntityAndPlayer(t,
                    new SRepositionPacket(t.getId(), this.position()));
        }

        this.bindTick++;
    }

    /** 瀹㈡埛绔鍙?rising 鍊肩敤浜庢覆鏌撳亸绉?*/
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