package com.gio.examplemod.network;

import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.progression.ProgressionManager;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CastTalentPayload() implements CustomPacketPayload {
    public static final Type<CastTalentPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "cast_talent"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, CastTalentPayload> STREAM_CODEC = StreamCodec.unit(new CastTalentPayload());

    @Override
    public Type<CastTalentPayload> type() {
        return TYPE;
    }

    public static void handle(CastTalentPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ProgressionManager.castSelectedTalent(player);
        }
    }
}
