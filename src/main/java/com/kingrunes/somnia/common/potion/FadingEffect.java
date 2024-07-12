package com.kingrunes.somnia.common.potion;

import java.util.ArrayList;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.SomniaConfig;

import net.minecraft.potion.Potion;

public class FadingEffect extends FatigueEffect {
	public FadingEffect() {
		super(SomniaConfig.getMobEffectsFromSideEffects(SomniaConfig.FATIGUE.fadingEffects), SomniaConfig.FATIGUE.fadingEffectsAmplifiers, 0);
		setRegistryName("fading");
        setPotionName(Somnia.MOD_ID+".effect.fading");
        setIconIndex(4, 0);
	}
}
