package com.kingrunes.somnia;

import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.PlayerSleepTickHandler;
import com.kingrunes.somnia.common.SomniaConfig;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;
import com.kingrunes.somnia.server.SomniaCommand;
import com.kingrunes.somnia.setup.IProxy;
import net.minecraft.entity.player.EntityPlayerMP;
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
	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		logger.info("------ Pre-Init -----");
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) 
	{
		logger.info("------ Init -----");
		eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MOD_ID);
		eventChannel.register(new PacketHandler());

		proxy.register();

		MinecraftForge.EVENT_BUS.register(new PlayerSleepTickHandler());

		forgeEventHandler = new ForgeEventHandler();
		MinecraftForge.EVENT_BUS.register(forgeEventHandler);
		CapabilityFatigue.register();
	}
	
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new SomniaCommand());
	}
}