# MimicItemDecorator 鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歮isc
- Java 绫伙細MimicItemDecorator
- 棣栨鍒涘缓锛?026-06-25

## 璁捐鎰忓浘

Mimic 鐗╁搧鐨勮楗板櫒锛岃礋璐ｅ湪鐗╁搧鏍?鑳屽寘涓樉绀轰吉瑁呯殑鐗╁搧澶栬銆?

## 婕斿寲璁板綍

### 2026-06-25 鈥?鍒涘缓

- 鏀瑰姩锛氬垱寤?MimicItemDecorator
- 鍘熷洜锛歁imic 闇€瑕佹楠楀瑙傛樉绀轰负琚吉瑁呯殑鐗╁搧
- 鍏宠仈锛歰penspec mimic-system

### 2026-07-01 鈥?淇 overlay 鍥惧眰杩囬珮闂

- 鏀瑰姩锛歚translate(0, 0, 400)` 鈫?`translate(0, 0, 0)`
- 鍘熷洜锛歾=400 瀵艰嚧 overlay 閬斀鎵嬫寔鐗╁搧棰勮鍜岄紶鏍囨偓鍋?tooltip銆侷ItemDecorator 宸插湪 `GuiGraphics.flush()` 鍚庢墽琛岋紝draw order 淇濊瘉鍦ㄦ渶涓婂眰锛屾棤闇€楂?z 鍊?
- 鍏宠仈锛歮imic-overlay-z-fix
