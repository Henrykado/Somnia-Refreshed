package com.kingrunes.somnia.api;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

public class SomniaAPI {
    private static final List<Triple<ItemStack, Double, Double>> REPLENISHING_ITEMS = new ArrayList<>();

    public static void addReplenishingItem(ItemStack stack, double fatigueToReplenish, double rateModifier) {
        REPLENISHING_ITEMS.add(Triple.of(stack, fatigueToReplenish, rateModifier));
    }

    public static List<Triple<ItemStack, Double, Double>> getReplenishingItems() {
        return new ArrayList<>(REPLENISHING_ITEMS);
    }
}
