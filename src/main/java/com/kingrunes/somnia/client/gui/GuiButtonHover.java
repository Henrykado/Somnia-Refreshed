package com.kingrunes.somnia.client.gui;

import com.kingrunes.somnia.Somnia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonHover extends GuiButton {
    private final long wakeTime;
    private final String hoverText;
    private final String buttonText;

    public GuiButtonHover(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, long wakeTime) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.wakeTime = wakeTime;
        this.buttonText = buttonText;
        this.hoverText = Somnia.timeStringForWorldTime(wakeTime);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        this.displayString = this.hoverText != null && this.hovered ? this.hoverText : this.buttonText;
    }

    public long getWakeTime() {
        return wakeTime;
    }
}
