import fs from 'node:fs';
import path from 'node:path';

const ASSETS = 'src/main/resources/assets/goety';
const BOOK = 'patchouli_books/black_book';

function readJson(filePath) {
  try {
    let txt = fs.readFileSync(filePath, 'utf8');
    if (txt.charCodeAt(0) === 0xFEFF) txt = txt.slice(1);
    return JSON.parse(txt);
  } catch {
    return {};
  }
}

function writeJson(filePath, data) {
  const dir = path.dirname(filePath);
  fs.mkdirSync(dir, { recursive: true });
  const json = JSON.stringify(data, null, 2) + '\n';
  fs.writeFileSync(filePath, json, 'utf8');
}

function appendLang(filePath, key, value) {
  const data = readJson(filePath);
  if (data[key] === value) return false;
  data[key] = value;
  writeJson(filePath, data);
  return true;
}

function buildEntry(name, icon, category, sortnum, pages, recipes) {
  const entry = {
    name,
    icon,
    category: `goety:goetymech_${category}`,
    sortnum,
    pages: [...pages],
  };
  if (recipes && recipes.length > 0) {
    const recipePages = recipes.map((r) => ({
      type: 'patchouli:crafting',
      recipe: r,
    }));
    entry.pages.push(...recipePages);
  }
  return entry;
}

function run(input) {
  const { slug, category, name_zh, name_en, icon, sortnum, pages_zh, pages_en, recipes } = input;

  if (!slug || !category || !name_zh || !name_en || !icon || !pages_zh || !pages_en) {
    console.error('Missing required fields: slug, category, name_zh, name_en, icon, pages_zh, pages_en');
    process.exit(1);
  }

  const base = `${ASSETS}/${BOOK}`;
  const enEntryPath = `${base}/en_us/entries/goetymech_${category}/${slug}.json`;
  const zhEntryPath = `${base}/zh_cn/entries/goetymech_${category}/${slug}.json`;
  const enLangPath = `src/main/resources/assets/goetymech/lang/en_us.json`;
  const zhLangPath = `src/main/resources/assets/goetymech/lang/zh_cn.json`;

  const enEntry = buildEntry(name_en, icon, category, sortnum, pages_en, recipes);
  const zhEntry = buildEntry(name_zh, icon, category, sortnum, pages_zh, recipes);

  writeJson(enEntryPath, enEntry);
  writeJson(zhEntryPath, zhEntry);

  const enKey = `goetymech.entry.${slug}`;
  const zhKey = `goetymech.entry.${slug}`;

  appendLang(enLangPath, enKey, name_en);
  appendLang(zhLangPath, zhKey, name_zh);

  console.log(`Created: ${enEntryPath}`);
  console.log(`Created: ${zhEntryPath}`);
  console.log(`Updated: ${enLangPath} (${enKey})`);
  console.log(`Updated: ${zhLangPath} (${zhKey})`);
}

const inputPath = process.argv[2];
if (!inputPath) {
  console.error('Usage: node scripts/generate-entry.mjs <input.json>');
  process.exit(1);
}
try {
  run(JSON.parse(fs.readFileSync(inputPath, 'utf8')));
} catch (err) {
  console.error('Error:', err.message);
  process.exit(1);
}
