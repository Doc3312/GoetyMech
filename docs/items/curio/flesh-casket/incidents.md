# Flesh Casket Incidents

## 2026-07-16 — GUI 纹理与容器高度不匹配
- 类型：GUI渲染
- 状态：已解决
- 根因：纹理256×256与容器标准高度166px的blit参数配置
- 经验：容器GUI纹理必须256×256，自定义内容在左上176×166区域
- 关联：`_craft/20260716.gui-texture-container-height.md`
