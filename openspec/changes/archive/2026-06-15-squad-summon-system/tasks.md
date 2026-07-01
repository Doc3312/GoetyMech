## 1. 娉ㄥ唽鍩虹璁炬柦

- [x] 1.1 鏂板 `ModMenuTypes.java` 鈥?娉ㄥ唽 ContainerType (DeferredRegister<MenuType<?>>)
- [x] 1.2 `ModConfig.java` 鈥?鏂板閰嶇疆椤癸細SQUAD_SUMMON_COOLDOWN, SQUAD_TIER2_DISCOUNT (0.5), SQUAD_TIER3_DISCOUNT (0.1), SQUAD_LIFESPAN (6000)
- [x] 1.3 `ModBlocks.java` 鈥?娉ㄥ唽 SquadBarracksBlock + BlockEntityType
- [x] 1.4 `ModItems.java` 鈥?娉ㄥ唽 SquadFocus + SquadBarracks BlockItem
- [x] 1.5 `GoetyMechMod.java` 鈥?娉ㄥ唽 ModMenuTypes.CONTAINER_TYPE
- [x] 1.6 `ModClientEvents.java` 鈥?娉ㄥ唽 SquadBarracksScreen (MenuScreens.register)
- [x] 1.7 娣诲姞 lang key锛氭柟鍧楀悕銆佽仛鏅跺悕銆丟UI 鏍囬銆乼ooltip 鏂囨湰銆佹彁绀轰俊鎭?
## 2. 鐐瑰皢鍙版柟鍧?(SquadBarracksBlock)

- [x] 2.1 鍒涘缓 `SquadBarracksBlock` 鈥?extends BaseEntityBlock, IntegerProperty TIER (1-3), 鍙抽敭浜や簰
- [x] 2.2 鍙抽敭鎵嬫寔鍗囩骇鏉愭枡 鈫?鍗囬樁閫昏緫锛堣媿鐧介噾灞炩啋浜岄樁銆侀粦鏆楅噾灞炩啋涓夐樁銆佷笅鐣屽悎閲戞棤鏁堬級
- [x] 2.3 鍙抽敭绌洪棽鎵?鈫?鎵撳紑 GUI锛圢etworkHooks.openScreen锛?- [x] 2.4 鍙抽敭鎸?SquadFocus 鈫?瑙﹀彂缁戝畾锛堝鎵樼粰鑱氭櫠锛?- [x] 2.5 鐮村潖鏃舵帀钀芥墍鏈夊唴瀹?+ 绉婚櫎 chunk ticket
- [x] 2.6 鏀剧疆鏃惰缃嫢鏈夎€?+ 娉ㄥ唽 chunk ticket

## 3. 鐐瑰皢鍙版柟鍧楀疄浣?(SquadBarracksBlockEntity) [宸蹭慨鏀筣

- [x] 3.1 鍒涘缓 `SquadBarracksBlockEntity` 鈥?瀹炵幇 WorldlyContainer锛堝熀纭€缁撴瀯淇濈暀锛?- [x] 3.2 瀹瑰櫒澶у皬锛歴quad 浣嶈繑鍥炵┖ ItemStack锛堣櫄閰嶇疆锛夛紝reserve 鎵╄嚦 27 鏍硷紙3脳9锛?- [x] 3.3 Squad slot 涓嶅啀鎺ュ彈鐗╁搧锛汻eserve slot 鎺ュ彈 MatureEmbryo
- [x] 3.4 `getSquadComposition()` 鈥?鏀逛负璇?`List<ResourceLocation> squadConfig`
- [x] 3.5 `consumeSquad(ServerLevel, Player)` 鈥?鏍规嵁 squadConfig 浠?reserve 鎵ｅ彇
- [x] 3.6 宸茬Щ闄?`restockFromReserve()`
- [x] 3.7 鍏ㄦ柟鍚?IItemHandler锛堜繚鐣欙級
- [x] 3.8 鎷ユ湁鑰呭瓨鍌?+ 鏉冮檺妫€鏌ワ紙淇濈暀锛?- [x] 3.9 NBT 鎸佷箙鍖栵細鏂板 SquadConfig ListTag 鎸佷箙鍖?- [x] 3.10 缃戠粶鍚屾锛歴quadConfig 闅?getUpdatePacket 鍙戦€?- [x] 3.11 `setSquadConfigSlot(int, ResourceLocation)`
- [x] 3.12 `clearSquadConfigSlot(int)`

## 4. 鐐瑰皢鍙?GUI [宸蹭慨鏀筣

- [x] 4.1 `SquadBarracksMenu` 鈥?閰嶇疆浣?locked slot锛坄mayPlace=false`, `mayPickup=false`锛?- [x] 4.2 閰嶇疆浣?6/12/18 鏍煎彧鏄剧ず锛涘偍澶囦綅 3脳9锛?7 鏍硷級锛岃捣鐐?x=8
- [x] 4.3 閰嶇疆浣嶄笉鎺ュ彈鐗╁搧锛涘偍澶囦綅 maxStack=16 鍙帴鍙?MatureEmbryo
- [x] 4.4 鏁版嵁鍚屾锛歴quadConfig 閫氳繃 BE 鍚屾
- [x] 4.5 `SquadBarracksScreen` 淇濈暀
- [x] 4.6 閰嶇疆浣嶆覆鏌擄細浠?`barracks.getSquadConfigSlot()` 璇?entity type 鏄剧ず鍚?- [x] 4.7 `mouseClicked()` 鈥?閰嶇疆浣嶇偣鍑?+ 鎵嬫寔鑳氳儙 鈫?鍙戝寘璁剧疆
- [x] 4.8 绾圭悊寮曠敤锛氭牴鎹?tier 鍔犺浇 `squad_barracks_t1/2/3.png`
- [x] 4.9 `SquadConfigC2SPacket` 鈥?C2S 鍖呭凡鍒涘缓骞舵敞鍐?
## 5. 鍏靛洟鑱氭櫠 (SquadFocus)

- [x] 5.1 鍒涘缓 `SquadFocus` 鈥?extends MagicFocus锛岀粦瀹?SquadSpell
- [x] 5.2 鍙抽敭鐐瑰皢鍙?鈫?缁戝畾锛歂BT 瀛?BlockPos + dimension + 缂撳瓨 squad composition
- [x] 5.3 Shift+鍙抽敭绌烘皵 鈫?娓呴櫎缁戝畾
- [x] 5.4 `onLeftClickEntity` 鈥?绌哄疄鐜帮紙涓嶅仛 TroopFocus/CallFocus 鐨勫疄浣撶粦瀹氾級
- [x] 5.5 瑕嗗啓 `appendHoverText` 鈥?鏄剧ず缁戝畾鐘舵€併€佺紦瀛橀樀瀹广€佹瘡鍗曚綅娑堣€楄鏄?- [x] 5.6 `defaultSoulCost() = 0`锛堝疄闄呮秷鑰楀湪 SpellResult 涓鐞嗭級

## 6. 鍏靛洟娉曟湳 (SquadSpell)

- [x] 6.1 鍒涘缓 `SquadSpell` 鈥?extends Spell
- [x] 6.2 `SpellResult()` 鈥?鏍稿績娴佺▼锛堝凡閫傞厤鏂?consumeSquad 绛惧悕锛夛細
  - 瑙ｆ瀽鑱氭櫠 NBT 鈫?BlockPos + dimension
  - 鑾峰彇 ServerLevel锛堜笁闃惰法缁村害 鈫?server.getLevel(dim)锛?  - 楠岃瘉鏂瑰潡瀛樺湪 + 鏉冮檺
  - 璇诲彇 tier 鈫?鎶樻墸鐜?  - 璇诲彇 squad composition锛堜粠 squadConfig锛?鈫?璁＄畻鎬荤伒榄傛秷鑰?  - 鏍￠獙鐏甸瓊 鈫?`SEHelper.decreaseSouls()`
  - 璋冪敤 `blockEntity.consumeSquad()` 鈥?**鏀逛负浠?reserve 鎵ｅ彇**
  - 鍦嗗舰鎺掑垪 鈫?閬嶅巻 spawn锛堝鐢?MinionSpawnHelper.spawnMinion锛?  - 姣忎釜瀹炰綋 `setHasLifespan(true)` + `setLifespan(config.lifespan)`
  - 璁剧疆鍐峰嵈
- [x] 6.3 `defaultSoulCost()` 杩斿洖 0
- [x] 6.4 `defaultSpellCooldown()` 浠?ModConfig 璇诲彇
- [x] 6.5 鍦嗗舰浣嶇疆璁＄畻锛歚getCirclePositions(totalCount, radius, center)` 鍧囧寑鍒嗗竷

## 7. 闄愭椂浠嗕粠 (Timed Servants)

- [x] 7.1 鍦?`MinionSpawnHelper.spawnMinion()` 鏈熬鍔狅細`mob.setHasLifespan(true); mob.setLifespan(config.lifespan)`
- [x] 7.2 鍙€夛細`SquadFocus` 鍙敜鏃堕澶栬缃紝鍖哄垎鏅€氭垚鐔熻儦鑳庯紙鏃犻檺鏃讹級鍜岃仛鏅跺彫鍞わ紙5鍒嗛挓锛?
## 8. Chunk 甯稿姞杞?
- [x] 8.1 鍒涘缓 `TicketType<BlockPos> SQUAD_BARRACKS` 鐢ㄤ簬 chunk 鍔犺浇
- [x] 8.2 鏂瑰潡鏀剧疆鏃讹細`serverLevel.getChunkSource().addRegionTicket(TICKET_TYPE, chunkPos, 2, blockPos)`
- [x] 8.3 鏂瑰潡鐮村潖鏃讹細`serverLevel.getChunkSource().removeRegionTicket(TICKET_TYPE, chunkPos, 2, blockPos)`
- [x] 8.4 鏈嶅姟绔叧闂椂娓呯悊鎵€鏈夋畫鐣?ticket锛圵orldTick 浜嬩欢鎴?onChunkUnloaded 妫€鏌ワ級

## 9. 楠岃瘉

- [x] 9.1 閲嶇紪璇戯細`gradlew compileJava` 鏃犳姤閿?- [ ] 9.2 鎵嬫寔鑳氳儙鐐瑰嚮閰嶇疆浣?鈫?璁剧疆鎴愬姛銆佽儦鑳庤繑鍥炪€佹樉绀?spawn egg 鍥炬爣
- [ ] 9.3 绌烘墜鐐瑰嚮閰嶇疆浣?鈫?娓呴櫎閰嶇疆
- [ ] 9.4 鍙敜娴嬭瘯锛氬叏婊℃棤鎻愮ず/閮ㄥ垎榛勫瓧/闆舵秷鑰楃孩瀛?- [ ] 9.5 绾圭悊楠岃瘉锛氬乏鍙冲竷灞€锛岄厤缃綅(3脳6) spawn egg + 鍌ㄥ浣?6脳3)瀹炵墿
- [ ] 9.6 鍌ㄥ浣嶆墿瀹?6脳3=18 鏍奸獙璇?- [ ] 9.7 鍗囬樁娴嬭瘯锛?闃垛啋6瑙ｉ攣銆?闃垛啋12瑙ｉ攣銆?闃垛啋鍏?8瑙ｉ攣
- [ ] 9.8 璺ㄧ淮搴?鎶樻墸/鏃堕檺/婕忔枟娴嬭瘯锛堢敱鐢ㄦ埛娴嬭瘯锛?