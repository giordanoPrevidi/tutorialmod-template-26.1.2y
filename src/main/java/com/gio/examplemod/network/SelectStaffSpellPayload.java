package com.gio.examplemod.network;

import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.item.ArcaneHourglassStaffItem;
import com.gio.examplemod.magic.MagicSpell;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectStaffSpellPayload(int spellIndex) implements CustomPacketPayload {
    public static final Type<SelectStaffSpellPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "select_staff_spell"));
    public static final StreamCodec<ByteBuf, SelectStaffSpellPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
            SelectStaffSpellPayload::new,
            SelectStaffSpellPayload::spellIndex
    );

    @Override
    public Type<SelectStaffSpellPayload> type() {
        return TYPE;
    }

    public static void handle(SelectStaffSpellPayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = findHeldStaff(player);

        if (!stack.isEmpty() && stack.getItem() instanceof ArcaneHourglassStaffItem) {
            MagicSpell spell = MagicSpell.byIndex(payload.spellIndex());
            ArcaneHourglassStaffItem.setSelectedSpell(stack, spell);
            ArcaneHourglassStaffItem.showSelectedSpell(player, spell);
        }
    }

    private static ItemStack findHeldStaff(Player player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.getItem() instanceof ArcaneHourglassStaffItem) {
            return mainHand;
        }

        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offHand.getItem() instanceof ArcaneHourglassStaffItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}
