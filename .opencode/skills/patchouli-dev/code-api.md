# 代码 API

## IPatchouliAPI

```java
IPatchouliAPI api = PatchouliAPI.get();
if (api.isStub()) { /* Patchouli 未安装 */ return; }
```

主要方法：

| 方法 | 说明 |
|---|---|
| `getBook()`, `openBookGUI()` | 获取/打开手册 |
| `registerCategory()` | 注册分类 |
| `registerMultiblock()` | 注册多方块 |
| `setConfigFlag()` | 设置 Config flag |
| `getConfigFlag()` | 查询 Config flag |

## IComponentProcessor

实现该接口对接模板数据：

```java
public class MyProcessor implements IComponentProcessor {
    @Override
    public void setup(IPageWrapper page) {
        String text = page.getText("text");
    }

    @Override
    public String process(String key) {
        if ("text".equals(key)) return "处理后的文本";
        return null;
    }
}
```

## ICustomComponent

自定义渲染组件：

```java
public class MyCustomComponent extends ICustomComponent {
    @Override
    public void build(ComponentBuildContext context, List<IGuiElement> top, List<IGuiElement> bottom) {
        // 添加自定义 GUI 元素
    }
}
```

## Config Flag

```java
PatchouliAPI.get().setConfigFlag("goetypunk:hard_mode", true);
```

## 注册多方块

```java
Multiblock mb = Multiblock.of("S");
PatchouliAPI.get().registerMultiblock(
    new ResourceLocation("goetypunk", "flesh_altar"), mb);
```

## 给予新手手册（Advancement）

**Advancement** (`/data/goetypunk/advancements/grant_book.json`)：

```json
{
  "criteria": {
    "tick": { "trigger": "minecraft:tick" }
  },
  "rewards": {
    "loot": [ "goetypunk:grant_book" ]
  }
}
```

**Loot Table** (`/data/goetypunk/loot_tables/grant_book.json`)：

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
              "tag": "{\"patchouli:book\": \"goetypunk:goetypunk_guide\"}"
            }
          ]
        }
      ]
    }
  ]
}
```
