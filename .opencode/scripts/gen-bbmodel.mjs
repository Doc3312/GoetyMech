import { randomUUID } from 'crypto';
import { writeFileSync } from 'fs';
import { join } from 'path';

const __dirname = process.cwd();
const outPath = join(__dirname, 'src', 'main', 'resources', 'assets', 'goetymech', 'crazed_illager.bbmodel');

const F = {}; // name → UUID
function u(name) { if (!F[name]) F[name] = randomUUID(); return F[name]; }

// MC coord (Y-down) → BB coord (Y-up)
function mcToBB(from, to) {
  return { from: [from[0], -to[1], from[2]], to: [to[0], -from[1], to[2]] };
}

function cube(name, ox, oy, oz, cx, cy, cz, sx, sy, sz, uv, inflate) {
  const mcFrom = [ox + cx, oy + cy, oz + cz];
  const mcTo   = [ox + cx + sx, oy + cy + sy, oz + cz + sz];
  const bb = mcToBB(mcFrom, mcTo);
  const el = {
    uuid: u(name),
    name,
    type: 'cube',
    from: bb.from.map(v => Math.round(v * 1000) / 1000),
    to: bb.to.map(v => Math.round(v * 1000) / 1000),
    origin: [ox, -oy, oz].map(v => Math.round(v * 1000) / 1000),
    rotation: [0, 0, 0],
    box_uv: true,
    uv_offset: uv,
  };
  if (inflate) el.inflate = inflate;
  return el;
}

const els = [];
const bodyChildren = [];
const headChildren = [];

// Body parts
const parts = [
  { name: 'body',      off: [0, 0, 0],    cube: [-4, 0, -2, 8, 12, 4],  uv: [16, 16] },
  { name: 'head',      off: [0, 0, 0],    cube: [-4, -8, -4, 8, 8, 8],  uv: [0, 0] },
  { name: 'hat',       off: [0, 0, 0],    cube: [-4, -8, -4, 8, 8, 8], uv: [32, 0], inflate: 0.5 },
  { name: 'left_arm',  off: [5, 2, 0],    cube: [-2, -2, -2, 4, 12, 4], uv: [44, 22] },
  { name: 'right_arm', off: [-5, 2, 0],   cube: [-2, -2, -2, 4, 12, 4], uv: [44, 22], mirror: true },
  { name: 'left_leg',  off: [2, 12, 0],   cube: [-2, 0, -2, 4, 12, 4],  uv: [0, 16] },
  { name: 'right_leg', off: [-2, 12, 0],  cube: [-2, 0, -2, 4, 12, 4],  uv: [0, 16], mirror: true },
];

for (const p of parts) {
  const el = cube(p.name, ...p.off, ...p.cube, p.uv, p.inflate);
  if (p.mirror) el.mirror = true;
  els.push(el);

  if (p.name === 'body') bodyChildren.push(u(p.name));
  else if (['head', 'hat'].includes(p.name)) headChildren.push(u(p.name));
  else bodyChildren.push(u(p.name));
}

// --- Saw blade ---
const sbOriginMC = [0, -4, -4];
const sbOriginBB = [0, 4, -4];

// blade_center
const bc = cube('blade_center', ...sbOriginMC, -0.5, -4, -4, 1, 8, 8, [48, 32]);
els.push(bc);

const sawChildren = [u('blade_center')];

// 8 teeth
const toothR = 4.5;
for (let i = 0; i < 8; i++) {
  const angle = i * 45 * Math.PI / 180;
  const ty = Math.cos(angle) * toothR;
  const tz = Math.sin(angle) * toothR;
  const tOffMC = [sbOriginMC[0], sbOriginMC[1] + ty, sbOriginMC[2] + tz];
  const t = cube(`tooth_${i}`, ...tOffMC, -0.5, -1.5, -1.5, 1, 3, 3, [48, 41]);
  els.push(t);
  sawChildren.push(u(`tooth_${i}`));
}

const doc = {
  meta: {
    format_version: '4.10',
    model_format: 'Java Block/Entity',
    box_uv: true,
  },
  name: 'crazed_illager',
  model_identifier: 'goetymech:crazed_illager',
  visible_box: [-8, -24, -8, 16, 32, 16],
  elements: els,
  outliner: [{
    type: 'group',
    uuid: u('root'),
    name: 'root',
    origin: [0, 0, 0],
    rotation: [0, 0, 0],
    children: [
      ...bodyChildren,
      {
        type: 'group',
        uuid: u('head_grp'),
        name: 'head',
        origin: [0, 0, 0],
        rotation: [0, 0, 0],
        children: [
          ...headChildren,
          {
            type: 'group',
            uuid: u('saw_blade'),
            name: 'saw_blade',
            origin: sbOriginBB,
            rotation: [0, 0, 0],
            children: sawChildren,
          },
        ],
      },
    ],
  }],
  textures: [{
    name: 'crazed_illager.png',
    id: randomUUID(),
    source: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAYdEVYdFNvZnR3YXJlAHBhaW50Lm5ldCA0LjEuNvEZ80IAAAHISURBVGhD7ZoxbsMwDEXdC/T+BfQMPUJHjR4yduzYMWMnT548ZerUqWjXH4YpUqIkShIlJf0BDxYCy5L4aT6mtVqW5X6/3+8IAAAA/vwKYJom13Vd1w0AAADAL4B9313XdQcAAADwC2BZVtd1HQAAAMAvgH3fXdd1BwAAAPALYFkW13UdAAAAwC+AZVlc1x0AAADAL4B5nl3XdQcAAADwC2CeZ9d1HQAAAMAvgHmeXdd1AAA',
    width: 64,
    height: 64,
  }],
};

writeFileSync(outPath, JSON.stringify(doc, null, 2), 'utf-8');
console.log('bbmodel generated:', outPath);
