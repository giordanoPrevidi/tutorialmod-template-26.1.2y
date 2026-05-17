package com.gio.examplemod.client;

import java.util.HashSet;
import java.util.Set;

import com.gio.examplemod.network.SyncProgressionPayload;
import com.gio.examplemod.progression.PlayerClass;

public final class ClientProgressionData {
    private static PlayerClass playerClass = PlayerClass.NONE;
    private static int level = 1;
    private static int xp;
    private static int nextXp = 100;
    private static int points;
    private static String selectedActiveTalent = "";
    private static String selectedPassiveTalent = "";
    private static String selectedUltimateTalent = "";
    private static long selectedCooldownEndsAtMillis;
    private static long selectedUltimateCooldownEndsAtMillis;
    private static long activeUltimateEndsAtMillis;
    private static boolean powerStrikeArmed;
    private static boolean thundersWrathActive;
    private static Set<String> unlockedTalents = new HashSet<>();

    private ClientProgressionData() {
    }

    public static void update(SyncProgressionPayload payload) {
        playerClass = payload.playerClass();
        level = payload.level();
        xp = payload.xp();
        nextXp = payload.nextXp();
        points = payload.unspentTalentPoints();
        selectedActiveTalent = payload.selectedActiveTalent();
        selectedPassiveTalent = payload.selectedPassiveTalent();
        selectedUltimateTalent = payload.selectedUltimateTalent();
        selectedCooldownEndsAtMillis = System.currentTimeMillis() + payload.selectedCooldownTicks() * 50L;
        selectedUltimateCooldownEndsAtMillis = System.currentTimeMillis() + payload.selectedUltimateCooldownTicks() * 50L;
        activeUltimateEndsAtMillis = System.currentTimeMillis() + payload.activeUltimateTicks() * 50L;
        powerStrikeArmed = payload.powerStrikeArmed();
        thundersWrathActive = payload.thundersWrathActive();
        unlockedTalents = new HashSet<>(payload.unlockedTalents());
    }

    public static PlayerClass playerClass() {
        return playerClass;
    }

    public static int level() {
        return level;
    }

    public static int xp() {
        return xp;
    }

    public static int nextXp() {
        return nextXp;
    }

    public static int points() {
        return points;
    }

    public static String selectedActiveTalent() {
        return selectedActiveTalent;
    }

    public static String selectedPassiveTalent() {
        return selectedPassiveTalent;
    }

    public static String selectedUltimateTalent() {
        return selectedUltimateTalent;
    }

    public static int selectedCooldownTicks() {
        long remainingMillis = selectedCooldownEndsAtMillis - System.currentTimeMillis();
        return (int) Math.max(0L, (remainingMillis + 49L) / 50L);
    }

    public static int selectedUltimateCooldownTicks() {
        long remainingMillis = selectedUltimateCooldownEndsAtMillis - System.currentTimeMillis();
        return (int) Math.max(0L, (remainingMillis + 49L) / 50L);
    }

    public static int activeUltimateTicks() {
        long remainingMillis = activeUltimateEndsAtMillis - System.currentTimeMillis();
        return (int) Math.max(0L, (remainingMillis + 49L) / 50L);
    }

    public static boolean powerStrikeArmed() {
        return powerStrikeArmed;
    }

    public static boolean thundersWrathActive() {
        return thundersWrathActive;
    }

    public static boolean hasTalent(String talentId) {
        return unlockedTalents.contains(talentId);
    }
}
