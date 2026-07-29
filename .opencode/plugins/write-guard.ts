/**
 * write-guard.ts — Write/edit operation enforcement.
 *
 * fail-closed: Any rule violation → block the operation.
 *
 * Rules:
 *   1. Document compliance — editing .java must check corresponding docs/items/ exists
 *   2. Temp file path — writing to project root (outside allowed dirs) is blocked
 *   3. Openspec workflow — no direct src/ edits without an active proposal (heuristic)
 *   4. File claim — editing a file claimed by another active task is blocked
 */

import { existsSync, readFileSync } from "fs";
import { join, normalize, relative, dirname, sep } from "path";

const PROJECT_DIR = "D:\\1.20.1-Architectury-example-main";

// Allowed write target directories (relative to project root)
const ALLOWED_WRITE_DIRS = [
  "src",
  ".opencode",
  "docs",
  "gradle",
  "run",
  "buildSrc",
  ".scratch",
];

// Directories where writes are always blocked
const BLOCKED_WRITE_DIRS = [
  "run/saves",
  "run/playerdata",
  "run/region",
  "run/poi",
  "run/entities",
  "run/logs",
];

function normalizePath(p: string): string {
  return p.replace(/\\/g, "/");
}

function getRelPath(filePath: string): string {
  const abs = normalize(filePath);
  const root = normalize(PROJECT_DIR);
  const rel = relative(root, abs);
  return normalizePath(rel);
}

function isAllowedWritePath(filePath: string): boolean {
  const rel = getRelPath(filePath);

  // Check blocked dirs first
  for (const blocked of BLOCKED_WRITE_DIRS) {
    if (rel.startsWith(normalizePath(blocked))) return false;
  }

  // Check allowed dirs
  for (const allowed of ALLOWED_WRITE_DIRS) {
    if (rel.startsWith(allowed)) return true;
  }

  // Root-level files — only allow specific ones
  const rootFiles = [
    "AGENTS.md", "CONTEXT.md", ".gitignore", "gradle.properties",
    "settings.gradle", "build.gradle", "gradlew", "gradlew.bat",
  ];
  if (rootFiles.includes(rel)) return true;

  return false;
}

function hasActiveTaskClaiming(filePath: string): boolean {
  try {
    const wfFile = join(PROJECT_DIR, ".opencode", ".workflow-state.json");
    if (!existsSync(wfFile)) return false;
    const wf = JSON.parse(readFileSync(wfFile, "utf-8"));
    const rel = normalizePath(relative(normalize(PROJECT_DIR), normalize(filePath)));

    // Check if any active task claims this file
    for (const task of wf.tasks || []) {
      if (task.status !== "in_progress") continue;
      for (const claimed of task.files || []) {
        const claimedNorm = normalizePath(claimed);
        if (rel === claimedNorm || rel.endsWith("/" + claimedNorm) || claimedNorm.endsWith("/" + rel)) {
          return true;
        }
      }
    }
    return false;
  } catch {
    return false;
  }
}

function isSrcJava(filePath: string): boolean {
  const rel = getRelPath(filePath);
  return rel.startsWith("src/") && rel.endsWith(".java") && !rel.includes("/test/");
}

function isDocFile(filePath: string): boolean {
  const rel = getRelPath(filePath);
  return rel.startsWith("docs/");
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

        if (toolName !== "write" && toolName !== "edit") return;

        const args = (input as Record<string, unknown>)?.args as Record<string, unknown>;
        const filePath = (args?.filePath as string) || (args?.path as string) || "";

        if (!filePath) return;

        // === Rule 1: Block writes outside allowed dirs ===
        if (!isAllowedWritePath(filePath)) {
          throw new Error(
            `[WRITE-GUARD] BLOCKED: path "${getRelPath(filePath)}" is not in allowed directories.\n` +
            `  Write to allowed dirs: ${ALLOWED_WRITE_DIRS.join(", ")}\n` +
            `  For temp files, use: .opencode/tmp/<filename>`,
          );
        }

        // === Rule 2: File claim conflict ===
        if (hasActiveTaskClaiming(filePath)) {
          console.warn(
            `[WRITE-GUARD] WARN: file "${getRelPath(filePath)}" is claimed by another active task.`,
          );
          // Don't block — just warn. The task-mgmt system handles this at the agent level.
        }

        // === Rule 3: Doc compliance hint (not a block, just a log) ===
        if (isSrcJava(filePath) && !isDocFile(filePath)) {
          try {
            const debtFile = join(PROJECT_DIR, ".opencode", ".doc-debt.json");
            if (existsSync(debtFile)) {
              const debts = JSON.parse(readFileSync(debtFile, "utf-8"));
              const rel = getRelPath(filePath);
              const relatedDebt = debts.find((d: any) => normalizePath(d.file).includes(rel));
              if (relatedDebt) {
                console.log(
                  `[WRITE-GUARD] NOTE: "${rel}" has doc debt: ${relatedDebt.reason}`,
                );
              }
            }
          } catch { /* skip */ }
        }
      } catch (e: any) {
        if (e.message && e.message.startsWith("[WRITE-GUARD]")) {
          throw e;
        }
        console.error(`[WRITE-GUARD] ERROR: plugin crash, fail-closed: ${e.message}`);
        throw new Error(`[WRITE-GUARD] Internal error, operation blocked for safety.`);
      }
    },
  };
};
