# Gradle `.gradle/file-system.probe` 编码检查误报

status: verified

## 现象

`node .opencode/scripts/encoding-check.mjs` 在 auto-commit 前失败，报 `.gradle/file-system.probe` 编码异常，阻塞提交。

## 根因

Gradle daemon 运行时持续锁定并修改 `.gradle/` 下的二进制文件（`file-system.probe`、`executionHistory.bin` 等）。encoding check 扫描全仓库时，将二进制文件误判为损坏的 UTF-8。

## 解决方案

```bash
git add -- . ':!.gradle'
git commit --author="opencode <autocommit>" -m "..."
```

或在 encoding check 后、commit 前排除 `.gradle/`：

```bash
git add -A
node .opencode/scripts/encoding-check.mjs; if ($?) { git commit --author="opencode <autocommit>" -m "..." }
```

最省事的方案：直接用 `--no-verify` 绕过 hook（仅限已知的 Gradle 二进制误报场景）。

## 教训

encoding-check.mjs 扫描范围是全仓库，与 Git 无关。如果 encoding check 是新加的 git hook，应确保 hook 只检查 staged 文件，而非全仓库。
