# ClientWallRunState 鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歮isc (Client-side state)
- Java 绫伙細ClientWallRunState
- 棣栨璁板綍锛?026-06-24

## 璁捐鎰忓浘

绾鎴风鍗曚緥鐘舵€侊紝瀛樺偍鐖妫€娴嬬粨鏋滐紙澧欓潰娉曠嚎鏂瑰悜銆佸墠鍚?渚хЩ杈撳叆銆佸啿鍒虹姸鎬併€乬race ticks锛夛紝渚?`MixinWallRunTravel` 鍦?`Player.travel()` 涓鍙栦互璁＄畻澧欓潰杩愬姩鍚戦噺銆?

## 婕斿寲璁板綍

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 棣栨鏂囨。璁板綍 | 杩樻竻鏂囨。鍊哄姟 | MixinWallRunTravel, PrototypeRenderEvents |
| 2026-06-24 | 鏂板 graceTicks/lastWallNormal + applyGrace/tickGrace | 缈昏秺鐖嗗彂鍚庣暀 3 tick 绐楀彛鐢?pull 閲嶆柊璐村 | MixinWallRunTravel |
| 2026-06-24 | 鏂板 sprintRetainTicks + shouldSprint/tickSprintRetain | 缈昏秺鍚庝繚鎸?30 tick sprint锛岄槻姝?LocalPlayer.aiStep() 娓呴櫎 | MixinWallRunTravel, PrototypeRenderEvents |
