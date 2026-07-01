# `shouldSprint()` 瀹堝崼瀵艰嚧璺戝鏂啿鍒?

status: verified

## 鐥囩姸

璺戝 vaule 缈昏秺鍚庯紝鐖鍐插埡鑷姩涓柇锛岄渶瑕侀噸鏂版寜鍐插埡閿€?

## 鏍瑰洜

`PrototypeRenderEvents.java:81-83` 鐨勫畧鍗€昏緫锛?

```java
if (!ClientWallRunState.shouldSprint()) {
    ClientWallRunState.sprintDown = mc.options.keySprint.isDown() || player.isSprinting();
}
```

`shouldSprint()` 鍦?vault 鍚庤繑鍥?true锛坄sprintRetainTicks > 0`锛?0 tick 绐楀彛锛夛紝瀵艰嚧 `sprintDown` **涓嶅埛鏂?*鈥斺€斿崱鍦?mixin 璁剧殑 `true`銆?

60 tick 杩囨湡鍚庯紝`shouldSprint()` 闄嶄负 false锛宍sprintDown` 浠庣墿鐞嗛敭鍒锋柊锛?

- **toggle sprint**: 閿凡鏉?false) || isSprinting 宸茶 vanilla 娓呮帀(false) 鈫?**sprintDown = false** 鈫?鏂啿鍒?
- **hold sprint**: 閿繕鎸夌潃(true) || isSprinting(false) 鈫?sprintDown = true 鈫?姝ｅ父

## 鍘嗗彶瀵圭収

鍘熷浠ｇ爜 (`f9b9ee8`) 姣?tick **鐩存帴**浠庣墿鐞嗛敭璇?sprintDown锛屾病鏈変换浣?`shouldSprint()` 瀹堝崼锛屼粠鏉ヤ笉璺?sprint銆?

## 鏁欒

1. **涓嶈鍔犱笉蹇呰鐨勫畧鍗€?* 鍘熷浠ｇ爜姣?tick 鐩存帴璇荤墿鐞嗛敭锛寁ault 鍚?sprint 涓嶄細鏂€傚姞 `sprintRetainTicks + shouldSprint()` 鐮村潖浜嗗師鏈夌殑绠€鍗曠洿杩炪€?
2. **"淇濇姢"寮忛€昏緫瀹规槗浜х敓闅愯棌鐨勮繃鏈熼棶棰樸€?* 60 tick 鐨勪繚鎶ょ獥鍙ｄ竴杩囷紝`sprintDown` 浠庤淇濇姢鐘舵€佽穼鍏?鎸夐敭鎶捣 + vanilla 娓呯┖ isSprinting"鐨勫弻閲?false 闄烽槺銆?
3. **鎭㈠鏂规**锛氬幓鎺夊畧鍗紝鎭㈠鍘熷鐨勭洿鎺ユ寜閿疆璇細

   ```java
   ClientWallRunState.tickSprintRetain();
   ClientWallRunState.sprintDown = mc.options.keySprint.isDown() || player.isSprinting();
   ```

## 褰卞搷鑼冨洿

浠?`src/main/java/com/GoetyMech/addon/client/PrototypeRenderEvents.java`锛屽垹闄や簡 3 琛岋紝鍑忎负 1 琛屻€?

## 鍏宠仈鎻愪氦

`0ac0ac8` 鈥?`[auto] edit: PrototypeRenderEvents.java | remove shouldSprint() guard, restore direct key polling for sprintDown`
