package com.gio.examplemod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.gio.examplemod.item.ArtemisBowItem;
import com.gio.examplemod.item.ArcaneHourglassStaffItem;
import com.gio.examplemod.entity.ZombieBruteEntity;
import com.gio.examplemod.item.DivineTuningRodItem;
import com.gio.examplemod.item.EirScepterItem;
import com.gio.examplemod.item.MjolnirItem;
import com.gio.examplemod.network.CastTalentPayload;
import com.gio.examplemod.network.CastUltimateTalentPayload;
import com.gio.examplemod.network.ChooseClassPayload;
import com.gio.examplemod.network.SelectActiveTalentPayload;
import com.gio.examplemod.network.SelectStaffSpellPayload;
import com.gio.examplemod.network.SyncProgressionPayload;
import com.gio.examplemod.network.UnlockTalentPayload;
import com.gio.examplemod.progression.ProgressionEvents;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
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
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ZombieBruteEntity>> ZOMBIE_BRUTE = ENTITIES.registerEntityType(
            "zombie_brute",
            ZombieBruteEntity::new,
            MobCategory.MONSTER,
            builder -> builder
                    .sized(0.9F, 2.925F)
                    .eyeHeight(2.61F)
                    .passengerAttachments(3.01875F)
                    .ridingOffset(-1.05F)
                    .clientTrackingRange(8)
                    .notInPeaceful()
    );

    public static final DeferredItem<Item> ARCANE_HOURGLASS_STAFF = ITEMS.registerItem(
            "arcane_hourglass_staff",
            properties -> new ArcaneHourglassStaffItem(properties
                    .sword(ToolMaterial.DIAMOND, 4.0F, -2.8F)
                    .rarity(Rarity.RARE)
                    .fireResistant())
    );

    public static final DeferredItem<Item> DIVINE_TUNING_ROD = ITEMS.registerItem(
            "divine_tuning_rod",
            properties -> new DivineTuningRodItem(properties
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant())
    );

    public static final DeferredItem<Item> MJOLNIR = ITEMS.registerItem(
            "mjolnir",
            properties -> new MjolnirItem(properties
                    .sword(ToolMaterial.NETHERITE, 7.0F, -3.3F)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
                    .stacksTo(1)
                    .durability(1200))
    );

    public static final DeferredItem<Item> EIR_SCEPTER = ITEMS.registerItem(
            "eir_scepter",
            properties -> new EirScepterItem(properties
                    .sword(ToolMaterial.GOLD, 1.0F, -2.0F)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
                    .stacksTo(1)
                    .durability(900))
    );

    public static final DeferredItem<Item> ARTEMIS_BOW = ITEMS.registerItem(
            "artemis_bow",
            properties -> new ArtemisBowItem(properties
                    .rarity(Rarity.EPIC)
                    .fireResistant()
                    .stacksTo(1)
                    .durability(1200))
    );

    public static final DeferredItem<Item> ZOMBIE_BRUTE_SPAWN_EGG = ITEMS.registerItem(
            "zombie_brute_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ZOMBIE_BRUTE.get()))
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TUTORIAL_TAB = CREATIVE_MODE_TABS.register(
            "tutorialmod_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.tutorialmod"))
                    .icon(() -> new ItemStack(ARCANE_HOURGLASS_STAFF.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ARCANE_HOURGLASS_STAFF.get());
                        output.accept(DIVINE_TUNING_ROD.get());
                        output.accept(MJOLNIR.get());
                        output.accept(EIR_SCEPTER.get());
                        output.accept(ARTEMIS_BOW.get());
                        output.accept(ZOMBIE_BRUTE_SPAWN_EGG.get());
                    })
                    .build()
    );

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TutorialMod(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::registerEntityAttributes);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (TutorialMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new ProgressionEvents());

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(MODID)
                .playToServer(SelectStaffSpellPayload.TYPE, SelectStaffSpellPayload.STREAM_CODEC, SelectStaffSpellPayload::handle)
                .playToServer(ChooseClassPayload.TYPE, ChooseClassPayload.STREAM_CODEC, ChooseClassPayload::handle)
                .playToServer(UnlockTalentPayload.TYPE, UnlockTalentPayload.STREAM_CODEC, UnlockTalentPayload::handle)
                .playToServer(SelectActiveTalentPayload.TYPE, SelectActiveTalentPayload.STREAM_CODEC, SelectActiveTalentPayload::handle)
                .playToServer(CastTalentPayload.TYPE, CastTalentPayload.STREAM_CODEC, CastTalentPayload::handle)
                .playToServer(CastUltimateTalentPayload.TYPE, CastUltimateTalentPayload.STREAM_CODEC, CastUltimateTalentPayload::handle)
                .playToClient(SyncProgressionPayload.TYPE, SyncProgressionPayload.STREAM_CODEC);
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ZOMBIE_BRUTE.get(), ZombieBruteEntity.createAttributes().build());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
