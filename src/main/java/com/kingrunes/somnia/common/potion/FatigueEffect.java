package com.kingrunes.somnia.common.potion;

import java.util.ArrayList;

import com.kingrunes.somnia.common.SomniaPotions;
import com.kingrunes.somnia.common.SomniaConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class FatigueEffect extends Potion {
	public ArrayList<Potion> extraPotions = new ArrayList<Potion>();
	public int[] extraPotionsAmplifiers;
	
	public FatigueEffect(ArrayList<Potion> extraPotions, int[] extraPotionsAmplifiers, int effectColor)
    {
        super(true, effectColor);
        
        this.extraPotions = extraPotions;
        this.extraPotionsAmplifiers = extraPotionsAmplifiers;
    }

    @Override
    public boolean hasStatusIcon()
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(SomniaPotions.TEXTURE);
        return true;
    }
    
    @Override
    public boolean isReady(int duration, int amplifier) { return true; }
    
    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier)
    {
    	if (!SomniaConfig.FATIGUE.enableFatigueSystem)
    	{
    		entityLivingBaseIn.removePotionEffect(this);
    	}
    }
    
    public void addExtraPotionEffects(EntityLivingBase player)
    {
    	for (int i = 0; i < extraPotions.size(); i++)
    	{
    		int _amplifier = 0;
    		if (i < extraPotionsAmplifiers.length) 
    			_amplifier = extraPotionsAmplifiers[i];
    		player.addPotionEffect(new PotionEffect(extraPotions.get(i), 310, _amplifier, false, true));
    	}
    }
    
    public void removeHarmfulPotionEffects(EntityLivingBase player)
    {
    	for (int i = 0; i < extraPotions.size(); i++)
    	{
    		Potion potion = extraPotions.get(i);
    		if (potion == MobEffects.POISON || potion == MobEffects.WITHER)
    			player.removePotionEffect(potion);
    	}
    }
}
