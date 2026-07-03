# CraterBlock

撞击坑视觉效果方块，用于 prototype charge jump 的 slam 落地。纯客户端 BESR 方块，临时替换地面方块，完成后恢复原状。

## Timeline

- **2026-07-04**: 用 EpicFight 精确公式替换自定义旋转/提升/弹跳算法
  - 旋转角度: `(distance/6)×15° + random(-5°,+5°)`，X/Y/Z 随机抖动 ±7.5°/±20°/±7.5°
  - 提升高度: `max(0, distance/6 - 0.5)×0.5`
  - 弹跳: `distance² × min(1/36, 0.1)`，抛物线动画
  - 最后 5tick 回正：translate lerp + quaternion slerp → 零
  - CraterBlockEntity 移除自定义 `BOUNCE_UP_TICKS`/`FINAL_DISSOLVE_TICKS`，改为 EpicFight 风格 last-10-ticks 粒子
