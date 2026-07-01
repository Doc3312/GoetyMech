# PrototypeRenderEvents 鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歮isc (Forge event subscriber, CLIENT)
- Java 绫伙細PrototypeRenderEvents
- 棣栨璁板綍锛?026-06-24

## 璁捐鎰忓浘

瀹㈡埛绔晶鐨?Prototype 绯荤粺娓叉煋涓庤緭鍏ュ鐞嗭細

1. **Client tick** 鈥?鎸夐敭杈撳叆閲囬泦锛圵ASD/鍐插埡/璺宠穬锛夛紝澧欓潰妫€娴嬶紝C2S 鍙戝寘锛堢埇澧欒緭鍏ャ€佽搫鍔涜烦璺冦€佸璺炽€佸彇娑?slam銆侀琛岃緭鍏ワ級
2. **Camera roll** 鈥?鐖鏃舵憚鍍忔満娌垮闈㈡硶绾挎棆杞紙quaternion SLERP锛?
3. **Model rotation** 鈥?鐖鏃剁帺瀹舵ā鍨?body/head/arms/legs 鍖归厤鎽勫儚鏈烘棆杞?
4. **Body render** 鈥?鍦ㄧ帺瀹惰韩浣撲笂鍙犲姞 Prototype 绾圭悊娓叉煋
5. **Particles** 鈥?Slam 涓嬪潬鏃?body trail 绮掑瓙锛圖ustParticle + Flame锛?

## 婕斿寲璁板綍

| Date | Change | Reason | Relates to |
|------|--------|--------|-----------|
| 2026-06-24 | 棣栨鏂囨。璁板綍 | 杩樻竻鏂囨。鍊哄姟 | ClientWallRunState, PrototypeEvents, PrototypeItem |
| 2026-06-24 | START 闃舵鍔?shift 妫€娴嬮€€鍑哄璺?| 鐢ㄦ埛闇€姹傦細Shift 閿澗寮€澧欓潰 | MixinWallRunTravel |
| 2026-06-24 | START 闃舵鐢?shouldSprint() 瀹堝崼 sprintDown锛宻printing 鍙橀噺鍔?|| ClientWallRunState.shouldSprint() | 缈昏秺鍚?sprint 涓㈠け锛岀敤 sprintRetainTicks 淇濇寔 30 tick | ClientWallRunState |
| 2026-06-24 | 鍒犻櫎 shouldSprint() 瀹堝崼锛屾仮澶嶇洿鎺ユ寜閿疆璇?sprintDown | shouldSprint() 鍦?grace 绐楀彛鏈熼樆姝?sprintDown 鍒锋柊 鈫?缈昏秺鍚?sprint 鏂帀 | sprint-cutoff-fix incident |
| 2026-06-24 | setWallNormal 鍔?!player.isInWater() 瀹堝崼锛沜amera roll 鍔?!player.isInWater() | 姘翠笅涓嶅簲瑙﹀彂澧欒窇锛堢Щ鍔?鐩告満鏃嬭浆锛?| PrototypeEvents |
| 2026-06-24 | 閲嶆瀯 charge jump C2S 鍙戝寘閫昏緫锛歝anCharge 涓嬬Щ鍒板唴灞備粎鎺у埗绱姞锛涘灞傛敼鐢?!isFallFlying锛涘彂鍖呰妭娴佹敼涓?tickCount%5锛涢噴鏀炬敼涓?!jumpDown | 璺岃惤杈圭紭鏃?onGround=false 鈫?canCharge=false 鈫?else 鍒嗘敮鍙?action=0 缁曡繃鏈嶅姟绔?3 鏍兼娴?| PrototypeEvents |
| 2026-06-25 | 鍚屽寲 tooltip锛歰nItemTooltip 浜嬩欢锛孲hift 灞曞紑鏄剧ず鑳藉姏鍒楄〃锛堥鑹叉爣璁拌В閿佺姸鎬侊級+ 缁熻琛岋紙鏀诲嚮鍔涘姞鎴?鐥呮瘨闄勫姞浼ゅ锛?| RPG 鎴愰暱浣撻獙 | PrototypeItem |
