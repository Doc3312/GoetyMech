# Mechanical Ritual — Timeline

## 2026-07-13 — 功能冻结归档

- **已完成功能**：
  - 新增 `MechanicalRitualType`（实现 `IRitualType`），注册为 `"mechanical"` 流派
  - 结构检测（±8 格）：安山机壳×16、炼药锅×2、动力冲压机×1、铁砧×1、锻造台×1、动力锯或切石机×1
  - 完成音效：`ANVIL_USE`
  - 注册方式：`FMLCommonSetupEvent` → `RitualType.addRitualType()`
  - Patchouli 双语条目（en_us + zh_cn）+ 占位图
- **关联 commits**：`330338c`（初始实现）、`42c819d`（机械锯→动力锯）
- **未完成**：
  - 流派中文/英文显示名称待定（暂为 "mechanical"/"机械"）
  - `jei.goety.craftType.mechanical` lang 键未添加（JEI/Patchouli 中不会正确显示类型标签）
  - 尚无使用 `craftType: "mechanical"` 的仪式配方 JSON
