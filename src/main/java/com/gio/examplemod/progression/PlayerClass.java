package com.gio.examplemod.progression;

import java.util.Locale;

import net.minecraft.network.chat.Component;

public enum PlayerClass {
    NONE,
    WARRIOR,
    MAGE,
    RANGER;

    public static PlayerClass byIndex(int index) {
        PlayerClass[] values = values();
        if (index < 0 || index >= values.length) {
            return NONE;
        }
        return values[index];
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Component displayName() {
        return Component.translatable("class.tutorialmod." + id());
    }
}
