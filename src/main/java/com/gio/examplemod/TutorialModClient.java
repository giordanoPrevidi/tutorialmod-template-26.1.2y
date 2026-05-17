package com.gio.examplemod;

import com.gio.examplemod.client.ClientProgressionPayloadHandler;
import com.gio.examplemod.client.StaffSpellSelectionScreen;
import com.gio.examplemod.client.TalentHudOverlay;
import com.gio.examplemod.client.TalentTreeScreen;
import com.gio.examplemod.client.ZombieBruteRenderer;
import com.gio.examplemod.magic.MagicSpell;
import com.gio.examplemod.network.CastTalentPayload;
import com.gio.examplemod.network.CastUltimateTalentPayload;
import com.gio.examplemod.network.SelectStaffSpellPayload;
import com.gio.examplemod.network.SyncProgressionPayload;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = TutorialMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = TutorialMod.MODID, value = Dist.CLIENT)
public class TutorialModClient {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "key"));
    private static final KeyMapping OPEN_SPELL_MENU = new KeyMapping("key.tutorialmod.open_spell_menu", InputConstants.KEY_R, CATEGORY);
    private static final KeyMapping OPEN_TALENT_MENU = new KeyMapping("key.tutorialmod.open_talent_menu", InputConstants.KEY_O, CATEGORY);
    private static final KeyMapping CAST_ACTIVE_TALENT = new KeyMapping("key.tutorialmod.cast_active_talent", InputConstants.KEY_F, CATEGORY);
    private static final KeyMapping CAST_ULTIMATE_TALENT = new KeyMapping("key.tutorialmod.cast_ultimate_talent", InputConstants.KEY_G, CATEGORY);
    private static final KeyMapping[] SELECT_SPELL_KEYS = {
            new KeyMapping("key.tutorialmod.select_arcane_fire", InputConstants.KEY_Z, CATEGORY),
            new KeyMapping("key.tutorialmod.select_wind_burst", InputConstants.KEY_X, CATEGORY),
            new KeyMapping("key.tutorialmod.select_healing_light", InputConstants.KEY_C, CATEGORY),
            new KeyMapping("key.tutorialmod.select_blink_step", InputConstants.KEY_V, CATEGORY),
            new KeyMapping("key.tutorialmod.select_storm_lance", InputConstants.KEY_B, CATEGORY),
            new KeyMapping("key.tutorialmod.select_frost_bind", InputConstants.KEY_N, CATEGORY),
            new KeyMapping("key.tutorialmod.select_earthen_guard", InputConstants.KEY_M, CATEGORY),
            new KeyMapping("key.tutorialmod.select_solar_flare", InputConstants.KEY_Y, CATEGORY),
            new KeyMapping("key.tutorialmod.select_wither_touch", InputConstants.KEY_H, CATEGORY),
            new KeyMapping("key.tutorialmod.select_soul_drain", InputConstants.KEY_J, CATEGORY),
            new KeyMapping("key.tutorialmod.select_bone_minion", InputConstants.KEY_K, CATEGORY),
            new KeyMapping("key.tutorialmod.select_grave_mist", InputConstants.KEY_L, CATEGORY)
    };
    private static final KeyMapping[] CTRL_SELECT_SPELL_KEYS = {
            ctrlSpellKey("key.tutorialmod.select_arcane_fire_ctrl", InputConstants.KEY_1),
            ctrlSpellKey("key.tutorialmod.select_wind_burst_ctrl", InputConstants.KEY_2),
            ctrlSpellKey("key.tutorialmod.select_healing_light_ctrl", InputConstants.KEY_3),
            ctrlSpellKey("key.tutorialmod.select_blink_step_ctrl", InputConstants.KEY_4),
            ctrlSpellKey("key.tutorialmod.select_storm_lance_ctrl", InputConstants.KEY_5),
            ctrlSpellKey("key.tutorialmod.select_frost_bind_ctrl", InputConstants.KEY_6),
            ctrlSpellKey("key.tutorialmod.select_earthen_guard_ctrl", InputConstants.KEY_7),
            ctrlSpellKey("key.tutorialmod.select_solar_flare_ctrl", InputConstants.KEY_8),
            ctrlSpellKey("key.tutorialmod.select_wither_touch_ctrl", InputConstants.KEY_9),
            ctrlSpellKey("key.tutorialmod.select_soul_drain_ctrl", InputConstants.KEY_0),
            ctrlSpellKey("key.tutorialmod.select_bone_minion_ctrl", InputConstants.KEY_MINUS),
            ctrlSpellKey("key.tutorialmod.select_grave_mist_ctrl", InputConstants.KEY_EQUALS)
    };

    public TutorialModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(TutorialModClient::onKeyInput);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        TutorialMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        TutorialMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SPELL_MENU);
        event.register(OPEN_TALENT_MENU);
        event.register(CAST_ACTIVE_TALENT);
        event.register(CAST_ULTIMATE_TALENT);
        for (KeyMapping keyMapping : SELECT_SPELL_KEYS) {
            event.register(keyMapping);
        }
        for (KeyMapping keyMapping : CTRL_SELECT_SPELL_KEYS) {
            event.register(keyMapping);
        }
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(TutorialMod.MODID, "talent_hud"), (extractor, deltaTracker) -> TalentHudOverlay.render(extractor));
    }

    @SubscribeEvent
    static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(SyncProgressionPayload.TYPE, ClientProgressionPayloadHandler::handle);
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TutorialMod.ZOMBIE_BRUTE.get(), ZombieBruteRenderer::new);
    }

    static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        while (OPEN_SPELL_MENU.consumeClick()) {
            minecraft.setScreen(new StaffSpellSelectionScreen());
        }

        while (OPEN_TALENT_MENU.consumeClick()) {
            minecraft.setScreen(new TalentTreeScreen());
        }

        while (CAST_ACTIVE_TALENT.consumeClick()) {
            ClientPacketDistributor.sendToServer(new CastTalentPayload());
        }

        while (CAST_ULTIMATE_TALENT.consumeClick()) {
            ClientPacketDistributor.sendToServer(new CastUltimateTalentPayload());
        }

        MagicSpell[] spells = MagicSpell.values();
        for (int i = 0; i < SELECT_SPELL_KEYS.length && i < spells.length; i++) {
            while (SELECT_SPELL_KEYS[i].consumeClick()) {
                ClientPacketDistributor.sendToServer(new SelectStaffSpellPayload(spells[i].ordinal()));
            }
        }

        for (int i = 0; i < CTRL_SELECT_SPELL_KEYS.length && i < spells.length; i++) {
            while (CTRL_SELECT_SPELL_KEYS[i].consumeClick()) {
                ClientPacketDistributor.sendToServer(new SelectStaffSpellPayload(spells[i].ordinal()));
            }
        }
    }

    private static KeyMapping ctrlSpellKey(String translationKey, int keyCode) {
        return new KeyMapping(
                translationKey,
                KeyConflictContext.IN_GAME,
                KeyModifier.CONTROL,
                InputConstants.Type.KEYSYM,
                keyCode,
                CATEGORY
        );
    }
}
