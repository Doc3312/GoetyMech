#!/usr/bin/env node
/**
 * health-check.mjs — Read-only dashboard for project documentation health.
 * Outputs color-coded terminal report or JSON.
 *
 * Usage: node .opencode/scripts/health-check.mjs [--json]
 */

import { readFileSync, existsSync } from "fs";
import { join } from "path";
import { execSync } from "child_process";

const PROJECT = execSync("git rev-parse --show-toplevel", { encoding: "utf-8" }).trim();
const INDEX_FILE = join(PROJECT, "docs", "INDEX.yaml");
const WORKFLOW_FILE = join(PROJECT, ".opencode", ".workflow-state.json");
const DOC_DEBT_FILE = join(PROJECT, ".opencode", ".doc-debt.json");

const GREEN = "\x1b[32m";
const YELLOW = "\x1b[33m";
const RED = "\x1b[31m";
const RESET = "\x1b[0m";
const BOLD = "\x1b[1m";

function color(level, text) { return `${level}${text}${RESET}`; }
function statusIcon(ok, warn, text) {
  if (ok) return color(GREEN, `✓ ${text}`);
  return color(warn ? YELLOW : RED, `${warn ? "⚠" : "✗"} ${text}`);
}

function parseIndexYaml() {
  if (!existsSync(INDEX_FILE)) return null;
  const raw = readFileSync(INDEX_FILE, "utf-8");
  const items = [];
  const incidents = [];
  const patterns = [];
  let current = null;
  let section = null;
  for (const line of raw.split("\n")) {
    const t = line.trim();
    if (t.startsWith("items:")) { section = "items"; continue; }
    if (t.startsWith("incidents:")) { section = "incidents"; continue; }
    if (t.startsWith("patterns:")) { section = "patterns"; continue; }
    if (t.startsWith("adrs:")) { section = "adrs"; continue; }
    if (t.startsWith("generated_at:")) continue;
    if (t.startsWith("total_")) continue;
    if (!section || t.startsWith("#") || t.startsWith("---")) continue;

    const sm = t.match(/^\s*-\s+(\w+):\s*"?(.+?)"?\s*$/);
    if (sm) {
      if (current) flush();
      current = { _kind: section };
      let val = sm[2].replace(/^"/, "").replace(/"$/, "");
      if (sm[1] === "tags" || sm[1] === "related_items") {
        val = val.replace(/^\[|\]$/g, "");
        current[sm[1]] = val.split(",").map(s => s.trim().replace(/^"|"$/g, "")).filter(Boolean);
      } else if (sm[1] === "occurrence_count" || sm[1] === "timeline_entries") {
        current[sm[1]] = parseInt(val) || 0;
      } else if (sm[1] === "has_spec" || sm[1] === "has_timeline") {
        current[sm[1]] = val === "true";
      } else {
        current[sm[1]] = val;
      }
    }
  }
  if (current) flush();

  function flush() {
    if (!current) return;
    switch (current._kind) {
      case "items": items.push(current); break;
      case "incidents": incidents.push(current); break;
      case "patterns": patterns.push(current); break;
    }
    current = null;
  }
  return { items, incidents, patterns };
}

function computeStats(index) {
  const complete = index.items.filter(i => i.has_spec && i.has_timeline);
  const coverage = index.items.length > 0 ? (complete.length / index.items.length * 100).toFixed(1) : 0;

  const now = new Date();
  const thirtyDaysAgo = new Date(now - 30 * 24 * 60 * 60 * 1000);
  const staleIncidents = index.incidents.filter(i => {
    if (i.status !== "open") return false;
    if (!i.date) return true;
    return new Date(i.date) < thirtyDaysAgo;
  });

  const orphanIncidents = index.incidents.filter(i =>
    Array.isArray(i.related_items) && i.related_items.length === 0
  );

  const patternsWithoutItems = index.patterns.filter(p =>
    !Array.isArray(p.related_items) || p.related_items.length === 0
  );

  let activeTasks = 0;
  try {
    if (existsSync(WORKFLOW_FILE)) {
      const wf = JSON.parse(readFileSync(WORKFLOW_FILE, "utf-8"));
      activeTasks = (wf.tasks || []).filter(t => t.status === "in_progress").length;
    }
  } catch {}

  let docDebt = 0;
  try {
    if (existsSync(DOC_DEBT_FILE)) {
      const dd = JSON.parse(readFileSync(DOC_DEBT_FILE, "utf-8"));
      docDebt = dd.length;
    }
  } catch {}

  return {
    totalItems: index.items.length,
    completeItems: complete.length,
    coverage,
    totalIncidents: index.incidents.length,
    staleIncidents: staleIncidents.length,
    orphanIncidents: orphanIncidents.length,
    totalPatterns: index.patterns.length,
    activeTasks,
    docDebt,
    unstalePatterns: index.patterns.filter(p => p.occurrence_count > 0).length,
  };
}

const index = parseIndexYaml();
const jsonOutput = process.argv.includes("--json");

if (!index) {
  if (jsonOutput) {
    console.log(JSON.stringify({ error: "INDEX.yaml not found. Run build-index.mjs first." }));
  } else {
    console.log(color(RED, "INDEX.yaml not found. Run: node .opencode/scripts/build-index.mjs"));
  }
  process.exit(1);
}

const s = computeStats(index);

if (jsonOutput) {
  console.log(JSON.stringify(s, null, 2));
  process.exit(0);
}

// Terminal report
console.log(color(BOLD, "\n=== GoetyMech Health Check ===\n"));
console.log(`  Generated: ${new Date().toISOString().replace("T", " ").slice(0, 19)}`);

console.log(color(BOLD, "\n-- Documentation --"));
console.log(`  ${statusIcon(s.coverage >= 80, s.coverage >= 50, `Coverage: ${s.coverage}% (${s.completeItems}/${s.totalItems})`)}`);
console.log(`  ${statusIcon(s.docDebt < 10, s.docDebt < 30, `Doc Debt: ${s.docDebt} entries`)}`);

console.log(color(BOLD, "\n-- Incidents --"));
console.log(`  Total: ${s.totalIncidents}`);
console.log(`  ${statusIcon(s.staleIncidents < 3, s.staleIncidents < 10, `Stale (>30d open): ${s.staleIncidents}`)}`);
console.log(`  ${statusIcon(s.orphanIncidents < 3, s.orphanIncidents < 10, `Orphan (no item refs): ${s.orphanIncidents}`)}`);

console.log(color(BOLD, "\n-- Patterns --"));
console.log(`  Total: ${s.totalPatterns} (${s.unstalePatterns} active)`);

console.log(color(BOLD, "\n-- Workflow --"));
console.log(`  ${statusIcon(s.activeTasks < 2, s.activeTasks < 5, `Active tasks: ${s.activeTasks}`)}`);

console.log("");
