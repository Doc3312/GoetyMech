## Why

鏈浠ｇ爜瀹℃煡鍙戠幇 6 椤规灦鏋勫亸绂伙紝鍖呮嫭姝讳唬鐮併€乊AGNI 杩濊銆佷唬鐮侀噸澶嶅拰 skill 鏂囨。杩囨椂銆傞渶瑕佺郴缁熷寲淇浠ヤ繚鎸侀」鐩灦鏋勬暣娲侊紝闃叉鎶€鏈€哄姟绱Н銆?
## What Changes

- **绉婚櫎姝讳唬鐮?*锛氬垹闄ゆ湭娉ㄥ唽鐨?`client/entity/SculkSpikeRenderer.java`
- **绉婚櫎绌虹洰褰?*锛氬垹闄ゆ棤鏂囦欢鐨?`com/GoetyMech/addon/event/` 鐩綍
- **鎻愬彇閲嶅閫昏緫**锛氬皢涓や釜鑳氳儙鐗╁搧涓噸澶嶇殑灏勭嚎杩借釜瀹氫綅閫昏緫鎻愬彇鍒?`MinionSpawnHelper`
- **绉诲姩宸ュ叿绫?*锛氬皢 `SquadChunkTickets` 浠?`common/block/entity/` 绉昏嚦 `common/utils/`
- **娓呯悊涓荤被鍐呰仈缃戠粶**锛氬皢缃戠粶閫氶亾瀹氫箟浠?`GoetyMechMod.java` 鎻愬彇涓虹嫭绔嬬被
- **鏇存柊鏋舵瀯 skill**锛氬悓姝?`GoetyMech-architecture` skill 鏂囨。锛屽綍鍏ョ偣灏嗗彴瀛愮郴缁?
## Capabilities

### New Capabilities
- `codebase-cleanup`: 绉婚櫎姝讳唬鐮佸拰绌虹洰褰曪紝閲嶆瀯閲嶅閫昏緫
- `skill-docs-sync`: 灏嗙偣灏嗗彴绯荤粺銆丮enu/GUI/缃戠粶鍖呯粨鏋勮ˉ褰曞埌鏋舵瀯 skill

### Modified Capabilities
- 锛堟棤鐜版湁 spec 琚慨鏀癸級

## Impact

- `client/entity/SculkSpikeRenderer.java` 鈥?鍒犻櫎
- `com/GoetyMech/addon/event/` 鈥?鍒犻櫎鏁翠釜绌虹洰褰?- `common/item/TwistedEmbryoItem.java` 鈥?鎻愬彇灏勭嚎杩借釜閫昏緫
- `common/item/MatureEmbryoItem.java` 鈥?鎻愬彇灏勭嚎杩借釜閫昏緫
- `common/utils/MinionSpawnHelper.java` 鈥?鏂板 `findSpawnPosition()` 鏂规硶
- `common/block/entity/SquadChunkTickets.java` 鈥?绉诲姩鍒?`common/utils/`
- `common/net/` 鈥?淇濇寔鐜版湁浣嶇疆锛岀敱 skill 鏂囨。璁板綍
- `.opencode/skills/GoetyMech-architecture/SKILL.md` 鈥?鏇存柊
