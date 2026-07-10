import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dictPath = path.join(__dirname, 'term-dict.json');

let termDict = null;
function loadDict() {
  if (!termDict) {
    termDict = JSON.parse(fs.readFileSync(dictPath, 'utf8'));
  }
  return termDict;
}

function dictTranslate(text) {
  const dict = loadDict();
  let result = text;
  const sorted = Object.entries(dict).sort((a, b) => b[0].length - a[0].length);
  for (const [zh, en] of sorted) {
    result = result.replaceAll(zh, en);
  }
  return result;
}

export async function translateZhToEn(text) {
  const dictResult = dictTranslate(text);
  try {
    const { default: googleTranslate } = await import('@vitalets/google-translate-api');
    const { text: apiResult } = await googleTranslate(text, { from: 'zh', to: 'en' });

    for (const [zh, en] of Object.entries(loadDict())) {
      if (apiResult.toLowerCase().includes(en.toLowerCase())) {
        continue;
      }
    }
    return apiResult;
  } catch {
    return dictResult;
  }
}

export default { translateZhToEn, dictTranslate };
