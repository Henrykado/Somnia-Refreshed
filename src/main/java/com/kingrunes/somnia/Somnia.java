package com.kingrunes.somnia;

import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.PlayerSleepTickHandler;
import com.kingrunes.somnia.common.SomniaConfig;
import com.kingrunes.somnia.common.SomniaPotions;
import com.kingrunes.somnia.common.compat.CompatModule;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;
import com.kingrunes.somnia.server.SomniaCommand;
import com.kingrunes.somnia.setup.IProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = Somnia.MOD_ID, name = Somnia.NAME, dependencies = "after:railcraft; after:baubles")
public class Somnia
{
	public static final String MOD_ID = "somnia";
	public static final String NAME = "Somnia";

	public final List<ServerTickHandler> tickHandlers = new ArrayList<>();
	public final List<WeakReference<EntityPlayerMP>> ignoreList = new ArrayList<>();

	@Instance(Somnia.MOD_ID)
	public static Somnia instance;

	@SidedProxy(serverSide="com.kingrunes.somnia.setup.ServerProxy", clientSide="com.kingrunes.somnia.setup.ClientProxy")
	public static IProxy proxy;
	public static Logger logger;
	public static FMLEventChannel eventChannel;
	public static TimePeriod enterSleepPeriod = new TimePeriod(SomniaConfig.TIMINGS.enterSleepStart, SomniaConfig.TIMINGS.enterSleepEnd);
	public static TimePeriod validSleepPeriod = new TimePeriod(SomniaConfig.TIMINGS.validSleepStart, SomniaConfig.TIMINGS.validSleepEnd);
	public static ForgeEventHandler forgeEventHandler;

	@SuppressWarnings("incomplete-switch")
	@EventHandler
    public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		logger.info("------ Pre-Init -----");
		
		switch (SomniaConfig.TIMINGS._enterSleepPreset)
		{
			case NIGHT: enterSleepPeriod = new TimePeriod(12000, 24000); break;
			case DAY: enterSleepPeriod = new TimePeriod(24000, 12000); break;
		}

		forgeEventHandler = new ForgeEventHandler();
		MinecraftForge.EVENT_BUS.register(forgeEventHandler);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		logger.info("------ Init -----");
		eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MOD_ID);
		eventChannel.register(new PacketHandler());
		proxy.register();

		if (!SomniaConfig.OPTIONS.disablePotions)
		{
			PotionHelper.addMix(PotionTypes.NIGHT_VISION, Items.SPECKLED_MELON, SomniaPotions.awakeningPotionType);
			PotionHelper.addMix(PotionTypes.LONG_NIGHT_VISION, Items.SPECKLED_MELON, SomniaPotions.longAwakeningPotionType);
			PotionHelper.addMix(SomniaPotions.awakeningPotionType, Items.BLAZE_POWDER, SomniaPotions.strongAwakeningPotionType);
	
			PotionHelper.addMix(SomniaPotions.awakeningPotionType, Items.FERMENTED_SPIDER_EYE, SomniaPotions.insomniaPotionType);
			PotionHelper.addMix(SomniaPotions.longAwakeningPotionType, Items.FERMENTED_SPIDER_EYE, SomniaPotions.longInsomniaPotionType);
			PotionHelper.addMix(SomniaPotions.strongAwakeningPotionType, Items.FERMENTED_SPIDER_EYE, SomniaPotions.strongInsomniaPotionType);
		}
			
		MinecraftForge.EVENT_BUS.register(new PlayerSleepTickHandler());

		CapabilityFatigue.register();
		SomniaConfig.registerReplenishingItems();
 	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new SomniaCommand());
	}
}
