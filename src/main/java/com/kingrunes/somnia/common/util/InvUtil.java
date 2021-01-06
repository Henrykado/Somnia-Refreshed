package com.kingrunes.somnia.common.util;

import com.kingrunes.somnia.common.compat.BaublesPlugin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class InvUtil {
    public static boolean hasItem(EntityPlayer player, ResourceLocation registryName) {
        if (BaublesPlugin.checkBauble(player, registryName)) return true;

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = player.getItemStackFromSlot(slot);
            ResourceLocation itemName = stack.getItem().getRegistryName();
            if (itemName != null && itemName.equals(registryName)) return true;
        }

        for(ItemStack stack : player.inventory.mainInventory) {
            ResourceLocation itemName = stack.getItem().getRegistryName();
            if (itemName != null && itemName.equals(registryName)) return true;
        }

        return false;
    }
}
