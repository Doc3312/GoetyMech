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
