import * as fs from "fs";
import * as path from "path";

const PROJECT_DIR = "D:\\1.20.1-Architectury-example-main";
const SKILLS_DIR = path.join(PROJECT_DIR, ".opencode", "skills");

interface DomainDef {
  name: string;
  skills: string[];
  triggers: RegExp[];
}

const DOMAINS: Record<string, DomainDef> = {
  rendering: {
    name: "渲染域 rendering",
    skills: ["item-ister-flat-quads", "entity-render-dev", "patchouli"],
    triggers: [
      /纹理|渲染|ISTER|BEWLR|overlay|quad|UV|顶点|实体渲染|碰撞箱|模型|贴图|图标|texture|entity.*render|patchouli|指南书|条目/i,
    ],
  },
  goety: {
    name: "Goety 域",
    skills: ["goety-addon-dev", "goety-block-anim-and-particle", "goetymech-architecture"],
    triggers: [
      /聚晶|法术|Spell|Focus|仆从|soul|灵魂|方块动画|粒子|镜头|注册|架构|包路径|servant|animation|particle|camera/i,
    ],
  },
  create: {
    name: "Create 域",
    skills: ["create-dev"],
    triggers: [/Create|应力|旋转|传动|kinetic|BESR|Ponder|机器|stress|rotation|动力/i],
  },
  ui: {
    name: "UI/配方域",
    skills: ["jei-dev"],
    triggers: [/JEI|配方|ghost|拖拽|条目|recipe|category|menu|screen/i],
  },
  physics: {
    name: "物理域",
    skills: ["prototype-physics"],
    triggers: [/蓄力|跳跃|charge.*jump|跳跃高度|鞘翅|飞行|elytra|velocity|速度|步高|wall.run|墙跑|slam|落地/i],
  },
  process: {
    name: "流程域 process",
    skills: ["closeout-feature"],
    triggers: [
      /构思|方案|讨论|设计|修复|改代码|实施|归档|完结|记录|spec|plan|task|explore|grill|openspec|closeout/i,
    ],
  },
};

let skillSummaries: Record<string, string> = {};
let loaded = false;

function extractSummary(content: string, skillName: string): string {
  try {
    const lines = content.split("\n");
    let inFront = false;
    let parts: string[] = [];
    for (const line of lines) {
      if (line.trim() === "---") { inFront = !inFront; continue; }
      if (inFront) continue;
      if (line.startsWith("#")) continue;
      const t = line.trim();
      if (t) parts.push(t);
      if (parts.length >= 5) break;
    }
    return parts.join(" ") || skillName;
  } catch {
    return skillName;
  }
}

function loadAll(): void {
  if (loaded) return;
  for (const def of Object.values(DOMAINS)) {
    for (const name of def.skills) {
      const fp = path.join(SKILLS_DIR, name, "SKILL.md");
      if (fs.existsSync(fp)) {
        skillSummaries[name] = extractSummary(fs.readFileSync(fp, "utf-8"), name);
      }
    }
  }
  loaded = true;
}

let matchedDomains: string[] = [];

export default async () => {
  loadAll();
  return {
    "chat.message": async (_in: unknown, out: any) => {
      try {
        const msg = out?.message;
        const text =
          msg?.content?.text || msg?.text ||
          (typeof msg?.content === "string" ? msg.content : "") ||
          "";
        if (!text) return;
        matchedDomains = [];
        for (const [id, def] of Object.entries(DOMAINS)) {
          if (def.triggers.some((r) => r.test(text)) && !matchedDomains.includes(id)) {
            matchedDomains.push(id);
          }
        }
      } catch { /* skip */ }
    },

    "experimental.chat.system.transform": async (_in: unknown, out: any) => {
      try {
        if (!out?.system || !Array.isArray(out.system)) return;
        if (matchedDomains.length === 0) return;
        const lines: string[] = ["\n## 已加载技能域\n"];
        for (const id of matchedDomains) {
          const def = DOMAINS[id];
          if (!def) continue;
          lines.push(`### ${def.name}`);
          if (def.skills.length === 0) {
            lines.push("手动加载流程域技能（openspec / grill-me）。");
          } else {
            for (const s of def.skills) {
              lines.push(`- ${s}: ${skillSummaries[s] || ""}`);
            }
            lines.push("优先使用以上 skill。");
          }
        }
        out.system.push(lines.join("\n"));
      } catch { /* skip */ }
    },

    "experimental.session.compacting": async (_in: unknown, out: any) => {
      try {
        const lines: string[] = ["\n## 技能域（压缩恢复）\n"];
        for (const [id, def] of Object.entries(DOMAINS)) {
          const sn = def.skills.length > 0 ? def.skills.join(", ") : "手动加载";
          lines.push(`- ${id}: ${sn}`);
        }
        lines.push("- _catchall: 无匹配域时走常规工作流");
        if (!out.context) out.context = [];
        out.context.push(lines.join("\n"));
      } catch { /* skip */ }
    },
  };
};
