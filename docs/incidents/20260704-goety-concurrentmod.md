# Goety ModEvents.worldLoad ConcurrentModificationException

status: verified

## 现象

`runClient` 启动后服务端立即崩溃：
```
java.util.ConcurrentModificationException
  at java.base/java.util.HashMap.forEach(HashMap.java:1424)
  at com.Polarice3.Goety.common.events.ModEvents.worldLoad(ModEvents.java:446)
```

## 根因

Goety 2.5.54.2 的 `ModEvents.worldLoad()` 对 `HashMap` 调用 `forEach` 时有并发线程修改该 map。不是本模组的问题。

## 修复建议

删除 `run/saves/` 下存档或创建新世界可绕过。该 bug 在 Goety 后续版本可能已修复。

## 关联

- 任务: EpicFight公式替换crater效果
- 文件: 无关（Goety 模组内部问题）
