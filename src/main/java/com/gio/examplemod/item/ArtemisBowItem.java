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
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ArtemisBowItem extends BowItem implements DivinePathWeaponItem {
    public ArtemisBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public PlayerClass divinePath() {
        return PlayerClass.RANGER;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            int pathLevel = player instanceof ServerPlayer serverPlayer ? ProgressionManager.get(serverPlayer).level() : 1;
            Vec3 look = player.getLookAngle().normalize();
            Arrow arrow = new Arrow(EntityType.ARROW, serverLevel);
            arrow.setOwner(player);
            arrow.setPos(player.getEyePosition().add(look.scale(0.7D)));
            arrow.setBaseDamage(3.0D + pathLevel * 1.25D);
            arrow.setCritArrow(pathLevel >= 4);
            if (pathLevel >= 3) {
                arrow.igniteForSeconds(5.0F);
            }
            arrow.shoot(look.x, look.y, look.z, 2.6F + pathLevel * 0.12F, 0.5F);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            serverLevel.addFreshEntity(arrow);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.75F, 1.35F + pathLevel * 0.05F);
            serverLevel.sendParticles(pathLevel >= 3 ? ParticleTypes.FLAME : ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 6 + pathLevel, 0.2D, 0.25D, 0.2D, 0.02D);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 10);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Artemis weapon - soulbound").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.accept(Component.literal("Right-click: fire a divine arrow without consuming ammo.").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Level 3: arrows ignite. Level 4: arrows become critical.").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
    }
}
