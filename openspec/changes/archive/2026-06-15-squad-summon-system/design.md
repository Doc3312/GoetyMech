## Context

GoetyMech 宸叉湁鎴愮啛鑳氳儙锛圡atureEmbryoItem锛変綔涓哄鎬佸埛鎬墿鍝侊紝鍙洿鎺ュ彸閿敓鎴愪粏浠庛€備絾缂轰箯鎵归噺鍙敜鏈哄埗銆侴oety 鍘熺敓鎻愪緵 TroopFocus锛堟寜绫诲瀷鍙敜鍏ㄩ儴锛夊拰 CallFocus锛堟寜 UUID 鎷夊彇鍗曚釜锛夛紝鍧囦笉婊¤冻"閰嶇疆缂栭槦鈫掕繙绋嬫秷鑰椻啋鎵归噺鐢熸垚"鐨勯渶姹傘€?
褰撳墠椤圭洰缁撴瀯锛?- 鏂瑰潡娉ㄥ唽锛歚ModBlocks` (DeferredRegister)
- 鐗╁搧娉ㄥ唽锛歚ModItems` (DeferredRegister)
- 瀹瑰櫒娉ㄥ唽锛氶渶瑕佹柊澧?`ModMenuTypes`
- 瀹炰綋娉ㄥ唽锛歚ModEntities`
- 瀹㈡埛绔敞鍐岋細`ModClientEvents`
- Goety 鑱氭櫠鍩虹被锛歚MagicFocus` 鈫?缁戝畾 `ISpell`
- Goety 浠嗕粠鐢熷懡鍛ㄦ湡锛歚IOwned` 鍐呯疆 `limitedLifespan`/`limitedLifeTicks`

## Goals / Non-Goals

**Goals:**
- 鐐瑰皢鍙版柟鍧楋細涓夐樁锛?/12/18 squad slot锛夛紝GUI 閰嶇疆锛屽叏鏂瑰悜鏈烘鍔ㄥ姏杈撳叆
- 鍏靛洟鑱氭櫠锛氱粦瀹氱偣灏嗗彴 鈫?杩滅▼鏂芥硶 鈫?娑堣€楄儦鑳?鐏甸瓊 鈫?闄愭椂鐢熸垚浠嗕粠
- 鍖哄潡甯稿姞杞斤細鐐瑰皢鍙版墍鍦?chunk force-load锛屼笁闃惰法缁村害
- 鐏甸瓊鎶樻墸锛氫簩闃?50%锛屼笁闃?10%锛堜竴闃跺叏棰濓級
- 浠嗕粠闄愭椂 5 鍒嗛挓

**Non-Goals:**
- 涓嶆秹鍙婂悎鎴愰厤鏂癸紙鍚庣画鍗曠嫭鍔狅級
- 涓嶆秹鍙婃柊瀹炰綋 AI
- 涓嶄慨鏀?Goety 鏍稿績搴?- 涓嶈法缁村害浼犻€佸凡鏈夊疄浣?
## Decisions

### 1. Squad Slot 閰嶇疆鏂瑰紡锛氳儦鑳庣洿鎻?
姣忎釜 squad slot 鍥哄畾鏀?1 涓?MatureEmbryo锛坢axStack=1锛夛紝slot 鏁伴噺闅忛樁閫掑銆傝儦鑳庣殑 `EntityTypeId` NBT 鍐冲畾鍙敜浠€涔堝叺绉嶃€?
- 鏇夸唬鏂规锛堥璁剧紪杈戝櫒锛夛細UI 澶嶆潅锛孧VP 涓嶉渶瑕?
### 2. 鑱氭櫠鐏甸瓊娑堣€楀姩鎬佸寲锛歚defaultSoulCost() = 0`

Goety 鐨?`MagicFocus.soulCost` 鍦ㄦ瀯閫犳椂鍥哄畾鍐欏叆锛屼笉鏀寔鍔ㄦ€併€傝涓?0 鍚?DarkWand 涓嶅仛棰勬鍜岄鎵ｏ紝鍏ㄩ儴鍦?`SquadSpell.SpellResult()` 涓墜鍔ㄥ鐞嗐€?
- tooltip 瑕嗙洊鏄剧ず"姣忓崟浣嶆秷鑰?X 鐏甸瓊"+鎶樻墸璇存槑锛岃€岄潪鏁板瓧

### 3. 鍖哄潡鍔犺浇锛歍icketType 甯稿姞杞?
鏂瑰潡鏀剧疆鏃堕€氳繃 `ServerChunkCache.addRegionTicket()` 娉ㄥ唽甯稿姞杞?ticket锛岀牬鍧忔椂绉婚櫎銆備笁闃堕澶栧瓨鍌ㄧ淮搴?ID锛岃法缁村害鏂芥硶鏃朵粠 `MinecraftServer.getLevel(dim)` 璁块棶鐩爣缁村害銆?
### 4. 浠嗕粠闄愭椂锛氬鐢?IOwned 鐢熷懡鍛ㄦ湡

`MinionSpawnHelper.spawnMinion()` 鏈熬澧炲姞 `mob.setHasLifespan(true); mob.setLifespan(6000);`銆傛墍鏈夌户鎵?`Owned` 鐨?Goety 浠嗕粠鍦?`ownedTick()` 涓嚜鍔ㄩ€掑噺骞舵秷浜°€?
### 5. 鐏甸瓊娑堣€楀鐢ㄧ幇鏈夐厤缃?
姣忎釜瀹炰綋娑堣€?`ModConfig.MATURE_EMBRYO_SOUL_COST`锛堥粯璁?100锛夈€傛€绘秷鑰?= 闈炵┖ slot 鏁?脳 baseCost 脳 tierDiscount銆備娇鐢?`MinionSpawnHelper.handleSoulCost()` 鐨勬墸榄傞€昏緫锛屼絾鎸夋€讳环涓€娆℃€ф牎楠屻€?
## Risks / Trade-offs

| 椋庨櫓 | 缂撹В鎺柦 |
|---|---|
| Chunk 甯稿姞杞借繃澶氫釜鐐瑰皢鍙板奖鍝嶆€ц兘 | 璁捐涓婇鏈熸瘡鐜╁ 1-2 涓偣灏嗗彴锛岄噺绾у彲鎺э紱鍙湪 config 涓坊鍔犵鐢ㄩ€夐」 |
| `defaultSoulCost()=0` 瀵艰嚧 wand tooltip 鏄剧ず"娑堣€?0" | 鍦?`SquadFocus.appendHoverText()` 涓鐩栵紝鏄剧ず璇存槑鏂囧瓧鑰岄潪鏁板瓧 |
| 璺ㄧ淮搴︽柦娉曟椂鐩爣缁村害鏈姞杞?| 涓夐樁 ticket 纭繚鐩爣缁村害鍖哄潡甯稿姞杞斤紱涓€闃朵簩闃朵粎鍚岀淮搴︿娇鐢?|
| GUI 鍔ㄦ€?slot 鏁帮紙18 slots + 9 鍌ㄥ锛夊昂瀵搁棶棰?| 鍌ㄥ浠撳簱鍥哄畾 9 鏍肩疆浜庝笅鏂癸紝squad slot 鎸夐樁鏄剧ず 1/2/3 琛?脳 6 鍒?|
