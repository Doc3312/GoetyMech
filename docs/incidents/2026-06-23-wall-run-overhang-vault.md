# 墙跑屋檐跨越方案（成功）

status: verified

## 问题

I 形墙顶部加一排向外延伸方块（倒 L 形屋檐），玩家墙跑攀爬到顶时被卡住，无法越过。

## 失败尝试

1. **后处理 teleport**（Y 抬升 + wallRight 平移）→ 视觉突兀
2. **预判 sidestep**（wallRight 方向绕行）→ 屋檐横跨整面墙，绕不开
3. **向外推开 vault**（wallN.scale(-out) + climb）→ 推开清除了屋檐，但玩家飞出检测范围（0.5），下一 tick 失去墙面吸附，坠落

## 成功方案

**向外推开（vault）+ 宽限期（grace period）**

两层机制：

### 1. 预判向外推开（`MixinWallRunTravel.java:48-56`）

```java
for (double out : new double[]{0.9, 1.2, 0.7, 0.5}) {
    Vec3 outward = wallN.scale(-out);
    AABB checkBox = player.getBoundingBox().move(climbVec.add(outward)).inflate(-0.02);
    if (player.level().noCollision(checkBox)) {
        wallMove = wallMove.add(outward);
        ClientWallRunState.applyGrace(3);  // ← 关键
        break;
    }
}
```

原理：`Entity.move()` 处理轴序为 X→Y→Z。向外推开在 X/Z 轴先执行，玩家离开墙面后 Y 轴碰撞检测在新位置进行，屋檐不再阻挡。

### 2. 宽限期吸附恢复（`ClientWallRunState.java` + `PrototypeRenderEvents.java`）

推开后玩家距墙面 ~0.9-1.2，超出检测范围 0.5。宽限期机制：

- `applyGrace(3)`：记录当前 wallNormal，设 3 tick 宽限
- `tickGrace()`：每 tick 递减，宽限期内 mixin 继续用 lastWallNormal
- 每 tick 有 0.3 的 pull 力拉向墙面
- 3 tick 后玩家回到 ~0-0.3 距离，检测恢复正常

## ParCool 对比

ParCool 不处理屋檐跨越。其 `VerticalWallRun.canContinue()` 检测 `playerYSpeed > 0`，头撞天花板后 Y 速度归零 → 墙跑自然结束。无 vault/climb-up 自动衔接。

## 关键决策

- **Entity.move() 轴序**：X→Y→Z 的顺序使推开后的 Y 检测在正确位置进行
- **宽限期而非增大检测范围**：避免误吸附远处墙面
- **3 tick 足够**：因 pull=0.3/tick，从 0.9 回到 0.3 需 2 tick，3 tick 留余量
