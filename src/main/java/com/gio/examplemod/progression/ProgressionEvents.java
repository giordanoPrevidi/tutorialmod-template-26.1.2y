package com.gio.examplemod.progression;

import java.io.File;

import com.gio.examplemod.talent.TalentEffects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ProgressionEvents {
    @SubscribeEvent
    public void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ProgressionManager.load(player, event.getPlayerFile(ProgressionManager.fileSuffix()));
        }
    }

    @SubscribeEvent
    public void onPlayerSave(PlayerEvent.SaveToFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            File file = event.getPlayerFile(ProgressionManager.fileSuffix());
            ProgressionManager.save(player, file);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ProgressionManager.ensureDivineWeapon(player);
            ProgressionManager.sync(player, false);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ProgressionManager.ensureDivineWeapon(player);
            ProgressionManager.sync(player, false);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ProgressionManager.handleLanding(player);
            ProgressionManager.tickThundersWrath(player);
        }

        if (event.getEntity() instanceof ServerPlayer player && player.level().getGameTime() % 100L == 0L) {
            PlayerProgressionData data = ProgressionManager.get(player);
            TalentEffects.applyPassive(player, data, null);
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player) || event.getEntity() instanceof ServerPlayer) {
            return;
        }

        if (event.getEntity() instanceof EnderDragon || event.getEntity() instanceof WitherBoss) {
            ProgressionManager.addXp(player, 100);
        } else if (event.getEntity() instanceof Monster) {
            ProgressionManager.addXp(player, 10);
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            event.getDrops().removeIf(drop -> ProgressionManager.isDivineWeapon(drop.getItem()));
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemEntity itemEntity = event.getEntity();
        if (ProgressionManager.isDivineWeapon(itemEntity.getItem())) {
            event.setCanceled(true);
            itemEntity.discard();
            event.getPlayer().getInventory().placeItemBackInInventory(itemEntity.getItem().copy());
        }
    }

    @SubscribeEvent
    public void onSpawnPlacement(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() == EntitySpawnReason.NATURAL
                && event.getEntityType().getCategory() == MobCategory.MONSTER
                && event.getLevel() instanceof ServerLevel level
                && ProgressionManager.blocksNaturalMonsterSpawns(level)) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
        }
    }

    @SubscribeEvent
    public void onIncomingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerProgressionData data = ProgressionManager.get(player);
            event.setNewDamage(TalentEffects.modifyIncomingDamage(player, data, event.getNewDamage()));
        }

        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            PlayerProgressionData data = ProgressionManager.get(player);
            float damage = TalentEffects.modifyOutgoingDamage(player, data, event.getNewDamage());
            if (event.getSource().getDirectEntity() == player) {
                damage = ProgressionManager.applyOutgoingMeleeDamage(player, event.getEntity(), damage);
            }
            event.setNewDamage(damage);
        }
    }
}
