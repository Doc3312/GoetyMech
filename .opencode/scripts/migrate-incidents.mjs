#!/usr/bin/env node
/**
 * migrate-incidents.mjs — Convert legacy incident files to new RCA template format.
 * Reads all .md files under docs/incidents/, extracts metadata, wraps in YAML front matter + 4-section layout.
 * Originals are backed up to docs/incidents/.archive/ before modification.
 *
 * Usage: node .opencode/scripts/migrate-incidents.mjs [--dry-run]
 */

import { readFileSync, writeFileSync, readdirSync, statSync, existsSync, mkdirSync, copyFileSync, renameSync } from "fs";
import { join, relative, dirname, basename } from "path";
import { execSync } from "child_process";

const PROJECT = execSync("git rev-parse --show-toplevel", { encoding: "utf-8" }).trim();
const INCIDENTS = join(PROJECT, "docs", "incidents");
const ARCHIVE = join(INCIDENTS, ".archive");
const DRY_RUN = process.argv.includes("--dry-run");

function walkSubdirs(dir) {
  const results = [];
  if (!existsSync(dir)) return results;
  for (const entry of readdirSync(dir)) {
    const full = join(dir, entry);
    if (entry.startsWith(".")) continue; // _craft/_meta/_mimic/_prototype are valid category dirs
    const st = statSync(full);
    if (st.isDirectory()) {
      for (const f of readdirSync(full)) {
        if (f.endsWith(".md") && !f.startsWith("INDEX") && !f.startsWith(".")) {
          results.push({ file: join(full, f), subdir: entry, filename: f });
        }
      }
    } else if (st.isFile() && entry.endsWith(".md") && !entry.startsWith("INDEX") && !entry.startsWith(".")) {
      results.push({ file: full, subdir: ".", filename: entry });
    }
  }
  return results;
}

function extractOldMetadata(fullPath) {
  const raw = readFileSync(fullPath, "utf-8");
  const lines = raw.split("\n");
  let status = "unknown";
  let title = basename(fullPath, ".md");
  let date = "";
  let contentStart = 0;
  let tags = [];

  // Try to find status line
  for (let i = 0; i < Math.min(lines.length, 15); i++) {
    const line = lines[i].trim();
    if (line.startsWith("**状态") || line.startsWith("**Status")) {
      const m = line.match(/\*\*状态\*\*:\s*(\w+)/) || line.match(/\*\*Status\*\*:\s*(\w+)/);
      if (m) status = m[1];
      contentStart = i + 1;
    } else if (line.startsWith("status:")) {
      let raw = line.replace(/^status:\s*/, "").trim();
      // Strip comments in parens
      raw = raw.replace(/\s*\(.*$/, "");
      status = raw;
      contentStart = i + 1;
    }
    // Try YAML front matter
    if (i === 0 && line === "---") {
      const endIdx = lines.slice(1).findIndex(l => l.trim() === "---");
      if (endIdx > 0) {
        const yamlLines = lines.slice(1, endIdx + 1);
        for (const yl of yamlLines) {
          const m = yl.match(/^(\w[\w_]*)\s*:\s*(.*)$/);
          if (m) {
            const k = m[1], v = m[2].trim();
            if (k === "status") status = v;
            if (k === "title") title = v;
            if (k === "date") date = v;
            if (k === "tags") tags = v.replace(/^\[|\]$/g, "").split(",").map(s => s.trim().replace(/^"|"$/g, ""));
          }
        }
        contentStart = endIdx + 2;
      }
    }
    // title detection from header
    const hm = line.match(/^##?\s+(.+)/);
    if (hm && !title) {
      title = hm[1].replace(/^#+\s*/, "").trim();
    }
  }

  // Extract date from filename
  if (!date) {
    const dMatch = basename(fullPath, ".md").match(/(\d{8})/);
    if (dMatch) date = dMatch[1];
  }

  // Read body content (everything after metadata)
  const body = lines.slice(contentStart).join("\n").trim();

  // Generate tags from body/content
  if (tags.length === 0 && body) {
    const keywords = body.match(/[A-Z][a-zA-Z]+/g) || [];
    tags = [...new Set(keywords)].slice(0, 5);
  }

  // Try to guess related items from path structure
  let relatedItems = [];
  const relPath = relative(INCIDENTS, fullPath).replace(/\\/g, "/");
  // If file is in a subdir named after a registered item, use that
  const subdir = dirname(relPath);
  if (subdir !== "." && !subdir.startsWith("_")) {
    // Map incident subdirs to doc items paths
    const dirMap = {
      "chase-focus": "focus/chase-focus",
      "ectoplasm-catcher": "curio/ectoplasm-catcher",
      "flesh-casket": "curio/flesh-casket",
      "phantom-wand": "curio/phantom-wand",
      "remote-engine": "block/remote-engine",
      "stress-reader": "block/stress-reader",
    };
    relatedItems = dirMap[subdir] ? [dirMap[subdir]] : [];
  }

  // Normalize status to new system: open | verified | superseded | legacy
  const statusNorm = { verified: "verified", open: "open", superseded: "superseded",
    resolved: "verified", consolidated: "superseded", disputed: "legacy",
    unknown: "open" };
  status = statusNorm[status] || "open";

  return { status, title, date, tags, relatedItems, body };
}

function generateNewContent(fullPath, meta) {
  const relPath = relative(INCIDENTS, fullPath).replace(/\\/g, "/");
  // Generate clean ID: <parent_dir>/<basename_without_ext>
  const parentDir = dirname(relPath);
  const slug = basename(fullPath, ".md");
  const id = (parentDir === "." ? slug : `${parentDir}/${slug}`).replace(/\\/g, "/");

  // Extract sections heuristically from body
  const body = meta.body || "";
  let symptom = "", rootCause = "", fix = "", lessons = "";

  // Try to split by known section headers
  const sections = body.split(/\n##\s+/);
  for (const sec of sections) {
    const lower = sec.toLowerCase();
    if (lower.startsWith("symptom") || lower.includes("症状") || lower.includes("现象") || lower.includes("错误")) {
      symptom = sec.split("\n").slice(1).join("\n").trim();
    } else if (lower.includes("root") || lower.includes("根因") || lower.includes("原因") || lower.includes("cause")) {
      rootCause = sec.split("\n").slice(1).join("\n").trim();
    } else if (lower.includes("fix") || lower.includes("修复") || lower.includes("解决") || lower.includes("solution")) {
      fix = sec.split("\n").slice(1).join("\n").trim();
    } else if (lower.includes("lesson") || lower.includes("经验") || lower.includes("总结") || lower.includes("教训")) {
      lessons = sec.split("\n").slice(1).join("\n").trim();
    }
  }

  const descMatch = body.match(/^#\s+(.+)/m);
  let title = meta.title;
  if (descMatch) title = descMatch[1];

  let yaml = `---
id: "${id}"
title: "${title.replace(/"/g, '\\"')}"
status: ${meta.status}
date: "${meta.date}"
tags: [${meta.tags.map(t => `"${t.replace(/"/g, '\\"')}"`).join(", ")}]
related_items: [${meta.relatedItems.map(t => `"${t}"`).join(", ")}]
related_commits: []
supersedes: []
superseded_by: []
---`;

  yaml += `

## 症状

${symptom || "<!-- TODO: human review — 描述问题现象 -->"}

## 根因

${rootCause || "<!-- TODO: human review — 描述根本原因 -->"}

## 修复

${fix || "<!-- TODO: human review — 描述修复方案 -->"}

## 经验提炼

${lessons || "<!-- TODO: human review — 从这次事件中学到的教训 -->"}
`;

  // Preserve original body as appendix if not fully split
  if (body && !symptom && !rootCause && !fix && !lessons) {
    yaml += `\n## 原始记录\n\n${body}\n`;
  }

  return yaml;
}

const files = walkSubdirs(INCIDENTS);
console.log(`Found ${files.length} incident files to migrate.`);

if (DRY_RUN) {
  console.log("[DRY-RUN] No files will be modified.\n");
}

if (!DRY_RUN) {
  if (!existsSync(ARCHIVE)) mkdirSync(ARCHIVE, { recursive: true });
}

let migrated = 0;
let skipped = 0;

for (const { file, subdir, filename } of files) {
  const meta = extractOldMetadata(file);

  // Check if already migrated (has YAML front matter with id/title/status properly)
  try {
    const raw = readFileSync(file, "utf-8");
    const hasFrontMatter = raw.startsWith("---");
    const hasRequiredSections = raw.includes("## 症状") && raw.includes("## 根因") && raw.includes("## 修复") && raw.includes("## 经验提炼");
    if (hasFrontMatter && hasRequiredSections) {
      console.log(`  SKIP (already migrated): ${filename}`);
      skipped++;
      continue;
    }
  } catch {}

  const newContent = generateNewContent(file, meta);

  if (DRY_RUN) {
    console.log(`  WOULD MIGRATE: ${subdir}/${filename}`);
    console.log(`    id=${subdir}, status=${meta.status}, date=${meta.date}`);
    migrated++;
  } else {
    const backupPath = join(ARCHIVE, `${subdir}_${filename}`);
    copyFileSync(file, backupPath);
    writeFileSync(file, newContent, "utf-8");
    console.log(`  MIGRATED: ${subdir}/${filename} → backup: .archive/${subdir}_${filename}`);
    migrated++;
  }
}

console.log(`\nDone: ${migrated} migrated, ${skipped} skipped.`);
