## Why

褰撳墠 TimeFocusPouch 鐨勬椂鍋滃疄鐜板瓨鍦ㄤ袱涓牳蹇冪己闄凤細entity 鍔ㄧ敾鐓у父鎾斁锛堢伀鐞冩棆杞?鎬墿鍔ㄤ綔鏈喕缁擄級锛屼笖 Goety 寰勫悜鑿滃崟鍦?`ClientLevel.tick()` 鍏ㄥ彇娑堟椂鍗℃銆傚弬鑰?endinglibrary 鐨勫疄鐜版柟妗堝悗锛岄渶瑕佸皢鍐荤粨绛栫暐浠?鍙栨秷鏁存潯 Level.tick()"鏀逛负"鍙栨秷 tickEntities() + 鍐荤粨娓叉煋 partialTick"銆?
## What Changes

- `ClientLevelMixin`: 鐩爣浠?`tick()` 鏀逛负 `tickEntities()`锛屼粎鍐荤粨 entity 閫昏緫锛屼繚鐣?block entity/time/pending update 澶勭悊
- `MinecraftMixin`: 鏂板 `getPartialTick()` 娉ㄥ叆锛屾椂鍋滀腑杩斿洖 0.0F锛屾秷闄ゆ覆鏌撴彃鍊兼紓绉?- `MinecraftServerMixin`: 缁存寔 `@WrapOperation` 鍖呰９ `ServerLevel.tick()`
- `ParticleEngineMixin`: 缁存寔鍙栨秷 `tick()`
- 鍒犻櫎姝ゅ墠鏃犵敤鐨?`EntityMixin`锛堝凡鍒狅級

## Capabilities

### New Capabilities
- `client-level-freeze`: 瀹㈡埛绔?ClientLevel 鐨?entity 灞傚喕缁擄紝淇濈暀闈?entity 澶勭悊閾?- `render-freeze`: 閫氳繃鍐荤粨 partialTick 闃绘娓叉煋鎻掑€硷紝娑堥櫎 jitter
- `server-level-freeze`: 鏈嶅姟绔淮搴︾骇 Level.tick() 鍙栨秷

### Modified Capabilities

鏃?
## Impact

- `src/main/java/com/GoetyMech/addon/mixin/ClientLevelMixin.java`: 閲嶅啓
- `src/main/java/com/GoetyMech/addon/mixin/MinecraftMixin.java`: 鏂板缓
- `src/main/java/com/GoetyMech/addon/mixin/EntityMixin.java`: 鍒犻櫎
- `src/main/resources/GoetyMech.mixins.json`: 鏇存柊 client mixin 闃靛垪
