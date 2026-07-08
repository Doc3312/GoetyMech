package com.goetymech.addon.common.item.curio;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * AutoCastDummyWand — 保留以供未来自动施法测试使用。
 * 原用于 Flesh Casket 自动施法功能的开发调试。
 * 如需重新激活：取消注释并恢复 ModItems 中的注册。
 *
public class AutoCastDummyWand extends Item implements IWand {
    private final SpellType primaryType;
    private final List<SpellType> spellTypes;

    public AutoCastDummyWand() {
        this(SpellType.VOID, SpellType.NONE, SpellType.NECROMANCY,
                SpellType.ILL, SpellType.GEOMANCY, SpellType.WIND);
    }

    public AutoCastDummyWand(SpellType primary, SpellType... additional) {
        super(new Properties().stacksTo(1));
        this.primaryType = primary;
        List<SpellType> list = new ArrayList<>();
        list.add(primary);
        Collections.addAll(list, additional);
        this.spellTypes = Collections.unmodifiableList(list);
    }

    @Override
    public SpellType getSpellType() {
        return primaryType;
    }

    @Override
    public List<SpellType> getSpellTypes() {
        return spellTypes;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        SoulUsingItemHandler handler = new SoulUsingItemHandler(stack);
        return new ICapabilityProvider() {
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.ITEM_HANDLER) {
                    return LazyOptional.of(() -> handler).cast();
                }
                return LazyOptional.empty();
            }
        };
    }
}
*/
