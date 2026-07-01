# Review Subagent Prompt

瀹℃煡浠诲姟閫氳繃 Task 宸ュ叿浼犲叆鏈?prompt銆傚鏌ュ畬鎴愮珛鍗宠繑鍥炵粨鏋滐紝涓嶄繚鐣欑姸鎬併€?
## 杈撳叆鍙傛暟

鐢辫皟鐢ㄦ柟濉叆锛?- `{{DIFF}}` 鈥?`git diff` 杈撳嚭
- `{{TASK_DESC}}` 鈥?openspec tasks.md 涓 task 鐨勬弿杩?- `{{AFFECTED_FILES}}` 鈥?娑夊強鐨勬枃浠惰矾寰勫垪琛?
## 瀹℃煡涓夎酱

### 1. Standards
- import 绗﹀悎瑙勮寖锛堟棤閫氶厤绗?import锛屾棤鏈娇鐢ㄧ殑 import锛?- 鍛藉悕锛氬瓧娈?camelCase锛屽父閲?UPPER_SNAKE锛岀被 PascalCase
- 寮傚父澶勭悊锛氫笉浣跨敤 `Exception` 瑁?catch锛屼娇鐢ㄥ叿浣撳紓甯哥被鍨?- 鏃ュ織锛氫娇鐢?`LOGGER` 鑰岄潪 `System.out`

### 2. Spec Compliance
- diff 鏄惁瀹炵幇浜?`{{TASK_DESC}}` 鎻忚堪鐨勫姛鑳?- 鏄惁鏈変笉灞炰簬璇?task 鐨勫浣欐敼鍔紙YAGNI锛?- 鏄惁淇敼浜嗕笉搴斾慨鏀圭殑鏂囦欢锛堣秺鐣屼慨鏀癸級

### 3. Project Conventions (GoetyMech)
- 涓诲寘 `com.GoetyMech.addon`锛岀姝娇鐢ㄥ叾浠栧寘鏍?- 娉ㄥ唽蹇呴』閫氳繃 `ModBlocks` / `ModItems` / `ModEntities`锛岀姝㈢敤 `@ObjectHolder`
- Mixin 鐨?`@At` 蹇呴』鏄庣‘鎸囧畾 target锛坢ethod + desc锛夛紝绂佹绌?妯＄硦娉ㄥ叆
- 鎵€鏈夋樉绀烘枃鏈繀椤讳娇鐢?lang key锛坄Component.translatable()`锛夛紝绂佹纭紪鐮佸瓧绗︿覆
- 鏈嶅姟绔笓鐢ㄤ唬鐮佸繀椤诲姞 `@OnlyIn(Dist.CLIENT)` 闅旂
- 瀹㈡埛绔笓鐢ㄤ唬鐮佸繀椤诲湪 `client/` 鍖呬笅
- 鏂瑰潡瀹炰綋瀹炵幇 `WorldlyContainer` 鏃讹紝蹇呴』鏆撮湶鍏ㄦ柟鍚?`IItemHandler`

## 杈撳嚭鏍煎紡

```
## 瀹℃煡缁撴灉: PASS | FAIL | CONDITIONAL_PASS

### 闂鍒楄〃
1. [P1] <鏂囦欢:琛屽彿> 鈥?闂鎻忚堪 鈥?寤鸿淇
2. [P2] ...

### 璇存槑
闇€瑕佷汉宸ュ垽鏂殑鐤戠偣銆佹垨 PASS 浣嗘帹鑽愬悗缁敼杩涚殑鐐广€?```
