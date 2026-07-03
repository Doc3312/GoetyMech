package com.goetymech.addon.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {

    @Accessor("RENDERTYPE_LINES_SHADER")
    static RenderStateShard.ShaderStateShard gp$getRendertypeLinesShader() { throw new AssertionError(); }

    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderStateShard.TransparencyStateShard gp$getTranslucentTransparency() { throw new AssertionError(); }

    @Accessor("NO_LAYERING")
    static RenderStateShard.LayeringStateShard gp$getNoLayering() { throw new AssertionError(); }

    @Accessor("NO_DEPTH_TEST")
    static RenderStateShard.DepthTestStateShard gp$getNoDepthTest() { throw new AssertionError(); }

    @Accessor("COLOR_DEPTH_WRITE")
    static RenderStateShard.WriteMaskStateShard gp$getColorDepthWrite() { throw new AssertionError(); }

    @Accessor("NO_CULL")
    static RenderStateShard.CullStateShard gp$getNoCull() { throw new AssertionError(); }
}
