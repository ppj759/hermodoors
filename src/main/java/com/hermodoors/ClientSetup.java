package com.hermodoors;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = HermoDoors.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void models(ModelRegistryEvent e) {
        ModelLoader.addSpecialModel(new ResourceLocation(HermoDoors.MODID, "block/zgd_leaf"));
        ModelLoader.addSpecialModel(new ResourceLocation(HermoDoors.MODID, "block/zgd_front"));
        ModelLoader.addSpecialModel(new ResourceLocation(HermoDoors.MODID, "block/zgd_back"));
    }

    @SubscribeEvent
    public static void client(FMLClientSetupEvent e) {
        ClientRegistry.bindTileEntityRenderer(HermoDoors.ZGD_TILE.get(), ZgdDoorRenderer::new);
    }
}
