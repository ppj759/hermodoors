package com.hermodoors;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(HermoDoors.MODID)
public class HermoDoors {
    public static final String MODID = "hermodoors";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);

    public static final RegistryObject<Block> ZGD_DOOR = BLOCKS.register("zgd_door",
            () -> new ZgdDoorBlock(AbstractBlock.Properties.create(Material.IRON)
                    .hardnessAndResistance(5.0F, 1200.0F)
                    .sound(SoundType.METAL)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
                    .setOpaque((s, w, p) -> false)));

    public static final RegistryObject<Item> ZGD_DOOR_ITEM = ITEMS.register("zgd_door",
            () -> new BlockItem(ZGD_DOOR.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<TileEntityType<ZgdDoorTile>> ZGD_TILE = TILES.register("zgd_door",
            () -> TileEntityType.Builder.create(ZgdDoorTile::new, ZGD_DOOR.get()).build(null));

    public HermoDoors() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
    }
}
