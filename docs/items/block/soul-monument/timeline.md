# 魂灵丰碑 — 变更记录

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
