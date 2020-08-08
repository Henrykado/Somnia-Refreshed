package com.kingrunes.somnia.common;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.capability.CapabilityFatigue;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class CommonProxy
{
	private static final int CONFIG_VERSION = 2;
	
	public static TimePeriod 	enterSleepPeriod;
	public TimePeriod 	validSleepPeriod;
	
	public static double 	fatigueRate,
							fatigueReplenishRate,
							minimumFatigueToSleep,
							baseMultiplier,
							multiplierCap;
	
	public static boolean 	fatigueSideEffects,
							tpsGraph,
							secondsOnGraph,
							sleepWithArmor,
							vanillaBugFixes,
							fading,
							somniaGui,
							muteSoundWhenSleeping,
							ignoreMonsters,
							disableCreatureSpawning,
							disableRendering,
							disableMoodSoundAndLightCheck;

	public static int 	sideEffectStage1,
						sideEffectStage1Potion,
						sideEffectStage1Amplifier,
						sideEffectStage1Duration,
						sideEffectStage2,
						sideEffectStage2Potion,
						sideEffectStage2Amplifier,
						sideEffectStage2Duration,
						sideEffectStage3,
						sideEffectStage3Potion,
						sideEffectStage3Amplifier,
						sideEffectStage3Duration,
						sideEffectStage4,
						sideEffectStage4Potion,
						sideEffectStage4Amplifier;

	public String		displayFatigue;
	
	public static ForgeEventHandler forgeEventHandler;
	
	public void configure(File file)
	{
		Configuration config = new Configuration(file);
		config.load();
		Property property = config.get(Configuration.CATEGORY_GENERAL, "configVersion", 0);
		if (property.getInt() != CONFIG_VERSION)
			file.delete();
		config = new Configuration(file);
		config.load();
		
		config.get(Configuration.CATEGORY_GENERAL, "configVersion", CONFIG_VERSION);

		/*
		 * Timings
		 */
		enterSleepPeriod =
				new TimePeriod(
						config.get("timings", "enterSleepStart", 0).getInt(),
						config.get("timings", "enterSleepEnd", 24000).getInt()
						);
		validSleepPeriod =
				new TimePeriod(
						config.get("timings", "validSleepStart", 0).getInt(),
						config.get("timings", "validSleepEnd", 24000).getInt()
						);
		
		/*
		 * Fatigue
		 */
		fatigueSideEffects = config.get("fatigue", "fatigueSideEffects", true).getBoolean(true);
		displayFatigue = config.get("fatigue", "displayFatigue", "br").getString();
		fatigueRate = config.get("fatigue", "fatigueRate", 0.00208d).getDouble(0.00208d);
		fatigueReplenishRate = config.get("fatigue", "fatigueReplenishRate", 0.00833d).getDouble(0.00833d);
		minimumFatigueToSleep = config.get("fatigue", "minimumFatigueToSleep", 20.0d).getDouble(20.0d);

		/*
		 * fatigue side effects
		 */
		config.setCategoryComment("fatigue side effects", "Fatigue levels to enter each side effect stage, their potion IDs, amplifiers and duration (ticks)");
		sideEffectStage1 = config.get("fatigue side effects", "sideEffectStage1", 70).getInt();
		sideEffectStage1Potion = config.get("fatigue side effects", "sideEffectStage1Potion", 9).getInt();
		sideEffectStage1Amplifier = config.get("fatigue side effects", "sideEffectStage1Amplifier", 0).getInt();
		sideEffectStage1Duration = config.get("fatigue side effects", "sideEffectStage1Duration", 150).getInt();

		sideEffectStage2 = config.get("fatigue side effects", "sideEffectStage2", 80).getInt();
		sideEffectStage2Potion = config.get("fatigue side effects", "sideEffectStage2Potion", 2).getInt();
		sideEffectStage2Amplifier = config.get("fatigue side effects", "sideEffectStage2Amplifier", 2).getInt();
		sideEffectStage2Duration = config.get("fatigue side effects", "sideEffectStage2Duration", 300).getInt();

		sideEffectStage3 = config.get("fatigue side effects", "sideEffectStage3", 90).getInt();
		sideEffectStage3Potion = config.get("fatigue side effects", "sideEffectStage3Potion", 19).getInt();
		sideEffectStage3Amplifier = config.get("fatigue side effects", "sideEffectStage3Amplifier", 1).getInt();
		sideEffectStage3Duration = config.get("fatigue side effects", "sideEffectStage3Duration", 200).getInt();

		sideEffectStage4 = config.get("fatigue side effects", "sideEffectStage4", 95).getInt();
		sideEffectStage4Potion = config.get("fatigue side effects", "sideEffectStage4Potion", 2).getInt();
		sideEffectStage4Amplifier = config.get("fatigue side effects", "sideEffectStage4Amplifier", 3).getInt();

		/*
		 * Logic
		 */
		baseMultiplier = config.get("logic", "baseMultiplier", 1.0d).getDouble(1.0d);
		multiplierCap = config.get("logic", "multiplierCap", 100.0d).getDouble(100.0d);
		
		/*
		 * Profiling (Not implemented)
		secondsOnGraph = config.get("profiling", "secondsOnGraph", 30).getInt();
		tpsGraph = config.get("profiling", "tpsGraph", false).getBoolean(false);
		*/
		
		/*
		 * Options
		 */
		sleepWithArmor = config.get("options", "sleepWithArmor", false).getBoolean(false);
		vanillaBugFixes = config.get("options", "vanillaBugFixes", true).getBoolean(true);
		fading = config.get("options", "fading", true).getBoolean(true);
		somniaGui = config.get("options", "somniaGui", true).getBoolean(true);
		muteSoundWhenSleeping = config.get("options", "muteSoundWhenSleeping", false).getBoolean(false);
		ignoreMonsters = config.get("options", "ignoreMonsters", false).getBoolean(false);

		/*
		 * Performance
		 */
		disableCreatureSpawning = config.get("performance", "disableCreatureSpawning", false).getBoolean(false);
		disableRendering = config.get("performance", "disableRendering", false).getBoolean(false);
		disableMoodSoundAndLightCheck = config.get("performance", "disableMoodSoundAndLightCheck", false).getBoolean(false);
		
		config.save();
	}
	
	public void register()
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		MinecraftForge.EVENT_BUS.register(new PlayerSleepTickHandler());
		
		forgeEventHandler = new ForgeEventHandler();
		MinecraftForge.EVENT_BUS.register(forgeEventHandler);
		MinecraftForge.EVENT_BUS.register(forgeEventHandler);
		CapabilityFatigue.register();
	}


	@SubscribeEvent
	public void worldLoadHook(WorldEvent.Load event)
	{
		if (event.getWorld() instanceof WorldServer)
		{
			WorldServer worldServer = (WorldServer)event.getWorld();
			Somnia.instance.tickHandlers.add(new ServerTickHandler(worldServer));
			Somnia.logger.info("Registering tick handler for loading world!");
		}
	}
	
	@SubscribeEvent
	public void worldUnloadHook(WorldEvent.Unload event)
	{
		if (event.getWorld() instanceof WorldServer)
		{
			WorldServer worldServer = (WorldServer)event.getWorld();
			Iterator<ServerTickHandler> iter = Somnia.instance.tickHandlers.iterator();
			ServerTickHandler serverTickHandler;
			while (iter.hasNext())
			{
				serverTickHandler = iter.next();
				if (serverTickHandler.worldServer == worldServer)
				{
					Somnia.logger.info("Removing tick handler for unloading world!");
					iter.remove();
					break;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerDamage(LivingHurtEvent event)
	{
		if (event.getEntityLiving() instanceof EntityPlayerMP)
		{
			if (!(event.getEntityLiving()).isPlayerSleeping())
				return;
			
	        Somnia.eventChannel.sendTo(PacketHandler.buildGUIClosePacket(), (EntityPlayerMP) event.getEntityLiving());
		}
	}
	
	/*
	 * The following methods are implemented client-side only
	 */
	
	public void handleGUIOpenPacket(DataInputStream in) throws IOException
	{}

	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{}

	public void handleGUIClosePacket(EntityPlayerMP player)
	{}
}