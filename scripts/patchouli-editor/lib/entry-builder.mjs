export function buildEntry(name, icon, category, sortnum, pages, recipes, extra = {}) {
  const entry = {
    name,
    icon,
    category: `goetymech:${category}`,
    sortnum,
    pages: [...pages],
  };

  if (recipes && recipes.length > 0) {
    const recipePages = recipes.map(r => ({
      type: 'patchouli:crafting',
      recipe: r,
    }));
    entry.pages.push(...recipePages);
  }

  if (extra.secret) entry.secret = true;
  if (extra.advancement) entry.advancement = extra.advancement;
  if (extra.flag) entry.flag = extra.flag;
  if (extra.read_by_default) entry.read_by_default = true;
  if (extra.priority !== undefined) entry.priority = extra.priority;

  return entry;
}

export function toSlug(nameEn) {
  return nameEn
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_|_$/g, '');
}

export default { buildEntry, toSlug };
