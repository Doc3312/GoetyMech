## Why

当前 TimeFocusPouch 的时停实现存在两个核心缺陷：entity 动画照常播放（火球旋转/怪物动作未冻结），且 Goety 径向菜单在 `ClientLevel.tick()` 全取消时卡死。参考 endinglibrary 的实现方案后，需要将冻结策略从"取消整条 Level.tick()"改为"取消 tickEntities() + 冻结渲染 partialTick"。

## What Changes

- `ClientLevelMixin`: 目标从 `tick()` 改为 `tickEntities()`，仅冻结 entity 逻辑，保留 block entity/time/pending update 处理
- `MinecraftMixin`: 新增 `getPartialTick()` 注入，时停中返回 0.0F，消除渲染插值漂移
- `MinecraftServerMixin`: 维持 `@WrapOperation` 包裹 `ServerLevel.tick()`
- `ParticleEngineMixin`: 维持取消 `tick()`
- 删除此前无用的 `EntityMixin`（已删）

## Capabilities

### New Capabilities
- `client-level-freeze`: 客户端 ClientLevel 的 entity 层冻结，保留非 entity 处理链
- `render-freeze`: 通过冻结 partialTick 阻止渲染插值，消除 jitter
- `server-level-freeze`: 服务端维度级 Level.tick() 取消

### Modified Capabilities

无

## Impact

- `src/main/java/com/GoetyMech/addon/mixin/ClientLevelMixin.java`: 重写
- `src/main/java/com/GoetyMech/addon/mixin/MinecraftMixin.java`: 新建
- `src/main/java/com/GoetyMech/addon/mixin/EntityMixin.java`: 删除
- `src/main/resources/GoetyMech.mixins.json`: 更新 client mixin 阵列
