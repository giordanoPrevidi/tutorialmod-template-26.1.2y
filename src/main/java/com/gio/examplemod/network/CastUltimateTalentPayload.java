package com.gio.examplemod.network;

import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.progression.ProgressionManager;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CastUltimateTalentPayload() implements CustomPacketPayload {
    public static final Type<CastUltimateTalentPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "cast_ultimate_talent"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, CastUltimateTalentPayload> STREAM_CODEC = StreamCodec.unit(new CastUltimateTalentPayload());

    @Override
    public Type<CastUltimateTalentPayload> type() {
        return TYPE;
    }

    public static void handle(CastUltimateTalentPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ProgressionManager.castSelectedUltimate(player);
        }
    }
}
