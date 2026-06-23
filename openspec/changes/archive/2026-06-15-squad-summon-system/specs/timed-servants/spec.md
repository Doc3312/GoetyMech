## 新增需求

### 需求：召唤的仆从拥有 5 分钟寿命
所有通过 SquadFocus 召唤的仆从必须拥有 6000 tick（5 分钟）的有限寿命。这必须通过在生成的实体上调用 `mob.setHasLifespan(true)` 和 `mob.setLifespan(6000)` 实现。Goety 的 `IOwned.ownedTick()` 必须处理自动倒计时和消亡。

#### 场景：仆从 5 分钟后消失
- **当** 一个 Vindicator 仆从通过 SquadFocus 生成
- **则** 该仆从必须在 5 分钟（6000 tick）后自动受伤并消失

#### 场景：仆从寿命跨区块持久化
- **当** 生成的仆从移入未加载区域然后返回
- **则** 剩余寿命必须被保留（通过 `Owned.addAdditionalSaveData` 保存在 NBT 中）

### 需求：寿命适用于所有生成的实体类型
寿命设置必须应用于所有通过此系统生成的实体类型，无论它们是 Neollager、Vindicator、Ravager 还是其他任何 Goety 仆从类型。

#### 场景：通用仆从寿命
- **当** 任何 Mob 实体通过 SquadSpell 的 MinionSpawnHelper 生成
- **则** 必须在该实体上调用 `setHasLifespan(true)` 和 `setLifespan(6000)`
