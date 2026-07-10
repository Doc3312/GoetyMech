# Stock Ticker Focus — Timeline

## 2026-07-10

- **创建物品 + 法术**：StockTickerFocus（继承 MagicFocus）+ StockTickerSpell（继承 Spell）
  - 右键 Stock Ticker 方块绑定 NBT（坐标 + 维度 + freqId）
  - Shift+右键解绑
  - 施法跨维度远程打开 Create 请求下单界面
  - 灵魂消耗 5，冷却 5 秒（100 tick）
  - 原因：用户需求，远程访问 Create 物流网络
  - 关联：openspec change `stock-ticker-focus`

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
