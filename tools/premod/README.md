# premod —— 前置模组注册索引

解决两个问题：

1. **开发时要找前置里叫什么**：不再翻 `D:\Libcode` 源码，直接查索引。
2. **写 JSON 时 id 打错**：提交前校验 `"goety:xxx"` / `"create:xxx"` 是否真实存在。

## 三个命令

```bash
node tools/premod/build.mjs                  # 重新生成索引（改前置版本后跑）
node tools/premod/find.mjs <关键词>           # 查 id / 字段 / 取值表达式 / 已有先例
node tools/premod/validate.mjs               # 校验本项目里的前置 id 引用（exit 1 = 有问题）
```

常用参数：

```bash
node tools/premod/find.mjs ingot --mod goety            # 只查 Goety
node tools/premod/find.mjs shaft --domain blocks        # 只查方块
node tools/premod/find.mjs fan_casing --resources       # 连 tag / 粒子 / 纹理一起查
node tools/premod/find.mjs --purge                      # 只列“引用了但前置里没有”的 id
node tools/premod/validate.mjs --warn                   # 只报告不返回失败
```

`find.mjs` 每行给出：完整 id、注册域、Java 取值表达式、类型、以及**本项目里已经用过的位置**（先例）。

## 数据来源（关键）

| 模组 | 注册表 | 资源域（tag / 粒子 / 纹理） |
|---|---|---|
| Goety | **当前编译用的 jar**（javap 读字节码） | 同一个 jar 的条目 |
| Create | 源码里的 `REGISTRATE.*` 字段 | Create 的 slim jar |

> Goety 的字段名和注册 id 经常不一致（`CURSED_METAL_INGOT` → `cursed_ingot`），
> 而且源码版本可能领先于依赖 jar，所以**注册表只从 jar 读**，保证索引与"能编译通过的现实"一致。
>
> `build.mjs` 会打印 `skipped`：源码里新出现、但 jar 里还没有的注册类会被跳过——这是版本差的正常信号。

## 索引文件

| 文件 | 内容 |
|---|---|
| `index/goety.json` / `index/create.json` | `{ meta, entries, resources }`；`entries[id] = {domain, field, holder, javaType}` |
| `index/usage.json` | 本项目的反向引用：`"goety:ectoplasm" -> [{file,line,kind}]` |
| `index/meta.json` | 版本戳（jar 文件名 / mtime / 各类计数）与跳过清单 |

## 升级前置的流程

```
改 build.gradle 的依赖版本
  → node tools/premod/build.mjs
  → 看 index/meta.json 的 artifact / mtime 是否变了
  → node tools/premod/validate.mjs        # 有没有 id 被删/改名
  → 对比新旧 index（entries 的 key 差集）= 升级清单
```

## 路径配置

`tools/premod/config.json`：JDK 路径、Libcode 源码目录、jar 所在目录、本项目的 java/resources 目录。
换机器或换前置目录时改这里，不用动脚本。

## 已知边界

- Create 只索引 `REGISTRATE.block/item/...` 这种**有字段名**的注册；链式内联注册会落到 `other` 域或被忽略。
- Goety 的 tag 名做了宽松处理（同时收录全路径和末段名），偏保守，宁可少报不误报。
- `validate.mjs` 按"文件位置 + 所在 JSON 键"判断上下文（粒子 json 的 `textures`、`tag` 键、含 `/` 的纹理路径、`category/parent` 这类 patchouli 键），不是简单的全局 id 匹配。
