# MixinWallRunTravel 鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歮isc (Mixin)
- Java 绫伙細MixinWallRunTravel
- Mixin 鐩爣锛歚Player.travel(Vec3)`
- 棣栨璁板綍锛?026-06-24

## 璁捐鎰忓浘

鍙栦唬 `Player.travel()` 鐨勯粯璁ょЩ鍔ㄩ€昏緫锛屽綋鐜╁绌跨潃 Prototype 涓斿湪鐖鐘舵€佹椂锛屼娇鐢ㄨ嚜瀹氫箟澧欓潰杩愬姩鍚戦噺锛堟部澧欎笂鐖?渚хЩ+閲嶅姏鍚搁檮锛夛紝璺宠繃鍘熺増绉诲姩璁＄畻銆?

## 婕斿寲璁板綍

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 棣栨鏂囨。璁板綍 | 杩樻竻鏂囨。鍊哄姟 | ClientWallRunState, PrototypeItem, WallUtil |
| 2026-06-24 | 澶撮《缈昏秺锛坥utward burst锛? grace period 3 tick | 澧欒窇閬囧埌澶╄姳鏉块殰纰嶈嚜鍔ㄥ悜澶栫垎鍙戠炕瓒?| ClientWallRunState |
| 2026-06-24 | 缈昏秺鐖嗗彂鏃惰 sprintRetainTicks=30锛堝悗鏀逛负 30锛?| 缈昏秺鍚?sprint 琚?LocalPlayer.aiStep() 娓呴櫎锛岄渶瑕佷繚鐣欑獥鍙ｉ噸鏂拌创澧?| ClientWallRunState, PrototypeRenderEvents |
