# 鐐瑰皢鍙?鈥?GoetyMech

## 韬唤

- 绫诲瀷锛歜lock
- 娉ㄥ唽绫伙細ModBlocks.TIER1/2/3_SQUAD_BARRACKS
- Java 绫伙細Tier1/2/3SquadBarracksBlock (extends SquadBarracksBlock)
- 鏂瑰潡瀹炰綋锛歋quadBarracksBlockEntity
- 棣栨鍒涘缓锛?026-06-14

## 璁捐鎰忓浘

涓夐樁鍗囩骇鐨勫鍔熻兘鏂瑰潡锛氱紪闃熼厤缃?鈫?娑堣€楄儦鑳?鈫?鎵归噺鍙敜銆傛敮鎸佹満姊板姩鍔涘叏鏂瑰悜杈撳叆銆丆hunk 甯稿姞杞姐€佽法缁村害鎿嶄綔銆?

## 婕斿寲璁板綍

### 2026-06-15 鈥?鍒濆璁捐涓庢敞鍐?

- 鏀瑰姩锛氬垱寤?Tier1/2/3SquadBarracksBlock + SquadBarracksBlockEntity + SquadBarracksMenu + SquadBarracksScreen銆傚疄鐜颁笁闃跺崌绾с€佺紪闃?GUI銆佹満姊板姩鍔涘吋瀹广€丆hunk Ticket銆佽法缁村害鎿嶄綔
- 鍘熷洜锛氶渶瑕佷竴涓泦涓鐞嗙紪闃熷彫鍞ょ殑浜や簰鐐癸紝浠ｆ浛闆舵暎鐨勫彸閿崟娆″彫鍞?
- 鍏宠仈锛歰penspec squad-summon-system

### 2026-06-16 鈥?Codebase Remediation: SquadChunkTickets 绉诲姩

- 鏀瑰姩锛歋quadChunkTickets 浠?`common/block/entity/` 绉昏嚦 `common/utils/`
- 鍘熷洜锛氳绫绘槸宸ュ叿绫伙紝涓嶅睘浜庢柟鍧楀疄浣撻€昏緫
- 鍏宠仈锛歰penspec codebase-remediation

### 2026-06-26 鈥?鏇挎崲 armory platform 妯″瀷 + 鎷嗗垎涓夐樁鐙珛绾圭悊

- 鏀瑰姩锛氱敤妗岄潰 armory_platform_pack 鐨?Blockbench 瀵煎嚭鏇挎崲妯″瀷锛岃楗板厓绱犵儤鐒欏埌绾圭悊涓Щ闄?3D vein/rivet/panel 鍏冪礌銆倀ier2/tier3 鍚?5 寮犺创鍥炬斁鍏ョ嫭绔嬪瓙鏂囦欢澶癸紝涓変釜闃跺悇鑷嫭绔嬬殑 blockstate/item/model锛堝凡鏀逛负鎸囧悜鐙珛鏂囦欢鑰岄潪鍏变韩锛?
- 鍘熷洜锛氱敤鎴锋洿鏂颁簡璧勬簮鍖呰璁★紝涓夐樁闇€瑕佷笉鍚岀汗鐞?
- 鍏宠仈锛氱敤鎴锋寚浠ゆ浛鎹?

### 2026-06-26~07-02 鈥?妯″瀷杩唬淇锛? 杞級

- 鏀瑰姩锛?
  1. 瀵圭О鍖?south 闈?+ 淇 z-fighting锛堜袱涓浉閭?face 鍦ㄥ悓涓€骞抽潰绔炰簤娓叉煋锛?
  2. UV 姘村钩缈昏浆淇 13 涓暅鍍?element锛? West + 7 South锛?
  3. 1.20.1 鍏煎 rotation 鏍煎紡淇锛堢姝?-180锛屾敼涓?0锛?
  4. Core_body / pillar / anvil_station UV 绮剧粏璋冩暣
  5. 7 寮犺创鍥炬洿鏂帮紙armory_anvil/body/frame/vein + 鏂板 blacklight_virus_overlay.png锛?
  6. 鏈€缁?clean export 鍘婚櫎瑁呴グ鎬?3D 鍏冪礌锛屽叏閮ㄧ儤鐒欏埌绾圭悊
- 鍘熷洜锛欱lockbench 瀵煎嚭鍦?1.20.1 涓婄殑鍏煎鎬ч棶棰樺拰瑙嗚鐟曠柕閫愭淇
- 鍏宠仈锛歝ommit a47b006, 549d6fe, bbf035e, 0ed93e1, c5f74cd, 49a279c, 2bccdd9
