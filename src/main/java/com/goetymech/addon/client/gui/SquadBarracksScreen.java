package com.goetymech.addon.client.gui;

import com.goetymech.addon.goetymechMod;
import com.goetymech.addon.common.block.Tier1SquadBarracksBlock;
import com.goetymech.addon.common.block.Tier2SquadBarracksBlock;
import com.goetymech.addon.common.block.Tier3SquadBarracksBlock;
import com.goetymech.addon.common.block.entity.SquadBarracksBlockEntity;
import com.goetymech.addon.common.menu.SquadBarracksMenu;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.SquadConfigC2SPacket;
import com.goetymech.addon.init.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class SquadBarracksScreen extends AbstractContainerScreen<SquadBarracksMenu> {

    private static final ResourceLocation TEXTURE_T1 =
            new ResourceLocation(goetymechMod.MOD_ID, "textures/gui/squad_barracks_t1.png");
    private static final ResourceLocation TEXTURE_T2 =
            new ResourceLocation(goetymechMod.MOD_ID, "textures/gui/squad_barracks_t2.png");
    private static final ResourceLocation TEXTURE_T3 =
            new ResourceLocation(goetymechMod.MOD_ID, "textures/gui/squad_barracks_t3.png");

    private final ResourceLocation texture;
    private final int configSlotCount;
    private final Map<Integer, ItemStack> slotIcons = new HashMap<>();
    private final Map<Integer, ResourceLocation> slotTypes = new HashMap<>();

    public SquadBarracksScreen(SquadBarracksMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        SquadBarracksBlockEntity barracks = menu.getBarracks();
        this.configSlotCount = barracks.getConfigSlotCount();
        for (int i = 0; i < configSlotCount; i++) {
            ResourceLocation type = barracks.getConfigSlot(i);
            if (type != null) {
                slotTypes.put(i, type);
                slotIcons.put(i, makeIconFor(type));
            }
        }

        Block block = barracks.getBlockState().getBlock();
        if (block instanceof Tier3SquadBarracksBlock) {
            this.texture = TEXTURE_T3;
        } else if (block instanceof Tier2SquadBarracksBlock) {
            this.texture = TEXTURE_T2;
        } else {
            this.texture = TEXTURE_T1;
        }

        this.imageWidth = 194;
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private static ItemStack makeIconFor(ResourceLocation entityTypeId) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityTypeId);
        if (type == null) return ItemStack.EMPTY;
        SpawnEggItem egg = SpawnEggItem.byId(type);
        if (egg != null) return new ItemStack(egg);
        ItemStack fallback = new ItemStack(ModItems.MATURE_EMBRYO.get());
        fallback.getOrCreateTag().putString("EntityTypeId", entityTypeId.toString());
        return fallback;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 17;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        gui.blit(texture, x, y, this.imageWidth, 72, 0.0F, 0.0F, this.imageWidth, 72, 194, 168);
        gui.blit(texture, x, y + 72, this.imageWidth, 96, 0.0F, 72.0F, this.imageWidth, 96, 194, 168);

        for (int i = 0; i < configSlotCount; i++) {
            int r = i / 6;
            int c = i % 6;
            int sx = x + 8 + c * 18;
            int sy = y + 18 + r * 18;

            ItemStack icon = slotIcons.get(i);
            if (icon != null && !icon.isEmpty()) {
                gui.renderItem(icon, sx + 1, sy + 1);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF);
        gui.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF);
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        int guiX = this.leftPos;
        int guiY = this.topPos;

        for (int i = 0; i < configSlotCount; i++) {
            int r = i / 6;
            int c = i % 6;
            int sx = guiX + 8 + c * 18;
            int sy = guiY + 18 + r * 18;
            if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
                ResourceLocation entityType = slotTypes.get(i);
                if (entityType != null) {
                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityType);
                    if (type != null) {
                        gui.renderTooltip(this.font, type.getDescription(), mouseX, mouseY);
                    }
                }
                break;
            }
        }

        super.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && this.hoveredSlot != null && this.hoveredSlot.index < configSlotCount) {
            int i = this.hoveredSlot.index;
            ItemStack held = menu.getCarried();

            boolean set = false;

            if (!held.isEmpty()) {
                if (held.getItem() instanceof SpawnEggItem eggItem) {
                    EntityType<?> type = eggItem.getType(held.getTag());
                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
                    if (id != null) {
                        ModNetwork.CHANNEL.sendToServer(
                                new SquadConfigC2SPacket(menu.containerId, i, id.toString()));
                        slotTypes.put(i, id);
                        slotIcons.put(i, new ItemStack(held.getItem()));
                        set = true;
                    }
                } else if (held.hasTag()) {
                    CompoundTag tag = held.getTag();
                    if (tag != null && tag.contains("EntityTypeId")) {
                        String typeId = tag.getString("EntityTypeId");
                        if (!typeId.isEmpty()) {
                            ModNetwork.CHANNEL.sendToServer(
                                    new SquadConfigC2SPacket(menu.containerId, i, typeId));
                            ResourceLocation rl = ResourceLocation.tryParse(typeId);
                            if (rl != null) {
                                slotTypes.put(i, rl);
                                slotIcons.put(i, new ItemStack(held.getItem()));
                            }
                            set = true;
                        }
                    }
                }
            }

            if (!set) {
                ModNetwork.CHANNEL.sendToServer(
                        new SquadConfigC2SPacket(menu.containerId, i, null));
                slotTypes.remove(i);
                slotIcons.remove(i);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        super.removed();
        slotIcons.clear();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }
}
