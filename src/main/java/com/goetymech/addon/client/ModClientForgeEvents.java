package com.goetymech.addon.client;

import com.Polarice3.Goety.client.gui.screen.inventory.FocusBagScreen;
import com.Polarice3.Goety.client.gui.screen.inventory.FocusPackScreen;
import com.Polarice3.Goety.client.gui.screen.inventory.FocusRadialMenuScreen;
import com.Polarice3.Goety.init.ModKeybindings;
import com.goetymech.addon.GoetyMechMod;
import com.goetymech.addon.common.item.RemoteEngineBlockItem;
import com.goetymech.addon.client.TimeStopUtilsWrapped;
import com.goetymech.addon.common.item.TimeFocusPouch;
import com.goetymech.addon.common.item.curio.FleshCasketItem;
import com.goetymech.addon.common.net.ModNetwork;
import com.goetymech.addon.common.net.c2s.C2SOpenFleshCasketPacket;
import com.goetymech.addon.common.net.c2s.C2STimeStopPacket;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = GoetyMechMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModClientForgeEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("goetymechTG");
    private static boolean cachedPouchResult = false;
    private static long pouchCacheTime = 0;
    private static boolean anyPouchOpen = false;
    private static boolean radMenuOpen = false;
    private static int freezePacketDelay = 0;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_RELEASE) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!(mc.screen instanceof FocusRadialMenuScreen)) return;
        int bindingCode = ModKeybindings.wandCircle().getKey().getValue();
        if (event.getKey() != bindingCode) return;

        LOGGER.debug("X released (InputEvent.Key) while radial menu open");
        if (!anyPouchOpen) {
            radMenuOpen = false;
            freezePacketDelay = 0;
            mc.setScreen(null);
        } else {
            TimeStopUtilsWrapped.disable();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        FreecamHandler fh = FreecamHandler.getInstance();
        if (fh.isActive()) {
            FreecamInputHandler.handleInput(mc.getFrameTime());
            fh.tick();
            return;
        }

        // === Freeze packet delay countdown ===
        if (freezePacketDelay > 0) {
            freezePacketDelay--;
            if (freezePacketDelay == 0 && hasTimeFocusPouch()) {
                LOGGER.debug("freezePacketDelay reached 0, sending freeze packet");
                anyPouchOpen = true;
                ModNetwork.CHANNEL.sendToServer(new C2STimeStopPacket(true));
            }
        }

        if (ModClientEvents.fleshCasketKey != null && ModClientEvents.fleshCasketKey.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new C2SOpenFleshCasketPacket());
        }

        Player player = mc.player;
        if (player == null) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof RemoteEngineBlockItem)) return;
        if (!RemoteEngineBlockItem.isBound(held)) return;

        BlockPos target = RemoteEngineBlockItem.getBoundPos(held);
        AABB aabb = new AABB(target);

        Outliner.getInstance()
                .showAABB("remote_engine_link", aabb)
                .colored(0xFFCC44)
                .lineWidth(1 / 16f);
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Screen screen = event.getScreen();
        if (!hasTimeFocusPouch()) return;
        boolean isFreezeScreen = screen instanceof FocusRadialMenuScreen
                || screen instanceof FocusPackScreen
                || screen instanceof FocusBagScreen;
        if (isFreezeScreen) {
            if (screen instanceof FocusRadialMenuScreen) {
                radMenuOpen = true;
                freezePacketDelay = 3;
            } else {
                anyPouchOpen = true;
                ModNetwork.CHANNEL.sendToServer(new C2STimeStopPacket(true));
            }
        }
    }

    @SubscribeEvent
    public static void onScreenClosed(ScreenEvent.Closing event) {
        Screen screen = event.getScreen();
        if (!anyPouchOpen && freezePacketDelay == 0) return;
        boolean isFreezeScreen = screen instanceof FocusRadialMenuScreen
                || screen instanceof FocusPackScreen
                || screen instanceof FocusBagScreen;
        if (!isFreezeScreen) return;
        LOGGER.debug("onScreenClosed for {}", screen.getClass().getSimpleName());
        anyPouchOpen = false;
        radMenuOpen = false;
        freezePacketDelay = 0;
        ModNetwork.CHANNEL.sendToServer(new C2STimeStopPacket(false));
        TimeStopUtilsWrapped.disable();
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (radMenuOpen) {
            renderOverlay(event.getGuiGraphics());
        }
    }

    private static void renderOverlay(GuiGraphics graphics) {
        var mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        graphics.fill(0, 0, w, h, 0xBB333333);
    }

    private static boolean hasTimeFocusPouch() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return false;
        long now = level.getGameTime();
        if (now - pouchCacheTime < 20) return cachedPouchResult;
        pouchCacheTime = now;

        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof TimeFocusPouch) {
                cachedPouchResult = true;
                return true;
            }
        }
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() instanceof TimeFocusPouch) {
            cachedPouchResult = true;
            return true;
        }
        if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() instanceof TimeFocusPouch) {
            cachedPouchResult = true;
            return true;
        }

        if (ModList.get().isLoaded("curios")) {
            Optional<SlotResult> found = CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.findFirstCurio(s -> s.getItem() instanceof TimeFocusPouch))
                    .orElse(Optional.empty());
            if (found.isPresent()) {
                cachedPouchResult = true;
                return true;
            }
        }
        cachedPouchResult = false;
        return false;
    }
}
