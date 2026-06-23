const BLOCKED_PATTERNS = [
  /[/\\]saves[/\\]/i,
  /[/\\]playerdata[/\\]/i,
  /[/\\]region[/\\]/i,
  /[/\\]poi[/\\]/i,
  /[/\\]entities[/\\]/i,
  /[/\\]level\.dat/i,
  /[/\\]level\.dat_old/i,
  /[/\\]DIM-?\d+[/\\]/i,
  /[/\\]session\.lock/i,
];

const DESTRUCTIVE_TOOLS = new Set(["bash", "write", "edit"]);

function matchesBlockedPath(value: string): boolean {
  const normalized = value.replace(/\\/g, "/");
  return BLOCKED_PATTERNS.some((p) => p.test(normalized));
}

function extractAllStrings(obj: unknown, depth = 0): string[] {
  if (depth > 10 || obj === null || obj === undefined) return [];
  const strings: string[] = [];
  if (typeof obj === "string") {
    strings.push(obj);
  } else if (Array.isArray(obj)) {
    for (const item of obj) {
      strings.push(...extractAllStrings(item, depth + 1));
    }
  } else if (typeof obj === "object") {
    for (const val of Object.values(obj as Record<string, unknown>)) {
      strings.push(...extractAllStrings(val, depth + 1));
    }
  }
  return strings;
}

export default async () => {
  return {
    "tool.execute.before": async (input: unknown, _output: unknown) => {
      const toolName =
        (input as Record<string, unknown>)?.name as string ||
        (input as Record<string, unknown>)?.tool as string ||
        "";
      if (!DESTRUCTIVE_TOOLS.has(toolName)) return;
      const allStrings = extractAllStrings(input);
      for (const s of allStrings) {
        if (matchesBlockedPath(s)) {
          throw new Error(
            `[SAVE-GUARD] BLOCKED: operation targeting save/world data was denied. ` +
              `Tool: ${toolName}, matched: "${s}"`,
          );
        }
      }
    },
  };
};
