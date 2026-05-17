package com.gio.examplemod.talent;

import com.gio.examplemod.progression.PlayerClass;

import net.minecraft.network.chat.Component;

public record Talent(String id, PlayerClass playerClass, int level, TalentKind kind, int cooldownTicks) {
    public Component displayName() {
        return Component.translatable("talent.tutorialmod." + id);
    }

    public Component description() {
        return Component.translatable("talent.tutorialmod." + id + ".description");
    }

    public boolean active() {
        return kind == TalentKind.ACTIVE || kind == TalentKind.SPELL || kind == TalentKind.POWER;
    }

    public boolean passive() {
        return kind == TalentKind.PASSIVE;
    }

    public boolean ultimate() {
        return kind == TalentKind.ULTIMATE;
    }
}
