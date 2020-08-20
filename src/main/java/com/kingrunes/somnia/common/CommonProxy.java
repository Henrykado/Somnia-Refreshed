package com.kingrunes.somnia.common;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.common.compat.CompatModule;
import com.kingrunes.somnia.common.compat.RailcraftPlugin;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;

public class CommonProxy
{
	public static TimePeriod enterSleepPeriod = new TimePeriod(SomniaConfig.TIMINGS.enterSleepStart, SomniaConfig.TIMINGS.enterSleepEnd);
	public static TimePeriod validSleepPeriod = new TimePeriod(SomniaConfig.TIMINGS.validSleepStart, SomniaConfig.TIMINGS.validSleepEnd);
	public static ForgeEventHandler forgeEventHandler;
	
	public void register()
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		MinecraftForge.EVENT_BUS.register(new PlayerSleepTickHandler());
		
		forgeEventHandler = new ForgeEventHandler();
		MinecraftForge.EVENT_BUS.register(forgeEventHandler);
		CapabilityFatigue.register();
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
			
	        Somnia.eventChannel.sendTo(PacketHandler.buildWakePacket(), (EntityPlayerMP) event.getEntityLiving());
		}
	}

	/*
	 * The following methods are implemented client-side only
	 */
	
	public void handleGUIOpenPacket()
	{}

	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{}

	public void handleWakePacket(EntityPlayerMP player)
	{
		player.wakeUpPlayer(true, true, true);
	}
}