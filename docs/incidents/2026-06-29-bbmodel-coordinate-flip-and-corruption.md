# bbmodel 坐标翻转 + 多次生成失败

status: verified

## 背景

为 crazed_illager 实体生成 Blockbench `.bbmodel` 文件（`src/main/resources/assets/goetypunk/crazed_illager.bbmodel`），用于纹理绘制参考。

## 事故一：文件放错路径

初始写入 `assets/goetypunk/`（项目根目录），正确路径为 `src/main/resources/assets/goetypunk/`。

## 事故二：Y-flip PowerShell 脚本损坏

模型在 Blockbench 中显示倒置（Minecraft 实体模型 Y+ = 向下，Blockbench Y+ = 向上）。

第一次尝试：`ConvertFrom-Json` → 修改坐标 → `ConvertTo-Json -Depth 10`。结果 `ConvertTo-Json` 嵌套截断，输出 0 字节。

**教训**：PowerShell 的 `ConvertTo-Json` 处理深层嵌套对象不可靠，深度参数经常无效。应使用 .NET 的 `Newtonsoft.Json` 或直接转 Python/Node.js。

## 事故三：PowerShell 数组运算脚本失败

第二次尝试：编写 PS 函数计算坐标偏移，但 `$ox+$cx` 在 PowerShell 中对数组执行的是拼接操作（不是逐元素加法），导致 `absCube` 函数返回 `$null`。

**教训**：PowerShell 数组算术不是逐元素的。数值计算用 Python/Node.js 更可靠。

## 最终方案

Node.js 脚本 `.opencode/scripts/gen-bbmodel.mjs` 成功生成 16 个元素（7 个身体部件 + 1 个锯片中心 + 8 个锯齿），YZ 平面锯片，坐标正确。

## 关键坐标验证

| 部件 | BB Y-up 范围 | 说明 |
|------|-------------|------|
| body | y=-12 ~ y=0 | 躯干 |
| head | y=0 ~ y=8 | 头部（在躯干之上） |
| hat | y=0 ~ y=8, inflate=0.5 | 帽子（包裹头部） |
| legs | y=-24 ~ y=-12 | 腿（在躯干之下） |
| blade_center | y=0~8, z=-8~0 | YZ 平面圆盘 |
| teeth | 半径 4.5 的 YZ 环 | 围绕 blade_center |

## 状态

bbmodel 生成成功。Node.js 生成脚本保留在 `.opencode/scripts/gen-bbmodel.mjs`。
