# Warden锛堝潥瀹堣€咃級寮€鍙戝疄鎴樼粡楠?
> 鏉ヨ嚜 SculkBindSpell 寮€鍙戣繃绋嬶紝娑电洊鍙敜鍘熺増 Warden銆丅rain 璁板繂鎿嶄綔銆佸嚭鍦熷姩鐢汇€侀煶鏁堝啿绐佺瓑銆?
## Warden Brain 娲诲姩浼樺厛绾?
| 浼樺厛绾?| 娲诲姩 | 璇存槑 |
|--------|------|------|
| 0 | EMERGE | 鍑哄湡鍔ㄧ敾 |
| 0 | DIG | 閬佸湴 |
| 1 | ROAR | 鎬掑惣 |
| 2 | FIGHT | 鎴樻枟 |
| 3 | INVESTIGATE | 璋冩煡 |
| 4 | SNIFF | 鍡呮帰 |
| 6 | IDLE | 绌洪棽 |

**鏍稿績闄烽槺**锛欴IG 鍜?EMERGE 浼樺厛绾х浉鍚屼笖閮介珮浜?FIGHT銆傚嵆浣胯缃?ATTACK_TARGET锛屽鏋?Warden 鏈夐亖鍦版潯浠讹紝Brain 浼氫紭鍏?DIG銆?
**姝ｇ‘鍋氭硶**锛氬彫鍞ゆ椂璁剧疆 DIG_COOLDOWN 绂佹閬佸湴锛?
```java
warden.getBrain().setMemoryWithExpiry(
    MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200); // 60s 鍐峰嵈
```

## 婵€鎬?Warden 鐨勬柟寮?
| 鏂瑰紡 | 鏁堟灉 | 璇勪环 |
|------|------|------|
| 鎵嬪姩璁剧疆 ANGRY_AT 璁板繂 | 涓嶈Е鍙?AngerManagement | 涓嶇ǔ瀹?|
| 鎵嬪姩璁剧疆 ATTACK_TARGET | 鐩存帴鎸囧畾鐩爣 | 涓嶇ǔ瀹?|
| 寰幆 `increaseAngerAt(target)` | 姣忔 +35锛? 娆?= 175 > 80 闃堝€?| 鉁?鍙敤 |
| `increaseAngerAt(target, 100, false)` | 鍗曟 100 = 80 + 20锛岀洿鎺ヨ秴 FIGHT 闃堝€?| **鎺ㄨ崘** |
| 鍙戝皠 `GameEvent.SHRIEK` | 鎸姩妫€娴?鈫?闂存帴绱㈡晫 | 杈呭姪鎵嬫 |

**鎺ㄨ崘鏂规**锛氫娇鐢?3 鍙傛暟閲嶈浇鐩存帴鎸囧畾鎰ゆ€掑€硷紙Goety 瀹樻柟鍋氭硶锛夛細

```java
// 100 = AngerLevel.ANGRY.getMinimumAnger() (80) + 20
// false = 涓嶈Е鍙?Sculk Shrieker 杩為攣鍙敜
warden.increaseAngerAt(target, 100, false);
```

**3 鍙傛暟閲嶈浇绛惧悕**锛坴anilla `Warden` 绫伙級锛?```java
public void increaseAngerAt(@Nullable LivingEntity target, int angerIncrease, boolean listen)
```
- `target` 鈥?鎰ゆ€掔洰鏍?- `angerIncrease` 鈥?鐩存帴鎸囧畾鎰ゆ€掑鍔犲€?- `listen` 鈥?`true` 鏃惰Е鍙戦檮杩?Sculk Shrieker 鎸姩鐩戝惉閾撅紙鍙兘鍐嶅彫 Warden锛夛紝`false` 闈欓粯娉ㄥ叆

**Goety 涓 API 鐨勪娇鐢ㄥ満鏅?*锛堝潎浼?`false` 闃茶繛閿佸彫 Warden锛夛細

| 鏉ユ簮 | 鎰ゆ€掑€?| 鍦烘櫙 |
|------|--------|------|
| `ModEvents.java` (Apostle 鏃犳晫闃舵) | 80+20=100 | 鎵?Apostle 鐨?mob 琚媺鍒?ObsidianMonolith |
| `AbstractObsidianMonolith.java` (鏌卞瓙姝讳骸) | 80+20=100 | 鍑绘潃鏌卞瓙鐨?mob 杞伀 Apostle |
| `SkullLord.java` (鎸ㄦ墦鏃? | 80+100=180 | 鏀诲嚮 SkullLord 鐨?mob 杞伀 BoneLord |

## Warden 鐩爣姝诲悗杞伀锛圠ivingDeathEvent 閲嶅畾鍚戯級

**鍦烘櫙**锛氬彫鍞?Warden 鍑绘潃鏉熺細鐩爣鍚庯紝绔嬪嵆瀵规柦娉曡€呮劋鎬掋€?
**瀹炵幇鏂规**锛?
1. **Warden 鐢熸垚鏃舵墦鏍囩**锛圥ersistentData锛夛細
```java
warden.getPersistentData().putUUID("GoetyMech.bind_caster", caster.getUUID());
warden.getPersistentData().putUUID("GoetyMech.bind_target", target.getUUID());
```

2. **LivingDeathEvent 涓浆鐏?*锛堜粎鍑绘潃鑰呰嚜韬浆鐏級锛?```java
if (!(event.getSource().getEntity() instanceof Warden warden)) return;
CompoundTag tag = warden.getPersistentData();
if (!tag.hasUUID("GoetyMech.bind_caster")) return;
if (!dead.getUUID().equals(tag.getUUID("GoetyMech.bind_target"))) return;

if (warden.level().getPlayerByUUID(tag.getUUID("GoetyMech.bind_caster")) instanceof ServerPlayer player) {
    warden.increaseAngerAt(player, 100, false);
}
tag.remove("GoetyMech.bind_caster");
tag.remove("GoetyMech.bind_target");
```

3. **娉曟湳鎵ц鏈熺洰鏍囨剰澶栨浜¤ˉ鏁?*锛圫pellResult 涓?fallback锛夛細
```java
if (!target.isAlive()) {
    warden.increaseAngerAt(caster, 100, false);
    // 娓呴櫎鏍囩闃查噸澶?    warden.getPersistentData().remove("GoetyMech.bind_caster");
    warden.getPersistentData().remove("GoetyMech.bind_target");
}
```

**瑙勫垯**锛?- 浠呬翰鎵嬪嚮鏉€鐨?Warden 鑾峰緱杞伀锛堝悇鑷嫭绔嬶級锛岄潪鍑绘潃鑰呯殑鍚屾壒 Warden 涓嶄細杞伀
- 閬垮厤浣跨敤 `setAttackTarget()` 鍜?Brain 璁板繂鐩存帴鎿嶄綔锛坉oc 鏍囨敞涓轰笉绋冲畾锛?- 浠呬緷璧?`increaseAngerAt()` 涓夊弬鏁伴噸杞斤紝鍜?Goety 瀹樻柟鍋氭硶涓€鑷?- `false` 绂佹 Sculk Shrieker 杩為攣鍙敜

## 鍏抽敭缁忛獙鎬荤粨

| 鍋氭硶 | 缁撹 |
|------|------|
| 鐢ㄥ惊鐜?`increaseAngerAt` 脳N 鎴栧崟娆?`increaseAngerAt(entity, amount, false)` | 鉁?绋冲畾 |
| 鐩存帴璁?`ATTACK_TARGET` / `ANGRY_AT` Brain 璁板繂 | 鉂?涓嶇ǔ瀹?|
| 鍙湁 `increaseAngerAt` 鑳借Е鍙?`AngerManagement` 绯荤粺 | 鉁?蹇呴』 |
| 杞伀鏃朵紶 `listen=false` | 鉁?闃茶繛閿佸彫 Warden |

## 鍑哄湡鍔ㄧ敾锛圗merging锛?
**閿欒**锛歚setMemory` 姘镐箙璁剧疆 IS_EMERGING 鈫?姝诲惊鐜?**姝ｇ‘**锛歚setMemoryWithExpiry` 璁捐繃鏈熸椂闂达細

```java
warden.getBrain().setMemoryWithExpiry(
    MemoryModuleType.IS_EMERGING, Unit.INSTANCE, 100); // 5 绉?```

## 鍑哄湡鍔ㄧ敾棣栧抚闂幇闂

**鐜拌薄**锛氬彫鍞ゆ椂鍏堝嚭鐜?0.5s锛岀劧鍚庢秷澶卞啀鎾嚭鍦熷姩鐢汇€?
**鏍瑰洜**锛氭湇鍔＄ `setPose(EMERGING)` 纭繚 spawn packet 鍖呭惈姝ｇ‘濮挎€侊紝浣嗗鎴风 animation state machine 灏氭湭鍒濆鍖栥€?
**瑙ｅ喅鏂规**锛氬欢杩熷彲瑙佹€э細

```java
// spawn 鍓?warden.setPose(Pose.EMERGING);
warden.setInvisible(true);
warden.getPersistentData().putLong("GoetyMech.visible_at",
    worldIn.getGameTime() + 15);
worldIn.addFreshEntity(warden);
```

```java
// LivingTickEvent 涓?if (entity instanceof Warden warden) {
    long visibleAt = entity.getPersistentData().getLong("GoetyMech.visible_at");
    if (visibleAt > 0 && entity.level().getGameTime() >= visibleAt) {
        warden.setInvisible(false);
        entity.getPersistentData().remove("GoetyMech.visible_at");
    }
}
```

## 闊虫晥鍐茬獊

**鏍瑰洜**锛氬悓涓€ SoundEvent + 鍚屼竴 SoundSource.HOSTILE 澶氬疄渚嬩簰鐩稿共鎵般€?
**瑙ｅ喅鏂规**锛氬悓涓€闊虫晥鍏ㄥ眬鍙挱涓€娆★紝閫傚綋鎻愰珮闊抽噺锛?
```java
// 鉁?姝ｇ‘
worldIn.playSound(null, targetPos, SoundEvents.SCULK_SHRIEKER_SHRIEK,
    SoundSource.HOSTILE, 3.0F, 1.0F);
for (int i = 0; i < wardenCount; i++) { /* 鍒涘缓 Warden锛屼笉鍐嶆挱闊虫晥 */ }

// 鉂?閿欒锛氬惊鐜唴鎾斁
for (int i = 0; i < wardenCount; i++) {
    worldIn.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, ...);
}
```

## 鍐崇瓥锛氳嚜瀹氫箟 Warden vs 鍘熺増 Warden

| 鏂规 | 浼樼偣 | 缂虹偣 | 缁撹 |
|------|------|------|------|
| 鑷畾涔?Warden 瀹炰綋 | 瀹屽叏鎺у埗 | 闇€閲嶅啓鍏ㄩ儴 AI锛垀3000 琛岋級 | **涓嶆帹鑽?* |
| 鍙敜鍘熺増 Warden + 鎿嶄綔 Brain | 闆剁淮鎶ゆ垚鏈?| 闇€鐞嗚В Brain 浼樺厛绾?| **鎺ㄨ崘** |

## 鍏抽敭璁板繂妯″潡閫熸煡

| 璁板繂妯″潡 | 绫诲瀷 | 鐢ㄩ€?|
|----------|------|------|
| `IS_EMERGING` | `Unit` | 瑙﹀彂鍑哄湡鍔ㄧ敾锛屽繀椤?`setMemoryWithExpiry` |
| `DIG_COOLDOWN` | `Unit` | 绂佹閬佸湴 |
| `ROAR_TARGET` | `Entity` | 鎬掑惣鐩爣 |
| `ATTACK_TARGET` | `Entity` | 鏀诲嚮鐩爣 |
| `ANGRY_AT` | `Entity` | 鎰ゆ€掑璞?|
| `IS_SNIFFING` | `Unit` | 姝ｅ湪鍡呮帰 |
| `DISTURBANCE_LOCATION` | `BlockPos` | 鎸姩鎵板姩浣嶇疆 |

## 寮€鍙戞祦绋?
### 鍙敜 Warden

```
 1. 鍒涘缓鍘熺増 Warden 瀹炰緥
 2. 璁剧疆浣嶇疆銆佹寔涔呭寲
 3. 璁剧疆 DIG_COOLDOWN 璁板繂锛堢閬佸湴锛? 4. 璁剧疆 IS_EMERGING 璁板繂锛堝甫杩囨湡鏃堕棿锛? 5. increaseAngerAt(target, 100, false) 娉ㄥ叆鎰ゆ€掞紙鍗曟涓夊弬鏁扮増鏈級
 6. setPose(EMERGING) + setInvisible(true)锛堥槻闂幇锛? 7. PersistentData 瀛樺彲瑙佹€ф仮澶嶆椂闂存埑 + bind_caster/bind_target 鏍囩
 8. addFreshEntity()
 9. [鍙€塢 鐩爣宸叉 鈫?increaseAngerAt(caster, 100, false) 杞伀
10. 浜嬩欢澶勭悊鍣細鍒版湡鎭㈠鍙 + LivingDeathEvent 杞伀閫昏緫
```

### 鐩爣姝诲悗杞伀 Warden

```
 1. LivingDeathEvent 鈫?event.getSource() 鏄甫鏍囩鐨?Warden
 2. 纭 dead.getUUID() == warden.getPersistentData().bind_target
 3. increaseAngerAt(player, 100, false) 娉ㄥ叆鎰ゆ€? 4. 娓呮爣绛鹃槻閲嶅瑙﹀彂
```
