# bbmodel 鍧愭爣缈昏浆 + 澶氭鐢熸垚澶辫触

status: verified

## 鑳屾櫙

涓?crazed_illager 瀹炰綋鐢熸垚 Blockbench `.bbmodel` 鏂囦欢锛坄src/main/resources/assets/GoetyMech/crazed_illager.bbmodel`锛夛紝鐢ㄤ簬绾圭悊缁樺埗鍙傝€冦€?

## 浜嬫晠涓€锛氭枃浠舵斁閿欒矾寰?

鍒濆鍐欏叆 `assets/GoetyMech/`锛堥」鐩牴鐩綍锛夛紝姝ｇ‘璺緞涓?`src/main/resources/assets/GoetyMech/`銆?

## 浜嬫晠浜岋細Y-flip PowerShell 鑴氭湰鎹熷潖

妯″瀷鍦?Blockbench 涓樉绀哄€掔疆锛圡inecraft 瀹炰綋妯″瀷 Y+ = 鍚戜笅锛孊lockbench Y+ = 鍚戜笂锛夈€?

绗竴娆″皾璇曪細`ConvertFrom-Json` 鈫?淇敼鍧愭爣 鈫?`ConvertTo-Json -Depth 10`銆傜粨鏋?`ConvertTo-Json` 宓屽鎴柇锛岃緭鍑?0 瀛楄妭銆?

**鏁欒**锛歅owerShell 鐨?`ConvertTo-Json` 澶勭悊娣卞眰宓屽瀵硅薄涓嶅彲闈狅紝娣卞害鍙傛暟缁忓父鏃犳晥銆傚簲浣跨敤 .NET 鐨?`Newtonsoft.Json` 鎴栫洿鎺ヨ浆 Python/Node.js銆?

## 浜嬫晠涓夛細PowerShell 鏁扮粍杩愮畻鑴氭湰澶辫触

绗簩娆″皾璇曪細缂栧啓 PS 鍑芥暟璁＄畻鍧愭爣鍋忕Щ锛屼絾 `$ox+$cx` 鍦?PowerShell 涓鏁扮粍鎵ц鐨勬槸鎷兼帴鎿嶄綔锛堜笉鏄€愬厓绱犲姞娉曪級锛屽鑷?`absCube` 鍑芥暟杩斿洖 `$null`銆?

**鏁欒**锛歅owerShell 鏁扮粍绠楁湳涓嶆槸閫愬厓绱犵殑銆傛暟鍊艰绠楃敤 Python/Node.js 鏇村彲闈犮€?

## 鏈€缁堟柟妗?

Node.js 鑴氭湰 `.opencode/scripts/gen-bbmodel.mjs` 鎴愬姛鐢熸垚 16 涓厓绱狅紙7 涓韩浣撻儴浠?+ 1 涓敮鐗囦腑蹇?+ 8 涓敮榻匡級锛孻Z 骞抽潰閿墖锛屽潗鏍囨纭€?

## 鍏抽敭鍧愭爣楠岃瘉

| 閮ㄤ欢 | BB Y-up 鑼冨洿 | 璇存槑 |
|------|-------------|------|
| body | y=-12 ~ y=0 | 韬共 |
| head | y=0 ~ y=8 | 澶撮儴锛堝湪韬共涔嬩笂锛?|
| hat | y=0 ~ y=8, inflate=0.5 | 甯藉瓙锛堝寘瑁瑰ご閮級 |
| legs | y=-24 ~ y=-12 | 鑵匡紙鍦ㄨ函骞蹭箣涓嬶級 |
| blade_center | y=0~8, z=-8~0 | YZ 骞抽潰鍦嗙洏 |
| teeth | 鍗婂緞 4.5 鐨?YZ 鐜?| 鍥寸粫 blade_center |

## 鐘舵€?

bbmodel 鐢熸垚鎴愬姛銆侼ode.js 鐢熸垚鑴氭湰淇濈暀鍦?`.opencode/scripts/gen-bbmodel.mjs`銆?
