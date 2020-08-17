package com.kingrunes.somnia.client;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.SomniaConfig;
import com.kingrunes.somnia.common.StreamUtils;
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

import static org.lwjgl.opengl.GL11.*;

public class ClientTickHandler
{
	private final Minecraft mc = Minecraft.getMinecraft();

	public static final String 	COLOR = new String(new char[]{ (char)167 }),
			BLACK = COLOR+"0",
			WHITE = COLOR+"f",
			RED = COLOR+"c",
			DARK_RED = COLOR+"4",
			GOLD = COLOR+"6";

	private static final String FATIGUE_FORMAT = WHITE + "Fatigue: %.2f";

	public static final String	TRANSLATION_FORMAT = "somnia.status.%s",
			SPEED_FORMAT = "%sx%s",
			ETA_FORMAT = WHITE + "(%s:%s)";

	public static final byte[]	BYTES_WHITE = new byte[]{ (byte) 255, (byte) 255, (byte) 255 },
			BYTES_DARK_RED = new byte[]{ (byte) 171, 0, 0 },
			BYTES_RED = new byte[]{ (byte) 255, 0, 0 },
			BYTES_GOLD = new byte[]{ (byte) 240, (byte) 200, 30 };
	
	private boolean moddedFOV = false;
	private float fov = -1;
	
	private boolean muted = false;
	private float defVol;

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
		 * Fixes some rendering issues with high FOVs when the GUIs are open during sleep
		 */
		
		if (mc.player.isPlayerSleeping())
		{
			if (SomniaConfig.OPTIONS.vanillaBugFixes)
			{
				if (!moddedFOV)
				{
					moddedFOV = true;
					if (gameSettings.fovSetting >= 0.75352114)
					{
						fov = gameSettings.fovSetting;
						gameSettings.fovSetting = 0.7253521f;
					}
				}
			}
		}
		else if (moddedFOV)
		{
			moddedFOV = false;
			if (fov > .0f)
				Minecraft.getMinecraft().gameSettings.fovSetting = fov;
		}
		
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
					defVol = gameSettings.getSoundLevel(SoundCategory.MASTER);
					gameSettings.setSoundLevel(SoundCategory.MASTER, .0f);
				}
			}
			if (mc.player.isPlayerSleeping() && !net.minecraftforge.event.ForgeEventFactory.fireSleepingLocationCheck(mc.player, mc.player.bedLocation)) Somnia.eventChannel.sendToServer(PacketHandler.buildWakePacket());
		}
		else
		{
			if (muted)
			{
				muted = false;
				gameSettings.setSoundLevel(SoundCategory.MASTER, defVol);
			}
		}
		
		/*
		 * Note the isPlayerSleeping() check. Without this, the mod exploits a bug which exists in vanilla Minecraft which
		 * allows the player to teleport back to there bed from anywhere in the world at any time.
		 */
		if (Somnia.clientAutoWakeTime > -1 && mc.player.isPlayerSleeping() && mc.world.getTotalWorldTime() >= Somnia.clientAutoWakeTime)
		{
			Somnia.clientAutoWakeTime = -1;
			Somnia.eventChannel.sendToServer(PacketHandler.buildWakePacket());
		}
	}
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		if ((mc.currentScreen != null && !(mc.currentScreen instanceof GuiIngameMenu))) {
			if (mc.player == null || !mc.player.isPlayerSleeping()) return;
		}
		
		FontRenderer fontRenderer = mc.fontRenderer;
		ScaledResolution scaledResolution = new ScaledResolution(mc);
		if (event.phase == Phase.END && !mc.player.capabilities.isCreativeMode) {
			String str = String.format(FATIGUE_FORMAT, ClientProxy.playerFatigue);
			int x, y, stringWidth = fontRenderer.getStringWidth(str);
			String param = mc.player.isPlayerSleeping() ? "br" : SomniaConfig.FATIGUE.displayFatigue.toLowerCase();
			switch (param) {
				case "tc":
					x = (scaledResolution.getScaledWidth() / 2 ) - (stringWidth / 2);
					y = fontRenderer.FONT_HEIGHT;
					break;
				case "tl":
					x = 10;
					y = fontRenderer.FONT_HEIGHT;
					break;
				case "tr":
					x = scaledResolution.getScaledWidth() - stringWidth - 10;
					y = fontRenderer.FONT_HEIGHT;
					break;
				case "bc":
					x = (scaledResolution.getScaledWidth() / 2 ) - (stringWidth / 2);
					y = scaledResolution.getScaledHeight() - fontRenderer.FONT_HEIGHT - 45;
					break;
				case "bl":
					x = 10;
					y = scaledResolution.getScaledHeight() - fontRenderer.FONT_HEIGHT - 10;
					break;
				case "br":
					x = scaledResolution.getScaledWidth() - stringWidth - 10;
					y = scaledResolution.getScaledHeight() - fontRenderer.FONT_HEIGHT - 10;
					break;
				default:
					return;
			}
			fontRenderer.drawString(str, x, y, Integer.MIN_VALUE);
		}

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
				startTicks = this.mc.world.getTotalWorldTime();
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
		if (startTicks != -1L && Somnia.clientAutoWakeTime != -1)
		{
			// Progress Bar
			mc.getTextureManager().bindTexture(Gui.ICONS);

			double 	rel = mc.world.getTotalWorldTime()-startTicks,
					diff = Somnia.clientAutoWakeTime-startTicks,
					progress = rel / diff;

			int 	x = 20,
					maxWidth = (scaledResolution.getScaledWidth()-(x*2));

			glEnable(GL_BLEND);
			glColor4f(1.0f, 1.0f, 1.0f, .2f);
			renderProgressBar(x, 10, maxWidth, 1.0d);

			glDisable(GL_BLEND);
			glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			renderProgressBar(x, 10, maxWidth, progress);

			// Multiplier
			int offsetX = SomniaConfig.FATIGUE.displayETASleep.equals("center") ? scaledResolution.getScaledWidth()/2 - 80 : SomniaConfig.FATIGUE.displayETASleep.equals("right") ? maxWidth - 160 : 0;
			renderScaledString(scaledResolution, x + offsetX, 20, 1.5f, SPEED_FORMAT, getColorStringForSpeed(speed), speed);

			// ETA
			double total = 0.0d;
			for (double value : speedValues)
				total += value;
			double avg = total / speedValues.size();
			int etaTotalSeconds = (int)((diff-rel) / (avg*20)); // remaining ticks / (average multiplier * standard tick rate)

			int etaSeconds = etaTotalSeconds % 60,
					etaMinutes = (etaTotalSeconds-etaSeconds) / 60;

			renderScaledString(scaledResolution, x + 50 + 10 + offsetX, 20, 1.5f, ETA_FORMAT, (etaMinutes<10?"0":"") + etaMinutes, (etaSeconds<10?"0":"") + etaSeconds);

			// Clock
			renderClock(maxWidth - 40, 30, 4.0f);
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

	private void renderScaledString(ScaledResolution scaledResolution, int x, int y, float scale, String format, Object... args)
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

		//return (int) (fontRendererObj.getStringWidth(str) * scale);
	}

	private void renderClock(int x, int y, float scale)
	{
		glPushMatrix();
		{
			glTranslatef(x, y, 0.0f);
			glScalef(scale, scale, 1.0f);
			mc.getRenderItem().renderItemAndEffectIntoGUI(mc.player, clockItemStack, 0, 0);
		}
		glPopMatrix();
	}

	public static byte[] getColorForSpeed(double speed)
	{
		if (speed < 8)
			return BYTES_WHITE;
		else if (speed < 20)
			return BYTES_DARK_RED;
		else if (speed < 30)
			return BYTES_RED;
		else
			return BYTES_GOLD;
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