package com.kingrunes.somnia;

import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.PlayerSleepTickHandler;
import com.kingrunes.somnia.common.SomniaConfig;
import com.kingrunes.somnia.common.compat.CompatModule;
import com.kingrunes.somnia.common.compat.RailcraftPlugin;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;
import com.kingrunes.somnia.server.SomniaCommand;
import com.kingrunes.somnia.setup.IProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = Somnia.MOD_ID, name = Somnia.NAME, version="-au", dependencies = "after:railcraft; after:baubles")
public class Somnia
{
	public static final String MOD_ID = "somnia";
	public static final String NAME = "Somnia";
	public static final String VERSION = SomniaVersion.getVersionString();
	
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
		event.getModMetadata().version = VERSION;
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

	@SubscribeEvent
	public void interactHook(PlayerInteractEvent event) {
		World world = event.getWorld();

		BlockPos pos = event.getPos();

		EntityPlayer player = event.getEntityPlayer();

		if ((event instanceof PlayerInteractEvent.RightClickBlock && CompatModule.isBed(player, pos)) || (event instanceof PlayerInteractEvent.EntityInteractSpecific && ((PlayerInteractEvent.EntityInteractSpecific)event).getTarget().getClass() == RailcraftPlugin.BED_CART_CLASS)) {
			if (player.bedInRange(pos, null)) //the facing can be null
			{
				ItemStack currentItem = player.inventory.getCurrentItem();
				ResourceLocation registryName = currentItem.getItem().getRegistryName();
				if (currentItem != ItemStack.EMPTY && registryName != null && registryName.toString().equals(SomniaConfig.OPTIONS.wakeTimeSelectItem)) {
					if (world.isRemote) {
						Minecraft minecraft = Minecraft.getMinecraft();
						if (minecraft.currentScreen instanceof GuiSelectWakeTime) return;
					}
					else Somnia.eventChannel.sendTo(PacketHandler.buildGUIOpenPacket(), (EntityPlayerMP) player);

					event.setCancellationResult(EnumActionResult.SUCCESS);
					event.setCanceled(true);
				}
			}
		}
	}
}