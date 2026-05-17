package com.gio.examplemod.client;

import com.gio.examplemod.talent.Talent;
import com.gio.examplemod.talent.TalentRegistry;
import com.gio.examplemod.progression.PlayerClass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class TalentHudOverlay {
    private TalentHudOverlay() {
    }

    public static void render(GuiGraphicsExtractor extractor) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        if (ClientProgressionData.thundersWrathActive()) {
            extractor.fill(0, 0, extractor.guiWidth(), extractor.guiHeight(), 0x66000618);
            renderUltimateActive(extractor, minecraft);
        }

        renderAttention(extractor, minecraft);

        int x = extractor.guiWidth() - 102;
        int y = extractor.guiHeight() / 2 + 18;
        Talent active = TalentRegistry.byId(ClientProgressionData.selectedActiveTalent());
        Talent passive = TalentRegistry.byId(ClientProgressionData.selectedPassiveTalent());
        Talent ultimate = TalentRegistry.byId(ClientProgressionData.selectedUltimateTalent());

        if (active != null) {
            renderSlot(extractor, minecraft, active, x, y, ClientProgressionData.selectedCooldownTicks());
            y -= 17;
        }
        if (passive != null) {
            renderPassive(extractor, minecraft, passive, x, y);
            y -= 17;
        }
        if (ultimate != null) {
            renderSlot(extractor, minecraft, ultimate, x, y, ClientProgressionData.selectedUltimateCooldownTicks());
        }
    }

    private static void renderUltimateActive(GuiGraphicsExtractor extractor, Minecraft minecraft) {
        int ticks = ClientProgressionData.activeUltimateTicks();
        int seconds = Math.max(1, (ticks + 19) / 20);
        int width = 132;
        int x = extractor.guiWidth() / 2 - width / 2;
        int y = 18;
        int barWidth = width - 8;
        int filled = Math.max(0, Math.min(barWidth, ticks * barWidth / 600));

        extractor.fill(x, y, x + width, y + 20, 0x88101824);
        extractor.fill(x + 1, y + 1, x + width - 1, y + 19, 0xAA172133);
        extractor.fill(x + 4, y + 15, x + width - 4, y + 17, 0x88313A52);
        extractor.fill(x + 4, y + 15, x + 4 + filled, y + 17, 0xFFEED66F);
        extractor.text(minecraft.font, Component.literal("Thunder's Wrath"), x + 7, y + 5, 0xFFFFE6A3);
        extractor.text(minecraft.font, Component.literal(seconds + "s"), x + width - 26, y + 5, 0xFFDDE6EE);
    }

    private static void renderAttention(GuiGraphicsExtractor extractor, Minecraft minecraft) {
        Component label = null;
        if (ClientProgressionData.playerClass() == PlayerClass.NONE) {
            label = Component.translatable("hud.tutorialmod.path_available");
        } else if (ClientProgressionData.points() > 0) {
            label = Component.translatable("hud.tutorialmod.points_available", ClientProgressionData.points());
        }

        if (label == null) {
            return;
        }

        int width = 68;
        int x = extractor.guiWidth() - width - 8;
        int y = 10;
        extractor.fill(x, y, x + width, y + 15, 0x77101418);
        extractor.fill(x + 1, y + 1, x + width - 1, y + 14, 0x991F2630);
        extractor.fill(x + 4, y + 4, x + 8, y + 8, 0xFFEED66F);
        extractor.text(minecraft.font, label, x + 13, y + 4, 0xFFFFE6A3);
    }

    private static void renderSlot(GuiGraphicsExtractor extractor, Minecraft minecraft, Talent talent, int x, int y, int cooldownTicks) {
        int width = 92;
        int height = 15;
        boolean charged = "warrior_power_strike".equals(talent.id()) && ClientProgressionData.powerStrikeArmed();

        int accent = charged ? 0xFFEED66F : cooldownTicks > 0 ? 0xFF8FA1B4 : 0xFF7ED6A4;
        int fill = charged ? 0xAA2C2518 : 0x99101418;
        extractor.fill(x, y, x + width, y + height, 0x661C2329);
        extractor.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
        extractor.fill(x + 3, y + 4, x + 7, y + 8, accent);

        Component status = charged
                ? Component.translatable("hud.tutorialmod.talent_charged_short")
                : cooldownTicks > 0
                        ? Component.translatable("hud.tutorialmod.talent_cooldown", Math.max(1, (cooldownTicks + 19) / 20))
                        : Component.translatable("hud.tutorialmod.talent_ready_short");

        extractor.text(minecraft.font, compactName(talent), x + 11, y + 4, 0xFFDDE6EE);
        extractor.text(minecraft.font, status, x + width - 24, y + 4, charged ? 0xFFFFE6A3 : cooldownTicks > 0 ? 0xFFB8C1CC : 0xFFBDF4D1);

        if (cooldownTicks > 0 && talent.cooldownTicks() > 0) {
            int barWidth = width - 6;
            int readyWidth = Math.max(0, Math.min(barWidth, barWidth - cooldownTicks * barWidth / talent.cooldownTicks()));
            extractor.fill(x + 3, y + height - 2, x + width - 3, y + height - 1, 0x88313A42);
            extractor.fill(x + 3, y + height - 2, x + 3 + readyWidth, y + height - 1, accent);
        }
    }

    private static void renderPassive(GuiGraphicsExtractor extractor, Minecraft minecraft, Talent talent, int x, int y) {
        int width = 92;
        int height = 15;
        extractor.fill(x, y, x + width, y + height, 0x552B3326);
        extractor.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x8820261E);
        extractor.fill(x + 3, y + 4, x + 7, y + 8, 0xFF9FE870);
        extractor.text(minecraft.font, compactName(talent), x + 11, y + 4, 0xFFDDE6EE);
        extractor.text(minecraft.font, Component.translatable("hud.tutorialmod.passive_short"), x + width - 20, y + 4, 0xFFBDF4D1);
    }

    private static Component compactName(Talent talent) {
        if ("warrior_power_strike".equals(talent.id())) {
            return Component.literal("Mighty");
        }
        String id = talent.id();
        int index = id.lastIndexOf('_');
        String name = index >= 0 ? id.substring(index + 1) : id;
        return Component.literal(name.length() > 7 ? name.substring(0, 7) : name);
    }
}
