import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import express from 'express';
import { scanRecipes } from './lib/recipe-scanner.mjs';
import { readEntry, writeEntry, listEntries, deleteEntry, appendLangKey } from './lib/lang-helper.mjs';
import { buildEntry, toSlug } from './lib/entry-builder.mjs';
import { translateZhToEn } from './lib/translator.mjs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const app = express();

app.use(express.json({ limit: '2mb' }));
app.use(express.static(path.join(__dirname, 'public')));

app.get('/api/recipes', (_req, res) => {
  res.json(scanRecipes());
});

app.get('/api/entries', (_req, res) => {
  res.json(listEntries());
});

app.get('/api/entries/:category/:slug', (req, res) => {
  const { category, slug } = req.params;
  const data = readEntry(category, slug);
  if (!data.en || Object.keys(data.en).length === 0) {
    return res.status(404).json({ error: 'Entry not found' });
  }

  const enPages = data.en.pages || [];
  const zhPages = data.zh.pages || [];
  const mergedPages = enPages.map((p, i) => ({
    type: p.type,
    ...p,
    text_zh: zhPages[i]?.text || '',
    text_en: p.text || '',
    text: p.text || '',
  }));

  res.json({
    category,
    slug,
    name_zh: data.zh.name || data.en.name,
    name_en: data.en.name,
    icon: data.en.icon || '',
    sortnum: data.en.sortnum ?? 0,
    priority: data.en.priority,
    secret: data.en.secret || false,
    advancement: data.en.advancement || '',
    flag: data.en.flag || '',
    read_by_default: data.en.read_by_default || false,
    pages: mergedPages,
    extra_recipe_mappings: data.en.extra_recipe_mappings,
  });
});

app.post('/api/save', (req, res) => {
  try {
    const {
      slug, category, name_zh, name_en, icon, sortnum,
      pages, priority, secret, advancement, flag, read_by_default,
    } = req.body;

    if (!slug || !category || !name_zh || !name_en || !icon) {
      return res.status(400).json({ error: 'Missing required fields: slug, category, name_zh, name_en, icon' });
    }

    const extra = { priority, secret, advancement, flag, read_by_default };

    const enPages = (pages || []).map(p => {
      const { text_zh, text_en, ...rest } = p;
      if (rest.type === 'patchouli:text') {
        return { ...rest, text: text_en || '' };
      }
      return { ...rest, text: text_en || rest.text || '' };
    });

    const zhPages = (pages || []).map(p => {
      const { text, text_zh, text_en, ...rest } = p;
      if (rest.type === 'patchouli:text') {
        return { ...rest, text: text_zh || '' };
      }
      return { ...rest, text: text_zh || rest.text || '' };
    });

    const enEntry = buildEntry(name_en, icon, category, sortnum, enPages, []);
    const zhEntry = buildEntry(name_zh, icon, category, sortnum, zhPages, []);

    if (secret) { enEntry.secret = true; zhEntry.secret = true; }
    if (advancement) { enEntry.advancement = advancement; zhEntry.advancement = advancement; }
    if (flag) { enEntry.flag = flag; zhEntry.flag = flag; }
    if (read_by_default) { enEntry.read_by_default = true; zhEntry.read_by_default = true; }

    writeEntry(category, slug, enEntry, zhEntry);

    const entryKey = `goetymech.entry.${slug}`;
    appendLangKey('en_us', entryKey, name_en);
    appendLangKey('zh_cn', entryKey, name_zh);

    res.json({ success: true, slug, category });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/translate', async (req, res) => {
  try {
    const { text } = req.body;
    if (!text) return res.status(400).json({ error: 'Missing text' });
    const result = await translateZhToEn(text);
    res.json({ result });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete('/api/entries/:category/:slug', (req, res) => {
  const { category, slug } = req.params;
  deleteEntry(category, slug);
  res.json({ success: true });
});

const PORT = 3000;
app.listen(PORT, () => {
  console.log(`Patchouli Editor running at http://localhost:${PORT}`);
});
