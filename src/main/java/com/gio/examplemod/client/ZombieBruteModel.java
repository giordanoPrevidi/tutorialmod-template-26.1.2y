package com.gio.examplemod.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.gio.examplemod.TutorialMod;
import com.gio.examplemod.entity.ZombieBruteEntity;

import net.minecraft.resources.Identifier;

public class ZombieBruteModel extends DefaultedEntityGeoModel<ZombieBruteEntity> {
    public ZombieBruteModel() {
        super(Identifier.fromNamespaceAndPath(TutorialMod.MODID, "zombie_brute"));
    }
}
