# Remote Storage Focus — Timeline

## 2026-07-10

- **创建物品 + 法术**：RemoteStorageFocus（继承 MagicFocus）+ RemoteStorageSpell（继承 Spell）
  - 右键 Stock Ticker 方块绑定 NBT（坐标 + 维度 + freqId）
  - Shift+右键解绑
  - 施法跨维度远程打开 Create 请求下单界面
  - 灵魂消耗 5，冷却 5 秒（100 tick）
  - 原因：用户需求，远程访问 Create 物流网络
  - 关联：openspec change `remote-storage-focus`

## 2026-07-10 (fix)

- **修复瞬间施法**：`defaultCastDuration()` 20→0
  - 原因：用户反馈需要长按 1 秒才能施法，应该是瞬间施法
  - Goety 的 `DarkWand.use()` 中 `castDuration>0` 走 `isNotInstant`（长按路径），`==0` 走 `notTouch`（瞬间路径）
- **修复轮盘切换后无法打开面板**：同一修改可能同时解决
  - 根因：Goety `CSwapFocusPacket` 的 `goety-dirty` 双翻转导致客户端 capability 不同步
  - `castDuration=20` 时服务端走 `isNotInstant`（需等 20 tick），客户端走 `notTouch`（瞬时），时序错配可能导致 `RELEASE_USE_ITEM` 取消服务端施法
  - `castDuration=0` 后两端都走 `notTouch`，服务端在 `use()` 中同步调 `SpellResult`→`openScreen`，无等待窗口
- **修复移动/滑翔时面板关闭**：改用自己的 `MenuProvider`，创建 `StockKeeperCategoryMenu` 的匿名子类覆写 `stillValid` 跳过距离检查
  - 根因：`StockKeeperCategoryMenu.stillValid` 检查 `player.getBlockReach() + 4` 的距离，移动时超出阈值
  - 改用 `!contentHolder.isRemoved()`（只检查 BE 是否存在）
- **跨维度远程支持（尝试）**：`S2CRegisterStockCachePacket` + `CrossDimStockCache` + `LevelMixin` 注入 `getBlockEntity`
  - 原理：服务端发送注册包创建假 BE → `Level.getBlockEntity` 在客户端找不到时走缓存
  - 约束：库存数据不自动刷新（依赖 `divideAndSendTo` 的初始推送）
- **3.1 玩家投递**：`PackagerBlockEntityMixin` @Inject TAIL 拦截打包机输出，地址匹配玩家名时 `ItemHandlerHelper.giveItemToPlayer()` 直接给包裹
  - 背包满 → PackageEntity 实体掉落
- **3.2 存入仓库**：`DepositProvider` + Shift+施法打开 54 格双箱子 GUI
  - 关闭 GUI → 遍历网络 PackagerLink 的 `targetInventory` → `insertItemStacked`
  - 满 → 重开 GUI 显示剩余物品
- **距离限制绕过**：`BlockEntityConfigurationPacketMixin` + `StockKeeperRequestMenuMixin` 跳过 createOnClient 的 BE 依赖
  - 问题：网络包竞态（不同频道）导致远距离 GUI 打不开；`StockKeeperRequestMenuMixin` 的假 BE 存在假包/卡顿


## 2026-07-10 (fix3: 修复假 BE 远距离 GUI)

- **修复 `StockKeeperRequestMenuMixin`**：fake BE 加 `cached.setLevel(level)` 使 `getLevel()` 可用
  - 根因：constructor 链已初始化 `behaviour`（`SmartBlockEntity` 构造器调 `addBehaviours`），但缺 `level` → `hasLevel()`=false → Screen 逻辑路径异常
  - `setLevel()` 后 fake BE 在客户端可被 Screen 正常使用
- **移除 `LevelMixin.getBlockEntity` 全局拦截**：删 `gp$onGetBlockEntity` 方法
  - 根因：`CrossDimStockCache` 全局注入所有维度 BE 查询 → 假包/延迟
  - 现在 fake BE 由 Mixin 直接返回给 Menu，不走 Level 全局拦截
- **删除死代码**：`S2CRegisterStockCachePacket.java` + `CrossDimStockCache.java`
  - 这两个文件已无引用，且之前是竞态根因之一（S2C 注册包 vs openScreen 包走不同频道）
- **编译启动通过**：`gradlew runClient` BUILD SUCCESSFUL，无 crash
- **预期效果**：同维度远距离应能正常打开面板 + 无假包/延迟
- **未修复**：跨维度仍不行（client level 维度 != 目标 BE 维度）

## 2026-07-10 (fix3b: 修复远距 GUI 一闪而过)

- **根因**：`StockKeeperRequestScreen.containerTick:439-440` 检查 keeper/blaze 实体是否存活 → 两者均为 null 时调用 `menu.player.closeContainer()`
  - 远距时区块未加载 → `getEntitiesOfClass` 找不到 SeatEntity/BlazeBurner → stockKeeper + blaze 均为 null → 秒关
- **修复**：新增 `StockKeeperRequestScreenMixin` → `@Inject` 拦截 `closeContainer()` 调用
  - 条件：`blockEntity.getLevel().getBlockEntity(pos) != blockEntity` → 说明是 fake BE（远距/区块未加载） → 取消 close
  - 正常距离：`getBlockEntity(pos)` 返回真 BE → `== blockEntity` → 不取消 → 正常流程
- **编译**：Mixin 编译通过，PrototypeEvents 存在无关编译错误（skip runClient）
- **预期**：远距 GUI 不再一闪而过

## 2026-07-13 (fix3c: 修复库存 stale + 玩家收包 + 诊断体系)

- **修复库存数据不刷新**：`StockKeeperRequestScreenMixin` 加 `@Unique refreshTick` 计数器，每 20 tick 调 `refreshClientStockSnapshot()`
  - 根因：fake BE 永不被 `tick()` → `ticksSinceLastUpdate` 恒为 15 → containerTick 永不触发 refresh
- **修复库存包无法送达 fake BE**：新增 `LogisticalStockResponsePacketMixin` 拦截 `handleClient()`
  - 根因：`getBlockEntity(pos)` 查不到 fake BE → 数据丢弃 → UI 显示"检索中"
  - 修复：`getBlockEntity` 失败时 → 从 `player.containerMenu.contentHolder` 获取 fake BE → 直接调 `receiveStockPacket()`
- **修复玩家收不到包**：`StockKeeperRequestScreenMixin.init()` RETURN 注入 → 自动设置 `addressBox` 为玩家名
  - 根因：client 端 `previouslyUsedAddress` 始终 `""` → address box 为空 → 包无地址 → 无法匹配玩家
- **重构 PackagerBlockEntityMixin**：注入点从 `attemptToSend TAIL` → `tick() wakeTheFrogs()` 前
  - 根因：TAIL 注入 + `heldBox=EMPTY` 破坏 `performPackageRequests` 的 100 次循环 → 无限发包/空包
  - 修复：在动画完成那一刻拦截 + `ci.cancel()` 阻止物理输出，不碰 `heldBox` 自然 flow
- **全链路诊断体系**：`STOCK_DIAG:SPELL|REFRESH|STOCK|ORDER|PACK` 6 阶段 trace，带维度+坐标

## 2026-07-13 (fix3d: 跨维度订单修复)

- **修复跨维度取不出物品**：`BlockEntityConfigurationPacketMixin` → 遍历 `getAllLevels()` 查找 ticker BE
  - 根因：`player.level().getBlockEntity(pos)` 只在玩家当前维度查 → 下界玩家查主世界坐标 = 永远 null
  - 修复：当前维度查不到 → 遍历所有 `ServerLevel` 再查 → `STOCK_DIAG:ORDER` 日志记录找到 BE 的维度
- **已确认可行**：下界施法 → STOCK(fakeBE) → REFRESH → ORDER(BE found in overworld) → PACK → 玩家收到包

## 2026-07-13 (fix4: 健壮性补强)

- **#7 反射静默吞异常**：`catch (Exception ignored){}` → `LOGGER.warn` + 异常详情
- **#1 fake BE 资源清理**：`StockKeeperRequestScreenMixin.removed()` TAIL 注入 `invalidateCaps()`
- **#4 日志降级**：`DROPPED (no target)` 日志从 `warn` → `debug`
- **假包模型修复**：`heldBox = EMPTY` 后加 `self.sendData()` 同步客户端

## 2026-07-13 — 功能冻结归档

- **已完成功能**：
  - 同维度/跨维度远程打开 Create 物流面板
  - 实时库存刷新（每 20t 自动推送）
  - 远程下单 + 直接交付给玩家背包
  - Shift+施法存入仓库
  - 全链路 `STOCK_DIAG` 诊断日志体系
  - 6 个 Mixin 健壮性修复（假包/延迟/跨维度/反射/内存/客户端同步）
- **未完成**：末地维度施法时主世界 chunk 可能卸载 → `targetLevel.getBlockEntity` 返回 null → 需临时 chunk 加载
- **关联 incidents**：`_craft.20260710.stock-ticker-remote-order-failed.md`

## 2026-07-13 (重命名 + 配方 + Patchouli + 机械组类型)

- **重命名**：StockTickerFocus → RemoteStorageFocus、StockTickerSpell → RemoteStorageSpell、StockTickerEvents → RemoteStorageEvents
  - 注册 ID：`stock_ticker_focus` → `remote_storage_focus`
  - 中文名："仓储聚晶" → "远程仓储聚晶"
- **机械法术类型**：`SpellType.create("mechanical", "mechanical")` 自建 MECHANICAL 类型
  - 注意：构造器已拼 `spell.goety.` 前缀，第二个参数只传裸名
  - lang key: `spell.goety.mechanical` = `"§e机械§e"`（黄色）
- **机械仪式配方**：`ritual_type: goetymech:mechanical`，`craftType: mechanical`
  - 材料：末影珍珠×2 + 箱子×2 + `create:stock_link` + `goety:call_focus`
  - 触发物：`goety:empty_focus`，8秒，灵魂 80
  - 新增 `MechanicalRitual` 类 + `ModRitualFactory` 注册
  - lang key: `jei.goety.craftType.mechanical` = `"机械"`（JEI/Patchouli 显示用）
- **Patchouli 条目**：双语 2 页，`$(br)` 换行，`$(4)` 暗红引号，`category: goety:goetymech_focuses`
- **Shift+施法 GUI 标题**：DepositProvider → `container.goetymech.deposit` = `"仓库中转站"`
- **Lang 更新**：shift_bound/shift_unbound 描述修正

## 2026-07-13 — 功能冻结归档

- **已完成功能**：
  - 远程仓储聚晶（RemoteStorageFocus）
  - 同维度/跨维度远程打开 Create 物流面板 + 实时库存刷新
  - 远程下单 + 直接背包交付（`wakeTheFrogs` 拦截）
  - Shift+施法存入仓库（DepositProvider，标题"仓库中转站"）
  - 机械仪式合成配方（JEI + Patchouli 双语条目）
  - 机械法术类型（`SpellType.MECHANICAL`，黄色 `§e`）
  - 全链路 `STOCK_DIAG` 6 阶段诊断日志
  - 7 个 Mixin + 1 个 Ritual + 1 个 SpellType 扩展
- **已知限制**：末地维度目标 chunk 可能卸载 → `targetLevel.getBlockEntity` null（需 force-load）
- **关联 incidents**：`_craft.20260710.stock-ticker-remote-order-failed.md`
- **翻译 key 备忘**：
  | key | 值 | 用途 |
  |-----|-----|------|
  | `spell.goety.mechanical` | `§e机械§e` | 法术类型 tooltip |
  | `jei.goety.craftType.mechanical` | `机械` | JEI/Patchouli 仪式类型名 |
  | `container.goetymech.deposit` | `仓库中转站` | Shift+施法 GUI 标题 |
  | `ritual.goety.mechanical` | ❌ 不存在 | 错误 key，Goety 用 `jei.goety.craftType.*` |

## 2026-07-13 — 配方补充归档

- **全模组配方补全**：为 8 个聚晶 + 6 个方块 + 1 个饰品创建配方（JSON 仪式 + 有序合成）
  - chase_focus（追猎）、sculk_bind_focus（幽匿）、squad_focus（兵团）
  - unstable_rift_focus（裂纬）、stable_rift_focus（界旅）、crushing_wheel_focus（粉碎轮）
  - stress_shield_focus（应力护盾）、gateway_focus（定点）、gateway_focus_advanced（神行）
  - tier1/2/3_squad_barracks（点将台×3）、flesh_furnace（血肉熔炉）、breeding_nest（育巢）
  - stress_reader（应力读取器）、remote_engine（远程引擎）、stress_ark（应力方舟）
  - ectoplasm_catcher（灵质捕手·有序合成）
- **JEI 胚胎转化**：新增 EmbryoConversionCategory + Recipe，展示育巢/血肉熔炉转化链
- **skill 体系更新**：goety-addon-dev（SpellType+翻译key+强制调用）、goety-ritual-dev（仪式速查）、patchouli（br/color警告）
- **教训**：Goety 的 `pure_unholy_blood` 不是独立注册 ID（NBT 属性），翻译 key ≠ register ID，需查 `ModItems.java` 确认
- **关联 incidents**：`_craft.20260713.recipe-pure-unholy-blood.md`


## 2026-07-13

- **2026-07-13** — 添加合成配方 (remote_storage_focus.json)
  - 原因：补全生存获取途径

## 2026-07-15

- **纹理 + 冷却**：
  - 纹理：`remote_storage_focus.png` 从 Desktop `create_item_vault.png` 重新覆盖
  - 默认冷却：`100t (5s)` → `60t (3s)`

## 合并自 remote_storage_focus

# Timeline

- **2026-07-30** — 功能冻结归档
  - 已完成功能：远程打开 Stock Ticker（正常模式 + Shift 存入模式）
  - **Bug 修复：DepositProvider StackOverflow 崩溃 + 区块卸载丢失物品**
    - 根因：`removed()` → `depositItems()` → `openScreen()` 无限递归
    - 修复：改用 `DepositSavedData`（WorldSavedData）持久化未存入物品，移除 `openScreen()` 递归路径
    - 区块卸载 / 断线场景：一致通过 WorldSavedData 持久化，不再丢失
  - 关联 openspec：`fix-deposit-provider-crash-and-persistence`
