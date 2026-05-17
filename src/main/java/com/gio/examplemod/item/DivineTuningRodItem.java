package com.gio.examplemod.item;

import com.gio.examplemod.progression.ProgressionManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class DivineTuningRodItem extends Item {
    public DivineTuningRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.isCreative()) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.admin_only").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        if (player.isSecondaryUseActive()) {
            ProgressionManager.resetForTesting(serverPlayer);
        } else {
            ProgressionManager.increaseLevelForTesting(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
