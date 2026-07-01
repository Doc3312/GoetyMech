# Flesh Casket Infuser Recipe 鈥?GoetyMech

## 韬唤

- 绫诲瀷锛欳ursedInfuserRecipes锛堣瘏鍜掓敞鍏ュ櫒閰嶆柟锛?- 娉ㄥ唽锛歁odRecipeSerializers.FLESH_CASKET_INFUSER
- Java 绫伙細FleshCasketInfuserRecipe
- 棣栨鍒涘缓锛?026-06-29

## 璁捐鎰忓浘

瀹炵幇鏅跺專瑙﹀彂绫诲瀷鐨勫惊鐜垏鎹紙A鈫払鈫扖鈫扐锛夛紝鏀惧叆璇呭拻娉ㄥ叆鍣ㄥ悗鑷姩鍒囨崲瑙﹀彂妯″紡銆?
## 婕斿寲璁板綍

### 2026-06-29 鈥?鍒濆鍒涘缓

- 鏀瑰姩锛欶leshCasketInfuserRecipe + FleshCasketInfuserRecipeSerializer銆俙assemble()` 澶嶅埗杈撳叆 NBT 鍚庢墽琛?`cycleTriggerType()`銆傞厤鏂?JSON 3 涓紙flesh_casket_cycle_1/2/3锛夈€?- 鍘熷洜锛氭浛浠?9 鍙樹綋娉ㄥ唽鏂瑰紡锛岄€氳繃 NBT 鍒囨崲瑙﹀彂绫诲瀷
- 鍏宠仈锛歝ommit b10c2c9

### 2026-07-02 鈥?淇鍗囩骇娓呯┖鑱氭櫠 + JEI 鍥炬爣淇

- 鏀瑰姩锛歚assemble()` 鏂板 ITEM_HANDLER capability 閫愭Ы澶嶅埗鑱氭櫠鏍堬紱瑕嗙洊 `getResultItem()` 杩斿洖宸插惊鐜被鍨嬬殑 output銆?- 鍘熷洜锛歚this.result.copy()` 鍒涘缓绌?handler锛宻etTag 涓嶆仮澶嶅唴閮ㄧ墿鍝侊紱JEI 鏄剧ず榛樿 A 绫诲瀷鍥炬爣銆?- 鍏宠仈锛歝ommit 86a72e3
