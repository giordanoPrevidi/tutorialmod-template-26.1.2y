package com.gio.examplemod.network;

import java.util.HashSet;
import java.util.Set;

import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.progression.PlayerClass;
import com.gio.examplemod.progression.PlayerProgressionData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncProgressionPayload(
        int classIndex,
        int level,
        int xp,
        int nextXp,
        int unspentTalentPoints,
        String selectedActiveTalent,
        String selectedPassiveTalent,
        String selectedUltimateTalent,
        int selectedCooldownTicks,
        int selectedUltimateCooldownTicks,
        int activeUltimateTicks,
        boolean powerStrikeArmed,
        boolean thundersWrathActive,
        Set<String> unlockedTalents,
        boolean openMenu
) implements CustomPacketPayload {
    public static final Type<SyncProgressionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "sync_progression"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncProgressionPayload> STREAM_CODEC = StreamCodec.of(
            SyncProgressionPayload::write,
            SyncProgressionPayload::read
    );

    public static SyncProgressionPayload from(PlayerProgressionData data, int selectedCooldownTicks, int selectedUltimateCooldownTicks, int activeUltimateTicks, boolean thundersWrathActive, boolean openMenu) {
        return new SyncProgressionPayload(
                data.playerClass().ordinal(),
                data.level(),
                data.xp(),
                data.xpForNextLevel(),
                data.unspentTalentPoints(),
                data.selectedActiveTalent(),
                data.selectedPassiveTalent(),
                data.selectedUltimateTalent(),
                selectedCooldownTicks,
                selectedUltimateCooldownTicks,
                activeUltimateTicks,
                data.powerStrikeArmed(),
                thundersWrathActive,
                new HashSet<>(data.unlockedTalents()),
                openMenu
        );
    }

    @Override
    public Type<SyncProgressionPayload> type() {
        return TYPE;
    }

    private static void write(RegistryFriendlyByteBuf buf, SyncProgressionPayload payload) {
        buf.writeVarInt(payload.classIndex());
        buf.writeVarInt(payload.level());
        buf.writeVarInt(payload.xp());
        buf.writeVarInt(payload.nextXp());
        buf.writeVarInt(payload.unspentTalentPoints());
        buf.writeUtf(payload.selectedActiveTalent());
        buf.writeUtf(payload.selectedPassiveTalent());
        buf.writeUtf(payload.selectedUltimateTalent());
        buf.writeVarInt(payload.selectedCooldownTicks());
        buf.writeVarInt(payload.selectedUltimateCooldownTicks());
        buf.writeVarInt(payload.activeUltimateTicks());
        buf.writeBoolean(payload.powerStrikeArmed());
        buf.writeBoolean(payload.thundersWrathActive());
        buf.writeVarInt(payload.unlockedTalents().size());
        for (String talent : payload.unlockedTalents()) {
            buf.writeUtf(talent);
        }
        buf.writeBoolean(payload.openMenu());
    }

    private static SyncProgressionPayload read(RegistryFriendlyByteBuf buf) {
        int classIndex = buf.readVarInt();
        int level = buf.readVarInt();
        int xp = buf.readVarInt();
        int nextXp = buf.readVarInt();
        int points = buf.readVarInt();
        String selected = buf.readUtf(64);
        String selectedPassive = buf.readUtf(64);
        String selectedUltimate = buf.readUtf(64);
        int cooldownTicks = buf.readVarInt();
        int ultimateCooldownTicks = buf.readVarInt();
        int activeUltimateTicks = buf.readVarInt();
        boolean powerStrikeArmed = buf.readBoolean();
        boolean thundersWrathActive = buf.readBoolean();
        int count = buf.readVarInt();
        Set<String> talents = new HashSet<>();
        for (int i = 0; i < count; i++) {
            talents.add(buf.readUtf(64));
        }
        return new SyncProgressionPayload(classIndex, level, xp, nextXp, points, selected, selectedPassive, selectedUltimate, cooldownTicks, ultimateCooldownTicks, activeUltimateTicks, powerStrikeArmed, thundersWrathActive, talents, buf.readBoolean());
    }

    public PlayerClass playerClass() {
        return PlayerClass.byIndex(classIndex);
    }
}
