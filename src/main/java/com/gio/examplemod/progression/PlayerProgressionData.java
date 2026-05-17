package com.gio.examplemod.progression;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class PlayerProgressionData {
    public static final int MAX_LEVEL = 5;
    private static final int[] XP_THRESHOLDS = {0, 100, 250, 500, 900};

    private PlayerClass playerClass = PlayerClass.NONE;
    private int level = 1;
    private int xp;
    private int unspentTalentPoints = 1;
    private String selectedActiveTalent = "";
    private String selectedPassiveTalent = "";
    private String selectedUltimateTalent = "";
    private boolean powerStrikeArmed;
    private boolean mightyCrushPending;
    private final Set<String> unlockedTalents = new HashSet<>();

    public PlayerClass playerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        if (this.playerClass == PlayerClass.NONE && playerClass != PlayerClass.NONE) {
            this.playerClass = playerClass;
        }
    }

    public int level() {
        return level;
    }

    public int xp() {
        return xp;
    }

    public int xpForNextLevel() {
        return level >= MAX_LEVEL ? XP_THRESHOLDS[MAX_LEVEL - 1] : XP_THRESHOLDS[level];
    }

    public int unspentTalentPoints() {
        return unspentTalentPoints;
    }

    public Set<String> unlockedTalents() {
        return unlockedTalents;
    }

    public String selectedActiveTalent() {
        return selectedActiveTalent;
    }

    public void setSelectedActiveTalent(String selectedActiveTalent) {
        this.selectedActiveTalent = selectedActiveTalent == null ? "" : selectedActiveTalent;
    }

    public String selectedPassiveTalent() {
        return selectedPassiveTalent;
    }

    public void setSelectedPassiveTalent(String selectedPassiveTalent) {
        this.selectedPassiveTalent = selectedPassiveTalent == null ? "" : selectedPassiveTalent;
    }

    public String selectedUltimateTalent() {
        return selectedUltimateTalent;
    }

    public void setSelectedUltimateTalent(String selectedUltimateTalent) {
        this.selectedUltimateTalent = selectedUltimateTalent == null ? "" : selectedUltimateTalent;
    }

    public boolean selectedPassiveIs(String talentId) {
        return selectedPassiveTalent.equals(talentId) && hasTalent(talentId);
    }

    public boolean powerStrikeArmed() {
        return powerStrikeArmed;
    }

    public void setPowerStrikeArmed(boolean powerStrikeArmed) {
        this.powerStrikeArmed = powerStrikeArmed;
    }

    public boolean mightyCrushPending() {
        return mightyCrushPending;
    }

    public void setMightyCrushPending(boolean mightyCrushPending) {
        this.mightyCrushPending = mightyCrushPending;
    }

    public void resetProgression() {
        playerClass = PlayerClass.NONE;
        level = 1;
        xp = 0;
        unspentTalentPoints = 1;
        selectedActiveTalent = "";
        selectedPassiveTalent = "";
        selectedUltimateTalent = "";
        powerStrikeArmed = false;
        mightyCrushPending = false;
        unlockedTalents.clear();
    }

    public void setLevelForTesting(int level) {
        this.level = clamp(level, 1, MAX_LEVEL);
        this.xp = XP_THRESHOLDS[this.level - 1];
        normalizeTalentPoints();
    }

    public boolean hasTalent(String talentId) {
        return unlockedTalents.contains(talentId);
    }

    public boolean unlockTalent(String talentId) {
        if (unspentTalentPoints <= 0 || unlockedTalents.contains(talentId)) {
            return false;
        }
        unlockedTalents.add(talentId);
        unspentTalentPoints--;
        return true;
    }

    public boolean addXp(int amount) {
        if (amount <= 0 || level >= MAX_LEVEL) {
            return false;
        }

        xp += amount;
        boolean leveled = false;
        while (level < MAX_LEVEL && xp >= XP_THRESHOLDS[level]) {
            level++;
            unspentTalentPoints++;
            leveled = true;
        }
        return leveled;
    }

    public void normalizeTalentPoints() {
        int earnedPoints = level;
        int assignedPoints = unlockedTalents.size() + unspentTalentPoints;
        if (assignedPoints < earnedPoints) {
            unspentTalentPoints += earnedPoints - assignedPoints;
        }
    }

    public Properties save() {
        Properties properties = new Properties();
        properties.setProperty("class", playerClass.name());
        properties.setProperty("level", Integer.toString(level));
        properties.setProperty("xp", Integer.toString(xp));
        properties.setProperty("points", Integer.toString(unspentTalentPoints));
        properties.setProperty("selected", selectedActiveTalent);
        properties.setProperty("selectedPassive", selectedPassiveTalent);
        properties.setProperty("selectedUltimate", selectedUltimateTalent);
        properties.setProperty("powerStrikeArmed", Boolean.toString(powerStrikeArmed));
        properties.setProperty("mightyCrushPending", Boolean.toString(mightyCrushPending));
        properties.setProperty("talents", String.join(",", unlockedTalents));
        return properties;
    }

    public static PlayerProgressionData load(Properties properties) {
        PlayerProgressionData data = new PlayerProgressionData();
        data.playerClass = parseClass(properties.getProperty("class", "NONE"));
        data.level = clamp(parseInt(properties.getProperty("level"), 1), 1, MAX_LEVEL);
        data.xp = Math.max(0, parseInt(properties.getProperty("xp"), 0));
        data.unspentTalentPoints = Math.max(0, parseInt(properties.getProperty("points"), 0));
        data.selectedActiveTalent = properties.getProperty("selected", "");
        data.selectedPassiveTalent = properties.getProperty("selectedPassive", "");
        data.selectedUltimateTalent = properties.getProperty("selectedUltimate", "");
        data.powerStrikeArmed = Boolean.parseBoolean(properties.getProperty("powerStrikeArmed", "false"));
        data.mightyCrushPending = Boolean.parseBoolean(properties.getProperty("mightyCrushPending", "false"));
        String talents = properties.getProperty("talents", "");
        if (!talents.isBlank()) {
            for (String talent : talents.split(",")) {
                if (!talent.isBlank()) {
                    data.unlockedTalents.add(talent);
                }
            }
        }
        data.normalizeTalentPoints();
        return data;
    }

    private static PlayerClass parseClass(String name) {
        try {
            return PlayerClass.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return PlayerClass.NONE;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
