#!/usr/bin/env node
/**
 * validate.mjs — 校验本项目里引用前置模组（goety / create）的字符串 id 是否真实存在。
 *
 * 为什么不能只做“扁平 id 集合”匹配：
 *   同一个前缀在不同上下文里指向不同注册域 —— `"tag": "goety:villagers"` 是 tag、
 *   `"type": "create:emptying"` 是配方类型、粒子 json 的 `"textures"` 是纹理、
 *   `"create:block/fan_casing"` 是模型纹理路径。扁平匹配会把它们全报成“不存在”。
 *   所以这里按 **文件位置 + 所在键名** 判断上下文，再落到对应的索引域。
 *
 * 用法：
 *   node tools/premod/validate.mjs            # 有未知 id 则 exit 1
 *   node tools/premod/validate.mjs --warn     # 只报告，不返回失败
 *   node tools/premod/validate.mjs --json
 */
import fs from "node:fs";
import path from "node:path";

const ROOT = process.cwd();
const IDX = path.join(ROOT, "tools/premod/index");
const RES = path.join(ROOT, "src/main/resources");
const argv = process.argv.slice(2);
const WARN_ONLY = argv.includes("--warn");
const AS_JSON = argv.includes("--json");

const load = (f) => JSON.parse(fs.readFileSync(path.join(IDX, f), "utf8"));
const goety = load("goety.json");
const create = load("create.json");
const mods = { goety, create };

const TEXTURE_KEYS = new Set(["texture", "textures", "particle", "base", "overlay", "empty", "filled", "edge", "all"]);

function walk(dir, cb) {
  if (!fs.existsSync(dir)) return;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, cb);
    else cb(p);
  }
}

/** 我们自己写在 goety/create 命名空间下的资源（patchouli 书、纹理等） */
const ownAssets = new Set();
const ownBasenames = new Set();
walk(path.join(RES, "assets"), (f) => {
  const rel = path.relative(RES, f).replace(/\\/g, "/");
  ownAssets.add(rel);
  ownBasenames.add(path.basename(rel));
});
const ownByMod = (mod, id) => {
  const needle = `assets/${mod}/`;
  return (
    ownAssets.has(`${needle}${id}`) ||
    ownAssets.has(`${needle}${id}.png`) ||
    ownAssets.has(`${needle}${id}.json`) ||
    ownBasenames.has(`${id}.json`) ||
    ownBasenames.has(`${id}.png`)
  );
};

const findings = [];

function check(mod, id, key, file, line) {
  const idx = mods[mod];
  const inParticlesJson = /\/particles\//.test(file);
  const isTextureKey = TEXTURE_KEYS.has(key) || id.includes("/");

  // 1) 粒子 json 的 textures：实际落在 textures/particle/ 下
  if (inParticlesJson && key === "textures") {
    if (idx.resources.textures.includes(`particle/${id}`)) return null;
    if (ownByMod(mod, `textures/particle/${id}`)) return null;
    return { mod, id, kind: "particle_texture", expect: `textures/particle/${id}.png` };
  }
  // 1.5) 完整资源路径（patchouli 图片等，带 .png 后缀）
  if (id.endsWith(".png")) {
    if (ownByMod(mod, id)) return null;
    return { mod, id, kind: "asset", expect: `assets/${mod}/${id}` };
  }
  // 2) 纹理路径（含 / 的，或 texture 类键）
  if (isTextureKey) {
    const p = id.startsWith("textures/") ? id : `textures/${id}`;
    if (idx.resources.textures.includes(id) || idx.resources.textures.includes(p.replace(/^textures\//, ""))) return null;
    if (ownByMod(mod, p)) return null;
    return { mod, id, kind: "texture", expect: `${p}.png` };
  }
  // 3) tag
  if (key === "tag" || key === "tags") {
    if (idx.resources.tags.includes(id)) return null;
    return { mod, id, kind: "tag", expect: `data/${mod}/tags/**/${id}.json` };
  }
  // 4) 注册项（物品/方块/实体/配方类型…）
  if (idx.entries[id]) return null;
  // 5) 兜底：我们自己在该命名空间下提供的资源（patchouli 分类/条目等）
  if (ownByMod(mod, id)) return null;
  // 6) 宽松兜底：tag / particle 名
  if (idx.resources.tags.includes(id) || idx.resources.particles.includes(id)) return null;

  return { mod, id, kind: "registry", expect: "注册表 / tag / 资源" };
}

walk(path.join(RES, "data"), scanFile);
walk(path.join(RES, "assets"), scanFile);

function scanFile(f) {
  if (!/\.json$/i.test(f)) return;
  const rel = path.relative(ROOT, f).replace(/\\/g, "/");
  const lines = fs.readFileSync(f, "utf8").split(/\r?\n/);
  let lastKey = "";
  lines.forEach((line, i) => {
    const keyMatch = line.match(/"([A-Za-z0-9_]+)"\s*:/);
    if (keyMatch) lastKey = keyMatch[1];
    for (const m of line.matchAll(/"(goety|create):([a-z0-9_./-]+)"/g)) {
      const [, mod, id] = m;
      if (id === mod) continue;
      const localKey = keyMatch ? keyMatch[1] : lastKey;
      const bad = check(mod, id, localKey, rel, i + 1);
      if (bad) findings.push({ ...bad, file: rel, line: i + 1, key: localKey });
    }
  });
}

const unique = new Map();
for (const f of findings) {
  const k = `${f.mod}:${f.id}`;
  if (!unique.has(k)) unique.set(k, { ...f, count: 0, refs: [] });
  const u = unique.get(k);
  u.count++;
  if (u.refs.length < 3) u.refs.push(`${f.file}:${f.line} (key=${f.key})`);
}

const list = [...unique.values()].sort((a, b) => b.count - a.count);

if (AS_JSON) {
  console.log(JSON.stringify({ ok: list.length === 0, total: list.length, findings: list }, null, 2));
} else if (!list.length) {
  console.log("✔ 前置 id 引用全部有效");
} else {
  console.log(`✗ 发现 ${list.length} 个可疑引用：\n`);
  for (const f of list) {
    console.log(`  ${f.mod}:${f.id}  [${f.kind}]  ×${f.count}  期望: ${f.expect}`);
    for (const r of f.refs) console.log(`      ${r}`);
  }
  console.log("\n提示：可能是拼写错误，也可能是前置版本升级后才有的新 id（对比 tools/premod/index/meta.json 的版本戳）。");
}
process.exit(list.length && !WARN_ONLY ? 1 : 0);
