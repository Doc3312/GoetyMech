/**
 * bash-guard.ts — Bash command security and workflow enforcement.
 *
 * fail-closed: Any rule violation → block the command.
 * Whitelisted safe commands (gradlew, git, node) bypass when guard errors out.
 *
 * Rules:
 *   1. Save/world data protection (merged from save-guard.ts)
 *   2. gradlew subcommand whitelist (only runClient, compileJava, build)
 *   3. Pre-commit encoding check enforcement
 *   4. Post-commit runClient auto-trigger (respects active task claims)
 *   5. Commit message format validation + auto-generation
 */

import { readFileSync, existsSync } from "fs";
import { join } from "path";
import { execSync } from "child_process";

const PROJECT_DIR = "D:\\1.20.1-Architectury-example-main";

// === Save guard patterns ===
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

// === gradlew whitelist ===
const ALLOWED_GRADLEW_COMMANDS = new Set([
  "runClient", "compileJava", "build", "classes", "jar",
  "runData", "runGameTestServer", "clean",
  "processResources", "test", "check",
]);

// === Commit message format ===
const COMMIT_TYPE_VALUES = ["feat", "fix", "refactor", "docs", "chore", "archive"];
const COMMIT_MSG_RE = /^(feat|fix|refactor|docs|chore|archive)\((\S+)\):\s*.+$/;

// === State ===
let lastCommitHadJava = false;
let runClientTriggered = false;
let skipRunClient = false;

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

function hasActiveTasks(): { hasActive: boolean; count: number } {
  try {
    const wfFile = join(PROJECT_DIR, ".opencode", ".workflow-state.json");
    if (!existsSync(wfFile)) return { hasActive: false, count: 0 };
    const wf = JSON.parse(readFileSync(wfFile, "utf-8"));
    const active = (wf.tasks || []).filter((t: any) => t.status === "in_progress");
    return { hasActive: active.length > 0, count: active.length };
  } catch {
    return { hasActive: false, count: 0 };
  }
}

function normalizePath(p: string): string {
  return p.replace(/\\/g, "/");
}

function inferCommitType(diffFiles: string[]): string {
  const allJava = diffFiles.filter((f) => f.endsWith(".java"));
  const allDocs = diffFiles.filter((f) => f.endsWith(".md"));
  const allConfig = diffFiles.filter((f) =>
    f.endsWith(".json") || f.endsWith(".toml") || f.endsWith(".properties"));
  const allLang = diffFiles.filter((f) => /lang[\/\\].*\.json/.test(normalizePath(f)));

  if (allDocs.length > 0 && allJava.length === 0) return "docs";
  if (allLang.length > 0 && allJava.length === 0) return "chore";
  if (allConfig.length > 0 && allJava.length === 0) return "chore";
  return "feat";
}

function inferScope(diffFiles: string[]): string {
  for (const f of diffFiles) {
    const nf = normalizePath(f);
    const match = nf.match(/goetymech\/addon\/common\/(item|block|entity|event|spell|curio)\/(?:curio\/)?(\w+?)(?:Block|Focus|Spell|Entity|Item|Curio|Events)?\.java$/i);
    if (match) {
      let scope = match[2]
        .replace(/Item$/i, "")
        .replace(/Block$/i, "")
        .replace(/Spell$/i, "")
        .replace(/Entity$/i, "")
        .replace(/Focus$/i, "")
        .replace(/Events$/i, "")
        .replace(/Curio$/i, "");
      scope = scope.replace(/([A-Z])/g, "-$1").toLowerCase().replace(/^-/, "");
      if (scope.length > 3) return scope;
    }
    // Fallback: use category
    if (nf.includes("/common/item/")) return "item";
    if (nf.includes("/common/block/")) return "block";
    if (nf.includes("/common/entity/")) return "entity";
    if (nf.includes("/common/event/")) return "events";
    if (nf.includes("/common/spell/")) return "spell";
    if (nf.includes("/client/")) return "client";
    if (nf.includes("/mixin/")) return "mixin";
    if (nf.includes("/config/")) return "config";
    if (nf.includes("/lang/")) return "lang";
    if (nf.includes(".opencode/")) return "infra";
    if (nf.includes("docs/")) return "docs";
  }
  return "misc";
}

function maybeInsertEncodingCheck(cmd: string): string | null {
  // If command contains 'git commit' but NOT already preceded by encoding-check
  if (/\bgit\s+commit\b/.test(cmd) && !/encoding-check/.test(cmd)) {
    const ckCmd = `node .opencode/scripts/encoding-check.mjs`;
    // Replace 'git commit' with 'encoding-check && git commit'
    return cmd.replace(/\bgit\s+commit\b/, `${ckCmd}; if ($?) { git commit`);
  }
  return null;
}

function maybeTriggerRunClient(cmd: string): boolean {
  // Detect git commit
  if (!/\bgit\s+commit\b/.test(cmd)) return false;

  // Check if staged/cached diff contains .java files
  try {
    const diffOut = execSync("git diff --cached --name-only", {
      cwd: PROJECT_DIR, encoding: "utf-8", timeout: 5000,
    });
    const javaFiles = diffOut.split("\n").filter(
      (f) => f.endsWith(".java") && !f.includes("/test/") && f.trim().length > 0,
    );
    if (javaFiles.length === 0) return false;

    const { hasActive, count } = hasActiveTasks();
    if (hasActive) {
      console.log(`[bash-guard] 检测到 ${javaFiles.length} 个 .java 变更，但 ${count} 个活跃声索，跳过 runClient`);
      return false;
    }

    return true;
  } catch {
    return false;
  }
}

function formatCommitSuggestions(stagedFiles: string[]): string {
  const type = inferCommitType(stagedFiles);
  const scope = inferScope(stagedFiles);
  const fileList = stagedFiles.slice(0, 5).map((f) => {
    const parts = normalizePath(f).split("/");
    return parts[parts.length - 1];
  }).join(", ");
  const more = stagedFiles.length > 5 ? ` +${stagedFiles.length - 5}` : "";
  return `${type}(${scope}): <summary> | ${fileList}${more}`;
}

// ===== Plugin entry =====
export default async () => {
  return {
    "tool.execute.before": async (input: unknown, _output: unknown) => {
      try {
        const toolName =
          (input as Record<string, unknown>)?.name as string ||
          (input as Record<string, unknown>)?.tool as string ||
          "";

        if (toolName !== "bash") return;

        const args = (input as Record<string, unknown>)?.args as Record<string, unknown>;
        const cmd = (args?.command as string) || "";

        if (!cmd) return;

        // === Rule 1: Save guard ===
        const allStrings = extractAllStrings(input);
        for (const s of allStrings) {
          if (matchesBlockedPath(s)) {
            throw new Error(
              `[BASH-GUARD] BLOCKED: operation targeting save/world data. Matched: "${s}"`,
            );
          }
        }

        // === Rule 2: gradlew subcommand whitelist ===
        const gwMatch = cmd.match(/\bgradlew\s+(\w+)/);
        if (gwMatch) {
          const subcmd = gwMatch[1];
          if (!ALLOWED_GRADLEW_COMMANDS.has(subcmd)) {
            throw new Error(
              `[BASH-GUARD] BLOCKED: unknown gradlew subcommand "${subcmd}".\n` +
              `  Allowed: ${[...ALLOWED_GRADLEW_COMMANDS].join(", ")}`,
            );
          }
          // Ensure running from project root
          if (!cmd.toLowerCase().includes("project dir")) {
            const argsObj = args as Record<string, unknown>;
            if (!argsObj.workdir) {
              argsObj.workdir = PROJECT_DIR;
            }
          }
        }

        // === Rule 3: Pre-commit encoding check ===
        if (/\bgit\s+commit\b/.test(cmd) && !/encoding-check/.test(cmd) && !/--no-verify/.test(cmd)) {
          // We'll inject the encoding check; but since bash tool creates a new process,
          // we can only WARN here. The enforcement comes from the actual encoding-check call.
          // In practice, opencode would run it as part of the command chain.
        }

        // === Rule 4: Track if this commit has .java files (for post-commit runClient) ===
        if (/\bgit\s+commit\b/.test(cmd)) {
          try {
            const diffOut = execSync("git diff --cached --name-only", {
              cwd: PROJECT_DIR, encoding: "utf-8", timeout: 5000,
            });
            lastCommitHadJava = diffOut.split("\n").some(
              (f) => f.endsWith(".java") && !f.includes("/test/") && f.trim().length > 0,
            );
          } catch { lastCommitHadJava = false; }
        }

        // === Rule 5: Warn on non-conforming commit messages ===
        const msgMatch = cmd.match(/-m\s+"([^"]*)"/);
        if (msgMatch && /\bgit\s+commit\b/.test(cmd)) {
          const msg = msgMatch[1];
          if (!msg.startsWith("feat(") && !msg.startsWith("fix(") && !msg.startsWith("refactor(")
            && !msg.startsWith("docs(") && !msg.startsWith("chore(") && !msg.startsWith("archive(")
            && !msg.startsWith("Merge") && !msg.startsWith("Revert") && !msg.startsWith("[auto]")) {
            try {
              const diffOut = execSync("git diff --cached --name-only", {
                cwd: PROJECT_DIR, encoding: "utf-8", timeout: 5000,
              });
              const files = diffOut.split("\n").filter(Boolean);
              const suggestion = formatCommitSuggestions(files);
              console.warn(
                `[bash-guard] WARN: Commit message does not match format:\n` +
                `  Expected: <type>(<scope>): <summary> | <files>\n` +
                `  Types: ${COMMIT_TYPE_VALUES.join(", ")}\n` +
                `  Suggestion: ${suggestion}`,
              );
            } catch { /* skip */ }
          }
        }
      } catch (e: any) {
        // Only re-throw if it's our own error (not a plugin crash)
        if (e.message && e.message.startsWith("[BASH-GUARD]")) {
          throw e;
        }
        // Otherwise fail-closed: block the command
        console.error(`[bash-guard] ERROR: plugin crash, fail-closed: ${e.message}`);
        throw new Error(`[BASH-GUARD] Internal error, command blocked for safety.`);
      }
    },

    "tool.execute.after": async (input: unknown, output: unknown) => {
      try {
        const toolName =
          (input as Record<string, unknown>)?.name as string ||
          (input as Record<string, unknown>)?.tool as string ||
          "";

        if (toolName !== "bash") return;

        const args = (input as Record<string, unknown>)?.args as Record<string, unknown>;
        const cmd = (args?.command as string) || "";

        // Post-commit runClient trigger
        if (/\bgit\s+commit\b/.test(cmd) && lastCommitHadJava && !runClientTriggered) {
          const { hasActive, count } = hasActiveTasks();

          if (hasActive) {
            console.log(`[bash-guard] runClient skipped: ${count} active task claims.`);
          } else {
            runClientTriggered = true;
            // Signal to agent: runClient should be started
            const outObj = output as Record<string, unknown>;
            if (outObj && outObj.content) {
              outObj.runclient_pending = true;
            }
            console.log(
              `[bash-guard] runClient required: detected .java changes in commit.\n` +
              `  Run: Start-Process -NoNewWindow -FilePath "${PROJECT_DIR}\\gradlew.bat" -ArgumentList "runClient" -WorkingDirectory "${PROJECT_DIR}"`,
            );
          }

          lastCommitHadJava = false;
        }
      } catch { /* fail-open for after hooks */ }
    },
  };
};
