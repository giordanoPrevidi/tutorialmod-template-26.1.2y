package com.gio.examplemod.item;

import com.gio.examplemod.progression.PlayerClass;
import com.gio.examplemod.progression.ProgressionManager;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class EirScepterItem extends Item implements DivinePathWeaponItem {
    public EirScepterItem(Properties properties) {
        super(properties);
    }

    @Override
    public PlayerClass divinePath() {
        return PlayerClass.MAGE;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            int pathLevel = player instanceof ServerPlayer serverPlayer ? ProgressionManager.get(serverPlayer).level() : 1;
            player.heal(4.0F + pathLevel * 1.5F);
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 180 + pathLevel * 35, pathLevel >= 4 ? 2 : 1));
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.35F);
            serverLevel.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0D, player.getZ(), 5 + pathLevel, 0.35D, 0.45D, 0.35D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 8 + pathLevel * 2, 0.35D, 0.45D, 0.35D, 0.02D);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), Math.max(80, 180 - pathLevel * 12));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Eir weapon - soulbound").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.literal("Right-click: heal yourself and gain absorption.").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Level scaling: stronger healing, longer wards, shorter cooldown.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
