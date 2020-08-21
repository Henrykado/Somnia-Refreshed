package com.kingrunes.somnia.common.compat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BaublesPlugin {
    private static final Method GET_BAUBLES_HANDLER_METHOD;
    private static final Method GET_SLOTS_METHOD;
    private static final Method GET_STACK_IN_SLOT_METHOD;

    static {
        Method methodGetBaublesHandler;
        Method methodGetSlots;
        Method methodGetStackInSlot;
        try {
            Class<?> classIBaublesItemHandler = Class.forName("baubles.api.cap.IBaublesItemHandler");
            Class<?> classBaublesApi = Class.forName("baubles.api.BaublesApi");
            methodGetBaublesHandler = classBaublesApi.getDeclaredMethod("getBaublesHandler", EntityPlayer.class);
            methodGetSlots = classIBaublesItemHandler.getMethod("getSlots");
            methodGetStackInSlot = classIBaublesItemHandler.getMethod("getStackInSlot", int.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            methodGetBaublesHandler = methodGetSlots = methodGetStackInSlot = null;
        }
        GET_BAUBLES_HANDLER_METHOD = methodGetBaublesHandler;
        GET_SLOTS_METHOD = methodGetSlots;
        GET_STACK_IN_SLOT_METHOD = methodGetStackInSlot;
    }

    public static boolean checkBauble(EntityPlayer player, ResourceLocation registryName) {
        try {
            Object baublesHandler = GET_BAUBLES_HANDLER_METHOD.invoke(null, player);
            int slots = (int) GET_SLOTS_METHOD.invoke(baublesHandler);
            for (int i = 0; i < slots; i++) {
                ItemStack stack = (ItemStack) GET_STACK_IN_SLOT_METHOD.invoke(baublesHandler, i);
                ResourceLocation itemName = stack.getItem().getRegistryName();
                if (itemName != null && itemName.equals(registryName)) return true;
            }

        } catch (IllegalAccessException | InvocationTargetException | NullPointerException ignored) {}

        return false;
    }
}
