# GoetyMech Mod 鈥?Domain Context

## 馃毇 绂佷护锛堟墍鏈夋搷浣滃墠蹇呴』瀵圭収锛?

**1. 鍥炲璇█**锛氭棤璁轰綍绉嶆儏鍐碉紙鍖呮嫭涓婁笅鏂囧帇缂┿€佹柇鐐圭画鎺ャ€佺姸鎬佸帇缂┿€乸lan/build 妯″紡鍒囨崲锛夛紝鎵€鏈夊彂閫佺粰鐢ㄦ埛鐨勬枃鏈俊鎭?*蹇呴』浣跨敤涓枃**銆備唬鐮佸唴瀹广€佹棩蹇楄緭鍑恒€佹妧鏈湳璇繚鎸佸師鏂囥€?

**2. 瀛樻。淇濇姢**锛氭湭缁忕敤鎴锋槑纭鍙紝绂佹浠ヤ换浣曠悊鐢便€佷换浣曟柟寮忓垱寤恒€佷慨鏀规垨鍒犻櫎浠ヤ笅鍐呭锛?
- **瀛樻。鏂囦欢**锛圡inecraft saves / world data / player data / region files锛?
- **浠讳綍杩愯鏃舵暟鎹枃浠?*锛堝 `level.dat`銆乣playerdata/`銆乣region/` 绛夛級
- 涓婅堪琛屼负鐨勮Е鍙戝寘鎷絾涓嶉檺浜庯細娴嬭瘯銆佹竻鐞嗚祫婧愩€侀噸缃幆澧冦€佷慨澶?bug銆?涓嶅皬蹇?瑕嗙洊

濡傞渶鎿嶄綔瀛樻。鎴栬繍琛屾椂鏁版嵁锛屽繀椤诲厛璇㈤棶鐢ㄦ埛骞跺彇寰楁槑纭鍙€?

**3. 鍒犻櫎蹇収淇濇姢**锛氫换浣曟枃浠?鐩綍鍒犻櫎鎿嶄綔锛屾墽琛屽墠蹇呴』鑷姩鍒涘缓 Git 鍒嗘敮蹇収锛堝垎鏀悕 `backup-N`锛孨 閫掑 0~9锛岃秴闄愯嚜鍔ㄥ垹闄ゆ渶鏃э級銆傚垱寤哄揩鐓у悗鍛婄煡鐢ㄦ埛鍒嗘敮鍚嶏紝鍐嶆墽琛屽垹闄ゃ€傚弬瑙?`~/.config/opencode/instructions/delete-guard.md`銆?

**4. 姣忔淇敼鑷姩鎻愪氦**锛歚edit` / `write` 鎿嶄綔瀹屾垚鍚庣珛鍗虫墽琛?`git commit --author="opencode <autocommit>" -m "[auto] <tool>: <filepath> | <diff>"`锛岀敤浜庣簿鍑嗗洖閫€鍗曟鏀瑰姩銆傛煡鐪嬶細`git log --author=opencode --oneline`锛屽洖閫€锛歚git revert <hash>`銆?

## 鈿?鏈€楂樹紭鍏堢骇锛氬墠缃ā缁勬簮鐮佽矾寰?

**鎵€鏈夊墠缃ā缁勭殑瀹屾暣婧愪唬鐮佸湪 `D:\Libcode\` 鐩綍涓嬶紝鍙洿鎺ユ煡闃呰€岄潪鍙嶇紪璇戙€?*

**鍦ㄥ弽缂栬瘧浠讳綍绫讳箣鍓嶏紝蹇呴』浼樺厛鍒颁互涓嬬洰褰曟煡鎵炬簮鐮侊細**

| 婧愮爜鐩綍 | 鍐呭 |
|----------|------|
| `D:\Libcode\Goety-2-1.20\Goety-2-1.20\` | Goety 2.x 瀹屾暣鍘熷鐮?|
| `D:\Libcode\Create-mc1.20.1-dev\Create-mc1.20.1-dev\` | Create 妯＄祫鍘熷鐮?|
| `D:\Libcode\ending_library-1.20.1\` | 鏃堕棿鍋滄绯荤粺鏀彺搴?(ending_library) |

**瑙勫垯**锛氫换浣曟椂鍊欓渶瑕佷簡瑙ｆ煇涓被鐨勮涓恒€佹煡鐪嬫柟娉曠鍚嶃€佹垨璋冭瘯浜や簰閫昏緫锛屽厛鍘?`D:\Libcode\` 瀵瑰簲鐩綍涓嬫悳绱㈡簮鏂囦欢銆備粎鍦?`D:\Libcode\` 涓壘涓嶅埌鏃舵墠浣跨敤鍙嶇紪璇戙€?

---

## Project Identity

| 椤?| 鍊?|
|----|-----|
| Mod ID | `GoetyMech` |
| 涓诲寘 | `com.GoetyMech.addon` |
| 涓诲叆鍙?| `com.GoetyMech.addon.GoetyMechMod` |
| MC 鐗堟湰 | 1.20.1 / Forge 47.4.0 / Java 17 |
| 鏄犲皠 | Parchment 2023.09.03 |
| 鍓嶇疆 | Curios 5.3.4, Goety 2.x, Create 6.0.8 |
