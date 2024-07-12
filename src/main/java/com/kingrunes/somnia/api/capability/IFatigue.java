package com.kingrunes.somnia.api.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IFatigue extends INBTSerializable<NBTTagCompound> {
    double getFatigue();

    void setFatigue(double fatigue);

    int updateFatigueCounter();

    void resetFatigueCounter();

    void maxFatigueCounter();

    void setResetSpawn(boolean resetSpawn);

    boolean shouldResetSpawn();

    void setSleepNormally(boolean sleepNormally);

    boolean shouldSleepNormally();

    double getExtraFatigueRate();

    void setExtraFatigueRate(double rate);

    double getReplenishedFatigue();

    void setReplenishedFatigue(double replenishedFatigue);
}
