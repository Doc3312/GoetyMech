/**
 * build-index.mjs — Scan docs/** / *.md for YAML front matter,
 * build docs/INDEX.yaml (single-file registry of all items, incidents, patterns, adrs).
 *
 * Usage: node .opencode/scripts/build-index.mjs [--quiet]
 */

import { readFileSync, writeFileSync, readdirSync, statSync, existsSync, mkdirSync } from "fs";
import { join, relative, dirname, basename } from "path";
import { execSync } from "child_process";

const PROJECT = execSync("git rev-parse --show-toplevel", { encoding: "utf-8" }).trim();
const DOCS = join(PROJECT, "docs");
const INDEX_FILE = join(PROJECT, "docs", "INDEX.yaml");

function walkDir(dir, files = []) {
  for (const entry of readdirSync(dir)) {
    if (entry.startsWith(".archive") || entry.startsWith(".")) continue;
    const full = join(dir, entry);
    if (statSync(full).isDirectory()) {
      walkDir(full, files);
    } else if (entry.endsWith(".md")) {
      files.push(full);
    }
  }
  return files;
}

function parseFrontMatter(text) {
  const lines = text.split("\n");
  if (lines[0].trim() !== "---") return null;
  const endIdx = lines.slice(1).findIndex((l) => l.trim() === "---");
  if (endIdx < 0) return null;
  const yamlLines = lines.slice(1, endIdx + 1);
  const result = {};
  for (const line of yamlLines) {
    const m = line.match(/^(\w[\w_]*)\s*:\s*(.*)$/);
    if (m) {
      let val = m[2].trim();
      if (val.startsWith("[") && val.endsWith("]")) {
        val = val.slice(1, -1).split(",").map((s) => s.trim().replace(/^['"]|['"]$/g, "")).filter(Boolean);
      } else {
        val = val.replace(/^['"]|['"]$/g, "");
      }
      result[m[1]] = val;
    }
  }
  return Object.keys(result).length > 0 ? result : null;
}

function buildItemsIndex(docFiles) {
  const items = [];
  const specFiles = docFiles.filter((f) => basename(f) === "spec.md");

  for (const f of specFiles) {
    const content = readFileSync(f, "utf-8");
    const fm = parseFrontMatter(content);
    const rel = relative(DOCS, f).replace(/\\/g, "/");
    const category = rel.split("/")[1] || "unknown";
    const itemSlug = rel.split("/")[2] || "unknown";
    const dir = dirname(f);

    let timelineStats = { lastEntry: "none", totalEntries: 0 };
    const tlFile = join(dir, "timeline.md");
    if (existsSync(tlFile)) {
      const tl = readFileSync(tlFile, "utf-8");
      const rows = tl.match(/^\| .+ \|/gm) || [];
      timelineStats.totalEntries = Math.max(0, rows.length - 1); // minus header
      if (rows.length > 1) {
        const lastRow = rows[rows.length - 1];
        const cells = lastRow.split("|").map((s) => s.trim());
        timelineStats.lastEntry = cells[1] || "unknown";
      }
    }

    items.push({
      slug: itemSlug,
      category,
      reg_name: fm?.reg_name || "",
      class_name: fm?.class_name || "",
      created_at: fm?.created_at || "",
      last_updated: fm?.last_updated || "",
      source_files: fm?.source_files || [],
      has_spec: true,
      has_timeline: existsSync(tlFile),
      timeline_entries: timelineStats.totalEntries,
      timeline_last: timelineStats.lastEntry,
      doc_path: rel,
    });
  }

  // Also find items with timeline but no spec
  const timelineFiles = docFiles.filter((f) => basename(f) === "timeline.md" && !existsSync(join(dirname(f), "spec.md")));
  for (const f of timelineFiles) {
    const rel = relative(DOCS, f).replace(/\\/g, "/");
    const category = rel.split("/")[1] || "unknown";
    const itemSlug = rel.split("/")[2] || "unknown";
    if (items.some((i) => i.slug === itemSlug && i.category === category)) continue;

    const tl = readFileSync(f, "utf-8");
    const rows = tl.match(/^\| .+ \|/gm) || [];
    let timelineStats = { lastEntry: "none", totalEntries: Math.max(0, rows.length - 1) };
    if (rows.length > 1) timelineStats.lastEntry = rows[rows.length - 1].split("|").map((s) => s.trim())[1] || "unknown";

    items.push({
      slug: itemSlug,
      category,
      reg_name: "",
      class_name: "",
      created_at: "",
      last_updated: "",
      source_files: [],
      has_spec: false,
      has_timeline: true,
      timeline_entries: timelineStats.totalEntries,
      timeline_last: timelineStats.lastEntry,
      doc_path: rel,
    });
  }

  items.sort((a, b) => a.category.localeCompare(b.category) || a.slug.localeCompare(b.slug));
  return items;
}

function buildIncidentsIndex(docFiles) {
  const incidents = [];
  const incidentFiles = docFiles.filter((f) => f.replace(/\\/g, "/").includes("/incidents/") && !f.endsWith("INDEX.md"));

  for (const f of incidentFiles) {
    const content = readFileSync(f, "utf-8");
    const fm = parseFrontMatter(content);
    const rel = relative(DOCS, f).replace(/\\/g, "/");

    incidents.push({
      id: fm?.id || basename(f, ".md"),
      title: fm?.title || basename(f, ".md"),
      status: fm?.status || "unknown",
      date: fm?.date || "",
      tags: Array.isArray(fm?.tags) ? fm.tags : [],
      related_items: Array.isArray(fm?.related_items) ? fm.related_items : [],
      related_commits: Array.isArray(fm?.related_commits) ? fm.related_commits : [],
      doc_path: rel,
    });
  }

  incidents.sort((a, b) => b.date.localeCompare(a.date));
  return incidents;
}

function buildPatternsIndex(docFiles) {
  const patterns = [];
  const patternFiles = docFiles.filter((f) => f.replace(/\\/g, "/").includes("/patterns/"));

  for (const f of patternFiles) {
    const content = readFileSync(f, "utf-8");
    const fm = parseFrontMatter(content);
    if (!fm) continue;
    const rel = relative(DOCS, f).replace(/\\/g, "/");

    patterns.push({
      id: fm.pattern_id || basename(f, ".md"),
      title: fm.title || basename(f, ".md"),
      category: fm.category || "unknown",
      severity: fm.severity || "minor",
      first_seen: fm.first_seen || "",
      last_seen: fm.last_seen || "",
      occurrence_count: parseInt(fm.occurrence_count) || 0,
      tags: Array.isArray(fm.tags) ? fm.tags : [],
      related_items: Array.isArray(fm?.related_items) ? fm.related_items : [],
      promoted_to: fm.promoted_to || "",
      doc_path: rel,
    });
  }

  patterns.sort((a, b) => a.id.localeCompare(b.id));
  return patterns;
}

function buildAdrsIndex(docFiles) {
  const adrs = [];
  const adrFiles = docFiles.filter((f) => f.replace(/\\/g, "/").includes("/adr/"));

  for (const f of adrFiles) {
    const content = readFileSync(f, "utf-8");
    const fm = parseFrontMatter(content);
    if (!fm) continue;
    const rel = relative(DOCS, f).replace(/\\/g, "/");

    adrs.push({
      id: fm.adr_id || basename(f, ".md"),
      title: fm.title || basename(f, ".md"),
      status: fm.status || "unknown",
      date: fm.date || "",
      doc_path: rel,
    });
  }

  adrs.sort((a, b) => a.id.localeCompare(b.id));
  return adrs;
}

const allDocs = walkDir(DOCS);
const itemsIdx = buildItemsIndex(allDocs);
const incidentsIdx = buildIncidentsIndex(allDocs);
const patternsIdx = buildPatternsIndex(allDocs);
const adrsIdx = buildAdrsIndex(allDocs);

const timestamp = new Date().toISOString().replace("T", " ").slice(0, 19);

const yaml = [
  "# Auto-generated by build-index.mjs — DO NOT EDIT MANUALLY",
  `# Generated: ${timestamp}`,
  "#",
  "# 列说明:",
  "#   items:     所有注册物品/方块的文档索引",
  "#   incidents: 所有RCA事件记录索引",
  "#   patterns:  所有代码模式/陷阱索引",
  "#   adrs:      所有架构决策记录索引",
  "#   stats:     统计摘要",
  "",
  "---",
  `generated_at: "${timestamp}"`,
  `total_items: ${itemsIdx.length}`,
  `total_incidents: ${incidentsIdx.length}`,
  `total_patterns: ${patternsIdx.length}`,
  `total_adrs: ${adrsIdx.length}`,
  "",
  "items:",
];

for (const item of itemsIdx) {
  yaml.push(`  - slug: "${item.slug}"`);
  yaml.push(`    category: "${item.category}"`);
  yaml.push(`    reg_name: "${item.reg_name}"`);
  yaml.push(`    class_name: "${item.class_name}"`);
  yaml.push(`    created_at: "${item.created_at}"`);
  yaml.push(`    last_updated: "${item.last_updated}"`);
  yaml.push(`    has_spec: ${item.has_spec}`);
  yaml.push(`    has_timeline: ${item.has_timeline}`);
  yaml.push(`    timeline_entries: ${item.timeline_entries}`);
  yaml.push(`    timeline_last: "${item.timeline_last}"`);
  yaml.push(`    doc_path: "${item.doc_path}"`);
}

yaml.push("");
yaml.push("incidents:");
for (const inc of incidentsIdx) {
  yaml.push(`  - id: "${inc.id}"`);
  yaml.push(`    title: "${inc.title}"`);
  yaml.push(`    status: "${inc.status}"`);
  yaml.push(`    date: "${inc.date}"`);
  yaml.push(`    tags: [${inc.tags.map((t) => `"${t}"`).join(", ")}]`);
  yaml.push(`    related_items: [${inc.related_items.map((t) => `"${t}"`).join(", ")}]`);
  yaml.push(`    related_commits: [${inc.related_commits.join(", ")}]`);
  yaml.push(`    doc_path: "${inc.doc_path}"`);
}

yaml.push("");
yaml.push("patterns:");
for (const p of patternsIdx) {
  yaml.push(`  - pattern_id: "${p.id}"`);
  yaml.push(`    title: "${p.title}"`);
  yaml.push(`    category: "${p.category}"`);
  yaml.push(`    severity: "${p.severity}"`);
  yaml.push(`    first_seen: "${p.first_seen}"`);
  yaml.push(`    last_seen: "${p.last_seen}"`);
  yaml.push(`    occurrence_count: ${p.occurrence_count}`);
  yaml.push(`    tags: [${p.tags.map((t) => `"${t}"`).join(", ")}]`);
  yaml.push(`    related_items: [${p.related_items.join(", ")}]`);
  yaml.push(`    doc_path: "${p.doc_path}"`);
}

yaml.push("");
yaml.push("adrs:");
for (const a of adrsIdx) {
  yaml.push(`  - adr_id: "${a.id}"`);
  yaml.push(`    title: "${a.title}"`);
  yaml.push(`    status: "${a.status}"`);
  yaml.push(`    date: "${a.date}"`);
  yaml.push(`    doc_path: "${a.doc_path}"`);
}

writeFileSync(INDEX_FILE, yaml.join("\n"), "utf-8");
console.log(`[build-index] INDEX.yaml generated: ${itemsIdx.length} items, ${incidentsIdx.length} incidents, ${patternsIdx.length} patterns, ${adrsIdx.length} ADRs`);
