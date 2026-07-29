#!/usr/bin/env node
// post-commit doc audit — runs after every commit, checks if .java changes have docs
import { execSync } from "child_process";
import { existsSync, readFileSync, writeFileSync, mkdirSync } from "fs";
import { join, dirname } from "path";

const PROJECT = execSync("git rev-parse --show-toplevel", { encoding: "utf-8" }).trim();
const DEBT_FILE = join(PROJECT, ".opencode", ".doc-debt.json");

const CATEGORY_RE = {
  misc: /[/\\](item|curio)[/\\]/i,
  block: /[/\\]block[/\\]/i,
  focus: /[/\\](focus|spell)[/\\]/i,
};

function getCategory(filePath) {
  for (const [cat, re] of Object.entries(CATEGORY_RE)) {
    if (re.test(filePath)) return cat;
  }
  // fallback: check file suffix
  const basename = filePath.split(/[/\\]/).pop();
  if (/Block$/i.test(basename)) return "block";
  if (/Focus$|Spell$/i.test(basename)) return "focus";
  return "misc";
}

function getSlug(filePath) {
  const basename = filePath.split(/[/\\]/).pop().replace(/\.java$/i, "");
  let slug = basename
    .replace(/Item$/i, "")
    .replace(/Block$/i, "")
    .replace(/Entity$/i, "")
    .replace(/Spell$/i, "")
    .replace(/Focus$/i, "");
  slug = slug
    .replace(/([A-Z])/g, "-$1")
    .toLowerCase()
    .replace(/^-/, "");
  return slug;
}

function loadDebt() {
  try {
    if (existsSync(DEBT_FILE)) {
      return JSON.parse(readFileSync(DEBT_FILE, "utf-8"));
    }
  } catch {}
  return [];
}

function saveDebt(entries) {
  try {
    const dir = dirname(DEBT_FILE);
    if (!existsSync(dir)) mkdirSync(dir, { recursive: true });
    writeFileSync(DEBT_FILE, JSON.stringify(entries, null, 2), "utf-8");
  } catch (e) {
    console.error("[doc-audit] write failed:", e.message);
  }
}

// Get changed .java files in HEAD commit
let changed;
try {
  const out = execSync("git diff-tree --no-commit-id -r --name-only HEAD", {
    cwd: PROJECT,
    encoding: "utf-8",
  });
  changed = out.trim().split("\n").filter(Boolean);
} catch {
  // No commits yet or error
  process.exit(0);
}

const javaChanged = changed.filter((f) => /\.java$/i.test(f) && !/\/test\//i.test(f) && !/\\test\\/i.test(f));
if (javaChanged.length === 0) process.exit(0);

const existingDebt = loadDebt();
const existingKeys = new Set(existingDebt.map((d) => `${d.category}:${d.slug}`));
const newDebts = [];

for (const file of javaChanged) {
  const category = getCategory(file);
  const slug = getSlug(file);
  const key = `${category}:${slug}`;
  const docPath = join(PROJECT, "docs", "items", category, `${slug}.md`);
  const expectedRel = `docs/items/${category}/${slug}.md`;

  if (existingKeys.has(key)) continue;

  // Check if doc was modified in this same commit
  const docInCommit = changed.some(
    (f) => f.replace(/\\/g, "/").startsWith(`docs/items/${category}/`) &&
          f.replace(/\\/g, "/").replace(/\.md$/i, "").endsWith(slug),
  );
  if (docInCommit) continue;

  // .java changed without corresponding doc update → debt
  const docExists = existsSync(docPath);
  const reason = docExists
    ? `${expectedRel} 未更新（.java 改了但 .md 没碰，需添加 timeline 条目）`
    : `缺少 ${expectedRel}`;

  newDebts.push({
    slug,
    category,
    file,
    expectedDoc: expectedRel,
    docExists,
    reason,
    firstCommit: execSync("git rev-parse --short HEAD", { cwd: PROJECT, encoding: "utf-8" }).trim(),
    timestamp: Date.now(),
  });
}

if (newDebts.length === 0) process.exit(0);

const merged = [...existingDebt, ...newDebts];
saveDebt(merged);

for (const d of newDebts) {
  console.log(`[doc-audit] ⚠  ${d.file} → ${d.reason}`);
}
console.log(`[doc-audit] ${newDebts.length} 个文档债务已记录到 .opencode/.doc-debt.json`);
