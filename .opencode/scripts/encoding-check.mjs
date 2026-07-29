#!/usr/bin/env node
// encoding-check: 校验待提交文件的 UTF-8 编码完整性
// 检查项：
//   1. Roundtrip — 读为 UTF-8 再写出，字节是否一致
//   2. JSON 格式 — .json 文件必须可 parse
//   3. 乱码特征 — lang 文件中 `搂`（§ 误转码）、`鈥` 等典型坏字符
//   4. 替换残留 — 纯文本文件中出现 goetypunk 视为替换未完成
//
// 用法: node encoding-check.mjs [--fix]
//   --fix: 对修复过的文件自动执行 `git checkout HEAD -- <file>` 从 HEAD 恢复
//          如果 HEAD 版本也不正确则跳过（留给人工处理）

import { existsSync, readFileSync, writeFileSync } from "fs";
import { execSync } from "child_process";

const GARBAGE_PATTERNS = [
  /搂/g,         // § 被误转码
  /鈥/g,         // UTF-8 高位字节被单字节解释
  /鍐/g,         // "冲"字损坏
  /嚮/g,         // "击"字损坏
  /杞/g,         // "轮"字损坏
];

// 通配 UTF-8 乱码特征：连续 3+ 个 \x80-\xBF 范围字节出现在非 ASCII 预期位置
// 但这会产生大量误报（合法中文也是 \x80-\xBF），故仅对纯 ascii 文件用
function hasGarbledHighBytes(text) {
  // 检查是否有单个乱码替换字符
  if (text.includes("\uFFFD")) return true;
  // 检查是否包含  (DEL) 控制字符以外不可见 ASCII 在文本中
  // 仅检查非打印 ASCII (0x01-0x08, 0x0B-0x0C, 0x0E-0x1F)
  for (let i = 0; i < text.length; i++) {
    const cp = text.charCodeAt(i);
    if ((cp >= 0x01 && cp <= 0x08) || (cp >= 0x0E && cp <= 0x1F)) {
      return true;
    }
  }
  return false;
}

function roundtripCheck(buf) {
  try {
    const decoded = buf.toString("utf8");
    const reEncoded = Buffer.from(decoded, "utf8");
    return { ok: reEncoded.equals(buf), decoded };
  } catch {
    return { ok: false, decoded: null };
  }
}

function checkFile(filePath) {
  const issues = [];

  // 跳过二进制文件
  const binExts = new Set([".png", ".jpg", ".jpeg", ".gif", ".ico", ".jar", ".zip", ".bin", ".class", ".lock", ".ttf", ".otf", ".woff", ".woff2", ".ogg", ".mp3", ".mp4"]);
  const ext = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
  if (binExts.has(ext) || !existsSync(filePath)) return issues;

  // 判断是否为源码/资源文件（需要严格检查乱码）
  const codeExts = new Set([".java", ".json", ".properties", ".toml", ".cfg", ".yml", ".yaml", ".xml", ".html", ".css", ".js", ".ts", ".mjs", ".cjs"]);
  const isCodeFile = codeExts.has(ext);

  const buf = readFileSync(filePath);

  // 字节数 < 3 跳过
  if (buf.length < 3) return issues;

  // ===== 1. Roundtrip 检查 =====
  const rt = roundtripCheck(buf);
  if (!rt.ok) {
    issues.push({ type: "encoding", msg: "UTF-8 roundtrip 失败：不是有效的 UTF-8 编码" });
    return issues;
  }

  // ===== 2. 乱码特征字符检查（仅源码/资源文件） =====
  if (isCodeFile) {
    for (const pattern of GARBAGE_PATTERNS) {
      const matches = rt.decoded.match(pattern);
      if (matches && matches.length > 0) {
        issues.push({ type: "garbled", msg: `发现已知乱码字符 "${pattern.source}" 共 ${matches.length} 处` });
      }
    }
  }

  // ===== 3. 高位字节乱码检查 =====
  if (hasGarbledHighBytes(rt.decoded)) {
    issues.push({ type: "encoding", msg: "发现不可见控制字符或替换字符" });
  }

  // ===== 4. JSON 格式检查 =====
  if (filePath.endsWith(".json")) {
    try {
      JSON.parse(rt.decoded);
    } catch (e) {
      issues.push({ type: "json", msg: `JSON 解析失败：${e.message.split("\n")[0]}` });
    }
  }

  // ===== 5. 替换残留检查 =====
  if (filePath.endsWith(".java") || filePath.endsWith(".json") || filePath.endsWith(".toml") || filePath.endsWith(".properties") || filePath.endsWith(".cfg")) {
    if (/goetypunk/i.test(rt.decoded)) {
      issues.push({ type: "residual", msg: '发现残留 "goetypunk" 引用（替换未完成）' });
    }
  }

  return issues;
}

// ===== Main =====

const args = process.argv.slice(2);
const doFix = args.includes("--fix");

// 获取待提交/已修改的文件
let changedFiles;
try {
  const output = execSync("git diff --cached --name-only", { encoding: "utf8" }).trim();
  const staged = output ? output.split("\n").filter(Boolean) : [];
  const output2 = execSync("git diff --name-only", { encoding: "utf8" }).trim();
  const unstaged = output2 ? output2.split("\n").filter(Boolean) : [];
  changedFiles = [...new Set([...staged, ...unstaged])];
  if (changedFiles.length === 0) {
    console.log("✅ 无待提交文件，跳过编码检查");
    process.exit(0);
  }
} catch {
  console.log("⚠️  无法获取 git diff，回退到全部跟踪文件检查");
  changedFiles = execSync("git ls-files", { encoding: "utf8" }).trim().split("\n").filter(Boolean);
}

let anyError = false;
let reportLines = [];

for (const file of changedFiles) {
  const issues = checkFile(file);
  if (issues.length > 0) {
    anyError = true;
    for (const issue of issues) {
      reportLines.push(`❌ ${file} → [${issue.type}] ${issue.msg}`);
    }
  }
}

if (anyError) {
  console.log("❌ 编码安全检查未通过：\n");
  console.log(reportLines.join("\n"));
  console.log("\n⚠️  禁止提交。请修复编码问题后再执行 git commit。");
  console.log("   提示：对每个损坏文件从稳定版恢复，确保使用 Node.js 而非 PowerShell 读写。\n");

  if (doFix) {
    console.log("=== --fix 模式：尝试从 HEAD 恢复损坏文件 ===");
    for (const line of reportLines) {
      const file = line.substring(2).split(" →")[0];
      try {
        const headBuf = execSync(`git show HEAD:${file}`, { encoding: "buffer" });
        const rt = roundtripCheck(headBuf);
        if (rt.ok) {
          // HEAD 版本编码正确 → 恢复
          writeFileSync(file, rt.decoded, "utf8");
          console.log(`  ✅ 已从 HEAD 恢复: ${file}`);
        } else {
          console.log(`  ⛔  HEAD 版本同样编码损坏，跳过: ${file}`);
        }
      } catch {
        console.log(`  ⛔  无法从 HEAD 读取: ${file}（可能是新文件）`);
      }
    }
  }

  process.exit(1);
} else {
  console.log(`✅ 编码检查通过 (${changedFiles.length} 个文件)`);
  process.exit(0);
}
