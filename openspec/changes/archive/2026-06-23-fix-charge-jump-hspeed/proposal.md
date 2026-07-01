## Why

褰撶帺瀹跺湪鍐插埡鐘舵€佷腑鎵ц钃勫姏璺虫椂锛屾按骞抽€熷害浠?~0.27 blocks/tick锛堟甯稿啿鍒洪€熷害锛夐闄嶈嚦 ~0.005 blocks/tick銆傝搫鍔涜烦鐨?`CJ_INJECT` 娉ㄥ叆鐨勭洰鏍囨按骞抽€熷害浠?~0.005 鈥?鍑犱箮涓洪浂锛屽鑷磋烦璺冨悗鐜╁鍍忔槸琚?瀹氫綇"浜嗐€?
## 璇婃柇鍘嗙▼涓庨敊璇鐩?
### 閿欒 1锛氬亣璁?handlePlayerInput 浼氭甯歌Е鍙?
鏈€鍒濊涓烘湇鍔＄ `player.zza == 0` 鏄洜涓?`handlePlayerInput` 琚皟鐢ㄦ椂浼犲叆浜嗛敊璇殑鍊笺€傛坊鍔?Mixin 娉ㄥ叆 `handlePlayerInput` 鐨?HEAD/RETURN 鍚庯紝鍙戠幇 `HANDLE_INPUT_CALLED` **浠庢湭鍑虹幇鍦ㄦ棩蹇椾腑**銆傝€屽鐓х粍鐨?`HANDLE_MOVE_PLAYER` 鍜?`HANDLE_PLAYER_COMMAND` 鍧囨甯歌Е鍙戙€?
### 閿欒 2锛氭€€鐤?PrototypeTravelMixin 鐨?elytra travel 瑕嗙洊

鍦?`applyChargedJump` 鍚庯紝鐜╁姘村钩閫熷害涓㈠け銆備竴搴﹁涓?`PrototypeTravelMixin` 鐨?`travel()` HEAD 娉ㄥ叆鍦ㄧ帺瀹惰烦璧峰悗锛坥nGround=false锛夐敊璇嫤鎴簡 travel 閫昏緫銆傚疄闄?`CJ_INJECT` 鏃ュ織鏄剧ず `targetHSpeed = 0.0051` 鈥?娉ㄥ叆鐨勫€兼湰韬氨寰堝皬锛屼笉鏄?travel 瑕嗙洊瀵艰嚧銆?
### 閿欒 3锛氳涓?groundSpeed 璺熻釜鍙潬

`groundSpeed = sqrt((x-xo)^2 + (z-zo)^2)` 渚濊禆鏈嶅姟绔帺瀹剁殑浣嶇疆鍙樺寲銆備絾鍗曟満妯″紡涓嬫湇鍔＄閫氳繃 `handleMovePlayer` 鎺ユ敹鐨勪綅缃寘骞堕潪姣?tick 閮芥洿鏂帮紙`moved` 鍊煎湪 `0.27` 鍜?`0.0` 涔嬮棿闅忔満鍑虹幇锛夈€俙applyChargedJump()` 璇诲彇鏃?groundSpeed 鍙兘鏄繃鏈熷€笺€?
### 鏍瑰洜鎬荤粨

1. **`handlePlayerInput()` 鍦ㄥ崟鏈烘ā寮忎笅浠庢湭琚皟鐢?* 鈥?杩欐槸 Minecraft 1.20.1 鐨勬甯歌涓猴紝鏈嶅姟绔帺瀹剁殑 `input` 瀛楁涓嶉€氳繃 vanilla 缃戠粶鍖呮洿鏂?2. 鏈嶅姟绔?`player.zza` 姘镐负 `0.0`锛宍player.jumping` 涔熷悓鐞?3. `groundSpeed` 渚濊禆 `moved`锛堟湇鍔＄浣嶇疆 delta锛夛紝鑰?`moved` 涓嶅彲闈?4. 钃勫姏璺抽噴鏀炬椂 `applyChargedJump()` 浣跨敤涓嶅彲闈犵殑 `groundSpeed` 鍋氭柟鍚戝悜閲忚绠楋紝瀵艰嚧姘村钩閫熷害娉ㄥ叆涓洪浂鎴栨瀬灏忓€?
## What Changes

- 鎵╁睍 `C2SJumpChargePacket`锛屽鍔?`float forward` 鍜?`boolean jumping` 瀛楁锛屼粠瀹㈡埛绔紶杈撳埌鏈嶅姟绔?- 鍦?`C2SJumpChargePacket.handle()` 涓紝鐩存帴璋冪敤 `player.setPlayerInput(forward, 0, jumping, false)` 瑕嗙洊鏈嶅姟绔帺瀹剁殑杈撳叆鐘舵€?- 鏀硅繘 `groundSpeed` 璺熻釜锛氬湪瀹㈡埛绔搫鍔涘垵濮嬪寘锛坱ick 1锛変腑鎼哄甫瀹㈡埛绔疄娴嬬殑鍦伴潰閫熷害锛坄sqrt(motion.x^2 + motion.z^2)`锛夛紝鏈嶅姟绔敹鍒板悗鍐欏洖 `groundSpeed`
- 绉婚櫎 `ServerGamePacketListenerImplMixin.java` 鐨勮瘖鏂棩蹇楋紙璋冭瘯瀹屾垚鍚庣殑娓呯悊锛?- 纭繚 `zza > 0.0F` 妫€鏌ワ紙鍦伴潰璧烽銆佺┖涓儴缃诧級鍦ㄦ湇鍔＄涔熻兘姝ｅ父瑙﹀彂

## Capabilities

### New Capabilities
- `charge-input-sync`: 浠庡鎴风鍚戞湇鍔＄鍚屾 `forward`锛圵 閿級鍜?`jumping` 鐘舵€佺殑缃戠粶鑳藉姏锛岀‘淇濇湇鍔＄ `player.zza` 鍜?`player.jumping` 姝ｇ‘鍙嶆槧鐜╁瀹為檯杈撳叆

### Modified Capabilities

None.

## Impact

- `src/main/java/com/GoetyMech/addon/common/net/c2s/C2SJumpChargePacket.java` 鈥?澧炲姞 `forward` 鍜?`jumping` 瀛楁锛宔ncode/decode 鏇存柊
- `src/main/java/com/GoetyMech/addon/common/events/PrototypeEvents.java` 鈥?`handleJumpChargeFromClient` 鎺ユ敹棰濆鍙傛暟锛岃皟鐢?`setPlayerInput`锛沗groundSpeed` 璺熻釜鍏煎瀹㈡埛绔笂鎶?- `src/main/java/com/GoetyMech/addon/client/PrototypeRenderEvents.java` 鈥?鍙戦€佽搫鍔涘寘锛坱ick 1锛夋椂闄勫甫 `forward` 鍜?`jumping` 鍊硷紱鍙戦€佸鎴风褰撳墠鍦伴潰閫熷害
- `src/main/java/com/GoetyMech/addon/mixin/ServerGamePacketListenerImplMixin.java` 鈥?鍒犻櫎璇婃柇鏃ュ織娉ㄥ叆锛堟竻鐞嗭級
