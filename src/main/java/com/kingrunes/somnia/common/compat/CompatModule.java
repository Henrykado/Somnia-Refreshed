package com.kingrunes.somnia.common.compat;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.SomniaAPI;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CompatModule {
    public static final ResourceLocation CHARM_SLEEP = new ResourceLocation("darkutils", "charm_sleep");
    public static final ResourceLocation CRYO_CHAMBER = new ResourceLocation("galacticraftplanets", "mars_machine");
    public static final ResourceLocation SLEEPING_BAG = new ResourceLocation("openblocks", "sleeping_bag");
    public static final ResourceLocation SLEEPING_MAT = new ResourceLocation("openblocks", "sleeping_mat");

    /**
     * Check if the world should be simulated in this bed
     */
    public static boolean checkBed(EntityPlayer player, BlockPos pos) {
        Entity riding = player.getRidingEntity();
        if (riding != null && RailcraftPlugin.isBedCart(riding)) return true;

        if (pos == null) return false;
        IBlockState state = player.world.getBlockState(pos);
        Block block = state.getBlock();

        if (block.getRegistryName().equals(CRYO_CHAMBER)) return false;

        ItemStack chest = player.inventory.armorInventory.get(2);
        ItemStack currentStack = player.inventory.getCurrentItem();
        if (!currentStack.isEmpty()) {
            ResourceLocation regName = currentStack.getItem().getRegistryName();
            if (regName.equals(SLEEPING_BAG)) return true;
            else if (regName.equals(SLEEPING_MAT)) return false;
        }
        if ((!chest.isEmpty() && chest.getItem().getRegistryName().equals(SLEEPING_BAG))) return true;

        return block.isBed(state, player.world, pos, player);
    }

    public static void registerCoffees()
    {
        Somnia.logger.info("Adding support for various coffees from other mods");
        if (Loader.isModLoaded("coffeespawner"))
        {
            SomniaAPI.addCoffee(getModItem("coffeespawner", "coffee"), 10);
            SomniaAPI.addCoffee(getModItem("coffeespawner", "coffee_milk"), 10);
            SomniaAPI.addCoffee(getModItem("coffeespawner", "coffee_sugar"), 15);
            SomniaAPI.addCoffee(getModItem("coffeespawner", "coffee_milk_sugar"), 15);
        }
        if (Loader.isModLoaded("harvestcraft"))
        {
            SomniaAPI.addCoffee(getModItem("harvestcraft", "coffeeitem"), 5);
            SomniaAPI.addCoffee(getModItem("harvestcraft", "coffeeconlecheitem"), 15);
            SomniaAPI.addCoffee(getModItem("harvestcraft", "espressoitem"), 15);
        }
        if (Loader.isModLoaded("coffeework"))
        {
            SomniaAPI.addCoffee(getModItem("coffeework", "coffee_instant"), 10);
            SomniaAPI.addCoffee(getModItem("coffeework", "coffee_instant_cup"), 10);
            SomniaAPI.addCoffee(getModItem("coffeework", "espresso"), 15);
        }
        if (Loader.isModLoaded("ic2")) {
            SomniaAPI.addCoffee(getModItem("ic2", "mug", 1), 5);
            SomniaAPI.addCoffee(getModItem("ic2", "mug", 2), 15);
            SomniaAPI.addCoffee(getModItem("ic2", "mug", 3), 10);
        }
        if (Loader.isModLoaded("actuallyadditions"))
        {
            SomniaAPI.addCoffee(getModItem("actuallyadditions", "item_coffee"), 10);
        }
    }

    public static ItemStack getModItem(String modid, String name)
    {
        return getModItem(modid, name, 0);
    }

    public static ItemStack getModItem(String modid, String name, int meta)
    {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, name));
        if (item != null) return new ItemStack(item, 1, meta);
        return ItemStack.EMPTY;
    }
}
