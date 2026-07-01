---
name: "prototype-flight-mixin"
description: "elytra flight / Mixin / velocity"
---

# Prototype 椋炶 Mixin 鈥?鎺㈢储涓庡疄鐜?
> 鍓嶇疆妯＄粍婧愮爜璺緞瑙?`.opencode/skills/_shared/libcode-paths.md`銆?> 蹇€熸帓闄わ細褰撳墠浠诲姟濡傛灉涓嶆秹鍙婇灅缈呴琛岀墿鐞嗐€丮ixin 娉ㄥ叆鐐归€夋嫨鎴栧鎴风-鏈嶅姟绔?velocity 鍚屾锛岃璺宠繃鏈?skill銆?
## 闂

涓?Prototype 闉樼繀瀹炵幇"鎸?W 鏃舵部瑙嗙嚎鏂瑰悜鍖€閫熼琛?锛坴elocity-set 妯″瀷锛夛紝鏇夸唬鍘熺増闉樼繀鐨勭儫鑺辩伀绠姞閫熴€傝姹傦細
- 骞抽涓嶄笅娌?- 浠板ご娌?y 杞翠笂鍗?- 淇啿鍚戞爣淇啿
- 涓嶆寜 W 鏃舵仮澶嶅師鐗堥灅缈呯墿鐞?
## 灏濊瘯璺緞

### V4锛歅layerTickEvent.END + 鍔犳硶鍔犻€熷櫒

```
motion += look 脳 BOOST_FORCE + (0, HOVER_LIFT, 0)
```

**缁撴灉锛?* 瀹屽叏鏃犳晥銆傚姞娉曞姏鍦ㄤ话澶存椂姘村钩鍒嗛噺涓?0锛屽瀭鐩村垎閲忎笉瓒充互瀵规姉閲嶅姏銆?
**鏁欒锛?* 鍔犳硶妯″瀷涓嶉€傚悎浠绘剰鏂瑰悜鐨勫寑閫熼琛屻€傚繀椤荤敤 velocity-set锛堟瘡 tick 閲嶇疆閫熷害涓虹洰鏍囧€硷級銆?
---

### V5锛歁ixin @Inject LivingEntity.travel() HEAD锛堟棤 cancel锛?
```java
@Mixin(LivingEntity.class)
@Inject(method = "travel", at = @At("HEAD"))
void gp$onTravelHead(...) {
    player.setDeltaMovement(look.scale(speed).add(0, 0.08, 0));
}
```

**缁撴灉锛?* 鏈嶅姟绔Е鍙戯紝浣嗗鎴风"鎵€鏈夋诞鍔涢兘娑堝け浜?鈥斺€斿畬鍏ㄩ€€鍥炲師鐗堥灅缈呫€?
**鏍瑰洜锛?* 瀹㈡埛绔?`getFlightForward()` 濮嬬粓杩斿洖 0銆俙C2SFlightInputPacket` 浠呭湪鏈嶅姟绔 `setFlightForward`锛屽鎴风 `LocalPlayer` 鐨?PersistentData 浠庢湭鍐欏叆銆?
**鏁欒锛?* C2S 鍖呯殑 handler 鍙窇鍦ㄦ湇鍔＄銆傚鎴风瑕佽鍚屼竴浠芥暟鎹紝蹇呴』鍦ㄥ彂閫佺涔熷啓鏈銆?
---

### V5b锛歁ixin @Inject Player.travel() HEAD锛坈ancellable = true锛?
```java
@Mixin(Player.class)
@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
void gp$onTravelHead(Vec3 travelVec, CallbackInfo ci) {
    player.setDeltaMovement(motion);
    player.move(MoverType.SELF, motion);
    ci.cancel();
}
```

**缁撴灉锛?* 鏈嶅姟绔?Mixin 瑙﹀彂锛坒orward=1.0锛夛紝瀹㈡埛绔?Mixin 涓嶈Е鍙戯紙forward=0锛夈€?
**鏍瑰洜锛?* 鍚屼笂鈥斺€斿鎴风 `getFlightForward` 鏈銆侻ixin 浠呭湪 `Server thread` 鍛戒腑锛宍Render thread` 涓婃潯浠朵笉閫氳繃銆?
**鏁欒锛?* 鍒ゆ柇 Mixin 鏄惁鐢熸晥鏃讹紝涓嶄粎瑕佺湅鏈嶅姟绔棩蹇楋紝杩樿纭瀹㈡埛绔嚎绋嬫槸鍚﹁Е鍙戙€?
---

## 鍏抽敭鎶€鏈彂鐜?
### 1. Player.travel() 濮旀墭閾?
```java
// Player.travel() 濮嬬粓璋冪敤 super.travel()
public void travel(Vec3 pTravelVector) {
    // 娓告吵淇銆佸垱閫犻琛?-> 鏈€缁堥兘璋?super.travel()
    super.travel(pTravelVector);  // 鈫?LivingEntity.travel()
    this.checkMovementStatistics(...);
}
```

`super.travel()` 鏄?`LivingEntity.travel()`锛岄灅缈呯墿鐞嗕唬鐮佸湪鍏朵腑銆傛墍浠?Mixin 娉ㄥ叆 `LivingEntity.travel()` HEAD **鐞嗚鍙**銆?
### 2. ServerPlayer.isEffectiveAi() 杩斿洖 true

```java
// Entity.java锛圥layer 鏈鐩栨鏂规硶锛?public boolean isEffectiveAi() {
    return !this.level().isClientSide;
}
```

鏈嶅姟绔潪鏃佽鑰呯帺瀹?鈫?`isEffectiveAi() = true` 鈫?`travel()` 浣撳唴閫昏緫锛堥灅缈呯墿鐞嗭級鎵ц銆?
### 3. ServerPlayer.zza 姘歌繙涓?0

```java
// LivingEntity.aiStep() 鈥?涓嶄細浠?input 璁剧疆 zza
this.zza = this.zza * 0.98f;  // 浠呰“鍑?
// LocalPlayer.aiStep() 鈥?瀹㈡埛绔墠鍋氭嫹璐?this.zza = this.input.forwardImpulse;  // 鎸?W -> 1.0
```

`LocalPlayer`锛堝鎴风锛夊湪 `aiStep()` 涓皢 `input.forwardImpulse` 鎷疯礉鍒?`this.zza`銆?**鏈嶅姟绔?`ServerPlayer` 娌℃湁杩欎釜閫昏緫**锛宍zza` 姘歌繙涓?0銆?
**缁撹锛?* 鐢?`player.zza` 鍒ゆ柇 W 鎸変笅鐨勬柟娉?*鍙湪瀹㈡埛绔湁鏁?*锛屾湇鍔＄涓嶈銆?
### 4. 瀹㈡埛绔?鏈嶅姟绔?forward 鍒嗚

| 瀛樺偍鏂瑰紡 | 瀹㈡埛绔?| 鏈嶅姟绔?|
|----------|--------|--------|
| `player.zza` | 鉁?1.0 (浠?input 鎷疯礉) | 鉂?姘歌繙 0 |
| `PrototypeItem.getFlightForward()` | 鉂?榛樿 0 | 鉁?C2S packet 璁剧疆 |

**淇锛?* 鍦?C2S 鍙戦€佺鍚屾椂鍐欐湰绔?PersistentData锛?
```java
// PrototypeRenderEvents.java
float forwardVal = forwardDown ? 1.0f : 0.0f;
PrototypeItem.setFlightForward(player, forwardVal);  // 瀹㈡埛绔湰绔啓鍏?if (tick % 3 == 0) {
    ModNetwork.CHANNEL.sendToServer(new C2SFlightInputPacket(forwardVal)); // 鏈嶅姟绔啓鍏?}
```

### 5. cancellable = true 鐨勬敞鎰忎簨椤?
`ci.cancel()` 浼氳烦杩?`Player.travel()` 鍏ㄩ儴閫昏緫锛屽寘鎷細
- `checkMovementStatistics()` 鈥?鍙帴鍙楋紙浠呯粺璁℃暟鎹級
- `super.travel()` 鈥?琚浛浠ｏ紝闇€鎵嬪姩璋?`player.move(MoverType.SELF, motion)`

```java
player.setDeltaMovement(motion);
player.move(MoverType.SELF, motion);
ci.cancel();
```

**cancel 鐨勯€傜敤鏉′欢锛?* 浠呭湪宸茬‘璁ら渶瑕佸畬鍏ㄦ帴绠¤ tick 鐨勮繍鍔ㄦ椂浣跨敤銆傚浜?浠呭湪 elytra + W 鎸変笅"鐨勬潯浠跺垎鏀紝闈炴縺娲绘椂 return 璁╁師鏂规硶姝ｅ父杩愯銆?
## 鏈€缁堝疄鐜?
### Mixin锛圥rototypeTravelMixin.java锛?
```java
@Mixin(Player.class)
public abstract class PrototypeTravelMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void gp$onTravelHead(Vec3 travelVec, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.isFallFlying()) return;
        if (!PrototypeItem.hasAnyPrototype(player)) return;
        float forward = PrototypeItem.getFlightForward(player);
        if (!(forward > 0.01f)) return;

        Vec3 look = player.getLookAngle();
        double speed = ModConfig.FLIGHT_TARGET_SPEED.get();
        Vec3 motion = look.scale(speed).add(0, 0.08, 0);
        player.setDeltaMovement(motion);
        player.move(MoverType.SELF, motion);
        ci.cancel();
    }
}
```

### Config

```java
FLIGHT_TARGET_SPEED = BUILDER
    .defineInRange("flight_target_speed", 0.5, 0.1, 2.0);
```

閲嶅姏琛ュ伩 0.08 纭紪鐮侊紙绛変簬鍘熺増闉樼繀 `motion.y -= 0.08`锛夈€?
## 瀹炴垬缁忛獙

### Mixin 娉ㄥ叆鐐归€夋嫨

| 鐩爣 | 鏂规硶 | 缁撴灉 |
|------|------|------|
| `LivingEntity.travel()` | `@At("HEAD")` | 鑳借Е鍙戯紝浣?client forward=0 闂鍚屽瓨鍦?|
| `Player.travel()` | `@At("HEAD")` cancellable | 鏇村彲鎺э紝鍙畬鍏ㄦ帴绠￠琛?|

**鎺ㄨ崘锛?* 瀵圭帺瀹朵笓灞炲姛鑳界洿鎺ョ敤 `@Mixin(Player.class)` + `cancellable = true`锛岄伩鍏嶄緷璧?LivingEntity 鍐呴儴閫昏緫鍒嗘敮銆?
### 瀹㈡埛绔?鏈嶅姟绔悓姝ラ櫡闃?
- `PlayerTickEvent` 鐨?START/END 鍦ㄥ鎴风鍜屾湇鍔＄閮借Е鍙?- Mixin 缂栬瘧鍒颁袱涓幆澧冿紝浣?`@Inject` 鐨勬潯浠跺垽鏂綋鍦ㄤ袱绔墽琛?- PersistentData锛坄entity.getPersistentData()`锛夊湪瀹㈡埛绔拰鏈嶅姟绔槸**鐙珛瀹炰緥**
- C2S 鍖?handler 鍙窇鏈嶅姟绔紝涓嶅啓瀹㈡埛绔?- `player.zza` 鍦?`LocalPlayer` 鏈夋晥锛宍ServerPlayer` 鏃犳晥

**榛勯噾娉曞垯锛?* 闇€瑕佸湪涓ょ璇诲悓涓€浠戒腑闂寸姸鎬佹椂鈥斺€旇涔堢敤 `player.zza` 杩欑鏈潵灏卞悓姝ョ殑瀛楁锛岃涔堝湪 C2S 鍙戦€佺鍚屾椂鍐欐湰绔€?
### 璋冭瘯鏂规硶

1. 鍔?`GoetyMechMod.LOGGER.info` 鐪?Mixin 鏄惁瑙﹀彂锛堝尯鍒?`[Server thread]` 鍜?`[Render thread]`锛?2. 鏃ュ織涓湅 `forward=` 鍊兼槸鍚?> 0.01
3. 濡?Mixin 鍙湪 Server thread 瑙﹀彂 鈫?forward 鍊煎湪瀹㈡埛绔负 0 鈫?C2S 鎴栨湰绔啓鍏ラ棶棰?4. 鐢?`player.isFallFlying()` + `hasAnyPrototype` 缂╁皬鏉′欢鑼冨洿

### 鐩稿叧鏂囦欢

| 璺緞 | 瑙掕壊 |
|------|------|
| `mixin/PrototypeTravelMixin.java` | Player.travel() HEAD 娉ㄥ叆 |
| `client/PrototypeRenderEvents.java` | C2S 鍙戦€?+ 鏈 forward 鍐欏叆 |
| `common/item/PrototypeItem.java` | getFlightForward / setFlightForward NBT |
| `config/ModConfig.java` | FLIGHT_TARGET_SPEED |
| `common/net/c2s/C2SFlightInputPacket.java` | 鏈嶅姟绔?forward 璁剧疆 |
