# Config Gating

内容可基于 flag 表达式动态显示/隐藏。

## 默认 flags

| Flag | 条件 |
|---|---|
| `debug` | IDE 调试模式 |
| `advancements_disabled` | Patchouli 配置禁用进度锁定 |
| `testing_mode` | Patchouli 测试模式 |
| `mod:MODID` | 某模组已加载，如 `mod:create` |

## 表达式

| 表达式 | 含义 |
|---|---|
| `!flag` | 否定 |
| `&flag1,flag2,flag3` | 全部为 true（AND） |
| `|flag1,flag2,flag3` | 任一为 true（OR） |

## 示例

```json
// 仅在 Create 加载时显示
{ "flag": "mod:create" }
// 不加载 Curios 时显示
{ "flag": "!mod:curios" }
// 两个模组同时加载时显示
{ "flag": "&mod:create,mod:jei" }
```

可用于：Category 的 `flag`、Entry 的 `flag`、Page 的 `flag`、Template Component 的 `flag`。
