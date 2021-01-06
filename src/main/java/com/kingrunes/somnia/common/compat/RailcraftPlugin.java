package com.kingrunes.somnia.common.compat;

import mods.railcraft.common.util.network.PacketBuilder;
import mods.railcraft.common.util.network.PacketKeyPress;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RailcraftPlugin {
    public static final Class<?> BED_CART_CLASS;
    private static final Method INSTANCE_METHOD;
    private static final Method SEND_KEY_PACKET_METHOD;
    private static final Field CART_BED_SLEEP_FIELD;

    static {
        Class<?> classEntityCartBed;
        Method methodInstance;
        Method methodSendKeyPressPacket;
        Field fieldBedCartSleep;
        try {
            classEntityCartBed = Class.forName("mods.railcraft.common.carts.EntityCartBed");
            Class<?> classPacketBuilder = Class.forName("mods.railcraft.common.util.network.PacketBuilder");
            Class<?> classEnumKeyBinding = Class.forName("mods.railcraft.common.util.network.PacketKeyPress$EnumKeyBinding");
            methodInstance = classPacketBuilder.getDeclaredMethod("instance");
            methodSendKeyPressPacket = classPacketBuilder.getDeclaredMethod("sendKeyPressPacket", classEnumKeyBinding);
            fieldBedCartSleep = classEnumKeyBinding.getDeclaredField("BED_CART_SLEEP");
        } catch (final ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            classEntityCartBed = null;
            methodInstance = methodSendKeyPressPacket = null;
            fieldBedCartSleep = null;
        }
        BED_CART_CLASS = classEntityCartBed;
        INSTANCE_METHOD = methodInstance;
        SEND_KEY_PACKET_METHOD = methodSendKeyPressPacket;
        CART_BED_SLEEP_FIELD = fieldBedCartSleep;
    }

    public static void sleepInBedCart() {
        if (Loader.isModLoaded("railcraft")) doSleepInBedCart();
    }

    @Optional.Method(modid = "railcraft")
    private static void doSleepInBedCart() {
        PacketBuilder.instance().sendKeyPressPacket(PacketKeyPress.EnumKeyBinding.BED_CART_SLEEP);
    }
}
