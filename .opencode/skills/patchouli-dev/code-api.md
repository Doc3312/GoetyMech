# 浠ｇ爜 API

## IPatchouliAPI

```java
IPatchouliAPI api = PatchouliAPI.get();
if (api.isStub()) { /* Patchouli 鏈畨瑁?*/ return; }
```

涓昏鏂规硶锛?

| 鏂规硶 | 璇存槑 |
|---|---|
| `getBook()`, `openBookGUI()` | 鑾峰彇/鎵撳紑鎵嬪唽 |
| `registerCategory()` | 娉ㄥ唽鍒嗙被 |
| `registerMultiblock()` | 娉ㄥ唽澶氭柟鍧?|
| `setConfigFlag()` | 璁剧疆 Config flag |
| `getConfigFlag()` | 鏌ヨ Config flag |

## IComponentProcessor

瀹炵幇璇ユ帴鍙ｅ鎺ユā鏉挎暟鎹細

```java
public class MyProcessor implements IComponentProcessor {
    @Override
    public void setup(IPageWrapper page) {
        String text = page.getText("text");
    }

    @Override
    public String process(String key) {
        if ("text".equals(key)) return "澶勭悊鍚庣殑鏂囨湰";
        return null;
    }
}
```

## ICustomComponent

鑷畾涔夋覆鏌撶粍浠讹細

```java
public class MyCustomComponent extends ICustomComponent {
    @Override
    public void build(ComponentBuildContext context, List<IGuiElement> top, List<IGuiElement> bottom) {
        // 娣诲姞鑷畾涔?GUI 鍏冪礌
    }
}
```

## Config Flag

```java
PatchouliAPI.get().setConfigFlag("GoetyMech:hard_mode", true);
```

## 娉ㄥ唽澶氭柟鍧?

```java
Multiblock mb = Multiblock.of("S");
PatchouliAPI.get().registerMultiblock(
    new ResourceLocation("GoetyMech", "flesh_altar"), mb);
```

## 缁欎簣鏂版墜鎵嬪唽锛圓dvancement锛?

**Advancement** (`/data/GoetyMech/advancements/grant_book.json`)锛?

```json
{
  "criteria": {
    "tick": { "trigger": "minecraft:tick" }
  },
  "rewards": {
    "loot": [ "GoetyMech:grant_book" ]
  }
}
```

**Loot Table** (`/data/GoetyMech/loot_tables/grant_book.json`)锛?

```json
{
  "type": "advancement_reward",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "item",
          "name": "patchouli:guide_book",
          "functions": [
            {
              "function": "set_nbt",
              "tag": "{\"patchouli:book\": \"GoetyMech:GoetyMech_guide\"}"
            }
          ]
        }
      ]
    }
  ]
}
```
