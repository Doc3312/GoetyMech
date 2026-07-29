#!/usr/bin/env node
/**
 * validate-index.mjs — Validate INDEX.yaml structure and cross-references.
 * Simplified: reads INDEX.yaml line-by-line, extracts fields, checks consistency.
 */
import { readFileSync, existsSync } from "fs";
import { join } from "path";
import { execSync } from "child_process";

const PROJECT = execSync("git rev-parse --show-toplevel", { encoding: "utf-8" }).trim();
const INDEX_FILE = join(PROJECT, "docs", "INDEX.yaml");

let errors = 0, warnings = 0;

if (!existsSync(INDEX_FILE)) {
  console.error("[validate] ERROR: INDEX.yaml not found.");
  process.exit(1);
}

const lines = readFileSync(INDEX_FILE, "utf-8").split("\n");

// State machine parsing
const items = [], incidents = [], patterns = [];
let section = null;   // "items" | "incidents" | "patterns" | "adrs"
let cur = null;       // current object being built

function commit() {
  if (!cur) return;
  if (section === "items" && cur.slug) items.push(cur);
  if (section === "incidents" && cur.id) incidents.push(cur);
  if (section === "patterns" && cur.pattern_id) patterns.push(cur);
  cur = null;
}

function parseValue(raw) {
  let v = raw.trim();
  if ((v.startsWith('"') && v.endsWith('"')) || (v.startsWith("'") && v.endsWith("'"))) {
    v = v.slice(1, -1);
  }
  return v;
}

function parseArray(raw) {
  const inner = raw.trim().replace(/^\[|\]$/g, "");
  if (!inner) return [];
  return inner.split(",").map(s => s.trim().replace(/^["']|["']$/g, "")).filter(Boolean);
}

for (const line of lines) {
  const t = line;
  // Section headers
  if (/^\w+:/.test(t) && !t.startsWith(" ") && !t.startsWith("-")) {
    commit();
    if (t.startsWith("items:")) { section = "items"; continue; }
    if (t.startsWith("incidents:")) { section = "incidents"; continue; }
    if (t.startsWith("patterns:")) { section = "patterns"; continue; }
    if (t.startsWith("adrs:")) { section = "adrs"; continue; }
    section = null;
    continue;
  }
  if (!section || t.startsWith("#") || t.startsWith("---") || t.startsWith("generated_at") || t.startsWith("total_")) continue;

  // New list item: "  - key: value"
  const dashMatch = t.match(/^\s*-\s+(\w+):\s*(.+)/);
  if (dashMatch) {
    commit();
    cur = {};
    const key = dashMatch[1], valRaw = dashMatch[2].trimEnd();
    if (valRaw.startsWith("[") && valRaw.endsWith("]")) {
      cur[key] = parseArray(valRaw);
    } else {
      cur[key] = parseValue(valRaw);
    }
    cur._section = section;
    continue;
  }

  // Continuation field: "    key: value"
  if (cur) {
    const contMatch = t.match(/^\s{2,}(\w+):\s*(.+)/);
    if (contMatch) {
      const key = contMatch[1], valRaw = contMatch[2].trimEnd();
      if (valRaw.startsWith("[") && valRaw.endsWith("]")) {
        cur[key] = parseArray(valRaw);
      } else {
        cur[key] = parseValue(valRaw);
      }
    }
  }
}
commit();

// Build lookup maps
const itemMap = new Map();
for (const it of items) itemMap.set(`${it.category}/${it.slug}`, it);

const incidentMap = new Map();
for (const inc of incidents) incidentMap.set(inc.id, inc);

// === Validation ===
const VALID_STATUSES = ["open", "verified", "superseded", "legacy"];
const VALID_PATTERN_CATS = ["pitfall", "convention", "idiom", "antipattern"];

// Structure checks
for (const it of items) {
  if (!it.slug || !it.category) {
    console.error(`[validate] ERROR: item missing slug/category: ${JSON.stringify(it)}`);
    errors++;
  }
  const expectedCat = it.doc_path?.split("/")[1];
  if (expectedCat && it.category !== expectedCat) {
    console.warn(`[validate] WARN: item "${it.slug}" category="${it.category}" but path has "${expectedCat}"`);
    warnings++;
  }
}

for (const inc of incidents) {
  if (!VALID_STATUSES.includes(inc.status)) {
    console.error(`[validate] ERROR: incident "${inc.id}" has status "${inc.status}" — valid: ${VALID_STATUSES}`);
    errors++;
  }
  for (const ref of (Array.isArray(inc.related_items) ? inc.related_items : [])) {
    if (ref && !itemMap.has(ref)) {
      console.warn(`[validate] WARN: incident "${inc.id}" refs unknown item "${ref}"`);
      warnings++;
    }
  }
}

for (const p of patterns) {
  if (!VALID_PATTERN_CATS.includes(p.category)) {
    console.error(`[validate] ERROR: pattern "${p.pattern_id}" category "${p.category}" — valid: ${VALID_PATTERN_CATS}`);
    errors++;
  }
}

if (errors > 0) {
  console.log(`\n[validate] FAILED: ${errors} errors, ${warnings} warnings`);
  process.exit(1);
}
if (warnings > 0) {
  console.log(`\n[validate] OK with warnings: ${warnings} issues`);
  process.exit(2);
}
console.log(`[validate] PASSED: ${items.length} items, ${incidents.length} incidents, ${patterns.length} patterns`);
process.exit(0);
