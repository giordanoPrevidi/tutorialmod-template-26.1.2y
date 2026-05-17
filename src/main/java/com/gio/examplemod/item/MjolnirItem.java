package com.gio.examplemod.item;

import com.gio.examplemod.progression.PlayerClass;
import com.gio.examplemod.progression.ProgressionManager;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class MjolnirItem extends MaceItem implements DivinePathWeaponItem {
    public MjolnirItem(Properties properties) {
        super(properties);
    }

    @Override
    public PlayerClass divinePath() {
        return PlayerClass.WARRIOR;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Thor weapon - soulbound").withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.literal("Heavy melee hammer. Bonus magic damage scales with divine level.").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Level scaling: +2.25 to +7.25 magic damage on hit.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel level) {
            int pathLevel = attacker instanceof ServerPlayer player ? ProgressionManager.get(player).level() : 1;
            target.hurtServer(level, level.damageSources().magic(), 1.0F + pathLevel * 1.25F);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.35F + pathLevel * 0.05F, 1.35F);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 8 + pathLevel * 3, 0.25D, 0.35D, 0.25D, 0.04D);
        }
    }
}
