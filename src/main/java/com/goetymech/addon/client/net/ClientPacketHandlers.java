package com.goetymech.addon.client.net;

import com.goetymech.addon.client.ClientLevelExpandedContext;
import com.goetymech.addon.client.ModClientEvents;
import com.goetymech.addon.client.PrototypeRenderEvents;
import com.goetymech.addon.client.SlamGroundTiltHandler;
import com.goetymech.addon.client.StressArkHUD;
import com.goetymech.addon.client.TimeStopUtilsWrapped;
import com.goetymech.addon.common.LevelEC;
import com.goetymech.addon.common.capabilities.SoulDominionProvider;
import com.goetymech.addon.common.capabilities.StressShieldProvider;
import com.goetymech.addon.common.item.PrototypeItem;
import com.goetymech.addon.common.net.s2c.S2CAssimilationDataPacket;
import com.goetymech.addon.common.net.s2c.S2CSetFreecamPacket;
import com.goetymech.addon.common.net.s2c.S2CSlamImpactPacket;
import com.goetymech.addon.common.net.s2c.S2CSlamStatePacket;
import com.goetymech.addon.common.net.s2c.S2CSoulDominionSyncPacket;
import com.goetymech.addon.common.net.s2c.S2CStressArkDataPacket;
import com.goetymech.addon.common.net.s2c.S2CStressShieldUpdatePacket;
import com.goetymech.addon.common.net.s2c.S2CTimeStopDimensionPacket;
import com.goetymech.addon.common.net.s2c.S2CTimeStopStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 客户端网络包处理器。
 * 所有 S2C 包的客户端逻辑集中于此，由 common 层通过 DistExecutor 转发调用，
 * 确保服务端（DEDICATED_SERVER）加载 packet 类时不引用任何客户端专用类。
 */
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {
    }

    public static void handleSlamImpact(S2CSlamImpactPacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());

        SlamGroundTiltHandler.startEffect(pos, packet.getGroundDeformRadius());

        level.playLocalSound(
                packet.getX(), packet.getY(), packet.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                2.0F, 0.8F + level.random.nextFloat() * 0.4F, false);

        int particleCount = Math.max(30, (int) (packet.getRadius() * 8));
        for (int i = 0; i < particleCount; i++) {
            double ox = (level.random.nextDouble() - 0.5) * packet.getRadius() * 2;
            double oz = (level.random.nextDouble() - 0.5) * packet.getRadius() * 2;
            if (Math.sqrt(ox * ox + oz * oz) > packet.getRadius()) continue;

            BlockPos bp = BlockPos.containing(packet.getX() + ox, packet.getY() - 0.5, packet.getZ() + oz);
            var state = level.getBlockState(bp);
            if (state.isAir()) continue;

            level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    bp.getX() + 0.5, bp.getY() + 1.0, bp.getZ() + 0.5,
                    (level.random.nextDouble() - 0.5) * 2,
                    level.random.nextDouble() * 1.5,
                    (level.random.nextDouble() - 0.5) * 2);
        }
    }

    public static void handleSlamState(S2CSlamStatePacket packet) {
        PrototypeRenderEvents.setClientSlamming(packet.isSlamming());
    }

    public static void handleTimeStopState(S2CTimeStopStatePacket packet) {
        if (packet.isTimeStop()) {
            TimeStopUtilsWrapped.enable(packet.getUser());
        } else {
            TimeStopUtilsWrapped.disable();
        }
    }

    public static void handleTimeStopDimension(S2CTimeStopDimensionPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ClientLevelExpandedContext ctx2 = (ClientLevelExpandedContext)
                ((LevelEC) mc.level).gp$levelECData();
        if (!packet.getToAddDimensionID().getPath().isEmpty()) {
            ResourceKey<Level> toAdd = ResourceKey.create(Registries.DIMENSION, packet.getToAddDimensionID());
            if (mc.level.dimension() == toAdd) {
                ctx2.currentTimeStopDimension = toAdd;
            }
        }
        if (!packet.getToRemoveDimensionID().getPath().isEmpty()) {
            ResourceKey<Level> toRemove = ResourceKey.create(Registries.DIMENSION, packet.getToRemoveDimensionID());
            if (mc.level.dimension() == toRemove) {
                ctx2.currentTimeStopDimension = null;
            }
        }
    }

    public static void handleStressShieldUpdate(S2CStressShieldUpdatePacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var entity = level.getEntity(packet.getEntityId());
        if (entity == null) return;
        entity.getCapability(StressShieldProvider.CAPABILITY).ifPresent(cap -> {
            cap.setShield(packet.getShield());
            cap.setMaxShield(packet.getMaxShield());
            cap.setExpireTick(packet.getExpireTick());
            cap.setLastHurtTick(packet.getLastHurtTick());
        });
    }

    public static void handleStressArkData(S2CStressArkDataPacket packet) {
        if (packet.getTotalCapacity() < 0) {
            StressArkHUD.clearData();
        } else {
            StressArkHUD.onData(packet.getTotalCapacity(), packet.getTotalStress(),
                    packet.getNetworkCount(), packet.getEngineConsumption());
        }
    }

    public static void handleSoulDominionSync(S2CSoulDominionSyncPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var entity = level.getEntity(packet.getEntityId());
        if (entity == null) return;
        entity.getCapability(SoulDominionProvider.CAPABILITY).ifPresent(cap -> {
            cap.setActive(packet.isActive());
            cap.setExpireTick(packet.getExpireTick());
            cap.setLinkedServants(packet.getLinkedServants());
            cap.setRange(packet.getRange());
            cap.setDuration(packet.getDuration());
        });
    }

    public static void handleAssimilationData(S2CAssimilationDataPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PrototypeItem.setAssimilationKills(player, packet.getKills());
        }
    }

    public static void handleSetFreecam(S2CSetFreecamPacket packet) {
        ModClientEvents.freecamActive = packet.isActive();
    }

    public static void spawnCraterBreakParticles(Level level, BlockPos pos, BlockState originalState, int maxLifetime, int lifetime) {
        if (originalState.shouldSpawnParticlesOnBreak() && maxLifetime - lifetime < 10) {
            if (level.random.nextInt(3) == 0) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, originalState),
                        pos.getX() + level.random.nextDouble(),
                        pos.getY() + 0.5,
                        pos.getZ() + level.random.nextDouble(),
                        (level.random.nextDouble() - 0.5) * 0.3,
                        level.random.nextDouble() * 0.5,
                        (level.random.nextDouble() - 0.5) * 0.3);
            }
        }
    }
}
