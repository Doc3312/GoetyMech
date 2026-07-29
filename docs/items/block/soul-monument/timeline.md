# 魂灵丰碑 — 变更记录

- **2026-07-22** — 新增：放置朝向跟随玩家
  - 添加 `HORIZONTAL_FACING` 属性，blockstate 4 个变体 + y 旋转
  - 碰撞箱不变
  - 关联 openspec：`soul-monument-rotation`

- **2026-07-18** — 修复：重新放置丰碑后 HUD 不显示仆从姓名

- **2026-07-09** — 功能冻结归档
  - 已完成功能：注册三阶方块+BlockItem+BE、.dat 文件存储、录入状态机+处决、TaglockKit 交互、仪式升级、SquadFocus 适配、HUD 显示、物品销毁保护
  - 修复：BE 网络同步（getUpdatePacket）解决 HUD/粒子不更新
  - 修复：选中仆从 PORTAL→ENCHANT 粒子 + MiscCapHelper.setShakeTime 颤抖
  - 修复：setPlacedBy 未设 ownerUUID 导致召唤权限拒绝
  - 修复：handleMonumentSummon 中 entityData.getCompound("EntityData") 错误（数据在根层级）
  - 修复：getMonumentSpawnPos Vec3(x,z,y) 坐标顺序错误（Y 取了 z 值，mob 生成在基岩深处窒息）
  - 修复：appendHoverText 不区分 bound_type 导致丰碑绑定显示为点将台
  - 修复：DETECTION_RANGE 6→10（20×20）
  - 修复：录入后未刷新绑定聚晶缓存（refreshMonumentFoci）
  - 关联 openspec：`add-soul-monument`

- **2026-07-10** — 丰碑召唤 Bug 修复 + 死亡掉落拦截
  - 修复：补召追踪改用 `monument_slot` 索引，解决重复召唤/跳召问题
  - 修复：冷却清除改用 `getFocusCoolDown().isOnCooldown(Item)` 直接查 cooldowns map
  - 修复：`LivingHurtEvent` 致死拦截（`monument_summoned` cancel+discard），防止 `soulJar()` / `tickDeath()` 等绕过 LivingDropsEvent 的特殊掉落
  - 新增：`LivingDropsEvent` 兜底拦截
  - 新增：`DarkWandUseMixin` — shift+右键 dismiss 在 `cannotCast()` 前截获，冷却期内可正常遣散
  - 关联 openspec：`monument-summon-improvements`
  - 关联 incidents：`_craft.20260710.cooldown-debug.md`

- **2026-07-15** — Bug 修复：冷却无效 + 三连击清除
  - 修复：移除 `handleMonumentSummon` 中 5 分钟冷却应用代码（`MONUMENT_SUMMON_COOLDOWN`），丰碑召唤不再有长冷却
  - 保留：全存活→传送的 20t 短冷却（防刷屏）
  - 新增：`PlayerInteractEvent.LeftClickBlock` — shift+左键空手三连击清除内部实体
  - 新增：`SoulMonumentBlockEntity.handleClearClick()` — 防连点冷却 20t（1s）+ 超时重置 100t（5s）
  - 新增 lang keys：`clear_step1/2`、`clear_done`、`clear_too_fast`
  - 关联：用户报告 bug

- **2026-07-14** — 大规模功能对接
  - 新增：丰碑 HUD 显示已录入仆从名字（`savedEntityNames` + 向后兼容 .dat 回填）
  - 新增：丰碑召唤每单位 `MONUMENT_SOUL_COST=1000`（独立于 points 台）
  - 修复：丰碑跨维度召唤（解除了 monument 的 dimension check 限制）
  - 修复：跨维度实体不出现（entity 创建/添加改用 `worldIn` 而非 `blockLevel`）
  - 新增：召唤/死亡 Goety 风格特效（`ScaledGodRayParticle` + `SOUL` 爆发 + `WITHER_DEATH`）
  - 新增：仆从默认跟随模式 + 攻击目标重定向
  - 关联 openspec：`monument-summon-improvements`
   - 关联 incidents：`_craft.20260714.squad-monument-architecture.md`

- **2026-07-18** — 修复：重新放置丰碑后 HUD 不显示仆从姓名
  - 根因：破坏时只保存 UUID 不保存姓名，放置时只恢复 UUID
  - 修复：`MonumentUUIDs` NBT 每条目新增 `Name` 字段，`setPlacedBy` 同步恢复
  - 新增：`SoulMonumentBlockEntity.setSavedEntityNames()`
  - 关联 openspec：`fix-soul-monument-hud-names`

- **2026-07-16** — 配方重做：机械仪式 + 新材料
  - 修改：一阶合成配方（`soul_monument_1.json`）
    - 激活物：暗影墓碑（`goety:shade_gravestone`）
    - 材料：黑暗金属锭×2, 指路石, 军团聚晶, 不洁之血, 灵质, 奥术方匣
  - 修改：二阶升级配方（`soul_monument_1_to_2.json`）
    - 材料：不洁之血, 神秘核心, 灵魂红宝石, 不祥核心, 奥术方匣
  - 修改：三阶升级配方（`soul_monument_2_to_3.json`）
    - 材料：暗夜信标, 贤者之石, 奥术方匣, 下界合金锭×2, 禁书碎片
  - 统一：所有配方 `craftType` → `mechanical`（机械仪式）
  - 统一：献祭村民不变，`ritual_type` 不变
  - 灵魂消耗：一阶 3000/6s, 二阶 4000/8s, 三阶 5000/10s（500/s）
  - Patchouli 条目未调整（配方页自动跟随 JSON）

- **2026-07-28** — 修复：丰碑 TaglockKit 录入缺少实体校验
  - 问题：非仆从实体（敌对/中立生物）也可被录入；非玩家所有的仆从（如野生红石巨兽）被记录后强制变为玩家所有
  - 修复：三层校验架构
    - Event 层（`SoulMonumentEvents.onRightClickBlock`）— 录入前校验实体是否 `IOwned` 且 `getOwnerId() == player.getUUID()`
    - BE 层（`SoulMonumentBlockEntity.tryStartRecording`）— 防御性重复校验
    - BE 层（`SoulMonumentBlockEntity.onVillagerDeath`）— 实际录入前二次校验所有权
  - 新增 lang key：`info.goetymech.soul_monument.not_a_servant`
  - 设计决策：仅当前维度搜索（仪式需要实体在 10 格内）；校验失败提示"§c该生物非仆从"
