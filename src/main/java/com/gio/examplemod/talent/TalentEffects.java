package com.gio.examplemod.talent;

import com.gio.examplemod.progression.PlayerProgressionData;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TalentEffects {
    private TalentEffects() {
    }

    public static boolean cast(ServerPlayer player, PlayerProgressionData data, Talent talent) {
        ServerLevel level = player.level();
        return switch (talent.id()) {
            case "warrior_charge" -> mightyCrushLeap(level, player);
            case "warrior_fury" -> fury(level, player);
            case "warrior_earthshatter" -> earthshatter(level, player);
            case "mage_arcane_bolt" -> arcaneBolt(level, player);
            case "mage_ethereal_step" -> etherealStep(level, player);
            case "mage_arcane_storm" -> arcaneStorm(level, player);
            case "ranger_trap" -> rangerTrap(level, player);
            case "ranger_barrage" -> barrage(level, player);
            case "ranger_hunters_mark" -> huntersMark(level, player);
            default -> false;
        };
    }

    public static void applyPassive(ServerPlayer player, PlayerProgressionData data, Talent talent) {
        if (data.selectedPassiveIs("ranger_silent_step")) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 0, false, false));
        }
        if (data.selectedPassiveIs("mage_runic_shield")) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 220, 0, false, false));
        }
    }

    public static float modifyIncomingDamage(ServerPlayer player, PlayerProgressionData data, float amount) {
        if (data.selectedPassiveIs("warrior_steel_skin")) {
            amount *= 0.85F;
        }
        if (data.selectedPassiveIs("mage_mana_reserve") && player.hasEffect(MobEffects.ABSORPTION)) {
            amount *= 0.9F;
        }
        if (data.selectedPassiveIs("mage_runic_shield")) {
            amount *= 0.9F;
        }
        return amount;
    }

    public static float modifyOutgoingDamage(ServerPlayer player, PlayerProgressionData data, float amount) {
        if (data.selectedPassiveIs("warrior_fury")) {
            amount *= 1.25F;
        }
        if (data.selectedPassiveIs("ranger_precise_shot") && (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem)) {
            amount *= 1.2F;
        }
        return amount;
    }

    public static void armPowerStrike(ServerPlayer player) {
        ServerLevel level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.35F, 1.6F);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0D, player.getZ(), 18, 0.35D, 0.4D, 0.35D, 0.02D);
    }

    public static void burstPowerStrike(ServerPlayer player, LivingEntity target) {
        ServerLevel level = player.level();
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.35F);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 0.85F);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 8, 0.2D, 0.45D, 0.2D, 0.08D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 36, 0.55D, 0.75D, 0.55D, 0.08D);
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 28, 0.45D, 0.45D, 0.45D, 0.08D);
    }

    public static void startThundersWrath(ServerPlayer player) {
        ServerLevel level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.25F, 0.55F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.0F, 0.75F);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.1D, player.getZ(), 80, 1.4D, 1.0D, 1.4D, 0.12D);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.1D, player.getZ(), 30, 0.8D, 0.9D, 0.8D, 0.1D);
    }

    public static void thundersWrathStrike(ServerPlayer player, LivingEntity target) {
        ServerLevel level = player.level();
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.05F);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.75F, 1.55F);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 42, 0.45D, 0.75D, 0.45D, 0.1D);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.25D, 0.45D, 0.25D, 0.08D);
    }

    public static void thundersWrathAmbientThunder(ServerPlayer player) {
        ServerLevel level = player.level();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 pos = player.position().add(look.scale(10.0D));
        level.playSound(null, pos.x, pos.y + 6.0D, pos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.85F, 0.75F + player.getRandom().nextFloat() * 0.5F);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 4.0D, pos.z, 14, 1.2D, 1.6D, 1.2D, 0.08D);
    }

    private static boolean mightyCrushLeap(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        player.push(look.x * 1.35D, 0.78D, look.z * 1.35D);
        player.hurtMarked = true;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 0.95F, 0.62F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS, 0.8F, 0.85F);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1D, player.getZ(), 20, 0.35D, 0.08D, 0.35D, 0.04D);
        return true;
    }

    public static void mightyCrushImpact(ServerPlayer player) {
        ServerLevel level = player.level();
        AABB area = player.getBoundingBox().inflate(3.25D, 1.35D, 3.25D);
        for (Entity entity : level.getEntities(player, area, entity -> entity instanceof LivingEntity && entity != player)) {
            Vec3 away = entity.position().subtract(player.position());
            if (away.lengthSqr() < 0.01D) {
                away = player.getLookAngle();
            }
            Vec3 knockback = away.normalize();
            entity.hurtServer(level, level.damageSources().playerAttack(player), 9.0F);
            entity.push(knockback.x * 1.05D, 0.48D, knockback.z * 1.05D);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.65F, 0.62F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.0F, 0.72F);
        level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 0.15D, player.getZ(), 50, 2.1D, 0.22D, 2.1D, 0.08D);
        level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.35D, player.getZ(), 34, 1.7D, 0.28D, 1.7D, 0.08D);
    }

    private static boolean fury(ServerLevel level, ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 220, 1));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 220, 0));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.55F, 1.25F);
        return true;
    }

    private static boolean earthshatter(ServerLevel level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(4.0D, 1.5D, 4.0D);
        for (Entity entity : level.getEntities(player, area, entity -> entity instanceof LivingEntity && entity != player)) {
            entity.hurtServer(level, level.damageSources().playerAttack(player), 8.0F);
            Vec3 away = entity.position().subtract(player.position()).normalize();
            entity.push(away.x * 0.6D, 0.45D, away.z * 0.6D);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7F, 1.4F);
        level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 0.2D, player.getZ(), 50, 3.5D, 0.25D, 3.5D, 0.04D);
        return true;
    }

    private static boolean arcaneBolt(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition().add(look.scale(0.8D));
        SmallFireball fireball = new SmallFireball(level, player, look.scale(1.7D));
        fireball.setPos(origin.x, origin.y - 0.1D, origin.z);
        fireball.setOwner(player);
        fireball.setDeltaMovement(look.scale(1.7D));
        level.addFreshEntity(fireball);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.7F, 1.25F);
        return true;
    }

    private static boolean etherealStep(ServerLevel level, ServerPlayer player) {
        Vec3 target = player.position().add(player.getLookAngle().normalize().multiply(6.0D, 0.0D, 6.0D));
        player.teleportTo(target.x, target.y, target.z);
        level.playSound(null, target.x, target.y, target.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);
        level.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 1.0D, target.z, 28, 0.35D, 0.7D, 0.35D, 0.04D);
        return true;
    }

    private static boolean arcaneStorm(ServerLevel level, ServerPlayer player) {
        Vec3 center = player.getEyePosition().add(player.getLookAngle().normalize().scale(8.0D));
        AABB area = AABB.ofSize(center, 7.0D, 4.0D, 7.0D);
        for (Entity entity : level.getEntities(player, area, entity -> entity instanceof LivingEntity && entity != player)) {
            entity.hurtServer(level, level.damageSources().magic(), 7.0F);
        }
        level.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6F, 1.6F);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, center.x, center.y, center.z, 70, 2.2D, 1.0D, 2.2D, 0.06D);
        return true;
    }

    private static boolean rangerTrap(ServerLevel level, ServerPlayer player) {
        Vec3 center = player.position().add(player.getLookAngle().normalize().multiply(3.0D, 0.0D, 3.0D));
        AABB area = AABB.ofSize(center, 5.0D, 2.0D, 5.0D);
        for (Entity entity : level.getEntities(player, area, entity -> entity instanceof LivingEntity && entity != player)) {
            LivingEntity living = (LivingEntity) entity;
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
            living.hurtServer(level, level.damageSources().playerAttack(player), 4.0F);
        }
        level.playSound(null, center.x, center.y, center.z, SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.8F, 0.8F);
        level.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.2D, center.z, 24, 2.0D, 0.2D, 2.0D, 0.03D);
        return true;
    }

    private static boolean barrage(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        for (int i = -1; i <= 1; i++) {
            Arrow arrow = new Arrow(EntityType.ARROW, level);
            Vec3 right = new Vec3(-look.z, 0.0D, look.x).scale(i * 0.15D);
            arrow.setOwner(player);
            arrow.setPos(player.getEyePosition().add(look.scale(0.8D)).add(right));
            arrow.shoot(look.x + right.x, look.y, look.z + right.z, 2.5F, 4.0F);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    private static boolean huntersMark(ServerLevel level, ServerPlayer player) {
        Vec3 center = player.getEyePosition().add(player.getLookAngle().normalize().scale(10.0D));
        AABB area = AABB.ofSize(center, 4.0D, 4.0D, 4.0D);
        for (Entity entity : level.getEntities(player, area, entity -> entity instanceof LivingEntity && entity != player)) {
            LivingEntity living = (LivingEntity) entity;
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 0));
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_QUICK_CHARGE_3, SoundSource.PLAYERS, 0.7F, 1.2F);
        return true;
    }
}
