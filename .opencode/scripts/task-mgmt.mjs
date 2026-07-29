#!/usr/bin/env node
// task-mgmt: 多对话任务声索与完成
// start:    node task-mgmt.mjs start --desc "描述" --files "f1.java,f2.java"
// complete: node task-mgmt.mjs complete [--id <id>]
// status:   node task-mgmt.mjs status
// check:    node task-mgmt.mjs check --files "f1.java"  → 返回文件是否被其他活跃任务声索

import { existsSync, readFileSync, writeFileSync, mkdirSync } from "fs";
import { dirname, join } from "path";
import { createRequire } from "module";
const require = createRequire(import.meta.url);
const { execSync } = require("child_process");

const PROJECT = execSync("git rev-parse --show-toplevel", { encoding: "utf-8" }).trim();
const STATE_PATH = join(PROJECT, ".opencode", ".workflow-state.json");

function load() {
  try {
    if (existsSync(STATE_PATH)) return JSON.parse(readFileSync(STATE_PATH, "utf-8"));
  } catch {}
  return { tasks: [] };
}

function save(state) {
  const dir = dirname(STATE_PATH);
  if (!existsSync(dir)) mkdirSync(dir, { recursive: true });
  writeFileSync(STATE_PATH, JSON.stringify(state, null, 2), "utf-8");
}

function startTask(desc, files) {
  const state = load();
  // 检查文件冲突
  const active = state.tasks.filter((t) => t.status === "active");
  for (const f of files) {
    for (const t of active) {
      if (t.files.some((tf) => tf.endsWith(f) || f.endsWith(tf))) {
        console.log(`[task-mgmt] ⚠ 文件 ${f} 已被任务 ${t.id} (${t.desc}) 声索`);
        process.exit(1);
      }
    }
  }
  const task = {
    id: (Math.random().toString(36).slice(2, 8)),
    desc,
    files: [...new Set(files.map((f) => f.replace(/\\/g, "/")))],
    status: "active",
    startedAt: new Date().toISOString(),
  };
  state.tasks.push(task);
  save(state);
  console.log(`[task-mgmt] ✓ 任务已注册: ${task.id} — ${desc}`);
  console.log(`[task-mgmt]   声索文件: ${task.files.length} 个`);
  task.files.forEach((f) => console.log(`      ${f}`));
  console.log(`[task-mgmt]   完成后执行: task-mgmt.mjs complete --id ${task.id}`);
}

function completeTask(id) {
  const state = load();
  const active = state.tasks.filter((t) => t.status === "active");
  if (active.length === 0) { console.log("[task-mgmt] 没有活跃任务"); process.exit(1); }
  let found = false;
  for (const t of active) {
    if (!id || t.id.startsWith(id)) {
      t.status = "complete";
      t.completedAt = new Date().toISOString();
      found = true;
      console.log(`[task-mgmt] ✓ 任务已完成: ${t.id} — ${t.desc}`);
    }
  }
  if (!found) { console.log(`[task-mgmt] 未找到匹配任务: ${id}`); process.exit(1); }
  save(state);
}

function showStatus() {
  const state = load();
  const active = state.tasks.filter((t) => t.status === "active");
  const done = state.tasks.filter((t) => t.status === "complete");
  console.log("=== 工作流状态 ===");
  console.log(`总任务: ${state.tasks.length} | 活跃: ${active.length} | 完成: ${done.length}`);
  if (active.length > 0) {
    console.log("\n⏳ 未完成任务：");
    for (const t of active) {
      console.log(`  [${t.id}] ${t.desc}`);
      t.files.forEach((f) => console.log(`       ${f}`));
    }
  }
  process.exit(active.length > 0 ? 1 : 0);
}

function checkConflict(files) {
  const state = load();
  const active = state.tasks.filter((t) => t.status === "active");
  for (const f of files) {
    const nf = f.replace(/\\/g, "/");
    for (const t of active) {
      if (t.files.some((tf) => nf.endsWith(tf) || tf.endsWith(nf))) {
        console.log(`[task-mgmt] ⚠ ${f} → 被任务 [${t.id}] ${t.desc} 声索`);
        process.exit(1);
      }
    }
  }
  console.log("[task-mgmt] ✓ 无冲突");
  process.exit(0);
}

const args = process.argv.slice(2);
const cmd = args[0];
const descIdx = args.indexOf("--desc");
const filesIdx = args.indexOf("--files");
const idIdx = args.indexOf("--id");
const desc = descIdx >= 0 ? args[descIdx + 1] : "";
const files = filesIdx >= 0 ? args[filesIdx + 1].split(",").map((s) => s.trim()).filter(Boolean) : [];
const id = idIdx >= 0 ? args[idIdx + 1] : "";

switch (cmd) {
  case "start": startTask(desc || "未命名", files); break;
  case "complete": completeTask(id); break;
  case "status": showStatus(); break;
  case "check": checkConflict(files); break;
  default:
    console.log("用法:");
    console.log("  start   --desc <描述> --files <f1,f2>");
    console.log("  complete [--id <id>]");
    console.log("  status");
    console.log("  check   --files <f1,f2>");
    process.exit(1);
}
