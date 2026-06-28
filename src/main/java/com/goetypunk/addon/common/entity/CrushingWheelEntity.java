package com.goetypunk.addon.common.entity;

import com.Polarice3.Goety.common.entities.neutral.AbstractObsidianMonolith;
import com.goetypunk.addon.GoetyPunkMod;
import com.goetypunk.addon.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrushingWheelEntity extends Entity {

    @Nullable private UUID ownerUUID;
    private Vec3 startPos;
    private double maxDistance;
    private float damage;
    private Vec3 direction = Vec3.ZERO;
    private final Map<UUID, Integer> hitCooldowns = new HashMap<>();

    private double currentSpeed;
    private int stuckTicks;
    private final double accelPerTick;
    private final double maxSpeed;
    private static final int MAX_LIFETIME_TICKS = 200;

    public CrushingWheelEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.accelPerTick = ModConfig.CRUSHING_WHEEL_ACCELERATION.get() / 400.0;
        this.maxSpeed = ModConfig.CRUSHING_WHEEL_MAX_SPEED.get() / 20.0;
        this.currentSpeed = ModConfig.CRUSHING_WHEEL_SPEED.get() / 20.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (tickCount >= MAX_LIFETIME_TICKS) {
            GoetyPunkMod.LOGGER.debug("Wheel {} reached max lifetime, despawning", getId());
            discard();
            return;
        }

        currentSpeed = Math.min(currentSpeed + accelPerTick, maxSpeed);

        double vy = onGround() ? 0 : getDeltaMovement().y - 0.04;
        Vec3 vel = new Vec3(direction.x * currentSpeed, vy, direction.z * currentSpeed);
        setDeltaMovement(vel);
        move(MoverType.SELF, vel);

        if (horizontalCollision && onGround()) {
            setDeltaMovement(Vec3.ZERO);
            ++stuckTicks;
            if (stuckTicks > 60) {
                discard();
                return;
            }
        } else {
            stuckTicks = 0;
        }

        if (startPos == null || startPos.distanceTo(position()) >= maxDistance) {
            discard();
            return;
        }

        GoetyPunkMod.LOGGER.debug("Wheel tick={} pos={},{},{} vel={},{},{} box={} ground={} collH={}",
                tickCount,
                String.format("%.1f", position().x),
                String.format("%.1f", position().y),
                String.format("%.1f", position().z),
                String.format("%.2f", getDeltaMovement().x),
                String.format("%.2f", getDeltaMovement().y),
                String.format("%.2f", getDeltaMovement().z),
                getBoundingBox(),
                onGround(), horizontalCollision);

        checkEntityCollisions();
    }

    @Override
    public float maxUpStep() {
        return 1.5F;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected AABB makeBoundingBox() {
        float rad = this.getYRot() * Mth.DEG_TO_RAD;
        double c = Math.abs(Mth.cos(rad));
        double s = Math.abs(Mth.sin(rad));
        double rx = 1.0 * c + 0.5 * s;
        double rz = 1.0 * s + 0.5 * c;
        double cx = this.getX();
        double cz = this.getZ() + 0.5;
        return new AABB(cx - rx, this.getY(), cz - rz,
                        cx + rx, this.getY() + 2.0, cz + rz);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    private void checkEntityCollisions() {
        AABB box = getBoundingBox();
        List<Entity> targets = level().getEntities(this, box.inflate(0.5),
                e -> e instanceof LivingEntity && !e.getUUID().equals(ownerUUID));

        GoetyPunkMod.LOGGER.debug("[WheelTest] tick={} box={} inflated={} targets={} owner={}",
                tickCount, box, box.inflate(0.5), targets.size(), ownerUUID);

        for (Entity target : targets) {
            UUID uuid = target.getUUID();
            GoetyPunkMod.LOGGER.debug("[WheelTest] found target id={} pos={} box={} class={}",
                    target.getDisplayName().getString(),
                    target.position(), target.getBoundingBox(),
                    target.getClass().getSimpleName());

            Integer lastHit = hitCooldowns.get(uuid);
            if (lastHit != null && tickCount - lastHit < 10) {
                GoetyPunkMod.LOGGER.debug("[WheelTest] skip {} (cooldown {})", uuid, 10 - (tickCount - lastHit));
                continue;
            }

            LivingEntity owner = getOwner();
            GoetyPunkMod.LOGGER.debug("[WheelTest] owner={} ownerUUID={} damage={}",
                    owner, ownerUUID, damage);

            if (target instanceof AbstractObsidianMonolith monolith) {
                boolean emerging = monolith.isEmerging();
                GoetyPunkMod.LOGGER.debug("[WheelTest] >>> MONOLITH detected! emerging={} empowered={} health={}",
                        emerging, monolith.empowered, monolith.getHealth());
            }

            boolean hurtResult = target.hurt(damageSources().indirectMagic(this, owner), damage);
            GoetyPunkMod.LOGGER.debug("[WheelTest] hurt()={} on {} dmg={}",
                    hurtResult, target.getDisplayName().getString(), damage);

            if (target instanceof AbstractObsidianMonolith monolith) {
                GoetyPunkMod.LOGGER.debug("[WheelTest] after hurt: health={} emerging={} empowered={}",
                        monolith.getHealth(), monolith.isEmerging(), monolith.empowered);
            }

            Vec3 knockDir = direction.scale(3.0).add(0, 1.0, 0);
            target.setDeltaMovement(target.getDeltaMovement().add(knockDir));

            hitCooldowns.put(uuid, tickCount);
        }
    }

    public void setOwnerUUID(UUID uuid) { this.ownerUUID = uuid; }
    public void setDamage(float damage) { this.damage = damage; }
    public void setMaxDistance(double distance) { this.maxDistance = distance; }

    public void setDirection(Vec3 dir) {
        this.direction = dir.normalize();
        this.startPos = position();
    }

    public Vec3 getDirVec() { return direction; }

    @Nullable
    private LivingEntity getOwner() {
        if (ownerUUID == null || !(level() instanceof ServerLevel server)) return null;
        Entity e = server.getEntity(ownerUUID);
        if (e == null) return null;
        return e instanceof LivingEntity ? (LivingEntity) e : null;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
        if (tag.contains("MaxDistance")) this.maxDistance = tag.getDouble("MaxDistance");
        if (tag.contains("Damage")) this.damage = tag.getFloat("Damage");
        if (tag.contains("DirX")) {
            this.direction = new Vec3(tag.getDouble("DirX"), tag.getDouble("DirY"), tag.getDouble("DirZ"));
        }
        if (tag.contains("StartX")) {
            this.startPos = new Vec3(tag.getDouble("StartX"), tag.getDouble("StartY"), tag.getDouble("StartZ"));
        }
        if (tag.contains("Speed")) this.currentSpeed = tag.getDouble("Speed");
        if (tag.contains("StuckTicks")) this.stuckTicks = tag.getInt("StuckTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) tag.putUUID("Owner", this.ownerUUID);
        tag.putDouble("MaxDistance", this.maxDistance);
        tag.putFloat("Damage", this.damage);
        tag.putDouble("DirX", this.direction.x);
        tag.putDouble("DirY", this.direction.y);
        tag.putDouble("DirZ", this.direction.z);
        if (this.startPos != null) {
            tag.putDouble("StartX", this.startPos.x);
            tag.putDouble("StartY", this.startPos.y);
            tag.putDouble("StartZ", this.startPos.z);
        }
        tag.putDouble("Speed", this.currentSpeed);
        tag.putInt("StuckTicks", this.stuckTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
