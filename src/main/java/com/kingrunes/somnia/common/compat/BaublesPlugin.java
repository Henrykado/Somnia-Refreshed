package com.kingrunes.somnia.common.compat;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class BaublesPlugin {

    public static boolean checkBauble(EntityPlayer player, ResourceLocation registryName) {
        return Loader.isModLoaded("baubles") && doCheckBauble(player, registryName);
    }

    @Optional.Method(modid = "baubles")
    private static boolean doCheckBauble(EntityPlayer player, ResourceLocation registryName) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem().getRegistryName().equals(registryName)) return true;
        }
        return false;
    }
}
