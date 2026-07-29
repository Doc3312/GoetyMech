#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const [category, item, regName, className] = process.argv.slice(2);

if (!category || !item) {
  console.error('用法: node .opencode/scripts/new-item-doc.mjs <category> <item> [reg_name] [class_name]');
  console.error('可用类别: focus, curio, block, crafts, entity, reference, design');
  console.error('命名规则: item 必须使用 kebab-case (如 gateway-focus, not gateway_focus)');
  process.exit(1);
}

if (item.includes('_') && category !== 'reference') {
  console.error(`WARNING: item "${item}" 包含下划线。请使用 kebab-case (连字符)。`);
  console.error('  正确示例: gateway-focus, sculk-bind-focus, remote-storage-focus');
  process.exit(1);
}

const valid = ['focus', 'curio', 'block', 'crafts', 'entity', 'reference', 'design'];
if (!valid.includes(category)) {
  console.error(`类别 "${category}" 未注册。可用: ${valid.join(', ')}`);
  console.error('新增类别请先在 AGENTS.md 注册');
  process.exit(1);
}

const base = path.join('docs/items', category);
const isFlat = ['entity', 'reference'].includes(category);

const now = new Date();
const dateStr = now.toISOString().slice(0, 10);
const dateTimeStr = `${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

const templateDir = path.join('.opencode', 'templates');

if (isFlat) {
  const fp = path.join(base, `${item}.md`);
  if (fs.existsSync(fp)) {
    console.error(`已存在: ${fp}`);
    process.exit(1);
  }
  fs.mkdirSync(base, { recursive: true });
  const template = `# ${item}\n\n<!-- TODO: 填写内容 -->\n`;
  fs.writeFileSync(fp, template, 'utf-8');
  console.log(`CREATED: ${fp}`);
} else {
  const dir = path.join(base, item);
  if (fs.existsSync(dir)) {
    console.error(`已存在: ${dir}/ (请手动操作)`);
    process.exit(1);
  }
  fs.mkdirSync(dir, { recursive: true });

  // Load templates
  const specTpl = fs.existsSync(path.join(templateDir, 'spec-template.md'))
    ? fs.readFileSync(path.join(templateDir, 'spec-template.md'), 'utf-8')
    : null;
  const timelineTpl = fs.existsSync(path.join(templateDir, 'timeline-template.md'))
    ? fs.readFileSync(path.join(templateDir, 'timeline-template.md'), 'utf-8')
    : null;

  if (specTpl) {
    const spec = specTpl
      .replace(/\{category\}/g, category)
      .replace(/\{item\}/g, item)
      .replace(/\{reg_name\}/g, regName || `TODO:goetymech:${item}`)
      .replace(/\{class_name\}/g, className || 'TODO')
      .replace(/\{created_at\}/g, dateStr);
    fs.writeFileSync(path.join(dir, 'spec.md'), spec, 'utf-8');
  } else {
    const spec = `# ${item}\n\n<!-- TODO: spec-template.md 未找到，请运行时构建后重试 -->\n`;
    fs.writeFileSync(path.join(dir, 'spec.md'), spec, 'utf-8');
  }

  if (timelineTpl) {
    const timeline = timelineTpl
      .replace(/\{item\}/g, item)
      .replace(/\{created_at\}/g, dateTimeStr);
    fs.writeFileSync(path.join(dir, 'timeline.md'), timeline, 'utf-8');
  } else {
    const timeline = `# ${item} Timeline\n\n| 日期时间 | 类型 | 改动摘要 | 涉及文件 | 关联 |\n|----------|------|----------|----------|------|\n| ${dateTimeStr} | init | 初始创建。 | | |\n`;
    fs.writeFileSync(path.join(dir, 'timeline.md'), timeline, 'utf-8');
  }

  console.log(`CREATED: ${dir}/spec.md`);
  console.log(`CREATED: ${dir}/timeline.md`);
}
