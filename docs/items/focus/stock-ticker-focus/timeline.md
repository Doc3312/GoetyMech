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