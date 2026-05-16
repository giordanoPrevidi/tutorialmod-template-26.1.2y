package com.gio.examplemod.client;

import com.gio.examplemod.item.ArcaneHourglassStaffItem;
import com.gio.examplemod.magic.MagicSpell;
import com.gio.examplemod.network.SelectStaffSpellPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class StaffSpellSelectionScreen extends Screen {
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public StaffSpellSelectionScreen() {
        super(Component.translatable("screen.tutorialmod.staff_spell_selection"));
    }

    @Override
    protected void init() {
        int columns = 2;
        int rows = (int) Math.ceil(MagicSpell.values().length / (double) columns);
        int gap = 6;
        int cardHeight = 22;
        int headerHeight = 48;

        this.panelWidth = Math.min(342, this.width - 28);
        this.panelHeight = headerHeight + rows * cardHeight + (rows - 1) * gap + 18;
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        int contentLeft = this.panelLeft + 14;
        int contentTop = this.panelTop + headerHeight;
        int cardWidth = (this.panelWidth - 28 - gap) / 2;

        for (MagicSpell spell : MagicSpell.values()) {
            int column = spell.ordinal() % columns;
            int row = spell.ordinal() / columns;
            int x = contentLeft + column * (cardWidth + gap);
            int y = contentTop + row * (cardHeight + gap);
            this.addRenderableWidget(new SpellCardWidget(x, y, cardWidth, cardHeight, spell));
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
        extractor.fill(this.panelLeft - 1, this.panelTop - 1, right + 1, bottom + 1, 0xAA6D5CA0);
        extractor.fill(this.panelLeft, this.panelTop, right, bottom, 0xF1111016);
        extractor.fill(this.panelLeft, this.panelTop, right, this.panelTop + 2, 0xFFBFA7FF);
        extractor.fill(this.panelLeft + 10, this.panelTop + 38, right - 10, this.panelTop + 39, 0x442A2534);

        MagicSpell selected = selectedSpell();
        extractor.text(this.font, Component.translatable("screen.tutorialmod.staff_spell_selection"), this.panelLeft + 14, this.panelTop + 10, 0xFFF3EFFB);
        extractor.text(this.font, Component.translatable("screen.tutorialmod.staff_spell_selection.current", selected.displayName()), this.panelLeft + 14, this.panelTop + 25, 0xFFBFC7D8);
        extractor.text(this.font, Component.translatable("screen.tutorialmod.staff_spell_selection.close"), right - 68, this.panelTop + 10, 0xFF6F7788);

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    private MagicSpell selectedSpell() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return MagicSpell.ARCANE_FIRE;
        }

        ItemStack mainHand = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.getItem() instanceof ArcaneHourglassStaffItem) {
            return ArcaneHourglassStaffItem.getSelectedSpell(mainHand);
        }

        ItemStack offHand = minecraft.player.getItemInHand(InteractionHand.OFF_HAND);
        if (offHand.getItem() instanceof ArcaneHourglassStaffItem) {
            return ArcaneHourglassStaffItem.getSelectedSpell(offHand);
        }

        return MagicSpell.ARCANE_FIRE;
    }

    private class SpellCardWidget extends AbstractWidget {
        private final MagicSpell spell;

        SpellCardWidget(int x, int y, int width, int height, MagicSpell spell) {
            super(x, y, width, height, spell.displayName());
            this.spell = spell;
            this.setTooltip(Tooltip.create(spell.description()));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
            boolean selected = selectedSpell() == this.spell;
            int border = selected ? 0xFFE9D37C : 0x553A3445;
            int fill = selected ? 0xFF26202E : (this.isHoveredOrFocused() ? 0xFF201B28 : 0xFF17141B);
            int accent = schoolColor(this.spell, false);

            extractor.fill(getX(), getY(), getRight(), getBottom(), border);
            extractor.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, fill);
            extractor.fill(getX() + 1, getY() + 1, getX() + 4, getBottom() - 1, accent);
            extractor.text(font, this.spell.shortcut(), getX() + 10, getY() + 7, selected ? 0xFFFFE8A6 : 0xFF8F96A3);
            extractor.text(font, this.spell.displayName(), getX() + 28, getY() + 7, selected ? 0xFFFFF4C6 : 0xFFE3DEE9);
            if (selected) {
                extractor.fill(getRight() - 8, getY() + 8, getRight() - 4, getY() + 12, 0xFFE9D37C);
            }
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            ClientPacketDistributor.sendToServer(new SelectStaffSpellPayload(this.spell.ordinal()));
            Minecraft.getInstance().setScreen(null);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static int schoolColor(MagicSpell spell, boolean muted) {
        return switch (spell.schoolId()) {
            case "necromancy" -> muted ? 0xFF334020 : 0xFF8DBA38;
            case "restoration" -> muted ? 0xFF564A24 : 0xFFFFD36A;
            case "storm" -> muted ? 0xFF1E4652 : 0xFF4EDBFF;
            case "ward" -> muted ? 0xFF34513B : 0xFF78D98A;
            default -> muted ? 0xFF46345F : 0xFFC884FF;
        };
    }
}
