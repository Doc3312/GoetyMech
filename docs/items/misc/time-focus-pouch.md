# 鏃跺仠鑱氭櫠琚?鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歱ouch
- 娉ㄥ唽绫伙細ModItems.TIME_FOCUS_POUCH
- Java 绫伙細TimeFocusPouch (extends FocusPack)
- 棣栨鍒涘缓锛?026-06-14

## 璁捐鎰忓浘

鎵撳紑 GUI 鏃惰Е鍙戞椂绌哄喕缁撱€傚鎴风 entity 浣嶇疆/鍔ㄧ敾/绮掑瓙鍐荤粨锛屾覆鏌?partialTick 褰掗浂娑堥櫎 jitter锛屾湇鍔＄缁村害 tick 鏆傚仠銆?

## 婕斿寲璁板綍

### 2026-06-18 鈥?Iteration 1锛氬彇娑?tickEntities() 鑰岄潪鏁存潯 tick()

- 鏀瑰姩锛欳lientLevelMixin 娉ㄥ叆鐩爣浠?tick() 鏀逛负 tickEntities()
- 鍘熷洜锛氬彇娑?tick() 瀵艰嚧 Goety 寰勫悜鑿滃崟鍗℃锛堜緷璧?level 涓婁笅鏂囷級
- 鍏宠仈锛歰penspec time-freeze-jitter-fix

### 2026-06-18 鈥?鏂板 MinecraftMixin 鍐荤粨 partialTick

- 鏀瑰姩锛歡etPartialTick() 鍦ㄦ椂鍋滀腑杩斿洖 0.0F
- 鍘熷洜锛歟ntity 浣嶇疆鍐荤粨鍚庯紝partialTick 缁х画閫掑瀵艰嚧娓叉煋鎻掑€兼紓绉伙紙jitter锛?
- 鍏宠仈锛歰penspec time-freeze-jitter-fix

### 2026-06-19 鈥?Iteration 3锛欵ntity.getPosition(float) 蹇収瑕嗙洊

- 鏀瑰姩锛氬垱寤?EntityMixin 蹇収绯荤粺锛屽喕缁?activate 鏃舵崟鑾锋墍鏈?entity 浣嶇疆/鏃嬭浆
- 鍘熷洜锛氭湇鍔＄鎸佺画鍙戦€佷綅缃寘 鈫?setPos() 鏇存柊 xo/x 鈫?getPosition(0)=xo 鈫?camera 姣忓寘璺宠穬
- 鍏宠仈锛歰penspec fix-freeze-jitter

### 2026-06-19 鈥?蹇収鐢熷懡鍛ㄦ湡瀹屽杽

- 鏀瑰姩锛氭敮鎸佹柊 entity 鐢熸垚鏃舵噿鍔犺浇銆佺Щ闄?entity 鏃舵竻鐞嗐€乨eactivate 娓呯┖ map
- 鍘熷洜锛氬喕缁撴湡闂存柊澧?鍗歌浇鐨?entity 闇€瑕佹纭殑蹇収琛屼负
- 鍏宠仈锛歰penspec fix-freeze-jitter
