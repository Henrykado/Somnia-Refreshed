package com.kingrunes.somnia.client;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.api.capability.IFatigue;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.SomniaConfig;
import com.kingrunes.somnia.common.SomniaConfig.DisplayPosition;
import com.kingrunes.somnia.common.StreamUtils;
import com.kingrunes.somnia.setup.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

public class ClientTickHandler
{
	private final Minecraft mc = Minecraft.getMinecraft();

	public static final String 	COLOR = new String(new char[]{ (char)167 }),
								WHITE = COLOR+"f",
								RED = COLOR+"c",
								DARK_RED = COLOR+"4",
								GOLD = COLOR+"6";

	public static final String	TRANSLATION_FORMAT = "somnia.status.%s",
								SPEED_FORMAT = "%sx%s",
								ETA_FORMAT = WHITE + "(%s:%s)";

	private boolean muted = false;
	private float defVol1; private float defVol2;
	private final ItemStack clockItemStack = new ItemStack(Items.CLOCK);
	public long startTicks = -1L;
	public double speed = 0;
	private final List<Double> speedValues = new ArrayList<>();
	public String status = "Waiting...";

	public ClientTickHandler() {
		NBTTagCompound clockNbt = new NBTTagCompound();
		clockNbt.setBoolean("quark:clock_calculated", true);
		this.clockItemStack.setTagCompound(clockNbt); //Disables Quark's clock display override
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == Phase.END)
			tickEnd();
	}

	public void readField(DataInputStream in) throws IOException
	{
		switch (in.readByte())
		{
			case 0x00:
				speed = in.readDouble();
				speedValues.add(speed);
				if (speedValues.size() > 5)
					speedValues.remove(0);
				break;
			case 0x01:
				String str = StreamUtils.readString(in);
				status = str.startsWith("f:") ? new TextComponentTranslation(String.format(TRANSLATION_FORMAT, str.substring(2).toLowerCase())).getUnformattedComponentText() : str;
				break;
		}
	}

	public void tickEnd()
	{
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player == null)
			return;
		
		GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
		
		/*
		 * If the player is sleeping and the player has chosen the 'muteSoundWhenSleeping' option in the config,
		 * set the master volume to 0
		 */
		
		if (mc.player.isPlayerSleeping())
		{
			if (SomniaConfig.OPTIONS.muteSoundWhenSleeping)
			{
				if (!muted)
				{
					muted = true;
					defVol1 = gameSettings.getSoundLevel(SoundCategory.HOSTILE);
					defVol2 = gameSettings.getSoundLevel(SoundCategory.NEUTRAL);
					gameSettings.setSoundLevel(SoundCategory.HOSTILE, .0f);
					gameSettings.setSoundLevel(SoundCategory.NEUTRAL, .0f);
				}
			}
		}
		else
		{
			if (muted)
			{
				muted = false;
				gameSettings.setSoundLevel(SoundCategory.HOSTILE, defVol1);
				gameSettings.setSoundLevel(SoundCategory.NEUTRAL, defVol2);
			}
		}
		
		/*
		 * Note the isPlayerSleeping() check. Without this, the mod exploits a bug which exists in vanilla Minecraft which
		 * allows the player to teleport back to there bed from anywhere in the world at any time.
		 */
		if (ClientProxy.clientAutoWakeTime > -1 && mc.player.isPlayerSleeping() && mc.world.getWorldTime() >= ClientProxy.clientAutoWakeTime)
		{
			ClientProxy.clientAutoWakeTime = -1;
			Somnia.eventChannel.sendToServer(PacketHandler.buildWakePacket());
		}
	}
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		if ((mc.currentScreen != null && !(mc.currentScreen instanceof GuiIngameMenu))) {
			if (mc.player == null || !mc.player.isPlayerSleeping()) return;
		}
		
		ScaledResolution scaledResolution = new ScaledResolution(mc);
		if (mc.player.isPlayerSleeping() && SomniaConfig.OPTIONS.somniaGui && ClientProxy.playerFatigue != -1) renderSleepGui(scaledResolution);
		else if (startTicks != -1 || speed != 0) {
			this.startTicks = -1;
			this.speed = 0;
		}
	}

	private void renderSleepGui(ScaledResolution scaledResolution) {
		boolean currentlySleeping = speed != .0d;
		
		if (currentlySleeping)
		{
			if (startTicks == -1L)
				startTicks = this.mc.world.getWorldTime();
		}
		else
			startTicks = -1L;

		/*
		 * GL stuff
		 */
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);

		/*
		 * Progress bar
		 * Multiplier
		 * ETA
		 * Clock
		 */
		if (startTicks != -1L && ClientProxy.clientAutoWakeTime != -1)
		{
			// Progress Bar
			mc.getTextureManager().bindTexture(Gui.ICONS);
			
			double 	rel = mc.world.getWorldTime()-startTicks,
					diff = ClientProxy.clientAutoWakeTime-startTicks,
					progress = rel / diff;
			
			int 	x = 20,
					maxWidth = (scaledResolution.getScaledWidth()-(x*2));

			glEnable(GL_BLEND);
			glColor4f(1.0f, 1.0f, 1.0f, .2f);
			renderProgressBar(x, 10, maxWidth, 1.0d);

			glDisable(GL_BLEND);
			glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			renderProgressBar(x, 10, maxWidth, progress);

			int offsetX = SomniaConfig.OPTIONS.displayETASleep == DisplayPosition.CENTER ? scaledResolution.getScaledWidth()/2 - 80 : SomniaConfig.OPTIONS.displayETASleep == DisplayPosition.RIGHT ? maxWidth - 160 : 0;
			
			// Multiplier
			if (!SomniaConfig.OPTIONS.guiDisableTickMultiplier)
			{
				if (SomniaConfig.PERFORMANCE.fasterWorldTime)
				{
					renderScaledString(x + offsetX, 20, 1.5f, SPEED_FORMAT, getColorStringForSpeed(speed), speed * SomniaConfig.PERFORMANCE.fasterWorldTimeMultiplier);
				}
				else renderScaledString(x + offsetX, 20, 1.5f, SPEED_FORMAT, getColorStringForSpeed(speed), speed);
			}
				
			// ETA
			if (!SomniaConfig.OPTIONS.guiDisableETA)
			{	
				double average = new ArrayList<>(speedValues)
						.stream()
						.filter(Objects::nonNull)
						.mapToDouble(Double::doubleValue)
						.summaryStatistics()
						.getAverage();
				
				if (SomniaConfig.PERFORMANCE.fasterWorldTime)
					average *= SomniaConfig.PERFORMANCE.fasterWorldTimeMultiplier;
				
				long etaTotalSeconds = Math.round((diff - rel) / (average * 20));
	
				long etaSeconds = etaTotalSeconds % 60,
						etaMinutes = (etaTotalSeconds-etaSeconds) / 60;
	
				renderScaledString(x + 50 + 10 + offsetX, 20, 1.5f, ETA_FORMAT, (etaMinutes<10?"0":"") + etaMinutes, (etaSeconds<10?"0":"") + etaSeconds);
			}
				
			// Clock
			if (!SomniaConfig.OPTIONS.guiDisableClock)
				renderClock(maxWidth);
		}
	}

	private void renderProgressBar(int x, int y, int maxWidth, double progress)
	{
		int amount = (int) (progress * maxWidth);
		while (amount > 0)
		{
			if (mc.currentScreen != null) this.mc.currentScreen.drawTexturedModalRect(x, y, 0, 69, (Math.min(amount, 180)), 5);

			amount -= 180;
			x += 180;
		}
	}

	private void renderScaledString(int x, int y, float scale, String format, Object... args)
	{
		if (mc.currentScreen == null) return;
		String str = String.format(format, args);
		glPushMatrix();
		{
			glTranslatef(x, 20, 0.0f);
			glScalef(scale, scale, 1.0f);
			this.mc.currentScreen.drawString
					(
							this.mc.fontRenderer,
							str,
							0,
							0,
							Integer.MIN_VALUE
					);
		}
		glPopMatrix();
	}

	private void renderClock(int maxWidth)
	{
		int x;
		switch (SomniaConfig.OPTIONS.somniaGuiClockPosition)
		{
			case LEFT:
				x = 40;
				break;
			case CENTER:
				x = maxWidth / 2;
				break;
			default:
			case RIGHT:
				x = maxWidth - 40;
				break;
		}
		glPushMatrix();
		{
			glTranslatef(x, 35, 0);
			glScalef(4F, 4F, 1);
			mc.getRenderItem().renderItemAndEffectIntoGUI(mc.player, clockItemStack, 0, 0);
		}
		glPopMatrix();
	}

	public static String getColorStringForSpeed(double speed)
	{
		if (speed < 8)
			return WHITE;
		else if (speed < 20)
			return DARK_RED;
		else if (speed < 30)
			return RED;
		else
			return GOLD;
	}
}