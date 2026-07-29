# Prototype Mimic Timeline

## 2026-07-14 — 功能冻结归档

- **改动**：修复拟态物品手持与 GUI 渲染，完成 closeout。
- **原因**：此前反复出现“GUI 对则手持错，手持对则 GUI 斜”的问题，根因是 `builtin/entity` 与 `item/generated` 的 base pose 不一致。
- **最终方案**：
  - `prototype_mimic.json` 保持 `parent: builtin/entity` 以触发 BEWLR。
  - 为手持/地面/展示框/头戴定义 `item/generated` 标准 display transforms。
  - GUI 不定义 transform（identity），源模型平铺显示。
- **关联 commits**：`e579bfa`、`b99f162`
- **关联 incidents**：`docs/incidents/_mimic/20260714.mimic-rendering-transforms.md`
