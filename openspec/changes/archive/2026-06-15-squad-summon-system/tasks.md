## 1. 注册基础设施

- [x] 1.1 新增 `ModMenuTypes.java` — 注册 ContainerType (DeferredRegister<MenuType<?>>)
- [x] 1.2 `ModConfig.java` — 新增配置项：SQUAD_SUMMON_COOLDOWN, SQUAD_TIER2_DISCOUNT (0.5), SQUAD_TIER3_DISCOUNT (0.1), SQUAD_LIFESPAN (6000)
- [x] 1.3 `ModBlocks.java` — 注册 SquadBarracksBlock + BlockEntityType
- [x] 1.4 `ModItems.java` — 注册 SquadFocus + SquadBarracks BlockItem
- [x] 1.5 `GoetyPunkMod.java` — 注册 ModMenuTypes.CONTAINER_TYPE
- [x] 1.6 `ModClientEvents.java` — 注册 SquadBarracksScreen (MenuScreens.register)
- [x] 1.7 添加 lang key：方块名、聚晶名、GUI 标题、tooltip 文本、提示信息

## 2. 点将台方块 (SquadBarracksBlock)

- [x] 2.1 创建 `SquadBarracksBlock` — extends BaseEntityBlock, IntegerProperty TIER (1-3), 右键交互
- [x] 2.2 右键手持升级材料 → 升阶逻辑（苍白金属→二阶、黑暗金属→三阶、下界合金无效）
- [x] 2.3 右键空闲手 → 打开 GUI（NetworkHooks.openScreen）
- [x] 2.4 右键持 SquadFocus → 触发绑定（委托给聚晶）
- [x] 2.5 破坏时掉落所有内容 + 移除 chunk ticket
- [x] 2.6 放置时设置拥有者 + 注册 chunk ticket

## 3. 点将台方块实体 (SquadBarracksBlockEntity) [已修改]

- [x] 3.1 创建 `SquadBarracksBlockEntity` — 实现 WorldlyContainer（基础结构保留）
- [x] 3.2 容器大小：squad 位返回空 ItemStack（虚配置），reserve 扩至 27 格（3×9）
- [x] 3.3 Squad slot 不再接受物品；Reserve slot 接受 MatureEmbryo
- [x] 3.4 `getSquadComposition()` — 改为读 `List<ResourceLocation> squadConfig`
- [x] 3.5 `consumeSquad(ServerLevel, Player)` — 根据 squadConfig 从 reserve 扣取
- [x] 3.6 已移除 `restockFromReserve()`
- [x] 3.7 全方向 IItemHandler（保留）
- [x] 3.8 拥有者存储 + 权限检查（保留）
- [x] 3.9 NBT 持久化：新增 SquadConfig ListTag 持久化
- [x] 3.10 网络同步：squadConfig 随 getUpdatePacket 发送
- [x] 3.11 `setSquadConfigSlot(int, ResourceLocation)`
- [x] 3.12 `clearSquadConfigSlot(int)`

## 4. 点将台 GUI [已修改]

- [x] 4.1 `SquadBarracksMenu` — 配置位 locked slot（`mayPlace=false`, `mayPickup=false`）
- [x] 4.2 配置位 6/12/18 格只显示；储备位 3×9（27 格），起点 x=8
- [x] 4.3 配置位不接受物品；储备位 maxStack=16 只接受 MatureEmbryo
- [x] 4.4 数据同步：squadConfig 通过 BE 同步
- [x] 4.5 `SquadBarracksScreen` 保留
- [x] 4.6 配置位渲染：从 `barracks.getSquadConfigSlot()` 读 entity type 显示名
- [x] 4.7 `mouseClicked()` — 配置位点击 + 手持胚胎 → 发包设置
- [x] 4.8 纹理引用：根据 tier 加载 `squad_barracks_t1/2/3.png`
- [x] 4.9 `SquadConfigC2SPacket` — C2S 包已创建并注册

## 5. 兵团聚晶 (SquadFocus)

- [x] 5.1 创建 `SquadFocus` — extends MagicFocus，绑定 SquadSpell
- [x] 5.2 右键点将台 → 绑定：NBT 存 BlockPos + dimension + 缓存 squad composition
- [x] 5.3 Shift+右键空气 → 清除绑定
- [x] 5.4 `onLeftClickEntity` — 空实现（不做 TroopFocus/CallFocus 的实体绑定）
- [x] 5.5 覆写 `appendHoverText` — 显示绑定状态、缓存阵容、每单位消耗说明
- [x] 5.6 `defaultSoulCost() = 0`（实际消耗在 SpellResult 中处理）

## 6. 兵团法术 (SquadSpell)

- [x] 6.1 创建 `SquadSpell` — extends Spell
- [x] 6.2 `SpellResult()` — 核心流程（已适配新 consumeSquad 签名）：
  - 解析聚晶 NBT → BlockPos + dimension
  - 获取 ServerLevel（三阶跨维度 → server.getLevel(dim)）
  - 验证方块存在 + 权限
  - 读取 tier → 折扣率
  - 读取 squad composition（从 squadConfig） → 计算总灵魂消耗
  - 校验灵魂 → `SEHelper.decreaseSouls()`
  - 调用 `blockEntity.consumeSquad()` — **改为从 reserve 扣取**
  - 圆形排列 → 遍历 spawn（复用 MinionSpawnHelper.spawnMinion）
  - 每个实体 `setHasLifespan(true)` + `setLifespan(config.lifespan)`
  - 设置冷却
- [x] 6.3 `defaultSoulCost()` 返回 0
- [x] 6.4 `defaultSpellCooldown()` 从 ModConfig 读取
- [x] 6.5 圆形位置计算：`getCirclePositions(totalCount, radius, center)` 均匀分布

## 7. 限时仆从 (Timed Servants)

- [x] 7.1 在 `MinionSpawnHelper.spawnMinion()` 末尾加：`mob.setHasLifespan(true); mob.setLifespan(config.lifespan)`
- [x] 7.2 可选：`SquadFocus` 召唤时额外设置，区分普通成熟胚胎（无限时）和聚晶召唤（5分钟）

## 8. Chunk 常加载

- [x] 8.1 创建 `TicketType<BlockPos> SQUAD_BARRACKS` 用于 chunk 加载
- [x] 8.2 方块放置时：`serverLevel.getChunkSource().addRegionTicket(TICKET_TYPE, chunkPos, 2, blockPos)`
- [x] 8.3 方块破坏时：`serverLevel.getChunkSource().removeRegionTicket(TICKET_TYPE, chunkPos, 2, blockPos)`
- [x] 8.4 服务端关闭时清理所有残留 ticket（WorldTick 事件或 onChunkUnloaded 检查）

## 9. 验证

- [x] 9.1 重编译：`gradlew compileJava` 无报错
- [ ] 9.2 手持胚胎点击配置位 → 设置成功、胚胎返回、显示 spawn egg 图标
- [ ] 9.3 空手点击配置位 → 清除配置
- [ ] 9.4 召唤测试：全满无提示/部分黄字/零消耗红字
- [ ] 9.5 纹理验证：左右布局，配置位(3×6) spawn egg + 储备位(6×3)实物
- [ ] 9.6 储备位扩容 6×3=18 格验证
- [ ] 9.7 升阶测试：1阶→6解锁、2阶→12解锁、3阶→全18解锁
- [ ] 9.8 跨维度/折扣/时限/漏斗测试（由用户测试）
