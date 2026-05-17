package com.gio.examplemod.network;

import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.progression.ProgressionManager;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectActiveTalentPayload(String talentId) implements CustomPacketPayload {
    public static final Type<SelectActiveTalentPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "select_active_talent"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectActiveTalentPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.talentId()),
            buf -> new SelectActiveTalentPayload(buf.readUtf(64))
    );

    @Override
    public Type<SelectActiveTalentPayload> type() {
        return TYPE;
    }

    public static void handle(SelectActiveTalentPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ProgressionManager.selectActiveTalent(player, payload.talentId());
        }
    }
}
