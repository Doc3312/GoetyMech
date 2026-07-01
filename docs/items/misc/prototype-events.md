# PrototypeEvents 鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歮isc (Forge event subscriber锛屾湇鍔＄)
- Java 绫伙細PrototypeEvents
- 棣栨璁板綍锛?026-06-24

## 璁捐鎰忓浘

Prototype 绯荤粺鏈嶅姟绔牳蹇冮€昏緫锛岀鐞?4 涓瓙绯荤粺锛?

| 瀛愮郴缁?| 浜嬩欢椹卞姩 | 璇存槑 |
|--------|---------|------|
| **Wall-run** | livingTick 鈫?`tryStartWallRun` / `handleWallRunTick` / `handleWallJumpOff` | 澧欓潰妫€娴?+ 鍚搁檮/瑙掕惤杞Щ/璺崇 |
| **Charge jump** | C2S 鍖?鈫?`handleJumpChargeFromClient` 鈫?`applyChargedJump` | 钃勫姏楂樺害鍙嶇畻鍒濋€熷害锛屾按骞虫儻鎬?+ 鎵ｈ烦 |
| **Slam** | C2S 鍖呰Е鍙?鈫?`enterSlamState` / `handleSlamTick` / `triggerSlamImpact` | 鎮仠鈫掕嚜鐢辫惤浣撯啋鍦伴潰鍐插嚮 AoE 浼ゅ + 纰庡潡 ring 鍔ㄧ敾 |
| **Sprint** | playerTick 鈫?`Attributes.MOVEMENT_SPEED` 淇グ鍣?| 闅忔椂闂寸嚎鎬у姞閫熷埌 `SPRINT_MAX_MULTIPLIER` |

杈呭姪鏈哄埗锛氬潬钀藉厤浼?(`LivingFallEvent`)锛宻lam 鏃犳晫甯?(`LivingAttackEvent`)锛屽欢杩?AoE 浼ゅ + 鍗囩┖鍐插嚮娉紝澶氱骇鐜姸纰庡潡寮瑰嚭鍔ㄧ敾銆?

## 婕斿寲璁板綍

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 棣栨鏂囨。璁板綍 | 杩樻竻鏂囨。鍊哄姟 | PrototypeItem, WallUtil, PrototypeRenderEvents |
| 2026-06-24 | 鍥為€€ CJ-during-wall-run锛氭仮澶?isWallRunning 瀹堝崼 | 澧欎笂璺宠穬涓庣炕瓒婄炕瓒婂啿绐侊紝缈昏秺鍚庡啿鍒烘柇鎺?| PrototypeRenderEvents |
| 2026-06-24 | 鏂板 Kill Absorption 瀛愮郴缁燂細LivingDeathEvent 鐩戝惉锛屽嚮鏉€鐢熺墿鍚告敹 80% maxHealth 娌荤枟鐜╁锛屾孩鍑鸿浆 absorption锛堜笂闄?maxHealth脳2锛夛紱鍚屾椂鎭㈠ foodLevel锛?/3 maxHealth锛夛紝婊″悗婧㈣浆 saturation | 鍘熷瀷缂轰箯鍑绘潃鏀剁泭锛岄渶瑕佹垬鏂楁鍚戝惊鐜?| ModConfig (kill_health_absorb_ratio/kill_absorption_cap_multiplier/kill_food_restore_ratio) |
| 2026-06-24 | tryStartWallRun 鍔?!player.isInWater() 瀹堝崼 | 姘翠笅涓嶅簲瑙﹀彂澧欒窇 | PrototypeRenderEvents |
| 2026-06-24 | 璺岃惤鑷姩閲婃斁闃堝€?3鈫? 鏍?| 3 鏍煎お瀹规槗瑙﹀彂 | 鈥?|
| 2026-06-25 | 鍚屽寲鍑绘潃璁℃暟锛歀ivingDeathEvent 绱姞 + S2C 鍖呭悓姝ワ紱PlayerLoggedInEvent 鐧诲綍鍚屾锛汱ivingHurtEvent 鍏ㄥ眬浼ゅ鍊嶇巼 + indirectMagic 鐥呮瘨闄勫姞浼ゅ锛涜兘鍔涢棬鎺э紙椋炶濮嬬粓鍙敤锛宻lam 鈮?0锛?| RPG 鎴愰暱绯荤粺 | PrototypeItem, S2CAssimilationDataPacket |
| 2026-06-25 | 鍚屽寲缂撳瓨钀界洏锛欰ssimilationSaveManager 姣?60s 鍐欎竴娆＄嫭绔嬫枃浠讹紱鐧诲綍鏃?cache > NBT 鑷姩鎭㈠锛涘啓涓存椂鏂囦欢+鍘熷瓙閲嶅懡鍚嶉槻宕╂簝鎴柇锛涙浛鎹㈠嵆鏃?save(sp) | crash 瀵艰嚧鍑绘潃鏁版竻闆剁殑鏁欒 | AssimilationSaveManager |
