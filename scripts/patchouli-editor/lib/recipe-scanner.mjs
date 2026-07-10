import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(__dirname, '..', '..', '..');
const RESOURCES = path.join(REPO_ROOT, 'src', 'main', 'resources');

export function scanRecipes() {
  const recipesDir = path.join(RESOURCES, 'data', 'goetymech', 'recipes');
  if (!fs.existsSync(recipesDir)) return [];

  const files = fs.readdirSync(recipesDir).filter(f => f.endsWith('.json'));
  return files.map(f => {
    const id = f.replace('.json', '');
    return { id: `goetymech:${id}`, name: id };
  });
}
