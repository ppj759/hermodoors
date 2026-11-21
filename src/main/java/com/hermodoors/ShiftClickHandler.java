package com.hermodoors;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Lets Shift+RMB reach the door even when the player holds an item.
@Mod.EventBusSubscriber(modid = HermoDoors.MODID)
public class ShiftClickHandler {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock e) {
        if (e.getPlayer().isSecondaryUseActive()
                && e.getWorld().getBlockState(e.getPos()).getBlock() instanceof ZgdDoorBlock) {
            e.setUseBlock(Event.Result.ALLOW);
            e.setUseItem(Event.Result.DENY);
        }
    }
}
