package com.gio.examplemod.client;

import com.gio.examplemod.network.SyncProgressionPayload;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientProgressionPayloadHandler {
    private ClientProgressionPayloadHandler() {
    }

    public static void handle(SyncProgressionPayload payload, IPayloadContext context) {
        ClientProgressionData.update(payload);
        if (payload.openMenu()) {
            Minecraft.getInstance().setScreen(new TalentTreeScreen());
        }
    }
}
