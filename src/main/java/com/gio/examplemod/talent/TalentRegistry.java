package com.gio.examplemod.talent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.gio.examplemod.progression.PlayerClass;

public final class TalentRegistry {
    public static final List<Talent> TALENTS = List.of(
            new Talent("warrior_power_strike", PlayerClass.WARRIOR, 1, TalentKind.ACTIVE, 120),
            new Talent("warrior_steel_skin", PlayerClass.WARRIOR, 2, TalentKind.PASSIVE, 0),
            new Talent("warrior_charge", PlayerClass.WARRIOR, 3, TalentKind.ACTIVE, 240),
            new Talent("warrior_fury", PlayerClass.WARRIOR, 4, TalentKind.PASSIVE, 0),
            new Talent("warrior_earthshatter", PlayerClass.WARRIOR, 5, TalentKind.ULTIMATE, 3600),
            new Talent("mage_arcane_bolt", PlayerClass.MAGE, 1, TalentKind.SPELL, 80),
            new Talent("mage_mana_reserve", PlayerClass.MAGE, 2, TalentKind.PASSIVE, 0),
            new Talent("mage_ethereal_step", PlayerClass.MAGE, 3, TalentKind.ACTIVE, 160),
            new Talent("mage_runic_shield", PlayerClass.MAGE, 4, TalentKind.PASSIVE, 0),
            new Talent("mage_arcane_storm", PlayerClass.MAGE, 5, TalentKind.ULTIMATE, 600),
            new Talent("ranger_precise_shot", PlayerClass.RANGER, 1, TalentKind.PASSIVE, 0),
            new Talent("ranger_silent_step", PlayerClass.RANGER, 2, TalentKind.PASSIVE, 0),
            new Talent("ranger_trap", PlayerClass.RANGER, 3, TalentKind.ACTIVE, 220),
            new Talent("ranger_barrage", PlayerClass.RANGER, 4, TalentKind.ACTIVE, 240),
            new Talent("ranger_hunters_mark", PlayerClass.RANGER, 5, TalentKind.ULTIMATE, 600)
    );

    private TalentRegistry() {
    }

    public static Talent byId(String id) {
        return TALENTS.stream().filter(talent -> talent.id().equals(id)).findFirst().orElse(null);
    }

    public static List<Talent> forClass(PlayerClass playerClass) {
        return TALENTS.stream()
                .filter(talent -> talent.playerClass() == playerClass)
                .sorted(Comparator.comparingInt(Talent::level))
                .toList();
    }

    public static List<String> allIds() {
        return TALENTS.stream().map(Talent::id).toList();
    }

    public static boolean isTalentId(String id) {
        return Arrays.stream(TALENTS.toArray(Talent[]::new)).anyMatch(talent -> talent.id().equals(id));
    }
}
