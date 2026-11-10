#!/usr/bin/env node
/**
 * check-encoding.mjs — 提交门禁：校验暂存区的文本文件没有编码损坏。
 *
 * 检查项（只查会被 commit 的文件）：
 *   1. UTF-8 往返一致（不是合法 UTF-8 直接挂）
 *   2. 无 U+FFFD 替换字符（PowerShell 管道转码的典型残留）
 *   3. .json 必须能 JSON.parse
 *
 * 用法：node tools/check-encoding.mjs [--staged|--all]
 *   --staged（默认）：git diff --cached 的文件
 *   --all：git ls-files 的全部跟踪文件
 *
 * 为什么不用 PowerShell 做这件事：PS 5.1 的 Set-Content 默认按 ANSI 写盘，
 * 会把 UTF-8 中文写成 GBK 字节，中文和 § 颜色码都会被毁掉。
 */
import fs from "node:fs";
import path from "node:path";
import { execSync } from "node:child_process";

const ROOT = process.cwd();
const ALL = process.argv.includes("--all");
const TEXT_EXT = new Set([
  ".java", ".json", ".properties", ".toml", ".cfg", ".yml", ".yaml", ".xml",
  ".md", ".txt", ".mjs", ".js", ".ts", ".mcmeta", ".snbt",
]);

function listFiles() {
  const cmd = ALL ? "git ls-files" : "git diff --cached --name-only --diff-filter=ACM";
  const out = execSync(cmd, { cwd: ROOT, encoding: "utf8" });
  return out.split(/\r?\n/).filter(Boolean);
}

const problems = [];
const files = listFiles();

for (const rel of files) {
  const ext = path.extname(rel).toLowerCase();
  if (!TEXT_EXT.has(ext)) continue;
  const abs = path.join(ROOT, rel);
  if (!fs.existsSync(abs)) continue;

  const buf = fs.readFileSync(abs);
  const text = buf.toString("utf8");

  if (!Buffer.from(text, "utf8").equals(buf)) {
    problems.push(`${rel}  不是合法 UTF-8（往返不一致）`);
    continue;
  }
  if (text.includes("\uFFFD")) {
    const line = text.slice(0, text.indexOf("\uFFFD")).split("\n").length;
    problems.push(`${rel}:${line}  含 U+FFFD 替换字符（编码已损坏）`);
  }
  if (ext === ".json") {
    try {
      JSON.parse(text);
    } catch (e) {
      problems.push(`${rel}  JSON 解析失败: ${String(e.message).split("\n")[0]}`);
    }
  }
}

if (problems.length === 0) {
  console.log(`✔ 编码检查通过（${files.length} 个暂存文件）`);
  process.exit(0);
}
console.error("✘ 编码检查未通过，禁止提交：\n");
for (const p of problems) console.error("  " + p);
console.error("\n修复方式：用 Node.js 重写该文件（不要用 PowerShell 的 Set-Content / 重定向），或从 HEAD 恢复。");
process.exit(1);
