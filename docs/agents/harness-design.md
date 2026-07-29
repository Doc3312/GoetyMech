# Harness 插件设计

## 架构概览

3 plugins enforce AGENTS.md rules at tool-execution level:

| Plugin | Hook | Strategy | Rules |
|--------|------|----------|-------|
| bash-guard.ts | tool.execute.before/after | fail-closed | Save guard, gradlew whitelist, commit encoding check, commit msg format, runClient auto-trigger |
| write-guard.ts | tool.execute.before | fail-closed | Write path check, file claim conflict detection, doc debt hints |
| skill-preloader.ts | chat.message, system.transform, session.compacting | fail-open | Domain skill hints, health-check snapshot injection |

## Fail Strategy

- bash-guard / write-guard: fail-closed — internal error blocks the tool execution to prevent damage
- skill-preloader: fail-open — only provides context hints, failure just means less guidance

## runClient Logic

```
commit contains .java files
  → read .workflow-state.json
  → has "in_progress" tasks? → skip, log "N active claims"
  → no active tasks → Start-Process -NoNewWindow gradlew runClient
  → returns immediately, no Wait-Process
```

## Commit Message Format

AI-editor tag (// @commit-type: fix) overrides plugin inference.
Without tag: plugin infers type from file changes:
- New src/ files → feat
- Only docs/ → docs
- Only lang/config → chore
