#!/usr/bin/env node
/**
 * find.mjs — 查前置模组里的注册项（物品/方块/实体/效果/tag/粒子/纹理…）
 *
 * 用法：
 *   node tools/premod/find.mjs cursed                 # 模糊查 id / 字段名
 *   node tools/premod/find.mjs ingot --mod goety      # 限定模组
 *   node tools/premod/find.mjs shaft --domain blocks  # 限定注册域
 *   node tools/premod/find.mjs fan_casing --resources # 连资源域(texture/tag/particle)一起查
 *   node tools/premod/find.mjs --purge                # 列出“引用了但不存在”的 id
 *
 * 输出每行包含：完整 id、域、Java 取值表达式、以及本项目里已有的使用先例。
 */
import fs from "node:fs";
import path from "node:path";

const ROOT = process.cwd();
const IDX = path.join(ROOT, "tools/premod/index");
const argv = process.argv.slice(2);
const flag = (name) => argv.includes(`--${name}`);
const opt = (name, def) => {
  const i = argv.indexOf(`--${name}`);
  return i >= 0 && argv[i + 1] ? argv[i + 1] : def;
};

const load = (f) => (fs.existsSync(path.join(IDX, f)) ? JSON.parse(fs.readFileSync(path.join(IDX, f), "utf8")) : null);
const goety = load("goety.json");
const create = load("create.json");
const usage = load("usage.json") ?? {};

if (!goety && !create) {
  console.error("索引不存在，先运行: node tools/premod/build.mjs");
  process.exit(1);
}

const mods = {};
if (goety) mods.goety = goety;
if (create) mods.create = create;

const modFilter = opt("mod", null);
const domainFilter = opt("domain", null);
const limit = Number(opt("limit", 15));
const queries = argv.filter((a) => !a.startsWith("--") && a !== modFilter && a !== domainFilter && a !== opt("limit", null));

function entryLine(mod, id, e, q) {
  const holderSimple = e.holder.split(".").pop();
  const expr = `${holderSimple}.${e.field}.get()`;
  const type = (e.javaType || "").replace(/^net\.minecraft\.(world|core|sounds|forge)\.[\w.]*\./, "");
  const used = usage[`${mod}:${id}`] ?? [];
  const where = used.slice(0, 2).map((u) => `${u.file.split("/").pop()}:${u.line}`).join(", ");
  const mark = q && id.toLowerCase() === q.toLowerCase() ? "★" : " ";
  return `${mark} ${(mod + ":" + id).padEnd(38)} ${String(e.domain).padEnd(16)} ${expr.padEnd(46)} ${type.padEnd(24)} ${used.length ? `已用×${used.length} ${where}` : "未用"}`;
}

function search(q) {
  const rows = [];
  for (const [mod, idx] of Object.entries(mods)) {
    if (modFilter && mod !== modFilter) continue;
    for (const [id, e] of Object.entries(idx.entries)) {
      if (domainFilter && e.domain !== domainFilter) continue;
      if (q) {
        const hay = `${id} ${e.field} ${e.javaType ?? ""}`.toLowerCase();
        if (!hay.includes(q.toLowerCase())) continue;
      }
      rows.push({ mod, id, e, score: q && id.toLowerCase() === q.toLowerCase() ? 0 : q && id.toLowerCase().startsWith(q.toLowerCase()) ? 1 : 2 });
    }
  }
  if (flag("resources")) {
    for (const [mod, idx] of Object.entries(mods)) {
      if (modFilter && mod !== modFilter) continue;
      const groups = [
        ["tags", idx.resources.tags, "tag"],
        ["particles", idx.resources.particles, "particle"],
        ["textures", idx.resources.textures, "texture"],
      ];
      for (const [kind, list, domain] of groups) {
        if (domainFilter && domainFilter !== domain) continue;
        for (const id of list) {
          if (q && !id.toLowerCase().includes(q.toLowerCase())) continue;
          rows.push({ mod, id, e: { domain, field: "—", holder: "—", javaType: kind }, score: 3 });
        }
      }
    }
  }
  rows.sort((a, b) => a.score - b.score || a.mod.localeCompare(b.mod) || a.id.localeCompare(b.id));
  return rows;
}

if (flag("purge")) {
  const bad = [];
  for (const [key, refs] of Object.entries(usage)) {
    const [mod, id] = key.split(":");
    const idx = mods[mod];
    if (!idx) continue;
    const known =
      idx.entries[id] ||
      idx.resources.tags.includes(id) ||
      idx.resources.particles.includes(id) ||
      idx.resources.textures.includes(id);
    if (!known) bad.push({ key, refs });
  }
  if (!bad.length) { console.log("✔ 没有发现悬空引用"); process.exit(0); }
  for (const b of bad) console.log(`✗ ${b.key}  (${b.refs.length} 处)  ${b.refs[0].file}:${b.refs[0].line}`);
  console.log(`\n共 ${bad.length} 个 id 引用了前置里不存在的注册项`);
  process.exit(0);
}

const q = queries[0];
if (!q && !flag("resources")) {
  console.log("用法: node tools/premod/find.mjs <关键词> [--mod goety|create] [--domain items] [--resources] [--limit 15]");
  console.log("\n可用域:", [...new Set(Object.values(mods).flatMap((m) => Object.values(m.entries).map((e) => e.domain)))].sort().join(", "));
  process.exit(0);
}

const rows = search(q).slice(0, limit);
if (!rows.length) {
  console.log(`没有匹配 "${q}"`);
  process.exit(0);
}
for (const r of rows) console.log(entryLine(r.mod, r.id, r.e, q));
console.log(`\n共 ${search(q).length} 条匹配，显示前 ${rows.length} 条`);
