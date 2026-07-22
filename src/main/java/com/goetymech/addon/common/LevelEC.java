package com.goetymech.addon.common;

import com.goetymech.addon.common.time.LevelExpandedContext;

/**
 * Level 扩展上下文接口。Mixin 注入用 Accessor。
 */
public interface LevelEC {
    LevelExpandedContext gp$levelECData();
    void gp$setECData(LevelExpandedContext data);
}
