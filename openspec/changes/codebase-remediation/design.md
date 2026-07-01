## Context

褰撳墠浠ｇ爜搴撶粡杩囦唬鐮佸鏌ュ彂鐜?6 椤规灦鏋勫亸绂汇€傚潎涓虹嫭绔嬨€佷綆椋庨櫓淇锛屼笉娑夊強璺ㄦā鍧楁灦鏋勫彉鏇淬€?
## Goals / Non-Goals

**Goals:**
- 娑堥櫎姝讳唬鐮佸拰绌虹洰褰?- 娑堥櫎涓や釜鑳氳儙鐗╁搧闂寸殑閲嶅灏勭嚎杩借釜閫昏緫
- 灏?`SquadChunkTickets` 绉昏嚦姝ｇ‘鐨?`common/utils/` 鍖?- 灏嗙綉缁滈€氶亾瀹氫箟浠庝富绫绘彁鍙栦负鐙珛绫?- 鏇存柊鏋舵瀯 skill 鏂囨。鍚屾鐐瑰皢鍙扮郴缁?
**Non-Goals:**
- 涓嶆敼鍙樻父鎴忓唴鍔熻兘琛屼负
- 涓嶄慨鏀规敞鍐岄€昏緫鎴栦笟鍔￠€昏緫
- 涓嶅紩鍏ユ柊渚濊禆

## Decisions

1. **灏勭嚎杩借釜鎻愬彇绛栫暐**锛氬湪 `MinionSpawnHelper` 涓柊澧?`findSpawnPosition(Player, double)` 闈欐€佹柟娉曪紝鏇夸唬涓や釜鑳氳儙鐗╁搧涓噸澶嶇殑 `use()` 閫昏緫銆傛柟娉曡繑鍥?`Vec3` 鐢熸垚鍧愭爣銆?2. **缃戠粶閫氶亾鎻愬彇**锛氬湪 `common/net/` 涓嬫柊寤?`ModNetwork.java`锛屼粠 `GoetyMechMod.java` 鎻愬彇 `SimpleChannel` 瀹氫箟鍜屾敞鍐岄€昏緫銆?3. **`SquadChunkTickets` 杩佺Щ**锛氫繚鐣欏師绫讳笉鍙橈紝浠呭湪 `common/utils/` 涓嬪垱寤哄紩鐢ㄧ被銆傚彈闄愪簬 Java 鍖呭悕鎯緥锛岀洿鎺ョЩ鍔ㄦ枃浠跺苟鏇存柊鎵€鏈?import銆?4. **skill 鏂囨。鏇存柊**锛氱洿鎺ョ紪杈?`.opencode/skills/GoetyMech-architecture/SKILL.md`锛屾柊澧炵偣灏嗗彴绯荤粺绔犺妭锛屾洿鏂板寘缁撴瀯鏍戙€?
## Risks / Trade-offs

- [浣嶿 绉诲姩 `SquadChunkTickets` 鍙兘涓㈠け git 鍘嗗彶璺熻釜 鈥?鎺ュ彈锛屼粎 9 琛屽伐鍏风被
- [浣嶿 `SculkSpikeRenderer.java` 琚垹闄ゅ悗濡傛湁澶栭儴寮曠敤浼氱紪璇戝け璐?鈥?宸?grep 纭鏃犲紩鐢?- [浣嶿 skill 鏂囨。鎵嬪姩缂栬緫鍙兘鏈夋牸寮忓亸宸?鈥?缂栬緫鍚庨獙璇?markdown 缁撴瀯
