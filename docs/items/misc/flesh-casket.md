# 琛€鑲夐瓟鍖?鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歝urio
- 娉ㄥ唽绫伙細ModItems.FLESH_CASKET_1 / 2 / 3
- Java 绫伙細FleshCasketItem (extends Item, implements ICurioItem)
- 棣栨鍒涘缓锛?026-06-23

## 璁捐鎰忓浘

Curios 楗板搧妲戒綅鐨勮嚜鍔ㄦ柦娉曞鍣ㄣ€傚唴缃?1/3/5 鏍硷紙鎸夐樁浣嶏級瀛樺偍 Focus锛屽懆鏈熻嚜鍔ㄦ柦娉曘€備笁闃跺惊鐜崌绾э細A鈫払鈫扖鈫扐锛岄厤鏂瑰湪 `data/goety/recipes/` 涓嬨€?

## 婕斿寲璁板綍

### 2026-06-23 鈥?鍒濆璁捐涓庢敞鍐?

- 鏀瑰姩锛氬垱寤?FleshCasketItem + FleshCasketItemCapability + FleshCasketItemHandler + FleshCasketContainer + FleshCasketEvents + AutoCastDummyWand + TargetingRegistry + TriggerType + ModItems 娉ㄥ唽 + 涓夐樁妯″瀷 + 涓夐樁寰幆鍗囩骇閰嶆柟 + ModConfig
- 鍘熷洜锛氶渶瑕佷竴涓彲浠ヨ嚜鍔ㄦ柦娉曠殑 Curios 瀹瑰櫒楗板搧锛岃В鏀炬墜鎸佹Ы浣?
- 鍏宠仈锛?4 new files, 4 modified (commit 0ec1b5d)

### 2026-06-23 鈥?淇 TargetingRegistry 鏃犳晥瀛楁寮曠敤

- 鏀瑰姩锛氫慨澶?3 涓棤鏁堢殑瀛楁寮曠敤
- 鍘熷洜锛氬垵濮嬩唬鐮佷腑寮曠敤杩樻湭娉ㄥ唽鐨勫瓧娈?
- 鍏宠仈锛歝ommit 6824236

### 2026-06-23 鈥?GUI 绯荤粺 + 瑁呭浜や簰閲嶅仛

- 鏀瑰姩锛氭坊鍔?FleshCasketContainer/Screen銆丮odMenuTypes銆丮odClientEvents锛沗use()` 鎵撳紑 GUI锛沗canEquipFromUse` 浠?`true` 鏀逛负 `isCrouching()`
- 鍘熷洜锛氬彸鍑绘墦寮€ GUI锛堜笉娼滆锛? 娼滆+鍙冲嚮瑁呭鍒?Curios 鏍忥紝鍖归厤 Goety FocusBag 浜や簰妯″紡
- 鍏宠仈锛欸UI 绾圭悊澶嶅埗鑷?Goety focus_bag.png

### 2026-06-23 鈥?Curios back tag 淇 + 涓枃鏈湴鍖?

- 鏀瑰姩锛歚back.json` 琛ュ叏 flesh_casket_a/b/c 鏉＄洰锛堜箣鍓嶅彧鍐欎簡 prototype锛夛紱閲嶅啓 zh_cn.json锛圲TF-8 淇锛岃ˉ鍏ㄦ潯鐩級锛沞n_us.json 娣诲姞 container title
- 鍘熷洜锛歜ack tag 缂哄け瀵艰嚧鍖ｅ瓙鏃犳硶瑁呭鍒拌儗閮?Curios 妲斤紱zh_cn.json 缂栫爜鎹熷潖
- 鏇存柊涓枃鍚嶏細鍙嶅嚮鈫掔泩钃勩€佸懡鐞嗗畧鎶も啋骞宠　銆佺粷鏈涒啋婵掓

### 2026-06-23 鈥?鑷姩鏂芥硶淇锛歍ier 1 EMPTY 榄旀潠闂

- 鏀瑰姩锛氭墍鏈?tier 缁熶竴浣跨敤 `AutoCastDummyWand`锛屼笉鍐嶄负 tier 1 浼?`ItemStack.EMPTY`
- 鍘熷洜锛歚rightStaff()` 瑕佹眰 staff 瀹炵幇 IWand 涓斿惈娉曟湳绫诲瀷锛汦MPTY 涓嶇鍚堬紝鎵€鏈夋硶鏈洿鎺ヤ腑鏂笉鎵ц
- 鍏宠仈锛歝ommit aade8a1

### 2026-06-23 鈥?鍐峰嵈绯荤粺閲嶆瀯 + 鑷敱绉婚櫎

- 鏀瑰姩锛歱er-focus 鐙珛鍐峰嵈 鈫?鍗曠墿鍝佸叏灞€鍐峰嵈锛堜互鏈€闀挎硶鏈喎鍗翠负鍑嗭級锛涙坊鍔?damage bar 鍙鍖栵紱`canUnequip` 濮嬬粓杩斿洖 true
- 鍘熷洜锛氱帺瀹跺弽棣堟棤娉曠洿瑙傜湅鍒板喎鍗淬€佸彇涓嶄笅楗板搧锛涘彧鏈夊叏閮ㄨ仛鏅跺喎鍗村畬鎵嶈兘鍐嶆瑙﹀彂
- 鍏宠仈锛歝ommit 0548c30, f48d756

### 2026-06-29 鈥?閲嶆瀯涓?3 闃剁嚎鎬у崌绾?+ NBT 瑙﹀彂绫诲瀷鍒囨崲

- 鏀瑰姩锛? 涓彉浣擄紙A1~C3锛夆啋 3 涓樁浣嶇墿鍝侊紙flesh_casket_1/2/3锛夛紱瑙﹀彂绫诲瀷浠?enum 绫婚噸鏋勪负 NBT 瀛楁锛涙坊鍔犺瘏鍜掓敞鍏ュ櫒寰幆閰嶆柟锛圓鈫払鈫扖鈫扐锛宍FleshCasketInfuserRecipe`锛夛紱鍒涘缓鑷畾涔変华寮?`UpgradeCasketRitual` 澶嶅埗 NBT锛涘垹闄?12 涓棫妯″瀷 JSON + 9 涓棫閰嶆柟 + 2 涓棫鐗╁搧寮曠敤
- 鍘熷洜锛? 涓彉浣撳お鍐椾綑锛岀嚎鎬у崌绾?+ NBT 鍒囨崲鏇寸畝娲侊紱NBT 淇濈暀淇濊瘉鍗囩骇涓嶆帀鑱氭櫠
- 鍏宠仈锛欶leshCasketInfuserRecipe.java, FleshCasketInfuserRecipeSerializer.java, UpgradeCasketRitual.java, ModRituals.java, ModItems 鍘?9鈫?锛涙柊澧?6 涓厤鏂?JSON锛沜ommit b10c2c8

### 2026-06-29 鈥?浠紡閰嶆柟淇锛欸oety 鐗╁搧 ID 绾犳 + 鍗犱綅绾圭悊

- 鏀瑰姩锛氫慨姝?3 涓?Upgrade 閰嶆柟 JSON 涓?6 涓敊璇?Goety 鐗╁搧 ID锛坧ale_metal_ingot鈫抪ale_steel_ingot, dark_metal_ingot鈫抎ark_ingot, night_core鈫抙eart_of_the_night, unholy_cloth鈫抲nholy_fabric, large_focus_bag鈫抐ocus_pack, pure_blood鈫抲nholy_blood锛夛紱鍒涘缓 3 涓崰浣?PNG 绾圭悊锛坒lesh_casket_1/2/3.png锛?
- 鍘熷洜锛氶厤鏂逛腑 item ID 涓嶅瓨鍦ㄥ鑷?JEI 涓嶆樉绀猴紱缂哄け绾圭悊瀵艰嚧鐗╁搧鏍忔樉绀虹传榛戝潡
- 鍏宠仈锛歝ommit [current session]

### 2026-07-02 鈥?瑙﹀彂绫诲瀷瑙嗚鍙樹綋 + 鐗╁搧鍗囩骇淇濈暀鍐呭淇

- 鏀瑰姩锛?
  1. 鏂板 9 涓Е鍙戠被鍨嬪瓙妯″瀷 JSON + 9 寮犺创鍥撅紙flesh_casket_{1/2/3}_{fullness/balance/deathbed}.png锛夛紝鏍规嵁 NBT trigger_type 鍒囨崲鐗╁搧澶栬
  2. FleshCasketItem 瑕嗙洊 `getName()` 杩斿洖 `item.GoetyMech.flesh_casket_{tier}.{suffix}` 缈昏瘧閿紝鐗╁搧鍚嶉殢瑙﹀彂绫诲瀷鍙樺寲
  3. 娣诲姞 ItemProperty 鍦?client 渚ф牴鎹?trigger_type 閫夋嫨瀛愭ā鍨?
  4. FleshCasketInfuserRecipe.assemble()锛氱敤 ITEM_HANDLER capability 閫愭Ы澶嶅埗鑱氭櫠鏍堬紝鏇夸唬浠呭鍒?NBT tag 鐨勬棫閫昏緫锛岄伩鍏嶅崌绾ф椂娓呯┖鍐呭
  5. 瑕嗗啓 getResultItem() 杩斿洖宸插惊鐜Е鍙戠被鍨嬬殑 output锛岃 JEI 鏄剧ず姝ｇ‘鐨勫崌绾т骇鐗╁浘鏍?
- 鍘熷洜锛氳Е鍙戠被鍨嬩粎鏈?NBT 鏃犺瑙夊弽棣堬紱鍗囩骇浠紡浼氭竻绌哄凡瀛樻斁鐨勮仛鏅讹紱JEI 鏄剧ず鏈惊鐜殑浜х墿锛堝缁堟樉绀?A 绫诲瀷锛?
- 鍏宠仈锛歝ommit 3a79aa6, 6b4b32d, d3064d1, 86a72e3

### 2026-06-23 鈥?涓変釜 Bug 淇

- 鏀瑰姩锛?
  1. AutoCastDummyWand 鎺掗櫎 FROST/STORM/WILD/NETHER/ABYSS 鈫?鍙敜涓嶅啀寮哄埗鍙樺啺绯伙紝鍥為€€缇ょ郴鍒ゅ畾
  2. `tryAutoCast` 涓?tier >= 2 鍔?+2 potency锛宼ier >= 3 鍔?+4 potency + +1 duration 鈫?楂樼骇鍖ｅ瓙涓嶅啀鍏ㄥ嚭浣庣骇鎬?
  3. `findFirstCurio` 鈫?`findAllFleshCaskets` 閬嶅巻鍏ㄩ儴 Curios 妲?鈫?澶氫釜鍖ｅ瓙鍚屾椂鐢熸晥
- 鍘熷洜锛氱帺瀹跺弽棣堜笁涓棶棰?
- 鍏宠仈锛歝ommit 7a632bf

### 2026-07-02 鈥?JEI 涓夊彉鎹㈡樉绀?+ 閲嶅鏉＄洰淇

- 鏀瑰姩锛欽EI 璇呭拻娉ㄥ叆鍣ㄥ垎绫讳腑娉ㄥ唽 6 鏉℃樉绀洪厤鏂癸紙3 tier 脳 2 鍙樻崲锛欱鈫扖, C鈫扐锛夛紝閰嶅悎 Goety 鑷姩娉ㄥ唽鐨?A鈫払 鏉＄洰褰㈡垚瀹屾暣寰幆銆傜Щ闄ゅ啑浣欑殑 A鈫払 鏄剧ず閰嶆柟銆?
- 鍘熷洜锛氱帺瀹跺弽棣堝彧鏄剧ず婊＄泩鈫掑钩琛★紙A鈫払锛変竴椤癸紝缂哄皯骞宠　鈫掓繏姝伙紙B鈫扖锛夊拰婵掓鈫掓弧鐩堬紙C鈫扐锛?
- 鍏宠仈锛歝ommit 14dfb1a, 86a72e3

### 2026-07-02 鈥?Tier 鍒嗙骇鐪熸硶鏉?+ Offhand fix + Z 閿?GUI

- 鏀瑰姩锛?
  1. AutoCastDummyWand 閲嶆瀯锛氭棤鍙傛瀯閫犲叏绫诲瀷锛圴OID, NONE, NECROMANCY, ILL, GEOMANCY, WIND锛夛紝鏂板鏈夊弬鏋勯€犮€傛敞鍐?AUTO_CAST_DUMMY_WAND_T1锛圢ONE-only锛夈€?
  2. `tryAutoCast()` 鎸?tier 閫夋硶鏉栵細tier 1 鐢?T1锛圢ONE-only锛夛紝tier 2/3 鐢ㄥ叏绫诲瀷娉曟潠銆?
  3. Offhand fix锛氬皢鑱氭櫠瑁呭叆鐪熸硶鏉?handler锛屼复鏃舵斁鍓墜锛孲pellResult 鍚庢仮澶嶃€傝В鍐?`WandUtil.findFocus()` 鎵句笉鍒拌仛鏅跺鑷?SquadFocus/StressFocus 绛?silent return 鐨勯棶棰樸€?
  4. Z 閿粦瀹氾紙`key.GoetyMech.open_flesh_casket`锛? C2SOpenFleshCasketPacket锛氭墜鎸佸專瀛愭寜 Z 鎵撳紑 GUI銆?
- 鍘熷洜锛氫綆闃跺專瀛愪笉搴斿彫鍞よ繃澶氭殫鍗紙rightStaff 濂栧姳锛夛紱SquadFocus/StressFocus 鑷姩鏂芥硶琚?instanceof 瀹堝崼闃绘柇锛涙墜鎸佹棤娉曞揩閫熷紑 GUI銆?
- 鍏宠仈锛歝ommit 56b1c30, 97a5ebe, a1d0b11
