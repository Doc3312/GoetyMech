import fs from 'fs';
const path = 'D:/1.20.1-Architectury-example-main/run/config/goety/goety-spells.toml';
let content = fs.readFileSync(path, 'utf8');
const lines = content.split('\n');
const seen = new Set();
const out = [];
let inDup = false;
let dupCount = 0;
for (let i = 0; i < lines.length; i++) {
    const m = lines[i].match(/^\t\[Spells\.\"(.+?)\"\]/);
    if (m) {
        if (seen.has(m[1])) {
            inDup = true;
            dupCount++;
            console.log('REMOVED duplicate [' + m[1] + '] at line ' + (i + 1));
            continue;
        }
        seen.add(m[1]);
        inDup = false;
    } else if (inDup) {
        continue;
    }
    out.push(lines[i]);
}
fs.writeFileSync(path, out.join('\n'), 'utf8');
console.log('Done. Wrote ' + out.length + ' lines (was ' + lines.length + '). Removed ' + dupCount + ' duplicate sections.');
