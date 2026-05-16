package com.gio.examplemod.item;

import java.util.function.Consumer;

import com.gio.examplemod.magic.MagicSpell;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ArcaneHourglassStaffItem extends Item {
    private static final String SELECTED_SPELL_TAG = "SelectedSpell";
    private static final int CAST_COOLDOWN_TICKS = 30;
    private static final float PROJECTILE_SPEED = 1.6F;

    public ArcaneHourglassStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            MagicSpell spell = getSelectedSpell(stack);
            castSpell(serverLevel, player, stack, spell);
            stack.hurtAndBreak(1, player, hand);
            player.getCooldowns().addCooldown(stack, CAST_COOLDOWN_TICKS);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResult.SUCCESS;
    }

    public static MagicSpell getSelectedSpell(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return MagicSpell.byIndex(tag.getIntOr(SELECTED_SPELL_TAG, MagicSpell.ARCANE_FIRE.ordinal()));
    }

    public static void setSelectedSpell(ItemStack stack, MagicSpell spell) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(SELECTED_SPELL_TAG, spell.ordinal()));
    }

    public static void showSelectedSpell(Player player, MagicSpell spell) {
        player.sendSystemMessage(Component.translatable("message.tutorialmod.selected_spell", spell.displayName()).withStyle(ChatFormatting.AQUA));
    }

    private static void castSpell(ServerLevel level, Player player, ItemStack stack, MagicSpell spell) {
        switch (spell) {
            case ARCANE_FIRE -> castArcaneBolt(level, player);
            case WIND_BURST -> castWindBurst(level, player);
            case HEALING_LIGHT -> castHealingLight(level, player);
            case BLINK_STEP -> castBlinkStep(level, player);
            case STORM_LANCE -> castStormLance(level, player);
            case FROST_BIND -> castFrostBind(level, player);
            case EARTHEN_GUARD -> castEarthenGuard(level, player);
            case SOLAR_FLARE -> castSolarFlare(level, player);
            case WITHER_TOUCH -> castWitherTouch(level, player);
            case SOUL_DRAIN -> castSoulDrain(level, player);
            case BONE_MINION -> castBoneMinion(level, player);
            case GRAVE_MIST -> castGraveMist(level, player);
        }
    }

    private static void castArcaneBolt(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition().add(look.scale(0.8D));
        Vec3 movement = look.scale(PROJECTILE_SPEED);

        SmallFireball fireball = new SmallFireball(level, player, movement);
        fireball.setPos(origin.x, origin.y - 0.1D, origin.z);
        fireball.setOwner(player);
        fireball.setDeltaMovement(movement);
        level.addFreshEntity(fireball);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.7F, 1.25F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5F, 0.9F);
        level.sendParticles(ParticleTypes.WITCH, origin.x, origin.y, origin.z, 12, 0.18D, 0.18D, 0.18D, 0.02D);
        level.sendParticles(ParticleTypes.ENCHANT, origin.x, origin.y, origin.z, 8, 0.25D, 0.25D, 0.25D, 0.0D);
    }

    private static void castWindBurst(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition().add(look.scale(0.8D));
        Vec3 movement = look.scale(1.2D);

        WindCharge windCharge = new WindCharge(level, origin.x, origin.y - 0.1D, origin.z, movement);
        windCharge.setOwner(player);
        windCharge.setDeltaMovement(movement);
        level.addFreshEntity(windCharge);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 0.75F, 1.0F);
        level.sendParticles(ParticleTypes.GUST, origin.x, origin.y, origin.z, 10, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    private static void castHealingLight(ServerLevel level, Player player) {
        player.heal(6.0F);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.35F);
        level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0D, player.getZ(), 8, 0.4D, 0.5D, 0.4D, 0.02D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.35D, 0.5D, 0.35D, 0.02D);
    }

    private static void castBlinkStep(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 destination = findSafeTeleportPosition(level, player, 7.0D);
        if (destination == null) {
            player.sendSystemMessage(Component.translatable("message.tutorialmod.spell_blocked").withStyle(ChatFormatting.RED));
            level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0.25D, 0.35D, 0.25D, 0.01D);
            return;
        }

        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 24, 0.35D, 0.7D, 0.35D, 0.04D);
        player.teleportTo(destination.x, destination.y, destination.z);
        level.playSound(null, destination.x, destination.y, destination.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.1F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, destination.x, destination.y + 1.0D, destination.z, 24, 0.35D, 0.7D, 0.35D, 0.04D);
    }

    private static void castStormLance(ServerLevel level, Player player) {
        Vec3 target = getTargetPosition(level, player, 16.0D);
        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightning.setPos(target.x, target.y, target.z);
        if (player instanceof ServerPlayer serverPlayer) {
            lightning.setCause(serverPlayer);
        }
        level.addFreshEntity(lightning);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.x, target.y + 0.5D, target.z, 24, 0.5D, 0.8D, 0.5D, 0.04D);
    }

    private static void castFrostBind(ServerLevel level, Player player) {
        LivingEntity target = findTarget(player, level, 12.0D);
        if (target != null) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 4));
            target.hurtServer(level, level.damageSources().magic(), 3.0F);
            level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 1.0D, target.getZ(), 30, 0.45D, 0.6D, 0.45D, 0.01D);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.8F, 0.7F);
        }
    }

    private static void castEarthenGuard(ServerLevel level, Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 1));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 160, 0));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.65F);
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.9D, player.getZ(), 28, 0.55D, 0.7D, 0.55D, 0.02D);
    }

    private static void castSolarFlare(ServerLevel level, Player player) {
        LivingEntity aimedTarget = findTarget(player, level, 14.0D);
        Vec3 target = aimedTarget != null ? aimedTarget.position() : getTargetPosition(level, player, 14.0D);
        AABB area = AABB.ofSize(target, 8.0D, 5.0D, 8.0D);
        for (Entity entity : level.getEntities(player, area, entity -> entity instanceof LivingEntity && entity != player)) {
            entity.hurtServer(level, level.damageSources().magic(), 5.0F);
            entity.igniteForSeconds(4.0F);
        }

        level.playSound(null, target.x, target.y, target.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.45F);
        level.sendParticles(ParticleTypes.FLAME, target.x, target.y + 1.0D, target.z, 60, 2.0D, 0.8D, 2.0D, 0.04D);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.x, target.y + 1.0D, target.z, 18, 1.6D, 0.6D, 1.6D, 0.03D);
    }

    private static void castWitherTouch(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.getEyePosition().add(look.scale(0.8D));
        Vec3 movement = look.scale(1.2D);

        WitherSkull skull = new WitherSkull(level, player, movement);
        skull.setPos(origin.x, origin.y - 0.1D, origin.z);
        skull.setOwner(player);
        skull.setDeltaMovement(movement);
        level.addFreshEntity(skull);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.55F, 1.2F);
        level.sendParticles(ParticleTypes.SOUL, origin.x, origin.y, origin.z, 14, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    private static void castSoulDrain(ServerLevel level, Player player) {
        LivingEntity target = findTarget(player, level, 10.0D);
        if (target != null) {
            target.hurtServer(level, level.damageSources().magic(), 6.0F);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
            player.heal(4.0F);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8F, 0.8F);
            level.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0D, target.getZ(), 18, 0.35D, 0.55D, 0.35D, 0.02D);
            level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0D, player.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D);
        }
    }

    private static void castBoneMinion(ServerLevel level, Player player) {
        LivingEntity skeleton = EntityType.SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (skeleton != null) {
            Vec3 position = findSafeSummonPosition(level, player, skeleton);
            if (position == null) {
                player.sendSystemMessage(Component.translatable("message.tutorialmod.spell_blocked").withStyle(ChatFormatting.RED));
                level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0.25D, 0.35D, 0.25D, 0.01D);
                return;
            }
            skeleton.setPos(position.x, position.y, position.z);
            level.addFreshEntity(skeleton);
            level.playSound(null, position.x, position.y, position.z, SoundEvents.SKELETON_AMBIENT, SoundSource.PLAYERS, 0.8F, 0.7F);
            level.sendParticles(ParticleTypes.SOUL, position.x, position.y + 1.0D, position.z, 20, 0.45D, 0.65D, 0.45D, 0.03D);
        }
    }

    private static void castGraveMist(ServerLevel level, Player player) {
        LivingEntity aimedTarget = findTarget(player, level, 10.0D);
        Vec3 target = aimedTarget != null ? aimedTarget.position() : getTargetPosition(level, player, 10.0D);
        AABB area = AABB.ofSize(target, 7.0D, 4.0D, 7.0D);
        int affected = 0;
        for (Entity entity : level.getEntities(player, area, entity -> entity instanceof LivingEntity && entity != player)) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            livingEntity.hurtServer(level, level.damageSources().wither(), 3.0F);
            affected++;
        }

        level.playSound(null, target.x, target.y, target.z, SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.6F, 1.4F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.x, target.y + 0.8D, target.z, 60, 2.0D, 0.8D, 2.0D, 0.02D);
        level.sendParticles(ParticleTypes.SCULK_SOUL, target.x, target.y + 1.0D, target.z, affected > 0 ? 26 : 12, 1.5D, 0.7D, 1.5D, 0.03D);
    }

    private static Vec3 getTargetPosition(Level level, Player player, double range) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(look.scale(range));
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }

    private static Vec3 findSafeTeleportPosition(ServerLevel level, Player player, double range) {
        Vec3 target = getTargetPosition(level, player, range);
        double eyeHeight = player.getEyeHeight();
        Vec3 base = new Vec3(target.x, target.y - eyeHeight, target.z);

        double[] verticalOffsets = {0.0D, 1.0D, 2.0D, -1.0D};
        for (double offset : verticalOffsets) {
            Vec3 candidate = base.add(0.0D, offset, 0.0D);
            if (isSafeForPlayer(level, player, candidate)) {
                return candidate;
            }
        }

        Vec3 fallback = player.position().add(player.getLookAngle().normalize().multiply(4.0D, 0.0D, 4.0D));
        return isSafeForPlayer(level, player, fallback) ? fallback : null;
    }

    private static boolean isSafeForPlayer(ServerLevel level, Player player, Vec3 position) {
        AABB box = player.getDimensions(player.getPose()).makeBoundingBox(position).inflate(-0.05D);
        return level.noCollision(player, box) && !level.containsAnyLiquid(box);
    }

    private static Vec3 findSafeSummonPosition(ServerLevel level, Player player, LivingEntity entity) {
        Vec3 look = player.getLookAngle().normalize().multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 right = new Vec3(-look.z, 0.0D, look.x);
        Vec3 base = player.position().add(look.scale(2.0D));
        Vec3[] candidates = {
                base,
                base.add(right.scale(1.25D)),
                base.add(right.scale(-1.25D)),
                base.add(0.0D, 1.0D, 0.0D),
                player.position().add(look.scale(3.0D))
        };

        for (Vec3 candidate : candidates) {
            AABB box = entity.getDimensions(entity.getPose()).makeBoundingBox(candidate).inflate(-0.05D);
            if (level.noCollision(entity, box) && !level.containsAnyLiquid(box)) {
                return candidate;
            }
        }

        return null;
    }

    private static LivingEntity findTarget(Player player, ServerLevel level, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchArea = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0D);

        LivingEntity bestTarget = null;
        double bestScore = 0.92D;

        for (Entity entity : level.getEntities(player, searchArea, entity -> entity instanceof LivingEntity && entity != player)) {
            Vec3 directionToEntity = entity.getEyePosition().subtract(eye).normalize();
            double score = directionToEntity.dot(look);
            if (score > bestScore && eye.distanceToSqr(entity.getEyePosition()) <= range * range) {
                bestScore = score;
                bestTarget = (LivingEntity) entity;
            }
        }

        return bestTarget;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        MagicSpell spell = getSelectedSpell(stack);
        tooltip.accept(Component.translatable("item.tutorialmod.arcane_hourglass_staff.tooltip", spell.displayName()).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(spell.description().copy().withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
    }
}
