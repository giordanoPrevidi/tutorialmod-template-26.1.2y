package com.gio.examplemod.network;

import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.progression.PlayerClass;
import com.gio.examplemod.progression.ProgressionManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChooseClassPayload(int classIndex) implements CustomPacketPayload {
    public static final Type<ChooseClassPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "choose_class"));
    public static final StreamCodec<ByteBuf, ChooseClassPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
            ChooseClassPayload::new,
            ChooseClassPayload::classIndex
    );

    @Override
    public Type<ChooseClassPayload> type() {
        return TYPE;
    }

    public static void handle(ChooseClassPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ProgressionManager.chooseClass(player, PlayerClass.byIndex(payload.classIndex()));
        }
    }
}
