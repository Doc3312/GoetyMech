## Agent skills

### Issue tracker

Local markdown 鈥?issues 瀛樻斁鍦?`.scratch/<feature-slug>/` 涓嬨€傚弬瑙?`docs/agents/issue-tracker.md`銆?
### Triage labels

榛樿 label 鍚嶇О銆傚弬瑙?`docs/agents/triage-labels.md`銆?
### Domain docs

Single-context repo銆傚弬瑙?`docs/agents/domain.md`銆?
### 鍙敤鎶€鑳?
| skill | 瑙﹀彂鍏抽敭璇?|
|-------|-----------|
| **item-ister-flat-quads** | **鈽呮渶楂樹紭鍏堢骇鈽?texture / png / 绾圭悊 / overlay / 鍙犲姞灞?/ quad / 鍥涜竟闈?/ ISTER / BEWLR / item/generated / flat item / 鐗╁搧娓叉煋 / 鍥炬爣 / scale(1,-1,-1) / UV mapping / vertex / 椤剁偣娓叉煋** |
| create-dev | Create machines / Ponder / kinetic BE / BESR |
| entity-render-dev | entity render / collision / invisible |
| goety-addon-dev | Goety Focus / Spell / servant / soul / 鑱氭櫠 / 娉曟湳 / 浠嗕粠 / 鐏甸瓊 |
| goety-block-anim-and-particle | block anim / particle / camera shake |
| GoetyMech-architecture | package / convention / register |
| jei-dev | JEI / recipe display / ghost drag |
| patchouli-dev | Patchouli / guide book / page |
| prototype-charge-jump | charge jump / jump height |
| prototype-flight-mixin | elytra flight / Mixin / velocity |
| openspec-propose | explore / design / spec |
| openspec-apply-change | implement / build / fix task |
| grill-me | discussion / plan / design |
| grill-with-docs | new term / concept / ADR |

## 鏍稿績宸ヤ綔娴侊紙楂樹紭鍏堢骇锛屾瘡鏉¤鍒欏潎涓?IF 鈫?THEN 鏍煎紡锛?
### 鏂规鍏堣

IF 鐢ㄦ埛璇淬€屽垎鏋?/ 瀹℃煡 / 璁ㄨ / 鏂规 / 鎵炬柟妗堛€?THEN 鍙緭鍑烘柟妗堜笉鍐欎唬鐮併€?
IF 鐢ㄦ埛璇淬€屼慨澶?/ 鏀逛唬鐮?/ 瀹炴柦 / 寮€濮嬪仛 / 鍔ㄦ墜 / 鎵ц / 鍐欎唬鐮併€?THEN 姝よ鍒欒В闄わ紝杩涘叆瀹炵幇闃舵銆?
### Openspec 娴佺▼寮哄埗

IF 宸茶繘鍏ュ疄鐜伴樁娈?THEN 璋冪敤 openspec-apply-change skill 璧板畬鏁?openspec 宸ヤ綔娴侊紝涓嶅緱鐩存帴缂栬緫鏂囦欢銆?
渚嬪锛? 琛屼互鍐呯殑 trivial 淇鍙互鐩存帴鏀广€?
### Skill 浼樺厛浜庢簮鐮?
IF 璁ㄨ/寮€鍙戞秹鍙婂彲鐢ㄦ妧鑳借〃涓换涓€ skill 瑕嗙洊鐨勯鍩燂紙濡傝仛鏅?娉曟湳銆佹柟鍧楀姩鐢汇€丣EI 閰嶆柟绛夛級 THEN 蹇呴』鍏堣皟鐢?`skill` tool 鍔犺浇瀵瑰簲 skill锛屽啀浠?skill 鏂囨。涓‘璁ょ害瀹氬拰妯″紡锛屾渶鍚庢墠璇绘簮鐮侀獙璇併€?
绂佹璺宠繃 skill 鐩存帴璇绘簮鐮併€俿kill 鏂囨。鏄害瀹氬眰锛屾簮鐮佹槸楠岃瘉灞傦紝涓よ€呴兘瑕佷絾涓嶅厑璁歌烦杩囩害瀹氬眰銆?
### 杩愯楠岃瘉锛堟瘡娆℃敼瀹屼唬鐮佽嚜鍔?runClient锛?
IF auto-commit 鍖呭惈 `src/` 涓嬬殑鐢熶骇浠ｇ爜鍙樻洿锛坄.java` 鏂囦欢锛?THEN 鑷姩鎵ц runClient 楠岃瘉锛?
1. `Start-Process -NoNewWindow gradlew runClient`
2. `Wait-Process -Timeout 240` 绛?240 绉?3. 濡傛灉杩涚▼閫€鍑?鈫?绔嬪嵆璇?`run/logs/latest.log`
4. 濡傛灉 240 绉掕秴鏃?鈫?`Stop-Process -Force` 鏉€杩涚▼ 鈫?璇绘棩蹇?5. 鎼滅储 `ERROR / CRASH / Exception / Caused by / Mixin`
6. 鏃犻敊璇?鈫?鍛婅瘔鐢ㄦ埛"缂栬瘧鍚姩閫氳繃锛屽彲浠ヨ繘娓告垙娴嬭瘯浜?
7. 鏈夐敊璇?鈫?鎸変互涓嬫祦绋嬪鐞嗭細
   a. **crash 鏃ュ織鑷姩蹇収**锛歚Copy-Item run/logs/latest.log "run/logs/crash-snapshot/$(Get-Date -Format 'yyyyMMdd-HHmmss')-$(Select-String -Pattern 'Caused by|at (com\.GoetyMech|net\.minecraft)' run/logs/latest.log | Select-Object -First 1 | ForEach-Object { $_ -replace '.*\.(\w+)\..*', '$1' }).log"`
   b. **璋冪敤 diagnose skill 鍒嗘瀽鏍瑰洜**锛堝繀椤伙紝涓嶅緱璺宠繃锛?   c. **绂佹鍦ㄥ垎鏋愬嚭鏍瑰洜鍓嶅垹闄?*浠讳綍鏂囦欢锛堟棩蹇?/ 瀛樻。 / 閰嶇疆锛?   d. 璐村嚭鎶ラ敊鎽樿 + 鏍瑰洜鍒嗘瀽 + 淇寤鸿鎴栨棤娉曚慨澶嶇殑鍘熷洜

涓嶈Е鍙?runClient 鐨勬帓闄ら」锛氫粎淇敼 `docs/`銆乣AGENTS.md`銆乣*.json`銆乣*.yaml`銆乣*.yml`銆乣*.toml` 绛夐潪浠ｇ爜鏂囦欢銆?
### 鍗虫椂閿欒璁板綍

IF 璋冭瘯瓒呰繃 **15 鍒嗛挓**鏈畾浣嶃€佷笖鏍瑰洜闈?trivial THEN 鍘嬬缉涓婁笅鏂囨垨鍏抽棴瀵硅瘽鍓嶅啓鍏?`docs/incidents/<date>-<slug>.md`銆?
- 銆?5 鍒嗛挓銆? 璺ㄥ璇濈疮璁＄函璋冭瘯鏃堕棿
- 鍚屼竴鏍瑰洜鐨勫娆″皾璇?= 涓€涓敊璇紝涓嶉噸澶嶅啓
- 璁板綍澶撮儴鍚?`status: verified | disputed | superseded`

### 鐗╁搧鏂囨。鑷姩璁板綍

IF 浠讳綍浠ｇ爜淇敼娑夊強宸叉敞鍐岀墿鍝?鏂瑰潡/瀹炰綋 THEN 鑷姩鍦?`docs/items/<category>/<item-slug>.md` 杩戒竴鏉?timeline 璁板綍锛堟牸寮忥細鏀瑰姩 / 鍘熷洜 / 鍏宠仈锛夈€?
瑙﹀彂鏉′欢涓嶅彧鏄?openspec task 瀹屾垚锛岃繕鍖呮嫭锛?- 鐩存帴缂栬緫浜嗙墿鍝?鏂瑰潡/瀹炰綋鐨?Java 婧愭枃浠?- 淇敼浜嗘敞鍐岀被锛圡odItems/ModBlocks/ModEntities锛?- 鏂板缓浜嗙墿鍝?鏂瑰潡/瀹炰綋鐨?Java 绫绘枃浠?- openspec-apply-change 鏍囪 task 瀹屾垚

### 鍘嬬缉鍓嶆枃妗ｆ鏌ワ紙涓婁笅鏂囧嵆灏嗗帇缂╂椂寮哄埗鎵ц锛?
IF 闇€瑕佸帇缂╀笂涓嬫枃 / 鍏抽棴瀵硅瘽 / 鍒囨崲璇濋 THEN AI 蹇呴』鑷姩鎵ц浠ヤ笅妫€鏌ユ竻鍗曪細

1. 璇诲彇 `.opencode/.session-changes.json`锛屾鏌ユ墍鏈?`docWritten: false` 鐨勭墿鍝佸彉鏇磋褰曘€傚搴旂殑 `docs/items/` 鏂囦欢蹇呴』琛ュ啓 timeline 鍚庢爣璁?`docWritten: true`銆?2. 妫€鏌ユ槸鍚︽湁鏈褰曠殑璋冭瘯缁忛獙銆傜疮璁?鈮?5 鍒嗛挓鐨勫啓鍏?`docs/incidents/`銆?3. 妫€鏌ユ槸鍚︽湁鏋舵瀯/娴佺▼鐩稿叧鏁欒锛堟瘮濡傝繖娆?runClient 瑙勫垯鐨勮璁″け璇級锛屽啓鍏?`docs/incidents/`銆?4. 鍏ㄩ儴瀹屾垚鍚庢墠鑳芥墽琛屽帇缂┿€?
### 澶氬璇濆崗鍚岀害鏉?
IF 寮€濮嬩竴涓柊浠诲姟锛堜慨鏀逛唬鐮侊級 THEN 蹇呴』鍏堝０绱㈡枃浠讹細
1. `node .opencode/scripts/task-mgmt.mjs start --desc "<浠诲姟鎻忚堪>" --files "<鏂囦欢1,鏂囦欢2>"`
2. 鏂囦欢璺緞鐩稿浜庨」鐩牴鐩綍锛岀敤閫楀彿鍒嗛殧

IF 缂栬瘧鎶ラ敊 THEN锛?1. 璇诲彇 `run/logs/latest.log`锛屾彁鍙栨爤杩借釜涓殑鏂囦欢鍚?2. 瀵圭収鑷繁鐨勫０绱㈡枃浠讹細閿欒娑夊強鍏朵腑浠讳綍涓€涓紵
   - **鏄?* 鈫?淇鑷繁鐨勪唬鐮?   - **鍚?* 鈫?鉀?绔嬪嵆鍋滄锛屼笉鍔ㄤ换浣曚唬鐮侊紝鎶ュ憡鐢ㄦ埛锛歚缂栬瘧閿欒涓嶅湪澹扮储鏂囦欢鑼冨洿鍐咃紝鍙兘鏄叾浠栧璇濈殑浠诲姟鏈畬鎴恅

IF 澹扮储鏂囦欢涓嶅湪鍑洪敊鏍堜腑 THEN 绂佹鎵ц浠讳綍 `edit` / `write` 鎿嶄綔锛堝伐鍏蜂笉鑳芥敼浠ｇ爜锛夈€?
IF 浠诲姟瀹屾垚 THEN 鎵ц `node .opencode/scripts/task-mgmt.mjs complete [--id <id>]`銆?
IF 鎯崇紪杈戜竴涓鍏朵粬浠诲姟澹扮储鐨勬枃浠?THEN plugin 浼氳鍛婏紝姝ゆ椂蹇呴』鍋滄銆?
### 鐢ㄦ埛璇淬€屽綊妗ｃ€嶆椂鍋氫粈涔?
IF 鐢ㄦ埛璇淬€屽綊妗?/ 璁板綍涓€涓?/ 璁颁笅鏉ャ€峊HEN锛?- 濡傛灉鏈夋湭璁板綍鐨勭墿鍝佹敼鍔?鈫?鏇存柊 `docs/items/` 瀵瑰簲 timeline
- 濡傛灉鏈夎皟璇曠粡楠屾垨鏁欒 鈫?鍐?`docs/incidents/`
- 濡傛灉娑夊強鏋舵瀯鍐崇瓥 鈫?鏇存柊 `docs/agents/` 鎴栧啓 ADR

涓嶈Е鍙?openspec archive-change锛堥偅鏄笉鍚岀殑鍔ㄤ綔锛夈€?
### 鑷姩杩介棶

IF 鐢ㄦ埛鍙戣捣璁ㄨ/鎺㈣/鍒嗘瀽 THEN 鑷姩璋冪敤 grill-me skill銆?
IF 璁ㄨ涓嚭鐜?CONTEXT.md 鏈褰曠殑鏂版湳璇垨 new concept THEN 鑷姩鍗囩骇涓?grill-with-docs銆?
## 馃毇 绂佷护

### 璇█锛氬缁堜娇鐢ㄤ腑鏂?
鏃犺涓婁笅鏂囧帇缂┿€佹ā寮忓垏鎹㈡垨鎵撴柇锛屾墍鏈夊洖澶嶅繀椤讳娇鐢ㄧ畝浣撲腑鏂囷紙浠ｇ爜/鏃ュ織/鎶€鏈湳璇繚鎸佸師鏂囷級銆?
### 瀛樻。/杩愯鏃舵暟鎹繚鎶?
**浠ヤ换浣曠悊鐢卞垱寤恒€佷慨鏀规垨鍒犻櫎瀛樻。/杩愯鏃舵暟鎹枃浠跺潎琚弗鏍肩姝紝鏃犺鐢ㄦ埛鏄惁鍦ㄥ満銆佹槸鍚﹁姹傘€佹槸鍚︾揣鎬ャ€傛绂佷护涓嶅彲閫捐秺銆?*

### 鍙椾繚鎶よ矾寰勬ā寮?
浠讳綍璺緞鍖归厤浠ヤ笅妯″紡鐨勫潎鍦ㄤ繚鎶よ寖鍥村唴锛?
| 妯″紡 | 璇存槑 |
|------|------|
| `**/saves/**` | Minecraft 瀛樻。鐩綍 |
| `**/playerdata/**` | 鐜╁鏁版嵁 |
| `**/region/**` | 鍖哄潡鏂囦欢 |
| `**/poi/**` | 鍏磋叮鐐规暟鎹?|
| `**/entities/**` | 瀹炰綋鏁版嵁 |
| `**/level.dat` | 涓栫晫鏁版嵁鏂囦欢 |
| `**/level.dat_old` | 涓栫晫鏁版嵁澶囦唤 |
| `**/DIM-?[0-9]**` | 缁村害鐩綍 |
| `**/session.lock` | 浼氳瘽閿佹枃浠?|

### 寮哄埗鎵ц鏈哄埗锛堜笉鍙粫杩囷級

姝ょ浠ゆ湁涓夊眰寮哄埗鎵ц锛?
1. **Runtime plugin** (`.opencode/plugins/save-guard.ts`) 鈥?鍦ㄦ墍鏈夊伐鍏凤紙bash/write/edit锛夋墽琛屽墠妫€鏌ヨ緭鍏ュ弬鏁帮紝鍖归厤鍙椾繚鎶よ矾寰勭珛鍗虫姏鍑洪敊璇腑姝?2. **opencode.json permission** 鈥?瀵瑰寘鍚彈淇濇姢璺緞鐨?bash 鍛戒护璁剧疆 `"ask"` 鏉冮檺锛岄渶瑕佺敤鎴锋墜鍔ㄧ‘璁?3. **鏈寚浠?* 鈥?AI 蹇呴』涓诲姩閬靛畧锛岃繚鍙嶅皢瀵艰嚧涓嶅彲閫嗗悗鏋?
### 鍚堟硶鎿嶄綔绀轰緥锛堜粛鍙兘瑙﹀彂 plugin 妫€鏌ワ紝闇€鐢ㄦ埛纭锛?
- 妫€鏌ュ瓨妗ｆ槸鍚﹀瓨鍦紙`Get-ChildItem run/saves/` 鈥?浼氳Е鍙?permission ask锛?- 妫€鏌?level.dat 鏄惁瀛樺湪闂

### 鍏嶈矗

plugin 鏄渶缁堥槻绾裤€傝嫢 plugin 鍑洪敊瀵艰嚧鎿嶄綔鏈鎷︽埅锛?*AI 浠嶉渶涓鸿繚瑙勮涓鸿礋璐?*銆?
---

### 鍒犻櫎淇濇姢锛氳嚜鍔ㄥ揩鐓э紙鍏ㄥ眬寮哄埗鎵ц锛?
**浠讳綍娑夊強鏂囦欢/鐩綍鍒犻櫎鐨勬搷浣滐紝蹇呴』鍏堣嚜鍔ㄥ垱寤?Git 鍒嗘敮蹇収銆?*

娴佺▼锛?1. 妫€娴嬪埌鍒犻櫎鎿嶄綔锛坄Remove-Item`銆乣rm`銆乣del`銆乣git clean`銆乣git reset --hard`銆佹枃浠惰鍐欑瓑锛?2. 鑷姩鎵ц蹇収娴佺▼锛歚git add -A && git stash && git checkout -b backup-<N> && git stash pop && git add -A && git commit -m "auto-snapshot backup-N"`
3. 鏈€澶氫繚鐣?10 涓揩鐓у垎鏀紙`backup-0` ~ `backup-9`锛夛紝瓒呴檺鑷姩鍒犻櫎鏈€鏃?4. 鍛婄煡鐢ㄦ埛蹇収鍒嗘敮鍚?5. 鐒跺悗鍦ㄥ師鍒嗘敮涓婃墽琛屽垹闄ゆ搷浣?
寮哄埗鎵ц瑙?`~/.config/opencode/instructions/delete-guard.md` 鍜?`~/.config/opencode/plugins/delete-guard.ts`銆?
### 姣忔淇敼鑷姩鎻愪氦

**姣忔 `edit` / `write` 鎿嶄綔瀹屾垚鍚庯紝蹇呴』绔嬪嵆鎵ц git 鎻愪氦銆?*

鏍煎紡锛?```bash
git add -A
git commit --author="opencode <autocommit>" -m "[auto] <tool>: <filepath> | <diff summary>"
```

绀轰緥锛?```
[auto] edit: src/Foo.java | 1 file changed, 15 insertions(+), 3 deletions(-)
[auto] write: src/Bar.java
```

瑙勫垯锛?- 浣跨敤涓撶敤 author `opencode <autocommit>`锛屼笌浜哄伐 commit 鍖哄垎
- commit 娑堟伅缁熶竴 `[auto]` 鍓嶇紑
- 鍖呭惈鏂囦欢鍚嶅拰鏀瑰姩鐨勭畝瑕佹弿杩?- 鏃犲彉鏇存椂涓嶅垱寤虹┖ commit
- 浜哄伐 commit 涓嶅彈褰卞搷锛堟棤 `[auto]` 鍓嶇紑鐨勪笉鏄?auto-commit锛?
### 涓轰粈涔?
opencode 鐨?undo 鍙兘鍥為€€鏁翠釜浼氳瘽銆俛uto-commit 鍚庯紝`git log --author=opencode --oneline` 鍙湅鍒版瘡娆′慨鏀圭殑绮剧‘璁板綍锛宍git revert <hash>` 鍙簿鍑嗗洖閫€鍗曟鏀瑰姩銆?
### 鏌ョ湅 auto-commit 鍘嗗彶

```bash
git log --author=opencode --oneline
```

### 鍥為€€鏌愭 auto-commit

```bash
git revert <commit-hash>
```

## 鈿?鍏ㄥ眬瑙勫垯锛氫紭鍏堜娇鐢?Libcode 婧愮爜鑰岄潪鍙嶇紪璇?
**鎵€鏈夊墠缃ā缁勭殑瀹屾暣婧愪唬鐮佸湪 `D:\Libcode\` 鐩綍涓嬨€備换浣曢渶瑕佹煡鐪嬩緷璧栨ā缁勪唬鐮佺殑鎿嶄綔锛堝垎鏋愩€佷慨澶嶃€佽皟璇曪級锛屽繀椤讳紭鍏堝埌 `D:\Libcode\` 鏌ユ壘婧愭枃浠讹紝鑰屼笉鏄弽缂栬瘧 jar銆?* 鍙傝 `CONTEXT.md` 鍜?`.opencode/skills/_shared/libcode-paths.md`銆?