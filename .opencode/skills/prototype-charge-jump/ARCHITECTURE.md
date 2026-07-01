# 绯荤粺鏋舵瀯 鈥?钃勫姏璺宠穬鍏ㄩ摼璺?
## 璁捐鍐崇瓥锛歊elease-Based

钃勫姏绯荤粺**涓嶅湪鎸変綇鏃剁珛鍗宠捣璺?*锛岃€屾槸**鍦ㄦ澗寮€鏃舵墽琛岃烦**锛?
```
鎸変綇绌烘牸 (action=1) 鈫?姣?tick 绱姞钃勫姏鍊硷紙NBT: GoetyMech.jump_charge_tick锛?鏉惧紑绌烘牸 (action=0) 鈫?鏍规嵁鎬昏搫鍔涘€艰绠楅€熷害骞舵墽琛岃烦
```

### 涓轰粈涔堜笉鐢?Client-Predictive

- 鏈嶅姟绔潈濞侊細鎵€鏈夎烦璺冨喅绛栧湪鏈嶅姟绔仛锛宑lient 鍙鍙?action
- 閬垮厤鍙嶄綔寮婂啿绐?- 绠€鍖栧悓姝ラ€昏緫

## 鏁版嵁娴?
```
Client 鈫愨啋 Server
  鈹?  鈹溾攢鈹€ C2SJumpChargePacket(action)
  鈹?    鈹溾攢鈹€ action > 0: 钃勫姏涓?鈫?PrototypeItem.setJumpChargeTick(player, charge)
  鈹?    鈹斺攢鈹€ action == 0: 閲婃斁 鈫?PrototypeEvents.applyChargedJump(player, charge)
  鈹?  鈹斺攢鈹€ PrototypeEvents.onPlayerTick(PlayerTickEvent)
        鈹溾攢鈹€ START phase: 钃勫姏绱姞 / 鍦伴潰璧烽 / 钀藉湴妫€娴?/ 椋炶鍚姩
        鈹斺攢鈹€ END phase: 椋炶鍔犻€?/ 澹侀潰绉诲姩
```

### 鍏抽敭鏂囦欢

| 鏂囦欢 | 鑱岃矗 |
|------|------|
| `common/events/PrototypeEvents.java` | 鏍稿績閫昏緫锛氳搫鍔涘鐞嗐€佽烦璺冩墽琛屻€佺墿鐞嗗叕寮?|
| `common/item/PrototypeItem.java` | NBT 瀛樺彇鏂规硶锛歡et/set/clearJumpCharge, hasAnyPrototype |
| `mixin/PrototypePlayerMixin.java` | 钃勫姏鏃跺帇鍒?`jumpFromGround()` 闃叉鍐茬獊 |
| `common/net/c2s/C2SJumpChargePacket.java` | 缃戠粶鍖?encode/decode/handle |
| `common/net/ModNetwork.java` | SimpleChannel 娉ㄥ唽 |
| `common/utils/CuriosHelper.java` | Curios 妲戒綅妫€娴?|
| `config/ModConfig.java` | JUMP_* 閰嶇疆椤?|
| `client/PrototypeRenderEvents.java` | 瀹㈡埛绔搫鍔涜繘搴︽覆鏌?|

## 鏃跺簭缁嗚妭

### PlayerTickEvent.START 闃舵闄烽槺

START phase 鐨勪簨浠堕『搴忥細

```
涓婁竴涓?tick 缁撴潫鏃剁殑 onGround = false/true
        鈫?褰撳墠 tick START phase fires
        鈫?  鈫?姝ゆ椂 player.onGround() 浠嶅弽鏄犱笂涓€ tick 鐨勮惤鍦扮姸鎬?  鈫?涓嶆槸褰撳墠 tick 鐨勮惤鍦扮姸鎬侊紒鈫?鍏抽敭
        鈫?aiStep() 鈫?travel() 鈫?move()锛堟洿鏂?onGround锛?        鈫?PlayerTickEvent.END phase fires
```

**褰卞搷**锛?- 钀藉湴妫€娴嬶細涓嶈兘鍦?START 鐩存帴妫€鏌?`onGround()`锛岃鐢?`WAS_AIRBORNE` 鐘舵€佹満
- 鍦伴潰璧烽锛氬悓鏍?START 鏃?`onGround()` 涓?true锛宍zza > 0` 鏃朵細鏃犳潯浠惰鍐欓€熷害

### 鍦伴潰璧烽瑕嗙洊闄烽槺

`onPlayerTick` START phase 涓湁濡備笅浠ｇ爜锛坄PrototypeEvents.java` ~line 238锛夛細

```java
if (player.onGround() && player.zza > 0.0F && charge == 0) {
    player.setDeltaMovement(m.x, 2.0, m.z);  // 鈫?瑕嗙洊钃勫姏璺抽€熷害锛?}
```

`charge == 0` 鍦?`clearJumpCharge()` 鍚庡缁堟垚绔嬨€傚鏋滅帺瀹舵寜 W锛宍zza > 0` 鈫?閫熷害琚鍐欎负 `2.0`锛堢害 15 鏍奸珮搴︼級銆?
**淇**锛氱粰杩欐浠ｇ爜鍔?`&& !hasPendingChargeJump` 鏉′欢锛屾垨鍦ㄨ搫鍔涜烦鍚庤涓€涓复鏃舵爣蹇椼€?
## Curios 妲戒綅妫€娴?
蹇呴』鍚屾椂鏌?chest 鍜?Curios 涓や釜妲斤細

```java
public static boolean hasAnyPrototype(LivingEntity entity) {
    if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PrototypeItem)
        return true;
    if (entity instanceof Player player) {
        return CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.isEquipped(s -> s.getItem() instanceof PrototypeItem))
            .orElse(false);
    }
    return false;
}
```

浠呮煡 chest 浼氭紡鎺?Curios 妲戒綅鐨勫師鍨嬨€?
## Mixin 璺宠穬鍘嬪埗

```java
@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
private void gp$cancelJumpWhileCharging(CallbackInfo ci) {
    if (PrototypeItem.getJumpChargeTick((Player)(Object)this) > 0) {
        ci.cancel();
    }
}
```

鍘熺増鐨?`jumpFromGround()` 浼氱粰 0.42 閫熷害锛屼細涓庤搫鍔涜烦鍐茬獊銆傝搫鍔涗腑蹇呴』鍙栨秷瀹冦€?
## Config 閰嶇疆椤?
| 閰嶇疆 | 榛樿 | 璇存槑 |
|------|------|------|
| `jump_tap_height` | 3.0 | 鐭寜璺宠穬楂樺害锛堜綆浜?threshold 鏃讹級 |
| `jump_charge_threshold` | 6 | 钃勫姏闂ㄦ tick锛堚墺姝ゅ€艰蛋钃勫姏鍏紡锛?|
| `jump_charge_min_height` | 8.0 | threshold 鏃剁殑楂樺害 |
| `jump_charge_max_height` | 80.0 | 鏈€澶ц搫鍔涙椂鐨勯珮搴︼紙瀹炴祴 ~60锛?|
| `jump_max_charge_ticks` | 60 | 鏈€澶ц搫鍔涙椂闂达紙tick锛?|

**娉ㄦ剰**锛氫慨鏀归厤缃粯璁ゅ€煎悗锛屽繀椤诲垹闄?`run/config/GoetyMech-common.toml` 寮哄埗鍐嶇敓锛屽惁鍒?TOML 涓殑鏃у€艰鐩栨柊榛樿鍊笺€?