package com.gio.examplemod.magic;

import java.util.Locale;

import net.minecraft.network.chat.Component;

public enum MagicSpell {
    ARCANE_FIRE("Z"),
    WIND_BURST("X"),
    HEALING_LIGHT("C"),
    BLINK_STEP("V"),
    STORM_LANCE("B"),
    FROST_BIND("N"),
    EARTHEN_GUARD("M"),
    SOLAR_FLARE("Y"),
    WITHER_TOUCH("H"),
    SOUL_DRAIN("J"),
    BONE_MINION("K"),
    GRAVE_MIST("L");

    private final String shortcut;

    MagicSpell(String shortcut) {
        this.shortcut = shortcut;
    }

    public static MagicSpell byIndex(int index) {
        MagicSpell[] spells = values();
        return spells[Math.floorMod(index, spells.length)];
    }

    public MagicSpell next() {
        return byIndex(ordinal() + 1);
    }

    public Component displayName() {
        return Component.translatable("spell.tutorialmod." + id());
    }

    public Component description() {
        return Component.translatable("spell.tutorialmod." + id() + ".description");
    }

    public Component menuLabel() {
        return Component.literal(shortcut + "  ").append(displayName());
    }

    public String shortcut() {
        return shortcut;
    }

    public String schoolId() {
        return switch (this) {
            case WITHER_TOUCH, SOUL_DRAIN, BONE_MINION, GRAVE_MIST -> "necromancy";
            case HEALING_LIGHT -> "restoration";
            case WIND_BURST, STORM_LANCE, FROST_BIND -> "storm";
            case EARTHEN_GUARD -> "ward";
            default -> "arcane";
        };
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
