# PhantomWandManager.tick() UnsupportedOperationException

status: verified

## 根因

`PhantomWandManager.tick()` 用 `PENDING_CLEAR.entrySet().removeIf()` 迭代，内部调 `entry.setValue(remaining)` 递减剩余 tick。但 `PENDING_CLEAR` 是 `ConcurrentHashMap`，其 `entrySet()` 返回 `Map.Entry` 实现为 `SimpleImmutableEntry`，`setValue()` 抛 `UnsupportedOperationException`。

## 复现条件

- `tryAutoCast` 触发 → 调 `PhantomWandManager.set()` + `scheduleClear(400)`
- 下个 `onServerTick(Phase.END)` → `PhantomWandManager.tick()` → `removeIf` lambda 内 `entry.setValue()`

第一次 tick（remaining=400-1=399）必然触发，无论上层逻辑如何。

## 修复

改为 snapshot 迭代：

```java
for (UUID uuid : new ArrayList<>(PENDING_CLEAR.keySet())) {
    int remaining = PENDING_CLEAR.get(uuid) - 1;
    if (remaining <= 0) {
        PENDING_CLEAR.remove(uuid);
        PHANTOM_WANDS.remove(uuid);
    } else {
        PENDING_CLEAR.put(uuid, remaining);
    }
}
```

## 教训

1. `ConcurrentHashMap.entrySet()` 返回的 entry 不支持 `setValue()`，本质原因：`EntrySet` 内部类没有实现 `setValue()` 方法重写
2. `removeIf` + entry mutation 的 pattern 不安全，`removeIf` 的 lambda 应只做 predicate 判断
3. 应当优先用 keySet snapshot + explicit get/put，而不是依赖 entry.setValue()
