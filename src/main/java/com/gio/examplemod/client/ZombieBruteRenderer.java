package com.gio.examplemod.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.gio.examplemod.entity.ZombieBruteEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class ZombieBruteRenderer extends GeoEntityRenderer<ZombieBruteEntity, LivingEntityRenderState> {
    public ZombieBruteRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieBruteModel());
    }
}
