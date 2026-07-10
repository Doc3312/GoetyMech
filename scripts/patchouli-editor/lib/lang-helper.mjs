import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(__dirname, '..', '..', '..');
const ASSETS = path.join(REPO_ROOT, 'src', 'main', 'resources', 'assets', 'goetymech');
const GOETY_ASSETS = path.join(REPO_ROOT, 'src', 'main', 'resources', 'assets', 'goety');

function readJson(filePath) {
  try {
    const txt = fs.readFileSync(filePath, 'utf8');
    const clean = txt.charCodeAt(0) === 0xFEFF ? txt.slice(1) : txt;
    return JSON.parse(clean);
  } catch {
    return {};
  }
}

function writeJson(filePath, data) {
  const dir = path.dirname(filePath);
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2) + '\n', 'utf8');
}

export function readLang(lang) {
  const langPath = path.join(ASSETS, 'lang', `${lang}.json`);
  return readJson(langPath);
}

export function writeLang(lang, data) {
  const langPath = path.join(ASSETS, 'lang', `${lang}.json`);
  writeJson(langPath, data);
}

export function appendLangKey(lang, key, value) {
  const data = readLang(lang);
  if (data[key] !== value) {
    data[key] = value;
    writeLang(lang, data);
    return true;
  }
  return false;
}

export function readEntry(category, slug) {
  const enPath = path.join(GOETY_ASSETS, 'patchouli_books', 'black_book', 'en_us', 'entries', category, `${slug}.json`);
  const zhPath = path.join(GOETY_ASSETS, 'patchouli_books', 'black_book', 'zh_cn', 'entries', category, `${slug}.json`);
  return {
    en: readJson(enPath),
    zh: readJson(zhPath),
  };
}

export function writeEntry(category, slug, enEntry, zhEntry) {
  const enPath = path.join(GOETY_ASSETS, 'patchouli_books', 'black_book', 'en_us', 'entries', category, `${slug}.json`);
  const zhPath = path.join(GOETY_ASSETS, 'patchouli_books', 'black_book', 'zh_cn', 'entries', category, `${slug}.json`);
  writeJson(enPath, enEntry);
  writeJson(zhPath, zhEntry);
}

export function listEntries() {
  const baseDir = path.join(GOETY_ASSETS, 'patchouli_books', 'black_book', 'en_us', 'entries');
  if (!fs.existsSync(baseDir)) return [];

  const entries = [];
  const cats = fs.readdirSync(baseDir, { withFileTypes: true }).filter(d => d.isDirectory());
  for (const cat of cats) {
    const catPath = path.join(baseDir, cat.name);
    const files = fs.readdirSync(catPath).filter(f => f.endsWith('.json'));
    for (const f of files) {
      const slug = f.replace('.json', '');
      const enData = readJson(path.join(catPath, f));
      entries.push({
        category: cat.name,
        slug,
        name_zh: enData.name || slug,
        name_en: enData.name || slug,
      });
    }
  }
  return entries;
}

export function deleteEntry(category, slug) {
  const enPath = path.join(GOETY_ASSETS, 'patchouli_books', 'black_book', 'en_us', 'entries', category, `${slug}.json`);
  const zhPath = path.join(GOETY_ASSETS, 'patchouli_books', 'black_book', 'zh_cn', 'entries', category, `${slug}.json`);
  if (fs.existsSync(enPath)) fs.unlinkSync(enPath);
  if (fs.existsSync(zhPath)) fs.unlinkSync(zhPath);
}
