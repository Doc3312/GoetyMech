# 琛€鑲夌啍鐐?鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歜lock
- 娉ㄥ唽绫伙細ModBlocks.FLESH_FURNACE / ModItems.FLESH_FURNACE
- Java 绫伙細FleshFurnaceBlock
- 鏂瑰潡瀹炰綋锛欶leshFurnaceBlockEntity
- 棣栨鍒涘缓锛?026-06-14

## 璁捐鎰忓浘

琛€鑲変富棰樼殑鐗规畩鐔旂倝锛屽叿鏈変笌鍘熺増鐔旂倝涓嶅悓鐨勫鐞嗛€昏緫鍜岃瑙夋晥鏋溿€?

## 婕斿寲璁板綍

### 2026-06-29 鈥?淇锛歴etItem stack.split(1) 瀵艰嚧 Create 婕忔枟涓嶆秷鑰楃墿鍝?
- **鍘熷洜**锛歚setItem()` 涓?`stack.split(1)` 淇敼浜?`SidedInvWrapper` 浼犲叆鐨勫師濮?ItemStack 寮曠敤锛屽鑷?Create 浼犻€佸甫渚х殑 `TransportedItemStack.stack` 琚涓虹┖鏍堬紝婕忔枟璁や负鏈垚鍔熶紶杈?
- **淇**锛氭敼涓?`stack.copyWithCount(1)`锛屽垱寤虹嫭绔嬪壇鏈紝涓嶄慨鏀瑰師寮曠敤
- **鍏宠仈**锛歚FleshFurnaceBlockEntity.java:232` + `:559`

### 2026-06-29 鈥?淇锛歝achedLinkRange 鏈垵濮嬪寲瀵艰嚧杩炴帴璺濈涓?0
- **鍘熷洜**锛歚refreshCache()` 鍙湪 `ModConfigEvent.Reloading` 鏃惰皟鐢紝缂哄皯 `ModConfigEvent.Loading`锛宍cachedLinkRange` 淇濇寔 `int` 榛樿鍊?`0`
- **淇**锛氭坊鍔?`onConfigLoad(ModConfigEvent.Loading)` handler
- **鍏宠仈**锛歚ModConfig.java:135-138`
