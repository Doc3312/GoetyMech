# 鏃堕棿鑱氭櫠琚嬶紙Time Focus Pouch锛夋灦鏋勮鏄?

> **鏈墿鍝佺殑鏃跺仠浠ｇ爜鍙傝€冭嚜 [EndLibrary](https://github.com/meglevel/ending_library) 鍜屾ⅵ骞荤粓鐒夈€?*  
> 鏈嶅姟鍣ㄧ鏃跺仠寮曟搸锛坄TimeStopUtils`銆乣TimeStopSavedData`銆丼2C 骞挎挱鍗忚锛夈€? 
> 瀹㈡埛绔?Mixin 灞傦紙`MinecraftMixin` HEAD 娉ㄥ叆銆乣ClientLevelMixin` tick 鍙栨秷銆乣Blaze3DMixin` 鏃堕棿鏇挎崲锛夈€? 
> 浠ュ強 `TimeContext.Client.timer` 鐙珛鏃堕挓鏈哄埗鍧囨簮鑷?EndLibrary銆? 
> 鏈」鐩湪姝ゅ熀纭€涓婂疄鐜颁簡 GUI 鍐荤粨娴佺▼銆乣InputEvent.Key` 鏉鹃敭妫€娴嬨€乣freezePacketDelay` 鍔ㄧ敾寤惰繜绛夊師鍒涢€昏緫銆?

## 姒傝堪

鏃堕棿鑱氭櫠琚嬫槸涓€涓壒娈婄殑鑱氭櫠琚嬶紙缁ф壙 Goety 鐨?`FocusPack`锛夛紝鎸佹湁瀹冩椂鎵撳紑 Goety 鐨勫緞鍚戣彍鍗?鑱氭櫠琚?鑱氭櫠鍖?GUI 浼?*瑙﹀彂鍏ㄥ眬鏃跺仠**锛岃鐜╁鍦ㄩ潤姝笘鐣屼腑瀹夊叏鍒囨崲鑱氭櫠銆?

| 鐗╁搧 | 绫?| 瑙﹀彂鏂瑰紡 |
|------|-----|---------|
| 鏃堕棿鑱氭櫠琚?| `TimeFocusPouch extends FocusPack` | 鎵撳紑 `FocusRadialMenuScreen`锛堝緞鍚戣彍鍗曪級 |
| 鏃堕棿鑱氭櫠琚?| `TimeFocusPouch extends FocusPack` | 鎵撳紑 `FocusPackScreen`锛堣仛鏅惰锛?|
| 鏃堕棿鑱氭櫠琚?| `TimeFocusPouch extends FocusPack` | 鎵撳紑 `FocusBagScreen`锛堣仛鏅跺寘锛?|

**鏍稿績闇€姹?*锛?
- 鎵撳紑 GUI 鏃跺喕缁撴墍鏈夊疄浣?绮掑瓙/澹伴煶/鏃堕棿
- 鐜╁浠嶈兘鎿嶄綔 GUI锛堥€夋嫨鑱氭櫠锛?
- 鍏抽棴 GUI 鏃舵仮澶嶄竴鍒?
- 寰勫悜鑿滃崟鐨勬墦寮€鍔ㄧ敾锛?.5 ticks锛夎鏈夎冻澶熸椂闂村畬鎴愭墠鍐荤粨

---

## 鏋舵瀯鍒嗗眰

### 1. 鐗╁搧灞傦紙Item锛?

**`TimeFocusPouch.java`** 鈥?鏈€绠€鍗曠殑绫伙紝浠呯户鎵?`FocusPack` 骞堕噸鍐欏悕瀛椼€傚叏閮ㄩ€昏緫鍦ㄤ簨浠剁郴缁熶腑銆?

### 2. 瀹㈡埛绔簨浠跺眰锛團orge Event Bus锛?

**`ModClientForgeEvents.java`** 鈥?鏍稿績鍗忚皟鑰咃紝澶勭悊浠ヤ笅浜嬩欢锛?

#### 2.1 鎵撳紑 GUI 鈥?`onScreenOpen(ScreenEvent.Opening)`

```
鐜╁鎵撳紑 GUI
  鈹溾攢 hasTimeFocusPouch()?    鈫?妫€鏌ヨ儗鍖?涓绘墜/鍓墜/Curios 鑵板甫妲?
  鈹?  鈹斺攢 缂撳瓨 20 ticks锛岄伩鍏嶉珮棰戦亶鍘?
  鈹溾攢 FocusRadialMenuScreen?  鈫?radMenuOpen=true, freezePacketDelay=3
  鈹斺攢 FocusPack/BagScreen?    鈫?anyPouchOpen=true, C2S鍐荤粨鍖呯珛鍗冲彂閫?
```

**寰勫悜鑿滃崟寤惰繜 3 ticks 鐨勫師鍥?*锛歚FocusRadialMenuScreen` 鐨勬墦寮€鍔ㄧ敾闇€瑕?2.5 ticks锛坄OPEN_ANIMATION_LENGTH`锛夛紝闇€瑕佺瓑鍔ㄧ敾瀹屾垚鍚庢墠鍐荤粨锛屽惁鍒?`isReady()` 涓€鐩磋繑鍥?false锛宍processClick()` 鏃犳硶瑙﹀彂鍏冲睆銆?

#### 2.2 鍐荤粨鍊掕鏃?鈥?`onClientTick(TickEvent.ClientTickEvent.END)`

```
姣忎釜姝ｅ父 tick锛堝喕缁撴湭婵€娲绘椂锛夛細
  freezePacketDelay > 0?
    鈹溾攢 freezePacketDelay-- (3鈫?鈫?鈫?)
    鈹斺攢 ==0 鈫?anyPouchOpen=true, C2STimeStopPacket(true) 鍙戦€佸埌鏈嶅姟鍣?
```

姝ゅ悗 `anyPouchOpen=true` 鏍囪鍐荤粨宸叉縺娲汇€?

#### 2.3 鏉鹃敭妫€娴?鈥?`onKeyInput(InputEvent.Key)`

> **涓轰粈涔堜笉鐢?`ClientTickEvent`锛?* 鍐荤粨鍚?HEAD 娉ㄥ叆灏?`msPerTick=1.0e32`锛屾父鎴忓惊鐜窇 0 娆★紝`tick()` 涓嶆墽琛岋紝`ClientTickEvent.END` 涓嶄細瑙﹀彂銆?

`InputEvent.Key` 鏄?GLFW 鍥炶皟锛屽湪 `runTick()` 鏈€寮€澶寸殑 `glfwPollEvents()` 涓Е鍙戯紝**瀹屽叏鐙珛浜庢父鎴忓惊鐜?*锛屽喕缁撴湡闂翠篃鑳芥甯稿伐浣溿€?

```
X 閿噴鏀?
  鈹溾攢 anyPouchOpen=false? (SHORT PRESS)
  鈹?  鈹溾攢 radMenuOpen=false, freezePacketDelay=0   鈫?鍙栨秷鍐荤粨鍊掕鏃?
  鈹?  鈹斺攢 mc.setScreen(null)                        鈫?鐩存帴鍏冲睆锛屼笉瑙﹀彂鍐荤粨
  鈹?
  鈹斺攢 anyPouchOpen=true? (LONG PRESS / 鍐荤粨涓?
      鈹斺攢 TimeStopUtilsWrapped.disable()            鈫?瑙ｅ喕瀹㈡埛绔紝璁?processClick() 鑷劧鎵ц
```

**SHORT PRESS 鍏冲睆浣嗕笉鍙戝喕缁撳寘** 鈫?`onScreenClosed` 涓?`!anyPouchOpen && freezePacketDelay==0` 鈫?鐩存帴 return锛屼笉鍙戦€佷换浣?C2S 鍖呫€?

**LONG PRESS 鍙仛 `disable()`** 鈫?涓嶅叧灞忋€佷笉鍙戣В鍐诲寘 鈫?`screen.tick()` 鍦ㄨ嚜瀹氫箟寰幆涓墽琛?`processClick()` 鈫?閫変腑鑱氭櫠 + 鍏冲睆 鈫?`onScreenClosed` 鍙?C2S 瑙ｅ喕鍖?+ `disable()`銆?

#### 2.4 鍏冲睆娓呯悊 鈥?`onScreenClosed(ScreenEvent.Closing)`

```
灞忓箷鍏抽棴锛堟棤璁轰綍绉嶆柟寮忥級
  鈹溾攢 !anyPouchOpen && freezePacketDelay==0? 鈫?return (SHORT PRESS 鏈喕缁?
  鈹溾攢 anyPouchOpen=true? 鈫?C2STimeStopPacket(false) 鍙戦€佸埌鏈嶅姟鍣?
  鈹溾攢 TimeStopUtilsWrapped.disable()                鈫?瀹㈡埛绔В鍐?
  鈹斺攢 閲嶇疆鎵€鏈夌姸鎬? anyPouchOpen=false, radMenuOpen=false, freezePacketDelay=0
```

#### 2.5 鏉傞」

- **`onScreenRenderPre`** 鈥?鍦ㄥ緞鍚戣彍鍗曟墦寮€鏃舵覆鏌撳崐閫忔槑閬僵锛坄0xBB333333`锛?
- **`hasTimeFocusPouch()`** 鈥?缂撳瓨 20 ticks 鐨勮儗鍖呮娴嬶紝閬嶅巻鑳屽寘 + 涓诲壇鎵?+ Curios銆?

### 3. 瀹㈡埛绔喕缁撶鐞?

**`TimeStopUtilsWrapped.java`** 鈥?瀹㈡埛绔晶鐨勫喕缁?瑙ｅ喕鍖呰鍣細

```java
enable(int id):
  鈹溾攢 mc.getSoundManager().pause()
  鈹溾攢 TimeStopUtils.isTimeStop = true                鈫?鍏ㄥ眬鍐荤粨鏍囧織
  鈹溾攢 ClientContext.isTimeStop_andSameDimension = true 鈫?HEAD 娉ㄥ叆鐨勫叧閿潯浠?
  鈹溾攢 ClientLevelExpandedContext.currentTimeStopDimension = 褰撳墠缁村害
  鈹斺攢 TimeContext.Client.timeStopGLFW = glfwGetTime()  鈫?Blaze3D 鏃堕棿閿氱偣

disable():
  鈹溾攢 TimeStopUtils.isTimeStop = false
  鈹溾攢 mc.getSoundManager().resume()
  鈹溾攢 ClientContext.isTimeStop_andSameDimension = false
  鈹斺攢 ClientLevelExpandedContext.currentTimeStopDimension = null
```

### 4. 缃戠粶灞?

#### C2S锛堝鎴风鈫掓湇鍔″櫒锛?

**`C2STimeStopPacket`** 鈥?鍙戦€佹椂鏈猴細

| 鏂瑰悜 | 瑙﹀彂 | action |
|------|------|--------|
| C2S | `FreezePacketDelay==0` (寰勫悜鑿滃崟) | `send(true)` |
| C2S | `onScreenOpen` (FocusPack/Bag) | `send(true)` |
| C2S | `onScreenClosed` | `send(false)` |

鏈嶅姟鍣ㄧ澶勭悊璋冪敤 `TimeStopUtils.use(activate, player, force=true, duration=180, sound=false)`銆?

#### S2C锛堟湇鍔″櫒鈫掑鎴风锛屽箍鎾級

| 鍖?| 浣滅敤 |
|---|------|
| `S2CTimeStopStatePacket` | 骞挎挱鍐荤粨/瑙ｅ喕鐘舵€?鈫?璋冪敤 `TimeStopUtilsWrapped.enable/disable` |
| `S2CTimeStopDimensionPacket` | 骞挎挱鍐荤粨缁村害鍙樻洿 鈫?璁剧疆 `currentTimeStopDimension` |

### 5. 鏈嶅姟绔喕缁撶鐞?

**`TimeStopUtils.java`** 鈥?鏈嶅姟绔晶鐨勫喕缁撴牳蹇冿細

```java
use(boolean z, LivingEntity source, boolean force, int time, boolean playSound):
  鈹溾攢 if (z && !ModConfig.TIME_STOP_ENABLED.get()) return
  鈹溾攢 if (source.level().isClientSide) return         鈫?鎷掔粷瀹㈡埛绔皟鐢?
  鈹溾攢 isTimeStop = z                                  鈫?鍏ㄥ眬鏍囧織
  鈹溾攢 TimeStopSavedData 璁板綍/娓呴櫎鍐荤粨缁村害
  鈹溾攢 broadcast S2CTimeStopStatePacket                鈫?閫氱煡鎵€鏈夊鎴风鍐荤粨鐘舵€?
  鈹溾攢 broadcast S2CTimeStopDimensionPacket            鈫?閫氱煡鎵€鏈夊鎴风鍐荤粨缁村害
  鈹斺攢 TimeStopEntityData 绠＄悊鍊掕鏃讹紙鍙€夛級
```

**`TimeStopSavedData.java`** 鈥?鎸佷箙鍖栧喕缁撶姸鎬侊紙缁村害闆嗗悎锛夛紝瀛樹簬 `GoetyMech_time_stop_data`銆?

### 6. Mixin 灞?鈥?瀹炵幇鍐荤粨鏁堟灉

#### 6.1 娓告垙寰幆鎺у埗

**`MinecraftMixin.java` 鈥?涓や釜娉ㄥ叆鐐癸細**

**HEAD 娉ㄥ叆**锛坄runTick` 鐨勬渶寮€澶达級锛?

```java
if (isTimeStop AND isTimeStop_andSameDimension)
    Minecraft.timer.msPerTick = 1.0e32
```

鏁堟灉锛歚timer.advancerCallable()` 杩斿洖 false 鈫?娓告垙寰幆璺?0 娆?鈫?`ClientLevel.tick()`銆乣ClientPacketListener.tick()` 绛夊叏閮ㄨ烦杩?鈫?娓告垙涓栫晫瀹屽叏鍐荤粨銆?

**浜屽彿娉ㄥ叆**锛坄ForgeEventFactory.onRenderTickStart` 澶勶紝浣跨敤鐙珛 `TimeContext.Client.timer`锛夛細

```java
long l = TimeContext.Client.timer.advanceTime(Util.getMillis());
for (int i = 0; i < l; i++) {
    // keybindings 澶勭悊锛堟澗寮€ X 閿椂鐨?isKeyDown 妫€娴嬶級
    // screen.tick() 鈫?杩欐槸寰勫悜鑿滃崟姝ｅ父宸ヤ綔鐨勫叧閿紒
    // keyboard handler
    // gameMode tick锛堝鏋滅帺瀹跺彲浠ョЩ鍔級
}
```

杩欎釜鐙珛 Timer锛坄TimeContext.Client.timer`锛岄粯璁?50ms/tick锛?*涓嶅彈 `msPerTick=1.0e32` 褰卞搷**锛屾墍浠ユ父鎴忓惊鐜仠杞悗灞忓箷浠嶇劧鑳?tick銆?

#### 6.2 鏃堕棿鍐荤粨

**`ClientLevelMixin.java`** 鈥?`ClientLevel` 鐨勪笁涓柟娉曡鍙栨秷锛?

| 鏂规硶 | 浣滅敤 | 鍙栨秷鍚庢灉 |
|------|------|---------|
| `tick()` | 涓栫晫 tick | 瀹炰綋銆佹柟鍧楀疄浣撲笉 tick |
| `tickTime()` | 娓告垙鏃堕棿鎺ㄨ繘 | `gameTime` 鍐荤粨 |
| `animateTick()` | 绮掑瓙鏁堟灉 | 鏃犵矑瀛?|

**`ClientLevelExpandedContext.java`** 鈥?`tickHead()` 鍦?`ClientLevel.tick()` 琚墽琛屽墠璁剧疆 `ClientContext.isTimeStop_andSameDimension` 骞跺彇娑?tick銆?

#### 6.3 Blaze3D 鏃堕棿鏇挎崲

**`Blaze3DMixin.java`** 鈥?鏇挎崲 `Blaze3D.getTime()` 杩斿洖鍊间负鍐荤粨鏃跺埢鐨?GLFW 鏃堕棿銆傚奖鍝嶇矑瀛愩€佸姩鐢荤瓑鍩轰簬 `Blaze3D.getTime()` 鐨勮瑙夋晥鏋溿€?

#### 6.4 Timer 璁块棶鍣?

**`TimerAccessor.java`** 鈥?Mixin Accessor锛屾彁渚涘 `Timer.msPerTick` 绉佹湁瀛楁鐨勮鍐欍€?

---

## 鍏抽敭璁捐鍐崇瓥

### 1. 鍙屽竷灏斿喕缁撴鏌?

```java
TimeStopUtils.isTimeStop                    // 鍏ㄥ眬鍐荤粨鏍囧織锛堝鎴风+鏈嶅姟绔叡浜級
ClientContext.isTimeStop_andSameDimension   // 瀹㈡埛绔細褰撳墠缁村害鏄惁鍦ㄥ喕缁撶淮搴﹀垪琛ㄤ腑
```

HEAD 娉ㄥ叆瑕佹眰**涓よ€呴兘涓?true** 鎵嶈缃?`msPerTick=1.0e32`銆傝繖浣垮緱 `disable()` 鍙互鐙珛璁剧疆 `isTimeStop_andSameDimension=false` 鏉ョ粫杩?HEAD 娉ㄥ叆锛屽嵆浣?S2C 鍐荤粨鍖呮櫄浜?`disable()` 鍒拌揪瀵艰嚧 `isTimeStop=true`銆?

### 2. 涓轰粈涔堢敤 `InputEvent.Key` 鑰屼笉鏄?`ClientTickEvent`

鍐荤粨鏈熼棿 `MinecraftMixin.HEAD` 娉ㄥ叆璁╂父鎴忓惊鐜窇 0 娆?鈫?`tick()` 涓嶆墽琛?鈫?`ClientTickEvent.Post` 涓嶈Е鍙戙€備絾 GLFW 鍥炶皟锛坄glfwPollEvents`锛夊湪 `runTick()` 鏈€寮€澶存墽琛岋紝鐙珛浜庢父鎴忓惊鐜€俙InputEvent.Key` 鍥犳鏄喕缁撴湡闂村敮涓€鍙潬鐨勯敭妫€娴嬫満鍒躲€?

### 3. 涓轰粈涔?`freezePacketDelay=3`

寰勫悜鑿滃崟鎵撳紑鍔ㄧ敾 `OPEN_ANIMATION_LENGTH=2.5 ticks`銆傚鏋滅珛鍗冲喕缁擄紝`isReady()` 杩斿洖 false锛宍processClick()` 鏃犳硶瑙﹀彂锛屽睆骞曞崱姝汇€傚欢杩?3 ticks 纭繚鍔ㄧ敾瀹屾垚銆?

### 4. 鍏抽棴娴佺▼涓?`disable()` 鐨勬椂鏈?

LONG PRESS 鏃跺湪 `InputEvent.Key` 涓厛璋?`disable()` 鍐嶇瓑 `screen.tick()` 鎵ц `processClick()`銆傚鏋滀笉鍏?`disable()`锛宍processClick()` 涓殑 `menu.clickItem()` 鍙兘鍥犱负鍐荤粨鐘舵€佽€屽紓甯搞€?

### 5. `onScreenClosed` 鐨勪簩娆℃竻鐞?

`InputEvent.Key`锛圠ONG PRESS锛夊彧璋?`disable()` 涓嶈皟 `mc.setScreen(null)`銆俙processClick()` 鍏冲睆 鈫?`onScreenClosed` 鍙?C2S 瑙ｅ喕鍖?+ 鍐嶆 `disable()`锛堝箓绛夛級銆係HORT PRESS 鏃?`onScreenClosed` 鐩存帴 return锛坄!anyPouchOpen && freezePacketDelay==0`锛夈€?

---

## 瀹屾暣鏃跺簭鍥?

### SHORT PRESS锛堟澗閿椂鍐荤粨鏈縺娲伙級

```
鏃堕棿绾?
鈹?
鈹溾攢 runTick N: 鐜╁鎸?X 鎵撳紑寰勫悜鑿滃崟
鈹?  鈹溾攢 onScreenOpen: radMenuOpen=true, freezePacketDelay=3
鈹?  鈹斺攢 鍔ㄧ敾寮€濮嬶紙OPENING锛?
鈹?
鈹溾攢 runTick N+1:
鈹?  鈹溾攢 onKeyInput(InputEvent.Key): X 宸叉澗寮€
鈹?  鈹?  鈹斺攢 anyPouchOpen=false 鈫?SHORT PRESS
鈹?  鈹?      鈹溾攢 radMenuOpen=false
鈹?  鈹?      鈹溾攢 freezePacketDelay=0  鈫?鍙栨秷鍐荤粨鍊掕鏃?
鈹?  鈹?      鈹斺攢 mc.setScreen(null)
鈹?  鈹溾攢 onScreenClosed: !anyPouchOpen && freezePacketDelay==0 鈫?return锛堜笉鍙戦€佷换浣曞寘锛?
鈹?  鈹斺攢 姝ｅ父娓告垙锛屽喕缁撲粠鏈縺娲?鉁?
```

### LONG PRESS锛堟寜浣?X 鐩村埌鍐荤粨婵€娲诲悗鏉惧紑锛?

```
鏃堕棿绾?
鈹?
鈹溾攢 runTick N: 鎸?X 鎵撳紑寰勫悜鑿滃崟
鈹?  鈹溾攢 onScreenOpen: radMenuOpen=true, freezePacketDelay=3
鈹?  鈹斺攢 鍔ㄧ敾寮€濮嬶紙OPENING锛?
鈹?
鈹溾攢 runTick N+1: onClientTick 鈫?freezePacketDelay=2
鈹溾攢 runTick N+2: onClientTick 鈫?freezePacketDelay=1
鈹?
鈹溾攢 runTick N+3: freezePacketDelay鈫?
鈹?  鈹溾攢 onClientTick: anyPouchOpen=true, C2S鍐荤粨鍖呪啋鏈嶅姟鍣?
鈹?  鈹?  鈹溾攢 鏈嶅姟绔? isTimeStop=true, 骞挎挱 S2C 鍐荤粨
鈹?  鈹?  鈹斺攢 瀹㈡埛绔? enable(0) 鈫?isTimeStop=true, isTimeStop_andSameDimension=true
鈹?  鈹斺攢 HEAD 娉ㄥ叆鐢熸晥 鈫?姝ゅ悗娓告垙寰幆璺?0 娆?
鈹?
鈹溾攢 runTick N+4..M: 鐜╁杩樻寜鐫€ X锛屾父鎴忓喕缁擄紝鑷畾涔夊惊鐜腑 screen.tick() 缁х画鎵ц
鈹?
鈹溾攢 runTick M: 鐜╁鏉惧紑 X
鈹?  鈹溾攢 onKeyInput(InputEvent.Key): anyPouchOpen=true 鈫?LONG PRESS
鈹?  鈹?  鈹斺攢 disable() 鈫?isTimeStop=false, isTimeStop_andSameDimension=false
鈹?  鈹溾攢 鑷畾涔夊惊鐜? screen.tick() 鈫?processClick()
鈹?  鈹?  鈹溾攢 menu.clickItem() 鈫?閫変腑鑱氭櫠锛?
鈹?  鈹?  鈹溾攢 mc.setScreen(null)
鈹?  鈹?  鈹斺攢 onScreenClosed: C2S瑙ｅ喕鍖?+ disable()锛堝箓绛夛級
鈹?  鈹溾攢 鏈嶅姟绔?tick: 澶勭悊 C2S瑙ｅ喕鍖?鈫?isTimeStop=false 鈫?骞挎挱 S2C瑙ｅ喕
鈹?  鈹斺攢 姝ｅ父娓告垙鎭㈠ 鉁?
```

---

## 娑夊強鐨勬簮鏂囦欢娓呭崟

| 璺緞 | 浣滅敤 |
|------|------|
| `common/item/TimeFocusPouch.java` | 鐗╁搧瀹氫箟锛堢户鎵?FocusPack锛?|
| `client/ModClientForgeEvents.java` | 瀹㈡埛绔簨浠跺崗璋冨櫒 |
| `client/TimeStopUtilsWrapped.java` | 瀹㈡埛绔喕缁?瑙ｅ喕 |
| `client/ClientContext.java` | `isTimeStop_andSameDimension` |
| `client/ClientLevelExpandedContext.java` | 褰撳墠鍐荤粨缁村害绠＄悊 |
| `common/time/TimeStopUtils.java` | 鏈嶅姟绔喕缁?瑙ｅ喕鏍稿績 |
| `common/time/TimeContext.java` | 鐙珛 Timer + GLFW 鏃堕棿閿氱偣 |
| `common/time/LevelExpandedContext.java` | 鏈嶅姟绔?tick 鍙栨秷 |
| `common/data/TimeStopSavedData.java` | 鍐荤粨缁村害鎸佷箙鍖?|
| `common/net/c2s/C2STimeStopPacket.java` | C2S 鍐荤粨鍖?|
| `common/net/s2c/S2CTimeStopStatePacket.java` | S2C 鐘舵€佸悓姝?|
| `common/net/s2c/S2CTimeStopDimensionPacket.java` | S2C 缁村害鍚屾 |
| `config/ModConfig.java` | 鏃跺仠寮€鍏炽€佸€掕鏃剁瓑閰嶇疆 |
| `mixin/MinecraftMixin.java` | 娓告垙寰幆鎺у埗 + 鑷畾涔夊惊鐜?|
| `mixin/ClientLevelMixin.java` | 瀹㈡埛绔笘鐣?tick 鍙栨秷 |
| `mixin/Blaze3DMixin.java` | Blaze3D 鏃堕棿鏇挎崲 |
| `mixin/TimerAccessor.java` | Timer.msPerTick 璁块棶鍣?|
| `mixin/ServerLevelMixin.java` | 鏈嶅姟绔?tick 鍙栨秷 |
