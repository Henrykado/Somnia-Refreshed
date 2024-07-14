package com.kingrunes.somnia.common.compat;

import mods.railcraft.common.util.network.PacketBuilder;
import mods.railcraft.common.util.network.PacketKeyPress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class RailcraftPlugin {
    public static final ResourceLocation BED_CART_ENTITY = new ResourceLocation("railcraft", "cart_bed");

    public static boolean isBedCart(Entity entity) {
        ResourceLocation name = EntityList.getKey(entity);
        return name != null && name.equals(BED_CART_ENTITY);
    }

    public static void sleepInBedCart() {
        if (Loader.isModLoaded("railcraft")) doSleepInBedCart();
    }

    @Optional.Method(modid = "railcraft")
    private static void doSleepInBedCart() {
        PacketBuilder.instance().sendKeyPressPacket(PacketKeyPress.EnumKeyBinding.BED_CART_SLEEP);
    }
}