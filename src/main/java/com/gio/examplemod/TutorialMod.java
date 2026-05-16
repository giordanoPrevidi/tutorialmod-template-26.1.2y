package com.gio.examplemod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.gio.examplemod.item.ArcaneHourglassStaffItem;
import com.gio.examplemod.network.SelectStaffSpellPayload;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TutorialMod.MODID)
public class TutorialMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tutorialmod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> ARCANE_HOURGLASS_STAFF = ITEMS.registerItem(
            "arcane_hourglass_staff",
            properties -> new ArcaneHourglassStaffItem(properties
                    .sword(ToolMaterial.DIAMOND, 4.0F, -2.8F)
                    .rarity(Rarity.RARE)
                    .fireResistant())
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TUTORIAL_TAB = CREATIVE_MODE_TABS.register(
            "tutorialmod_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.tutorialmod"))
                    .icon(() -> new ItemStack(ARCANE_HOURGLASS_STAFF.get()))
                    .displayItems((parameters, output) -> output.accept(ARCANE_HOURGLASS_STAFF.get()))
                    .build()
    );

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TutorialMod(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (TutorialMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(MODID)
                .playToServer(SelectStaffSpellPayload.TYPE, SelectStaffSpellPayload.STREAM_CODEC, SelectStaffSpellPayload::handle);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
