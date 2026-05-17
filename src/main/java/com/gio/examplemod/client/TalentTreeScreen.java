package com.gio.examplemod.client;

import com.gio.examplemod.network.ChooseClassPayload;
import com.gio.examplemod.network.SelectActiveTalentPayload;
import com.gio.examplemod.network.UnlockTalentPayload;
import com.gio.examplemod.progression.PlayerClass;
import com.gio.examplemod.talent.Talent;
import com.gio.examplemod.talent.TalentRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class TalentTreeScreen extends Screen {
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public TalentTreeScreen() {
        super(Component.translatable("screen.tutorialmod.talents"));
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(380, this.width - 28);
        this.panelHeight = ClientProgressionData.playerClass() == PlayerClass.NONE ? 164 : 238;
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        if (ClientProgressionData.playerClass() == PlayerClass.NONE) {
            int buttonWidth = (this.panelWidth - 44) / 3;
            int y = this.panelTop + 82;
            addRenderableWidget(new ClassButton(this.panelLeft + 14, y, buttonWidth, 54, PlayerClass.WARRIOR));
            addRenderableWidget(new ClassButton(this.panelLeft + 22 + buttonWidth, y, buttonWidth, 54, PlayerClass.MAGE));
            addRenderableWidget(new ClassButton(this.panelLeft + 30 + buttonWidth * 2, y, buttonWidth, 54, PlayerClass.RANGER));
            return;
        }

        int y = this.panelTop + 66;
        for (Talent talent : TalentRegistry.forClass(ClientProgressionData.playerClass())) {
            addRenderableWidget(new TalentButton(this.panelLeft + 16, y, this.panelWidth - 32, 28, talent));
            y += 32;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(extractor);
        extractor.fill(0, 0, this.width, this.height, 0x9A050608);

        int right = this.panelLeft + this.panelWidth;
        int bottom = this.panelTop + this.panelHeight;
        extractor.fill(this.panelLeft - 1, this.panelTop - 1, right + 1, bottom + 1, 0xAA516782);
        extractor.fill(this.panelLeft, this.panelTop, right, bottom, 0xF1101418);
        extractor.fill(this.panelLeft, this.panelTop, right, this.panelTop + 2, 0xFF7ED6A4);
        extractor.text(this.font, Component.translatable("screen.tutorialmod.talents"), this.panelLeft + 14, this.panelTop + 10, 0xFFF4F7FB);

        if (ClientProgressionData.playerClass() == PlayerClass.NONE) {
            extractor.text(this.font, Component.translatable("screen.tutorialmod.choose_class"), this.panelLeft + 14, this.panelTop + 34, 0xFFC9D2DD);
        } else {
            extractor.text(this.font, Component.translatable("screen.tutorialmod.talent_status",
                    ClientProgressionData.playerClass().displayName(),
                    ClientProgressionData.level(),
                    ClientProgressionData.xp(),
                    ClientProgressionData.nextXp(),
                    ClientProgressionData.points()), this.panelLeft + 14, this.panelTop + 30, 0xFFC9D2DD);
            extractor.text(this.font, Component.translatable("screen.tutorialmod.selected_slots", selectedName(ClientProgressionData.selectedActiveTalent()), selectedName(ClientProgressionData.selectedPassiveTalent()), selectedName(ClientProgressionData.selectedUltimateTalent())), this.panelLeft + 14, this.panelTop + 45, 0xFF8FA1B4);
        }

        extractor.text(this.font, Component.translatable("screen.tutorialmod.talents.close"), right - 68, this.panelTop + 10, 0xFF6F7788);
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    private Component selectedName(String talentId) {
        Talent selected = TalentRegistry.byId(talentId);
        return selected == null ? Component.translatable("screen.tutorialmod.none") : selected.displayName();
    }

    private static int classColor(PlayerClass playerClass) {
        return switch (playerClass) {
            case WARRIOR -> 0xFFE07A54;
            case MAGE -> 0xFF7DB7FF;
            case RANGER -> 0xFF7ED6A4;
            default -> 0xFF9CA3AF;
        };
    }

    private class ClassButton extends AbstractWidget {
        private final PlayerClass playerClass;

        ClassButton(int x, int y, int width, int height, PlayerClass playerClass) {
            super(x, y, width, height, playerClass.displayName());
            this.playerClass = playerClass;
            this.setTooltip(Tooltip.create(Component.translatable("class.tutorialmod." + playerClass.id() + ".description")));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
            int fill = isHoveredOrFocused() ? 0xFF20272F : 0xFF171D22;
            extractor.fill(getX(), getY(), getRight(), getBottom(), classColor(playerClass));
            extractor.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, fill);
            extractor.text(font, playerClass.displayName(), getX() + 8, getY() + 7, 0xFFF4F7FB);
            extractor.textWithWordWrap(font, Component.translatable("class.tutorialmod." + playerClass.id() + ".short"), getX() + 8, getY() + 19, getWidth() - 14, 0xFF9EADBB);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            ClientPacketDistributor.sendToServer(new ChooseClassPayload(playerClass.ordinal()));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private class TalentButton extends AbstractWidget {
        private final Talent talent;

        TalentButton(int x, int y, int width, int height, Talent talent) {
            super(x, y, width, height, talent.displayName());
            this.talent = talent;
            this.setTooltip(Tooltip.create(talent.description()));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
            boolean unlocked = ClientProgressionData.hasTalent(talent.id());
            boolean available = !unlocked && ClientProgressionData.level() >= talent.level() && ClientProgressionData.points() > 0;
            boolean selected = talent.id().equals(ClientProgressionData.selectedActiveTalent())
                    || talent.id().equals(ClientProgressionData.selectedPassiveTalent())
                    || talent.id().equals(ClientProgressionData.selectedUltimateTalent());
            int border = selected ? 0xFFEED66F : available ? 0xFF7ED6A4 : unlocked ? 0xFF7DB7FF : 0xFF3A4651;
            int fill = isHoveredOrFocused() ? 0xFF20272F : 0xFF171D22;

            extractor.fill(getX(), getY(), getRight(), getBottom(), border);
            extractor.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, fill);
            extractor.text(font, Component.literal("Lv " + talent.level()), getX() + 8, getY() + 10, 0xFF8FA1B4);
            extractor.text(font, talent.displayName(), getX() + 48, getY() + 10, unlocked ? 0xFFF4F7FB : available ? 0xFFBDF4D1 : 0xFF7B8794);
            extractor.text(font, Component.translatable("talent_kind.tutorialmod." + talent.kind().name().toLowerCase()), getRight() - 72, getY() + 10, 0xFF8FA1B4);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            if (ClientProgressionData.hasTalent(talent.id()) && (talent.active() || talent.passive() || talent.ultimate())) {
                ClientPacketDistributor.sendToServer(new SelectActiveTalentPayload(talent.id()));
                return;
            }
            ClientPacketDistributor.sendToServer(new UnlockTalentPayload(talent.id()));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
