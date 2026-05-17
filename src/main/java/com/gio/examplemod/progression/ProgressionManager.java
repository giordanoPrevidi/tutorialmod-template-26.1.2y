package com.gio.examplemod.progression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.network.SyncProgressionPayload;
import com.gio.examplemod.talent.Talent;
import com.gio.examplemod.talent.TalentEffects;
import com.gio.examplemod.talent.TalentRegistry;
import com.gio.examplemod.item.DivinePathWeaponItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ProgressionManager {
    private static final String FILE_SUFFIX = "tutorialmod_progression";
    private static final int THUNDERS_WRATH_DURATION_TICKS = 600;
    private static final int THUNDERS_WRATH_STRIKE_INTERVAL_TICKS = 60;
    private static final Map<UUID, PlayerProgressionData> DATA = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, ThunderWrathState> THUNDERS_WRATH = new ConcurrentHashMap<>();

    private ProgressionManager() {
    }

    public static PlayerProgressionData get(ServerPlayer player) {
        PlayerProgressionData data = DATA.computeIfAbsent(player.getUUID(), ignored -> new PlayerProgressionData());
        data.normalizeTalentPoints();
        return data;
    }

    public static void chooseClass(ServerPlayer player, PlayerClass playerClass) {
        PlayerProgressionData data = get(player);
        if (playerClass == PlayerClass.NONE || data.playerClass() != PlayerClass.NONE) {
            sync(player, false);
            return;
        }

        data.setPlayerClass(playerClass);
        removeDivineWeapons(player);
        giveDivineWeapon(player, playerClass);
        player.sendSystemMessage(Component.translatable("message.tutorialmod.class_chosen", playerClass.displayName()).withStyle(ChatFormatting.AQUA));
        sync(player, false);
    }

    public static void unlockTalent(ServerPlayer player, String talentId) {
        PlayerProgressionData data = get(player);
        Talent talent = TalentRegistry.byId(talentId);
        if (talent == null || data.playerClass() == PlayerClass.NONE || talent.playerClass() != data.playerClass()) {
            sync(player, false);
            return;
        }

        if (data.level() < talent.level() || data.hasTalent(talentId) || data.unspentTalentPoints() <= 0) {
            sync(player, false);
            return;
        }

        if (data.unlockTalent(talentId)) {
            if (talent.active() && data.selectedActiveTalent().isBlank()) {
                data.setSelectedActiveTalent(talentId);
            }
            if (talent.passive() && data.selectedPassiveTalent().isBlank()) {
                data.setSelectedPassiveTalent(talentId);
            }
            if (talent.ultimate() && data.selectedUltimateTalent().isBlank()) {
                data.setSelectedUltimateTalent(talentId);
            }
            TalentEffects.applyPassive(player, data, talent);
            player.sendSystemMessage(Component.translatable("message.tutorialmod.talent_unlocked", talent.displayName()).withStyle(ChatFormatting.GREEN));
        }
        sync(player, false);
    }

    public static void selectActiveTalent(ServerPlayer player, String talentId) {
        PlayerProgressionData data = get(player);
        Talent talent = TalentRegistry.byId(talentId);
        if (talent != null && data.hasTalent(talentId) && talent.active()) {
            data.setSelectedActiveTalent(talentId);
            player.sendSystemMessage(Component.translatable("message.tutorialmod.active_talent_selected", talent.displayName()).withStyle(ChatFormatting.AQUA));
        } else if (talent != null && data.hasTalent(talentId) && talent.passive()) {
            data.setSelectedPassiveTalent(talentId);
            TalentEffects.applyPassive(player, data, talent);
            player.sendSystemMessage(Component.translatable("message.tutorialmod.passive_talent_selected", talent.displayName()).withStyle(ChatFormatting.AQUA));
        } else if (talent != null && data.hasTalent(talentId) && talent.ultimate()) {
            data.setSelectedUltimateTalent(talentId);
            player.sendSystemMessage(Component.translatable("message.tutorialmod.ultimate_talent_selected", talent.displayName()).withStyle(ChatFormatting.AQUA));
        }
        sync(player, false);
    }

    public static void castSelectedTalent(ServerPlayer player) {
        PlayerProgressionData data = get(player);
        Talent talent = TalentRegistry.byId(data.selectedActiveTalent());
        if (talent == null || !talent.active() || !data.hasTalent(talent.id())) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.no_active_talent").withStyle(ChatFormatting.RED));
            sync(player, false);
            return;
        }

        long gameTime = player.level().getGameTime();
        long readyAt = COOLDOWNS.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>()).getOrDefault(talent.id(), 0L);
        if (readyAt > gameTime) {
            long seconds = Math.max(1L, (readyAt - gameTime + 19L) / 20L);
            player.sendOverlayMessage(Component.translatable("message.tutorialmod.talent_cooldown", seconds).withStyle(ChatFormatting.RED));
            sync(player, false);
            return;
        }

        if ("warrior_power_strike".equals(talent.id())) {
            armPowerStrike(player, data);
            return;
        }

        if (TalentEffects.cast(player, data, talent)) {
            if ("warrior_charge".equals(talent.id())) {
                data.setMightyCrushPending(true);
            }
            startCooldown(player, talent);
            sync(player, false);
        }
    }

    public static void castSelectedUltimate(ServerPlayer player) {
        PlayerProgressionData data = get(player);
        Talent talent = TalentRegistry.byId(data.selectedUltimateTalent());
        if (talent == null || !talent.ultimate() || !data.hasTalent(talent.id())) {
            player.sendOverlayMessage(Component.translatable("message.tutorialmod.no_ultimate_talent").withStyle(ChatFormatting.RED));
            sync(player, false);
            return;
        }

        long gameTime = player.level().getGameTime();
        long readyAt = COOLDOWNS.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>()).getOrDefault(talent.id(), 0L);
        if (readyAt > gameTime) {
            long seconds = Math.max(1L, (readyAt - gameTime + 19L) / 20L);
            player.sendOverlayMessage(Component.translatable("message.tutorialmod.talent_cooldown", seconds).withStyle(ChatFormatting.RED));
            sync(player, false);
            return;
        }

        if ("warrior_earthshatter".equals(talent.id())) {
            activateThundersWrath(player);
            startCooldown(player, talent);
            sync(player, false);
            return;
        }

        if (TalentEffects.cast(player, data, talent)) {
            startCooldown(player, talent);
            sync(player, false);
        }
    }

    public static float applyOutgoingMeleeDamage(ServerPlayer player, net.minecraft.world.entity.LivingEntity target, float amount) {
        PlayerProgressionData data = get(player);
        if (THUNDERS_WRATH.containsKey(player.getUUID())) {
            TalentEffects.burstPowerStrike(player, target);
            return Math.max(amount * 2.5F + 10.0F, 24.0F);
        }

        if (!data.powerStrikeArmed() || !data.hasTalent("warrior_power_strike")) {
            return amount;
        }

        data.setPowerStrikeArmed(false);
        Talent talent = TalentRegistry.byId("warrior_power_strike");
        if (talent != null) {
            startCooldown(player, talent);
        }
        TalentEffects.burstPowerStrike(player, target);
        sync(player, false);
        return Math.max(amount * 2.5F + 10.0F, 24.0F);
    }

    public static int selectedCooldownTicks(ServerPlayer player, PlayerProgressionData data) {
        return cooldownTicks(player, data.selectedActiveTalent());
    }

    public static int selectedUltimateCooldownTicks(ServerPlayer player, PlayerProgressionData data) {
        return cooldownTicks(player, data.selectedUltimateTalent());
    }

    private static int cooldownTicks(ServerPlayer player, String talentId) {
        Talent talent = TalentRegistry.byId(talentId);
        if (talent == null) {
            return 0;
        }
        long readyAt = COOLDOWNS.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>()).getOrDefault(talent.id(), 0L);
        return (int) Math.max(0L, readyAt - player.level().getGameTime());
    }

    private static void armPowerStrike(ServerPlayer player, PlayerProgressionData data) {
        if (data.powerStrikeArmed()) {
            player.sendOverlayMessage(Component.translatable("message.tutorialmod.power_strike_already_armed").withStyle(ChatFormatting.YELLOW));
            sync(player, false);
            return;
        }
        data.setPowerStrikeArmed(true);
        TalentEffects.armPowerStrike(player);
        player.sendOverlayMessage(Component.translatable("message.tutorialmod.power_strike_armed").withStyle(ChatFormatting.GOLD));
        sync(player, false);
    }

    private static void startCooldown(ServerPlayer player, Talent talent) {
        COOLDOWNS.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>())
                .put(talent.id(), player.level().getGameTime() + talent.cooldownTicks());
    }

    public static void handleLanding(ServerPlayer player) {
        PlayerProgressionData data = get(player);
        if (data.mightyCrushPending() && player.onGround()) {
            data.setMightyCrushPending(false);
            TalentEffects.mightyCrushImpact(player);
            sync(player, false);
        }
    }

    public static void tickThundersWrath(ServerPlayer player) {
        ThunderWrathState state = THUNDERS_WRATH.get(player.getUUID());
        if (state == null) {
            return;
        }

        long gameTime = player.level().getGameTime();
        if (gameTime >= state.endsAt()) {
            THUNDERS_WRATH.remove(player.getUUID());
            player.level().resetWeatherCycle();
            sendWeatherPacket(player, false);
            player.sendOverlayMessage(Component.translatable("message.tutorialmod.thunders_wrath_ended").withStyle(ChatFormatting.GRAY));
            sync(player, false);
            return;
        }

        forceWrathWeather(player, 80);
        if (gameTime % 20L == 0L) {
            sendWeatherPacket(player, true);
            sync(player, false);
        }

        if (gameTime % 45L == 0L) {
            TalentEffects.thundersWrathAmbientThunder(player);
        }

        if (gameTime >= state.nextStrikeAt()) {
            strikeRandomEnemy(player);
            THUNDERS_WRATH.put(player.getUUID(), new ThunderWrathState(state.endsAt(), gameTime + THUNDERS_WRATH_STRIKE_INTERVAL_TICKS));
        }
    }

    public static void resetForTesting(ServerPlayer player) {
        removeDivineWeapons(player);
        get(player).resetProgression();
        COOLDOWNS.remove(player.getUUID());
        THUNDERS_WRATH.remove(player.getUUID());
        player.sendSystemMessage(Component.translatable("message.tutorialmod.admin_reset").withStyle(ChatFormatting.YELLOW));
        sync(player, false);
    }

    public static void increaseLevelForTesting(ServerPlayer player) {
        PlayerProgressionData data = get(player);
        data.setLevelForTesting(Math.min(PlayerProgressionData.MAX_LEVEL, data.level() + 1));
        player.sendSystemMessage(Component.translatable("message.tutorialmod.admin_level_set", data.level()).withStyle(ChatFormatting.YELLOW));
        sync(player, false);
    }

    public static void ensureDivineWeapon(ServerPlayer player) {
        PlayerProgressionData data = get(player);
        if (data.playerClass() != PlayerClass.NONE && !hasDivineWeapon(player, data.playerClass())) {
            removeDivineWeapons(player);
            giveDivineWeapon(player, data.playerClass());
        }
    }

    public static boolean isDivineWeapon(ItemStack stack) {
        return stack.getItem() instanceof DivinePathWeaponItem;
    }

    public static void removeDivineWeapons(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (isDivineWeapon(player.getInventory().getItem(slot))) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    public static void addXp(ServerPlayer player, int amount) {
        PlayerProgressionData data = get(player);
        boolean leveled = data.addXp(amount);
        player.sendSystemMessage(Component.translatable("message.tutorialmod.gained_xp", amount).withStyle(ChatFormatting.GRAY));
        if (leveled) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.level_up", data.level()).withStyle(ChatFormatting.GOLD));
        }
        sync(player, false);
    }

    public static void sync(ServerPlayer player, boolean openMenu) {
        PlayerProgressionData data = get(player);
        PacketDistributor.sendToPlayer(player, SyncProgressionPayload.from(data, selectedCooldownTicks(player, data), selectedUltimateCooldownTicks(player, data), activeUltimateTicks(player), THUNDERS_WRATH.containsKey(player.getUUID()), openMenu));
    }

    public static void load(ServerPlayer player, File file) {
        Properties properties = new Properties();
        if (file.isFile()) {
            try (FileInputStream stream = new FileInputStream(file)) {
                properties.load(stream);
            } catch (IOException exception) {
                player.sendSystemMessage(Component.literal("TutorialMod progression load failed: " + exception.getMessage()).withStyle(ChatFormatting.RED));
            }
        }
        DATA.put(player.getUUID(), PlayerProgressionData.load(properties));
    }

    public static void save(ServerPlayer player, File file) {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            get(player).save().store(stream, "TutorialMod progression");
        } catch (IOException exception) {
            player.sendSystemMessage(Component.literal("TutorialMod progression save failed: " + exception.getMessage()).withStyle(ChatFormatting.RED));
        }
    }

    public static String fileSuffix() {
        return FILE_SUFFIX;
    }

    private static boolean hasDivineWeapon(ServerPlayer player, PlayerClass playerClass) {
        return player.getInventory().contains(stack -> stack.getItem() instanceof DivinePathWeaponItem weapon && weapon.divinePath() == playerClass);
    }

    private static void giveDivineWeapon(ServerPlayer player, PlayerClass playerClass) {
        Item item = switch (playerClass) {
            case WARRIOR -> TutorialMod.MJOLNIR.get();
            case MAGE -> TutorialMod.EIR_SCEPTER.get();
            case RANGER -> TutorialMod.ARTEMIS_BOW.get();
            default -> null;
        };

        if (item != null) {
            player.getInventory().addAndPickItem(new ItemStack(item));
        }
    }

    private static void activateThundersWrath(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        THUNDERS_WRATH.put(player.getUUID(), new ThunderWrathState(gameTime + THUNDERS_WRATH_DURATION_TICKS, gameTime + THUNDERS_WRATH_STRIKE_INTERVAL_TICKS));
        forceWrathWeather(player, THUNDERS_WRATH_DURATION_TICKS + 80);
        sendWeatherPacket(player, true);
        TalentEffects.startThundersWrath(player);
        player.sendOverlayMessage(Component.translatable("message.tutorialmod.thunders_wrath_started").withStyle(ChatFormatting.GOLD));
    }

    private static void forceWrathWeather(ServerPlayer player, int durationTicks) {
        WeatherData weather = player.level().getWeatherData();
        weather.setRaining(true);
        weather.setThundering(true);
        weather.setRainTime(durationTicks);
        weather.setThunderTime(durationTicks);
        weather.setClearWeatherTime(0);
        player.level().setRainLevel(1.0F);
        player.level().setThunderLevel(1.0F);
    }

    private static void sendWeatherPacket(ServerPlayer player, boolean active) {
        player.connection.send(new ClientboundGameEventPacket(active ? ClientboundGameEventPacket.START_RAINING : ClientboundGameEventPacket.STOP_RAINING, 0.0F));
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, active ? 1.0F : 0.0F));
        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, active ? 1.0F : 0.0F));
    }

    private static void strikeRandomEnemy(ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(24.0D, 10.0D, 24.0D);
        java.util.List<Monster> targets = player.level().getEntitiesOfClass(Monster.class, area, LivingEntity::isAlive);
        if (targets.isEmpty()) {
            return;
        }

        Monster target = targets.get(player.getRandom().nextInt(targets.size()));
        TalentEffects.thundersWrathStrike(player, target);
        target.hurtServer(player.level(), player.level().damageSources().magic(), 18.0F);
    }

    private static int activeUltimateTicks(ServerPlayer player) {
        ThunderWrathState state = THUNDERS_WRATH.get(player.getUUID());
        if (state == null) {
            return 0;
        }
        return (int) Math.max(0L, state.endsAt() - player.level().getGameTime());
    }

    private record ThunderWrathState(long endsAt, long nextStrikeAt) {
    }

    public static boolean blocksNaturalMonsterSpawns(net.minecraft.server.level.ServerLevel level) {
        for (UUID uuid : THUNDERS_WRATH.keySet()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null && player.level() == level) {
                return true;
            }
        }
        return false;
    }
}
