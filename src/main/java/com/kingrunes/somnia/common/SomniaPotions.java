package com.kingrunes.somnia.common;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.potion.FatigueEffect;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;

public class SomniaPotions
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(Somnia.MOD_ID, "textures/gui/potion_effects.png");

    public static Potion awakeningPotion;
    public static PotionType awakeningPotionType;
    public static PotionType longAwakeningPotionType;
    public static PotionType strongAwakeningPotionType;

    public static Potion insomniaPotion;
    public static PotionType insomniaPotionType;
    public static PotionType longInsomniaPotionType;
    public static PotionType strongInsomniaPotionType;
    
    public static FatigueEffect sleepyEffect;
    public static FatigueEffect exhaustedEffect;
    public static FatigueEffect fadingEffect;
}
