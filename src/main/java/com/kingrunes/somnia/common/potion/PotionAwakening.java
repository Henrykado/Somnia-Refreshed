package com.kingrunes.somnia.common.potion;

import com.kingrunes.somnia.Somnia;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class PotionAwakening extends Potion
{

    public PotionAwakening()
    {
        super(false, 0x00ffee);
        setRegistryName("awakening");
        setPotionName(Somnia.MOD_ID+".effect.awakening");
        setIconIndex(0, 0);
    }

    @Override
    public boolean hasStatusIcon()
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Somnia.MOD_ID, "textures/gui/potion_effects.png"));
        return true;
    }
}
