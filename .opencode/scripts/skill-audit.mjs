#!/usr/bin/env node
/**
 * Skill 健康检查脚本
 * 检查：frontmatter、断链、交叉引用覆盖、编码问题
 * Usage: node .opencode/scripts/skill-audit.mjs [--fix]
 */
import * as fs from 'fs';
import * as path from 'path';

const SKILLS_DIR = path.join('.opencode', 'skills');
const EXCLUDE = ['_shared'];
const LINK_RX = /\[([^\]]+)\]\(([^)]+)\)/g;

const issues = [];

function relink(checkDir) {
  const links = [];
  for (const item of fs.readdirSync(checkDir, { withFileTypes: true })) {
    if (item.isDirectory()) {
      links.push(...relink(path.join(checkDir, item.name)));
    } else if (item.isFile() && item.name.endsWith('.md')) {
      links.push(path.join(checkDir, item.name));
    }
  }
  return links;
}

const allMds = relink(SKILLS_DIR);

for (const fp of allMds) {
  const content = fs.readFileSync(fp, 'utf-8');
  const lines = content.split('\n');
  const rel = path.relative(SKILLS_DIR, fp);

  // 1. Frontmatter check (only SKILL.md requires frontmatter)
  const isSkillMain = path.basename(fp) === 'SKILL.md' && !rel.startsWith('_shared');
  if (isSkillMain) {
    if (!content.startsWith('---')) {
      issues.push({ file: rel, severity: 'ERROR', msg: '缺少 frontmatter (---开头)' });
    } else {
      const frontEnd = content.indexOf('---', 3);
      if (frontEnd === -1) {
        issues.push({ file: rel, severity: 'ERROR', msg: 'frontmatter 未闭合' });
      } else {
        const front = content.slice(3, frontEnd);
        if (!/^name:\s/m.test(front)) issues.push({ file: rel, severity: 'WARN', msg: 'frontmatter 缺少 name' });
        if (!/^description:\s/m.test(front)) issues.push({ file: rel, severity: 'WARN', msg: 'frontmatter 缺少 description' });
      }
    }
  }

  // 2. Encoding check — flag non-ASCII chars that look like mojibake
  for (const [i, line] of lines.entries()) {
    // Check for common mojibake patterns
    if (/[\uFFFD]/.test(line)) {
      issues.push({ file: rel, severity: 'ERROR', msg: `行 ${i+1}: 包含替换字符 U+FFFD` });
    }
  }

  // 3. Link check
  let m;
  LINK_RX.lastIndex = 0;
  while ((m = LINK_RX.exec(content)) !== null) {
    const target = m[2];
    // Skip external URLs and section anchors
    if (target.startsWith('http') || target.startsWith('#')) continue;
    // Skip _shared references
    if (target.startsWith('_shared/')) continue;
    const resolved = path.resolve(path.dirname(fp), target);
    if (!fs.existsSync(resolved)) {
      issues.push({ file: rel, severity: 'WARN', msg: `断链: "${target}" (${m[1]})` });
    }
  }

  // 4. Cross-reference check (only for non-superseded SKILL.md)
  if (!rel.endsWith('/SKILL.md') || rel.startsWith('_shared')) continue;
  const hasSuperseded = content.includes('superseded_by');
  if (hasSuperseded) continue;

  // Check for "相关技能" section
  const skillDir = path.dirname(fp);
  const skillName = path.basename(skillDir);

  // Known skills that SHOULD have cross-references
  const needCrossRef = ['goety-addon-dev', 'entity-render-dev', 'jei-dev', 'item-ister-flat-quads', 'goety-block-anim-and-particle'];
  if (needCrossRef.includes(skillName) && !content.includes('相关技能')) {
    issues.push({ file: rel, severity: 'INFO', msg: '缺少「相关技能」段落' });
  }
}

// Summary
const bySeverity = { ERROR: [], WARN: [], INFO: [] };
for (const i of issues) bySeverity[i.severity].push(i);

console.log(`\n=== Skill Audit Report ===`);
console.log(`Scanned: ${allMds.length} files`);
console.log(`Issues: ${issues.length} (${bySeverity.ERROR.length} ERROR, ${bySeverity.WARN.length} WARN, ${bySeverity.INFO.length} INFO)\n`);

for (const sev of ['ERROR', 'WARN', 'INFO']) {
  if (bySeverity[sev].length === 0) continue;
  console.log(`--- ${sev} (${bySeverity[sev].length}) ---`);
  for (const i of bySeverity[sev]) {
    console.log(`  ${i.file}: ${i.msg}`);
  }
  console.log();
}

if (bySeverity.ERROR.length > 0) process.exit(1);
