## Why

当玩家在冲刺状态中执行蓄力跳时，水平速度从 ~0.27 blocks/tick（正常冲刺速度）骤降至 ~0.005 blocks/tick。蓄力跳的 `CJ_INJECT` 注入的目标水平速度仅 ~0.005 — 几乎为零，导致跳跃后玩家像是被"定住"了。

## 诊断历程与错误复盘

### 错误 1：假设 handlePlayerInput 会正常触发

最初认为服务端 `player.zza == 0` 是因为 `handlePlayerInput` 被调用时传入了错误的值。添加 Mixin 注入 `handlePlayerInput` 的 HEAD/RETURN 后，发现 `HANDLE_INPUT_CALLED` **从未出现在日志中**。而对照组的 `HANDLE_MOVE_PLAYER` 和 `HANDLE_PLAYER_COMMAND` 均正常触发。

### 错误 2：怀疑 PrototypeTravelMixin 的 elytra travel 覆盖

在 `applyChargedJump` 后，玩家水平速度丢失。一度认为 `PrototypeTravelMixin` 的 `travel()` HEAD 注入在玩家跳起后（onGround=false）错误拦截了 travel 逻辑。实际 `CJ_INJECT` 日志显示 `targetHSpeed = 0.0051` — 注入的值本身就很小，不是 travel 覆盖导致。

### 错误 3：认为 groundSpeed 跟踪可靠

`groundSpeed = sqrt((x-xo)^2 + (z-zo)^2)` 依赖服务端玩家的位置变化。但单机模式下服务端通过 `handleMovePlayer` 接收的位置包并非每 tick 都更新（`moved` 值在 `0.27` 和 `0.0` 之间随机出现）。`applyChargedJump()` 读取时 groundSpeed 可能是过期值。

### 根因总结

1. **`handlePlayerInput()` 在单机模式下从未被调用** — 这是 Minecraft 1.20.1 的正常行为，服务端玩家的 `input` 字段不通过 vanilla 网络包更新
2. 服务端 `player.zza` 永为 `0.0`，`player.jumping` 也同理
3. `groundSpeed` 依赖 `moved`（服务端位置 delta），而 `moved` 不可靠
4. 蓄力跳释放时 `applyChargedJump()` 使用不可靠的 `groundSpeed` 做方向向量计算，导致水平速度注入为零或极小值

## What Changes

- 扩展 `C2SJumpChargePacket`，增加 `float forward` 和 `boolean jumping` 字段，从客户端传输到服务端
- 在 `C2SJumpChargePacket.handle()` 中，直接调用 `player.setPlayerInput(forward, 0, jumping, false)` 覆盖服务端玩家的输入状态
- 改进 `groundSpeed` 跟踪：在客户端蓄力初始包（tick 1）中携带客户端实测的地面速度（`sqrt(motion.x^2 + motion.z^2)`），服务端收到后写回 `groundSpeed`
- 移除 `ServerGamePacketListenerImplMixin.java` 的诊断日志（调试完成后的清理）
- 确保 `zza > 0.0F` 检查（地面起飞、空中部署）在服务端也能正常触发

## Capabilities

### New Capabilities
- `charge-input-sync`: 从客户端向服务端同步 `forward`（W 键）和 `jumping` 状态的网络能力，确保服务端 `player.zza` 和 `player.jumping` 正确反映玩家实际输入

### Modified Capabilities

None.

## Impact

- `src/main/java/com/goetypunk/addon/common/net/c2s/C2SJumpChargePacket.java` — 增加 `forward` 和 `jumping` 字段，encode/decode 更新
- `src/main/java/com/goetypunk/addon/common/events/PrototypeEvents.java` — `handleJumpChargeFromClient` 接收额外参数，调用 `setPlayerInput`；`groundSpeed` 跟踪兼容客户端上报
- `src/main/java/com/goetypunk/addon/client/PrototypeRenderEvents.java` — 发送蓄力包（tick 1）时附带 `forward` 和 `jumping` 值；发送客户端当前地面速度
- `src/main/java/com/goetypunk/addon/mixin/ServerGamePacketListenerImplMixin.java` — 删除诊断日志注入（清理）
