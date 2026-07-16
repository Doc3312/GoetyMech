# Flesh Casket Incidents

## 2026-07-17 — 蓄力法术通道死锁 + 幻影法杖竞态清除
- 类型：自动施法 / 通道调度
- 状态：已解决
- 根因：maxShots 不含 castUp 导致蓄力法术通道提前终止；ChannelTask.stop 清除共享 PhantomWand 导致裂隙实体被 discard
- 经验：共享资源的清理必须由最后一个使用者负责；PhantomWandManager.set() 会移除 PENDING_CLEAR
- 关联：`flesh-casket/20260717.charging-channel-deadlock.md`

## 2026-07-16 — GUI 纹理与容器高度不匹配
- 类型：GUI渲染
- 状态：已解决
- 根因：纹理256×256与容器标准高度166px的blit参数配置
- 经验：容器GUI纹理必须256×256，自定义内容在左上176×166区域
- 关联：`_craft/20260716.gui-texture-container-height.md`
