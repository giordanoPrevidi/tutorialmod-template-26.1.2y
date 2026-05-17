package com.gio.examplemod.network;

import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.progression.ProgressionManager;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UnlockTalentPayload(String talentId) implements CustomPacketPayload {
    public static final Type<UnlockTalentPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "unlock_talent"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockTalentPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.talentId()),
            buf -> new UnlockTalentPayload(buf.readUtf(64))
    );

    @Override
    public Type<UnlockTalentPayload> type() {
        return TYPE;
    }

    public static void handle(UnlockTalentPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ProgressionManager.unlockTalent(player, payload.talentId());
        }
    }
}
