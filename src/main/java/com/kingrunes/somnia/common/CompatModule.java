package com.kingrunes.somnia.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CompatModule {

    /**
     * Check if the world should be simulated in this bed
     */
    public static boolean shouldSimulate(EntityPlayer player, BlockPos pos) {
        Entity riding = player.getRidingEntity();
        if (riding != null && isBedCart(riding)) return true;

        if (pos == null) return false;
        IBlockState state = player.world.getBlockState(pos);
        Block block = state.getBlock();
        ResourceLocation regName = block.getRegistryName();

        if (regName != null && regName.toString().equals("galacticraftplanets:mars_machine")) return false;

        return state.getBlock().isBed(state, player.world, pos, player);
    }

    public static boolean isBedCart(Entity entity) {
        if (entity == null) return false;
        ResourceLocation entityName = EntityList.getKey(entity);
        return entityName != null && entityName.toString().equals("railcraft:cart_bed");
    }
}
