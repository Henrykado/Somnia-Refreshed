package com.kingrunes.somnia.common.potion;

import java.util.ArrayList;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.SomniaConfig;
import com.kingrunes.somnia.common.SomniaPotions;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class ExhaustedEffect extends FatigueEffect {
	public ArrayList<Potion> exhaustedEffects = new ArrayList<Potion>();
	public int[] exhaustedEffectsAmplifiers;
	
	public ExhaustedEffect()
    {
        super(SomniaConfig.getMobEffectsFromSideEffects(SomniaConfig.FATIGUE.exhaustedEffects), SomniaConfig.FATIGUE.exhaustedEffectsAmplifiers, 0x23009a);
        setRegistryName("exhausted");
        setPotionName(Somnia.MOD_ID+".effect.exhausted");
        setIconIndex(3, 0);
    }
}
