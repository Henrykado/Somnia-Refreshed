package com.kingrunes.somnia.api.capability;

import net.minecraft.nbt.NBTTagCompound;

//Thanks @TheSilkMiner for a custom capability example
public class Fatigue implements IFatigue {

    private double fatigue, extraFatigueRate, replenishedFatigue;
    private int fatigueUpdateCounter = 0, sideEffectStage = -1;
    private boolean resetSpawn = true, sleepNormally = false;

    @Override
    public double getFatigue()
    {
        return this.fatigue;
    }

    @Override
    public void setFatigue(double fatigue)
    {
        this.fatigue = fatigue;
    }

    @Override
    public int getSideEffectStage()
    {
        return this.sideEffectStage;
    }

    @Override
    public void setSideEffectStage(int stage)
    {
        this.sideEffectStage = stage;
    }

    @Override
    public int updateFatigueCounter()
    {
        return ++fatigueUpdateCounter;
    }

    @Override
    public void resetFatigueCounter()
    {
        this.fatigueUpdateCounter = 0;
    }

    @Override
    public void maxFatigueCounter() {
        this.fatigueUpdateCounter = 100;
    }

    @Override
    public void setResetSpawn(boolean resetSpawn) {
        this.resetSpawn = resetSpawn;
    }

    @Override
    public boolean shouldResetSpawn() {
        return this.resetSpawn;
    }

    @Override
    public void setSleepNormally(boolean sleepNormally) {
        this.sleepNormally = sleepNormally;
    }

    @Override
    public boolean shouldSleepNormally() {
        return this.sleepNormally;
    }

    @Override
    public double getExtraFatigueRate() {
        return this.extraFatigueRate;
    }

    @Override
    public void setExtraFatigueRate(double rate) {
        this.extraFatigueRate = rate;
    }

    @Override
    public double getReplenishedFatigue() {
        return this.replenishedFatigue;
    }

    @Override
    public void setReplenishedFatigue(double replenishedFatigue) {
        this.replenishedFatigue = replenishedFatigue;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("fatigue", this.fatigue);
        tag.setDouble("extraFatigueRate", this.extraFatigueRate);
        tag.setDouble("replenishedFatigue", this.replenishedFatigue);
        tag.setInteger("sideEffectStage", this.sideEffectStage);
        tag.setBoolean("resetSpawn", this.resetSpawn);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.fatigue = nbt.getDouble("fatigue");
        this.extraFatigueRate = nbt.getDouble("extraFatigueRate");
        this.replenishedFatigue = nbt.getDouble("replenishedFatigue");
        this.sideEffectStage = nbt.getInteger("sideEffectStage");
        this.resetSpawn = nbt.getBoolean("resetSpawn");
    }
}
