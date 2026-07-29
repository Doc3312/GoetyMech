# Remote Storage Focus (远程仓储聚晶)

远程打开绑定的 Stock Ticker 下单界面；Shift+施法打开仓库中转站（DepositProvider）。

## 绑定

- 手持聚晶右键 Stock Ticker 方块绑定
- 绑定写入 NBT：坐标、维度、freqId

## 施法

### 正常模式

打开 Stock Keeper Category Menu（远程下单），使用 Mixin 缓存假 BE 以支持跨维度。

### Shift 模式（存入模式）

打开 DepositProvider（54 格 ChestMenu），关闭 GUI 时尝试将物品存入同 freqId 的 Packager。
