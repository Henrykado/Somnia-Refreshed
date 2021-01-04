package com.kingrunes.somnia.common.compat;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CompatModule {
    public static final ResourceLocation CHARM_SLEEP = new ResourceLocation("darkutils", "charm_sleep");
    public static final ResourceLocation CRYO_CHAMBER = new ResourceLocation("galacticraftplanets", "mars_machine");
    public static final ResourceLocation SLEEPING_BAG = new ResourceLocation("openblocks", "sleeping_bag");

    /**
     * Check if the world should be simulated in this bed
     */
    public static boolean isPlayerInBed(EntityPlayer player, BlockPos pos) {
        Entity riding = player.getRidingEntity();
        if (riding != null && riding.getClass() == RailcraftPlugin.BED_CART_CLASS) return true;

        if (pos == null) return false;
        IBlockState state = player.world.getBlockState(pos);
        Block block = state.getBlock();

        if (block.getRegistryName().equals(CRYO_CHAMBER)) return false;

        ItemStack chest = player.inventory.armorInventory.get(2);
        ItemStack currentStack = player.inventory.getCurrentItem();
        if ((!chest.isEmpty() && chest.getItem().getRegistryName().equals(SLEEPING_BAG)) || (!currentStack.isEmpty() && currentStack.getItem().getRegistryName().equals(SLEEPING_BAG))) return true;

        return block.isBed(state, player.world, pos, player);
    }
}
