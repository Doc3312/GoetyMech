## 1. C2SJumpChargePacket 扩展

- [x] 1.1 增加 `float forward`、`boolean jumping`、`double clientGroundSpeed` 字段
- [x] 1.2 更新 `encode` 写入三个新字段
- [x] 1.3 更新 `decode` 读取三个新字段
- [x] 1.4 更新 `handle` 将新参数传递给 `PrototypeEvents.handleJumpChargeFromClient`

## 2. PrototypeEvents.handleJumpChargeFromClient 更新

- [x] 2.1 修改方法签名接收 `float forward, boolean jumping, double clientGroundSpeed`
- [x] 2.2 方法体第一行调用 `player.setPlayerInput(forward, 0, jumping, false)`
- [x] 2.3 如果 `clientGroundSpeed > 0.001`，写回 `PrototypeItem.setGroundSpeed`
- [ ] 2.4 验证 `applyChargedJump()` 在收到 release 包时使用正确的 `groundSpeed`

## 3. 客户端发送同步（PrototypeRenderEvents）

- [x] 3.1 蓄力第一拍（`clientChargeTicks == 1`）：发送 `forward=player.zza, jumping=jumpDown, clientGroundSpeed=当前客户端 hSpeed`
- [x] 3.2 每 5tick 节流发送：发送 `forward=player.zza, jumping=jumpDown, clientGroundSpeed=0`
- [x] 3.3 release 包（`action=0`）：发送 `forward=player.zza, jumping=jumpDown, clientGroundSpeed=0`
- [x] 3.4 非蓄力时不发送额外输入包（zz=0 按兵不动）

## 4. 诊断清理

- [x] 4.1 删除 `ServerGamePacketListenerImplMixin.java` 中的所有 `@Inject` 方法（保留空类或删除文件）
- [ ] 4.2 可选：减少 `D POST_TRAVEL`、`D START`、`D END` 日志频率或移除

## 5. 验证

- [x] 5.1 `.\gradlew.bat runClient` 编译运行无报错
- [ ] 5.2 测试冲刺蓄力跳：跳跃后水平速度保持在正常范围（目测：不应骤降）
- [ ] 5.3 测试纯冲刺（不蓄力）：不应退步
- [ ] 5.4 测试纯蓄力跳（不冲刺）：应正常垂直弹跳
- [x] 5.5 验证日志中 `HANDLE_INPUT_CALLED` 相关行不再出现
