package com.kingrunes.somnia.common.potion;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.SomniaConfig;

public class SleepyEffect extends FatigueEffect {	
	public SleepyEffect()
    {
        super(SomniaConfig.getMobEffectsFromSideEffects(SomniaConfig.FATIGUE.sleepyEffects), SomniaConfig.FATIGUE.sleepyEffectsAmplifiers, 0x00ffee);
        setRegistryName("sleepy");
        setPotionName(Somnia.MOD_ID+".effect.sleepy");
        setIconIndex(2, 0);
    }
}
