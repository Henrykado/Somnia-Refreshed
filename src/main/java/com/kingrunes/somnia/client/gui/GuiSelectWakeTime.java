package com.kingrunes.somnia.client.gui;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.api.capability.IFatigue;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.compat.RailcraftPlugin;
import com.kingrunes.somnia.common.util.SomniaUtil;
import com.kingrunes.somnia.setup.ClientProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class GuiSelectWakeTime extends GuiScreen
{
	private boolean resetSpawn = true;

	@Override
	public void initGui()
	{
		int i = 0;
		int buttonWidth = 100, buttonHeight = 20;
		int buttonCenterX = this.width / 2 - 50;
		int buttonCenterY = this.height / 2 - 10;

		buttonList.add(new GuiButton(i++, buttonCenterX, buttonCenterY - 22, buttonWidth, buttonHeight, "Reset spawn: "+(this.resetSpawn ? "Yes" : "No")));
		buttonList.add(new GuiButton(i++, buttonCenterX, buttonCenterY + 22, buttonWidth, buttonHeight, "Cancel"));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX, buttonCenterY + 88, buttonWidth, buttonHeight, "Midnight", 18000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX - 80, buttonCenterY + 66, buttonWidth, buttonHeight, "After Midnight", 20000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX - 110, buttonCenterY + 44, buttonWidth, buttonHeight, "Before Sunrise", 22000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX - 130, buttonCenterY + 22, buttonWidth, buttonHeight, "Mid Sunrise", 23000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX - 140, buttonCenterY, buttonWidth, buttonHeight, "After Sunrise", 0));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX - 130, buttonCenterY - 22, buttonWidth, buttonHeight, "Early Morning", 1500));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX - 110, buttonCenterY - 44, buttonWidth, buttonHeight, "Mid Morning", 3000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX - 80, buttonCenterY - 66, buttonWidth, buttonHeight, "Late Morning", 4500));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX, buttonCenterY - 88, buttonWidth, buttonHeight, "Noon", 6000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX + 80, buttonCenterY - 66, buttonWidth, buttonHeight, "Early Afternoon", 7500));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX + 110, buttonCenterY - 44, buttonWidth, buttonHeight, "Mid Afternoon", 9000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX + 130, buttonCenterY - 22, buttonWidth, buttonHeight, "Late Afternoon", 10500));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX + 140, buttonCenterY, buttonWidth, buttonHeight, "Before Sunset", 12000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX + 130, buttonCenterY + 22, buttonWidth, buttonHeight, "Mid Sunset", 13000));
		buttonList.add(new GuiButtonHover(i++, buttonCenterX + 100, buttonCenterY + 44, buttonWidth, buttonHeight, "After Sunset", 14000));
		buttonList.add(new GuiButtonHover(i, buttonCenterX + 88, buttonCenterY + 66, buttonWidth, buttonHeight, "Before Midnight", 16000));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawCenteredString(this.fontRenderer, "Sleep until...?", this.width / 2, this.height / 2 - 5, 16777215);
		drawCenteredString(this.fontRenderer, SomniaUtil.timeStringForWorldTime(this.mc.player.world.getWorldTime()), this.width/2, this.height/2 - 66, 16777215);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		int i = 0;
		if (par1GuiButton.id == 0) {
			this.resetSpawn = !this.resetSpawn;
			par1GuiButton.displayString = "Reset spawn: "+(resetSpawn ? "Yes" : "No");
			return;
		} else if (par1GuiButton.id == 1) {
			mc.displayGuiScreen(null);
			return;
		} else if (par1GuiButton instanceof GuiButtonHover) {
			i = (int) ((GuiButtonHover)par1GuiButton).getWakeTime();
		}

		IFatigue props = mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
		if (props != null) {
			props.setResetSpawn(this.resetSpawn);
			Somnia.eventChannel.sendToServer(PacketHandler.buildPropUpdatePacket(0x01, 0x01, props.shouldResetSpawn()));
		}
		ClientProxy.clientAutoWakeTime = SomniaUtil.calculateWakeTime(mc.world.getTotalWorldTime(), i);
		/*
		 * Nice little hack to simulate a right click on the bed, don't try this at home kids
		 */
		RayTraceResult mouseOver = mc.objectMouseOver;
		BlockPos pos = mouseOver.getBlockPos();

		if (pos != null) { //pos is nullable!!!
			Somnia.eventChannel.sendToServer(PacketHandler.buildRightClickBlockPacket(mouseOver.getBlockPos(), mouseOver.sideHit, (float) mouseOver.hitVec.x, (float) mouseOver.hitVec.y, (float) mouseOver.hitVec.z));
		}
		else if (mouseOver.entityHit != null && RailcraftPlugin.isBedCart(mouseOver.entityHit)) {
			Somnia.eventChannel.sendToServer(PacketHandler.buildRideEntityPacket(mouseOver.entityHit));
			RailcraftPlugin.sleepInBedCart();
		}

		mc.displayGuiScreen(null);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}