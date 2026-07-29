#!/usr/bin/env node
/**
 * validate-index.mjs — Validate INDEX.yaml and front matter consistency.
 * Three-step: (1) structure (hard errors), (2) consistency (soft warnings), (3) time drift (soft warnings).
 *
 * Usage: node .opencode/scripts/validate-index.mjs
 * Exit codes: 0=ok, 1=hard errors, 2=soft warnings only
 */

import { readFileSync, existsSync } from "fs";
import { join, dirname, relative } from "path";
import { execSync } from "child_process";

const PROJECT = execSync("git rev-parse --show-toplevel", { encoding: "utf-8" }).trim();
const DOCS = join(PROJECT, "docs");
const INDEX_FILE = join(PROJECT, "docs", "INDEX.yaml");

let errors = 0;
let warnings = 0;

function parseIndex() {
  if (!existsSync(INDEX_FILE)) {
    console.error("[validate] ERROR: INDEX.yaml not found. Run build-index.mjs first.");
    process.exit(1);
  }
  const raw = readFileSync(INDEX_FILE, "utf-8");
  const items = { list: [], bySlug: new Map() };
  const incidents = { list: [], byId: new Map() };
  const patterns = { list: [], byId: new Map() };
  const adrs = { list: [], byId: new Map() };

  let section = null;
  let current = null;
  for (const line of raw.split("\n")) {
    const t = line.trim();
    if (t.startsWith("items:")) { section = "items"; continue; }
    if (t.startsWith("incidents:")) { section = "incidents"; continue; }
    if (t.startsWith("patterns:")) { section = "patterns"; continue; }
    if (t.startsWith("adrs:")) { section = "adrs"; continue; }
    if (!section || t.startsWith("#") || t.startsWith("---") || t.startsWith("generated_at") || t.startsWith("total_")) continue;

    const sm = t.match(/^\s*-\s+(\w+):\s*"?(.+?)"?\s*$/);
    if (sm) {
      if (current) flushCurrent();
      current = { };
      const key = sm[1];
      let val = sm[2].replace(/^"/, "").replace(/"$/, "").trim();
      if (key === "tags" || key === "related_items") {
        val = val.replace(/^\[|\]$/g, "");
        current[key] = val.split(",").map(s => s.trim().replace(/^"|"$/g, "")).filter(Boolean);
      } else if (key === "occurrence_count" || key === "timeline_entries") {
        current[key] = parseInt(val) || 0;
      } else if (key === "has_spec" || key === "has_timeline") {
        current[key] = val === "true";
      } else {
        current[key] = val || "";
      }
    } else {
      const km = t.match(/^\s*-\s+(\w+):\s*\[([^\]]*)\]\s*$/);
      if (km && current) {
        const arr = km[2].split(",").map(s => s.trim().replace(/^"|"$/g, "")).filter(Boolean);
        current[km[1]] = arr;
      }
    }
  }
  if (current) flushCurrent();

  function flushCurrent() {
    if (!current) return;
    if (section === "items" && current.slug) {
      items.list.push(current);
      items.bySlug.set(`${current.category}/${current.slug}`, current);
    }
    if (section === "incidents" && current.id) {
      incidents.list.push(current);
      incidents.byId.set(current.id, current);
    }
    if (section === "patterns" && current.pattern_id) {
      patterns.list.push(current);
      patterns.byId.set(current.pattern_id, current);
    }
    if (section === "adrs" && current.adr_id) {
      adrs.list.push(current);
      adrs.byId.set(current.adr_id, current);
    }
    current = null;
  }

  return { items, incidents, patterns, adrs };
}

// Step 1: Structure validation (hard errors)
function validateStructure(index) {
  // Items must have slug + category
  for (const item of index.items.list) {
    if (!item.slug || !item.category) {
      console.error(`[validate] ERROR: item missing slug/category: ${JSON.stringify(item)}`);
      errors++;
    }
    const expectedCat = item.doc_path?.split("/")[1];
    if (expectedCat && item.category !== expectedCat) {
      console.error(`[validate] ERROR: item "${item.slug}" category="${item.category}" but path is docs/items/${expectedCat}/${item.slug}/`);
      errors++;
    }
  }

  // Incidents must have valid status
  const validStatuses = ["open", "verified", "superseded", "legacy"];
  for (const inc of index.incidents.list) {
    if (!validStatuses.includes(inc.status)) {
      console.error(`[validate] ERROR: incident "${inc.id}" has invalid status "${inc.status}". Valid: ${validStatuses.join(", ")}`);
      errors++;
    }
  }

  // Patterns must have category
  const validPatternCategories = ["pitfall", "convention", "idiom", "antipattern"];
  for (const p of index.patterns.list) {
    if (!validPatternCategories.includes(p.category)) {
      console.error(`[validate] ERROR: pattern "${p.pattern_id}" has invalid category "${p.category}"`);
      errors++;
    }
  }
}

// Step 2: Consistency (soft warnings)
function validateConsistency(index) {
  for (const inc of index.incidents.list) {
    for (const ref of inc.related_items) {
      if (!index.items.bySlug.has(ref)) {
        console.warn(`[validate] WARN: incident "${inc.id}" references unknown item "${ref}"`);
        warnings++;
      }
    }
    for (const ref of inc.supersedes || []) {
      if (!index.incidents.byId.has(ref)) {
        console.warn(`[validate] WARN: incident "${inc.id}" supersedes unknown incident "${ref}"`);
        warnings++;
      }
    }
  }

  for (const p of index.patterns.list) {
    for (const ref of p.related_items) {
      if (!index.items.bySlug.has(ref) && !ref.startsWith("_")) {
        console.warn(`[validate] WARN: pattern "${p.pattern_id}" references unknown item "${ref}"`);
        warnings++;
      }
    }
  }
}

// Step 3: Time drift (soft warnings)
function validateTimeDrift(index) {
  for (const item of index.items.list) {
    if (!item.has_timeline || item.timeline_entries === 0) continue;
    try {
      const docPath = join(PROJECT, "docs", item.doc_path.replace("spec.md", "timeline.md"));
      if (!existsSync(docPath)) continue;
      const logOut = execSync(
        `git log -1 --format="%ad" --date=short -- "${relative(PROJECT, docPath).replace(/\\/g, "/")}"`,
        { cwd: PROJECT, encoding: "utf-8" }
      ).trim();
      if (logOut && item.last_updated && logOut > item.last_updated) {
        console.warn(`[validate] WARN: "${item.slug}" last_updated=${item.last_updated} but git last modified=${logOut}`);
        warnings++;
      }
    } catch { /* skip git errors */ }
  }
}

const index = parseIndex();
validateStructure(index);
validateConsistency(index);
validateTimeDrift(index);

if (errors > 0) {
  console.log(`\n[validate] FAILED: ${errors} errors, ${warnings} warnings`);
  process.exit(1);
} else if (warnings > 0) {
  console.log(`\n[validate] OK with warnings: ${warnings} issues`);
  process.exit(2);
} else {
  console.log("[validate] PASSED: all checks clean");
  process.exit(0);
}
