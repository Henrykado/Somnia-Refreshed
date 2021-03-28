package com.kingrunes.somnia.api;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SomniaAPI {
    private static final List<Pair<ItemStack, Double>> REPLENISHING_ITEMS = new ArrayList<>();

    public static void addReplenishingItem(ItemStack stack, double fatigueToReplenish) {
        REPLENISHING_ITEMS.add(Pair.of(stack, fatigueToReplenish));
    }

    public static List<Pair<ItemStack, Double>> getReplenishingItems() {
        return new ArrayList<>(REPLENISHING_ITEMS);
    }
}
