## Context

单机模式下，`ServerboundPlayerInputPacket` 不被分派到 `ServerGamePacketListenerImpl.handlePlayerInput()`，导致服务端 `player.zza` 永远为 0。蓄力跳水平速度丢失是因为 `applyChargedJump()` 使用的 `groundSpeed` 依赖服务端 `moved`（位置 delta），而 `moved` 在单机下不可靠。

现有设施：
- `C2SJumpChargePacket`: 每 tick 1 和每 5tick 发送一次蓄力进度，release（action=0）触发 `applyChargedJump()`
- `C2SFlightInputPacket`: 已有发送 `float forward` 的完整模式
- `player.setPlayerInput(float forward, float strafe, boolean jumping, boolean sneaking)`: `Player` 类的 public 方法，直接设置 `input` 字段

## Goals / Non-Goals

**Goals:**
- 服务端 `player.zza` 正确反映 W 键状态（通过自定义包同步）
- `groundSpeed` 跟踪可靠，蓄力跳释放时使用正确的地面速度
- 地面起飞检查（`zza > 0.0F`）和空中部署在服务端可正常触发

**Non-Goals:**
- 修复 vanilla 的 `handlePlayerInput` 不触发问题（这是 Minecraft 1.20.1 的正常行为）
- 每 tick 发送输入包（只在蓄力相关的时间点发送，减少网络流量）
- 修改 `strafe`（左右输入）— 仅同步 forward 和 jumping

## Decisions

### Decision 1: 扩展 C2SJumpChargePacket 而非新建包

在现有 packet 中增加 `float forward` 和 `boolean jumping` 字段。

**为什么不是新建独立包：**
- 独立包需要额外注册、额外网络带宽
- 输入同步只在蓄力跳场景需要（地面起飞/空中部署也需要 zza）
- 捆绑发送节省序列化开销

### Decision 2: 在蓄力第一拍（tick 1）、每 5tick、release 时同步 forward+jumping

**模式：**
```java
// tick 1: 附带首次 forward+jumping + 客户端地面速度
C2SJumpChargePacket(1, player.zza, jumpDown, clientGroundSpeed)

// 每 5tick: 附带 forward+jumping
C2SJumpChargePacket(clientChargeTicks, player.zza, jumpDown, 0)

// release: 附带最后一次 forward+jumping
C2SJumpChargePacket(0, player.zza, jumpDown, 0)
```

### Decision 3: 在 handleJumpChargeFromClient 中调用 setPlayerInput

```java
public static void handleJumpChargeFromClient(ServerPlayer player, int action, float forward, boolean jumping, double clientGroundSpeed) {
    // 1. 同步输入
    player.setPlayerInput(forward, 0, jumping, false);

    // 2. 如果客户端上报了可靠的地面速度（tick 1），写回 groundSpeed
    if (clientGroundSpeed > 0.001) {
        PrototypeItem.setGroundSpeed(player, clientGroundSpeed);
    }

    // 3. 原有的蓄力逻辑
    // ...
}
```

### Decision 4: 客户端在 tick 1 上报 groundSpeed

客户端从 `player.getDeltaMovement()` 计算 `hSpeed = sqrt(motion.x^2 + motion.z^2)`，在蓄力第一拍发给服务端。

**为什么：** 客户端的地面速度总是准确的（客户端每 tick 执行 `travel()`），而服务端 `moved` 不可靠。

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| `setPlayerInput` 影响服务端 AI/寻路 | 仅对玩家的 `ServerPlayer` 实例调用，不影响其他实体 |
| 客户端被改包/作弊，上报虚假 groundSpeed | 直接 `setDeltaMovement` 的值需要服务端二次验证（可选）；当前阈值 `>0.001` 做最小过滤 |
| release 包丢失，zza 直到下次蓄力才同步 | 减少影响：地面起飞/空中部署只在非蓄力状态检查，蓄力时玩家在地面，zz=0 不会触发飞行 |
| 玩家未蓄力时 zza 仍然为 0 | 地面起飞需要 W+charge==0，不受影响；空中部署需要 `!isCJ`，略受影响但玩家可以通过按住跳跃键进入飞行（GameTest 中已验证） |
