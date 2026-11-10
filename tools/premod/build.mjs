#!/usr/bin/env node
/**
 * build.mjs — 生成前置模组（Goety / Create）的注册索引 + 本项目使用映射。
 *
 * 数据源策略（避免“按索引写代码却编译不过”）：
 *   - 注册表：从**正在编译的那个 jar** 用 javap 读字节码（字段名与注册 id 都来自 class，
 *     Goety 的字段名与 id 经常不一致，如 CURSED_METAL_INGOT -> cursed_ingot，只能从字节码配对）
 *   - create：源码 + jar 资源（源码版本与 jar 一致）
 *   - 资源域（tag / particle / texture）：从 jar 条目里读，保证能校验 JSON 里的字符串 id
 *
 * 输出：
 *   tools/premod/index/goety.json    { meta, entries, resources }
 *   tools/premod/index/create.json
 *   tools/premod/index/usage.json    { "goety:xxx": [ { file, line, kind } ] }
 *   tools/premod/index/meta.json
 *
 * 用法：node tools/premod/build.mjs [--quiet]
 */
import fs from "node:fs";
import path from "node:path";
import { spawnSync } from "node:child_process";

const ROOT = process.cwd();
const CONF = JSON.parse(fs.readFileSync(path.join(ROOT, "tools/premod/config.json"), "utf8"));
const OUT_DIR = path.join(ROOT, "tools/premod/index");
const QUIET = process.argv.includes("--quiet");
const log = (...a) => { if (!QUIET) console.log(...a); };
const expand = (p) => (p?.startsWith("~") ? path.join(process.env.USERPROFILE, p.slice(1)) : p);

/** RegistryObject 的泛型 -> 域 */
function domainOfType(javaType) {
  const t = (javaType || "").replace(/<.*$/, "");
  const map = {
    "net.minecraft.world.item.Item": "items",
    "net.minecraft.world.level.block.Block": "blocks",
    "net.minecraft.world.entity.EntityType": "entities",
    "net.minecraft.world.effect.MobEffect": "effects",
    "net.minecraft.world.entity.ai.attributes.Attribute": "attributes",
    "net.minecraft.world.item.enchantment.Enchantment": "enchantments",
    "net.minecraft.sounds.SoundEvent": "sounds",
    "net.minecraft.core.particles.ParticleType": "particles",
    "net.minecraft.world.inventory.MenuType": "menu_types",
    "net.minecraftforge.fluids.FluidType": "fluid_types",
    "net.minecraft.world.level.material.Fluid": "fluids",
    "net.minecraft.world.item.alchemy.Potion": "potions",
    "net.minecraft.world.item.crafting.RecipeType": "recipe_types",
    "net.minecraft.world.item.crafting.RecipeSerializer": "recipe_serializers",
    "net.minecraft.world.level.levelgen.feature.Feature": "features",
    "net.minecraft.world.level.levelgen.structure.StructureType": "structure_types",
    "net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType": "structure_processors",
    "net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType": "placement_types",
    "net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType": "trunk_placers",
    "net.minecraft.world.level.block.entity.BannerPattern": "banner_patterns",
    "net.minecraft.world.item.CreativeModeTab": "creative_tabs",
    "net.minecraft.world.entity.decoration.PaintingVariant": "paintings",
    "net.minecraft.world.level.block.entity.BlockEntityType": "block_entities",
  };
  return map[t] ?? "other";
}

function run(cmd, args) {
  const r = spawnSync(cmd, args, { encoding: "utf8", maxBuffer: 512 * 1024 * 1024 });
  if (r.error) throw new Error(`${path.basename(cmd)} 启动失败: ${r.error.code || r.error.message}（DSH 需 danger-full-access）`);
  if (r.status !== 0) throw new Error(`${path.basename(cmd)} 退出码 ${r.status}: ${(r.stderr || "").slice(0, 400)}`);
  return r.stdout;
}
const javaBin = (javaHome, name) =>
  path.join(javaHome, "bin", process.platform === "win32" ? `${name}.exe` : name);
const javap = (j, jar, fq) => run(javaBin(j, "javap"), ["-p", "-c", "-cp", jar, fq]);
const jarEntries = (j, jar) => run(javaBin(j, "jar"), ["tf", jar]).split(/\r?\n/).filter(Boolean);

/** 解析 javap -p -c：字段泛型 + <clinit> 里「最近的 ldc 字符串」配「putstatic」 */
function parseJavap(text) {
  const lines = text.split(/\r?\n/);
  const fieldType = new Map();
  const pairs = [];
  let start = lines.findIndex((l) => /^\s*static \{\};\s*$/.test(l));
  if (start < 0) return { fieldType, pairs };

  const fieldRe = /^\s{2}(?:public|private|protected)\s+(?:static\s+)?(?:final\s+)?([\w.$]+(?:<[^;]+>)?)\s+([A-Za-z_$][\w$]*);\s*$/;
  for (const line of lines.slice(0, start)) {
    const m = line.match(fieldRe);
    if (!m) continue;
    fieldType.set(m[2], m[1]); // 存原始类型（含 RegistryObject<...>）
  }

  let lastString = null;
  for (const line of lines.slice(start)) {
    const s = line.match(/\/\/ String (.+)$/);
    if (s) lastString = s[1];
    const p = line.match(/putstatic\s+#\d+\s+\/\/ Field ([\w$]+):/);
    if (p) {
      const field = p[1];
      const raw = fieldType.get(field);
      if (lastString && raw && raw.startsWith("net.minecraftforge.registries.RegistryObject<")) {
        const g = raw.match(/<(.+)>$/);
        pairs.push({ id: lastString, field, javaType: g ? g[1] : raw });
      }
      lastString = null;
    }
  }
  return { fieldType, pairs };
}

function findJar(root, prefix) {
  const base = expand(root);
  if (!fs.existsSync(base)) return null;
  const jars = [];
  const walk = (d, depth) => {
    if (depth > 4) return;
    for (const e of fs.readdirSync(d, { withFileTypes: true })) {
      const p = path.join(d, e.name);
      if (e.isDirectory()) walk(p, depth + 1);
      else if (e.name.toLowerCase().endsWith(".jar")) jars.push(p);
    }
  };
  for (const dir of fs.readdirSync(base)) {
    if (dir.startsWith(prefix)) walk(path.join(base, dir), 0);
  }
  jars.sort((a, b) => fs.statSync(b).mtimeMs - fs.statSync(a).mtimeMs);
  return jars[0] ?? null;
}

function collectResources(entries) {
  const tags = new Set();
  const particles = [];
  const textures = [];
  for (const e of entries) {
    const t = e.match(/^data\/[^/]+\/tags\/(.+)\.json$/);
    if (t) {
      const segs = t[1].split("/");
      tags.add(segs[segs.length - 1]);
      if (segs.length > 1) tags.add(segs.slice(1).join("/"));
      continue;
    }
    const p = e.match(/^assets\/[^/]+\/particles\/(.+)\.json$/);
    if (p) { particles.push(p[1]); continue; }
    const x = e.match(/^assets\/[^/]+\/textures\/(.+)\.png$/);
    if (x) textures.push(x[1]);
  }
  return { tags: [...tags].sort(), particles: particles.sort(), textures: textures.sort() };
}

function walkFiles(dir, cb) {
  if (!fs.existsSync(dir)) return;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walkFiles(p, cb);
    else cb(p);
  }
}

function buildGoety(cfg, javaHome) {
  const jar = findJar(cfg.jarRoot, cfg.jarDirPrefix);
  if (!jar) throw new Error("找不到 goety jar（检查 config.json jarRoot / jarDirPrefix）");
  log(`  jar: ${path.basename(jar)}`);
  const je = jarEntries(javaHome, jar);
  const classSet = new Set(je.filter((e) => e.endsWith(".class")).map((e) => e.replace(/\.class$/, "").replace(/\//g, ".")));

  const registryClasses = [];
  walkFiles(cfg.sourceDir, (f) => {
    if (!f.endsWith(".java")) return;
    const txt = fs.readFileSync(f, "utf8");
    if (!txt.includes("DeferredRegister.create")) return;
    registryClasses.push(path.relative(cfg.sourceDir, f).replace(/\\/g, ".").replace(/\.java$/, ""));
  });

  const entries = {};
  const skipped = [];
  let scanned = 0;
  for (const fqcn of registryClasses) {
    if (!classSet.has(fqcn)) { skipped.push(`${fqcn}: jar 中不存在（源码比依赖新）`); continue; }
    let out;
    try { out = javap(javaHome, jar, fqcn); } catch (e) { skipped.push(`${fqcn}: ${e.message}`); continue; }
    scanned++;
    for (const p of parseJavap(out).pairs) {
      if (entries[p.id]) continue;
      entries[p.id] = { domain: domainOfType(p.javaType), field: p.field, holder: fqcn, javaType: p.javaType };
    }
  }

  const resources = collectResources(je);
  return {
    meta: {
      mod: "goety", source: "jar", artifact: path.basename(jar),
      artifactMtime: new Date(fs.statSync(jar).mtimeMs).toISOString(),
      registryClasses: scanned, generatedAt: new Date().toISOString(),
      counts: { registry: Object.keys(entries).length, tags: resources.tags.length, particles: resources.particles.length, textures: resources.textures.length },
    },
    entries, resources, skipped,
  };
}

const REG_METHOD_DOMAIN = {
  block: "blocks", blockItem: "items", item: "items", blockEntity: "block_entities",
  fluid: "fluid_types", itemEntity: "entities", entity: "entities",
};

function buildCreate(cfg, javaHome) {
  const fields = [];
  walkFiles(cfg.sourceDir, (f) => { if (f.endsWith(".java")) fields.push(f); });

  const entries = {};
  const fieldRe = /(?:public|private|protected)?\s*static\s+final\s+([\w.]+)<([\w.$]+)>\s+([A-Z][A-Z0-9_]*)\s*=\s*REGISTRATE\.(\w+)\("([a-z0-9_]+)"/g;
  for (const f of fields) {
    const txt = fs.readFileSync(f, "utf8");
    if (!txt.includes("REGISTRATE.")) continue;
    const fqcn = path.relative(cfg.sourceDir, f).replace(/\\/g, ".").replace(/\.java$/, "");
    for (const m of txt.matchAll(fieldRe)) {
      const [, entryClass, javaType, field, method, id] = m;
      if (entries[id]) continue;
      entries[id] = { domain: REG_METHOD_DOMAIN[method] ?? "other", field, holder: fqcn, javaType, entryClass };
    }
  }

  // 配方类型：AllRecipeTypes 的枚举常量 -> Lang.asId(name())
  const art = path.join(cfg.sourceDir, "com", "simibubi", "create", "AllRecipeTypes.java");
  if (fs.existsSync(art)) {
    const txt = fs.readFileSync(art, "utf8");
    const body = txt.slice(txt.indexOf("public enum AllRecipeTypes"));
    for (const m of body.matchAll(/^\t([A-Z][A-Z0-9_]+)\(/gm)) {
      const id = m[1].toLowerCase();
      entries[id] ||= { domain: "recipe_types", field: m[1], holder: "com.simibubi.create.AllRecipeTypes", javaType: "RecipeType" };
    }
  }

  const jar = cfg.jarRoot ? findJar(cfg.jarRoot, cfg.jarDirPrefix) : null;
  let resources = { tags: [], particles: [], textures: [] };
  if (jar) {
    log(`  jar: ${path.basename(jar)}`);
    resources = collectResources(jarEntries(javaHome, jar));
  }

  return {
    meta: {
      mod: "create", source: jar ? "source+jar" : "source", artifact: jar ? path.basename(jar) : path.basename(cfg.sourceDir),
      generatedAt: new Date().toISOString(),
      counts: { registry: Object.keys(entries).length, tags: resources.tags.length, particles: resources.particles.length, textures: resources.textures.length },
    },
    entries, resources, skipped: [],
  };
}

/** 本项目使用映射：java 引用 + 资源里的字符串 id */
function buildUsage(indexes, cfg) {
  const holders = new Map();
  for (const [mod, idx] of Object.entries(indexes)) {
    for (const [id, e] of Object.entries(idx.entries)) {
      const simple = e.holder.split(".").pop();
      if (!holders.has(simple)) holders.set(simple, { mod, ids: new Map() });
      holders.get(simple).ids.set(e.field, id);
    }
  }

  const usage = {};
  const add = (key, rec) => { (usage[key] ||= []).push(rec); };

  walkFiles(path.join(ROOT, cfg.javaDir), (f) => {
    if (!f.endsWith(".java")) return;
    const txt = fs.readFileSync(f, "utf8");
    const rel = path.relative(ROOT, f).replace(/\\/g, "/");
    const imports = new Map();
    for (const m of txt.matchAll(/^import\s+(?:static\s+)?([\w.]+)\.(\w+);/gm)) imports.set(m[2], m[1]);
    txt.split(/\r?\n/).forEach((line, i) => {
      for (const m of line.matchAll(/\b([A-Z][A-Za-z0-9_]*)\.([A-Z][A-Z0-9_]{2,})\b/g)) {
        const [, cls, field] = m;
        const h = holders.get(cls);
        if (!h) continue;
        const owner = imports.get(cls);
        if (owner && !owner.endsWith(cls)) continue;
        const id = h.ids.get(field);
        if (id) add(`${h.mod}:${id}`, { file: rel, line: i + 1, kind: "java" });
      }
    });
  });

  walkFiles(path.join(ROOT, cfg.resourcesDir), (f) => {
    if (!/\.(json|mcmeta)$/i.test(f)) return;
    const rel = path.relative(ROOT, f).replace(/\\/g, "/");
    fs.readFileSync(f, "utf8").split(/\r?\n/).forEach((line, i) => {
      for (const m of line.matchAll(/"goety:([a-z0-9_./-]+)"/g)) add(`goety:${m[1]}`, { file: rel, line: i + 1, kind: "json" });
      for (const m of line.matchAll(/"create:([a-z0-9_./-]+)"/g)) add(`create:${m[1]}`, { file: rel, line: i + 1, kind: "json" });
    });
  });

  return usage;
}

function main() {
  const javaHome = expand(CONF.javaHome) ?? process.env.JAVA_HOME;
  fs.mkdirSync(OUT_DIR, { recursive: true });

  log("▶ goety（jar 字节码）");
  const goety = buildGoety(CONF.mods.goety, javaHome);
  log(`  注册 ${goety.meta.counts.registry} · tag ${goety.meta.counts.tags} · particle ${goety.meta.counts.particles} · texture ${goety.meta.counts.textures}`);
  if (goety.skipped.length) log(`  ⚠ 跳过 ${goety.skipped.length} 个注册类`);

  log("▶ create（源码 + jar 资源）");
  const create = buildCreate(CONF.mods.create, javaHome);
  log(`  注册 ${create.meta.counts.registry} · tag ${create.meta.counts.tags} · particle ${create.meta.counts.particles} · texture ${create.meta.counts.textures}`);

  log("▶ 本项目使用映射");
  const usage = buildUsage({ goety, create }, CONF.project);
  log(`  ${Object.keys(usage).length} 个前置 id 被引用，共 ${Object.values(usage).reduce((a, b) => a + b.length, 0)} 处`);

  const write = (name, data) => fs.writeFileSync(path.join(OUT_DIR, name), JSON.stringify(data, null, 1), "utf8");
  write("goety.json", goety);
  write("create.json", create);
  write("usage.json", usage);
  write("meta.json", {
    generatedAt: new Date().toISOString(),
    goety: goety.meta, create: create.meta,
    usageKeys: Object.keys(usage).length, skipped: goety.skipped,
  });
  log("\n✔ 完成");
}

main();
