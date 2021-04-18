package com.kingrunes.somnia.common.potion;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.SomniaPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;

public class PotionInsomnia extends Potion {

    public PotionInsomnia()
    {
        super(true, 0x23009a);
        setRegistryName("insomnia");
        setPotionName(Somnia.MOD_ID+".effect.insomnia");
        setIconIndex(1, 0);
    }

    @Override
    public boolean hasStatusIcon()
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(SomniaPotions.TEXTURE);
        return super.hasStatusIcon();
    }
}
