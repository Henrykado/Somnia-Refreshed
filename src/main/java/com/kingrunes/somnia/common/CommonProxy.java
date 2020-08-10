package com.kingrunes.somnia.common;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.common.capability.CapabilityFatigue;
import com.kingrunes.somnia.common.util.TimePeriod;
import com.kingrunes.somnia.server.ForgeEventHandler;
import com.kingrunes.somnia.server.ServerTickHandler;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
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
		MinecraftForge.EVENT_BUS.register(forgeEventHandler);
		CapabilityFatigue.register();
	}

	@SubscribeEvent
	public void interactHook(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();

		BlockPos pos = event.getPos();

		EntityPlayer player = event.getEntityPlayer();
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock() == Blocks.BED) {

			if (state.getValue(BlockBed.PART) != BlockBed.EnumPartType.HEAD) {
				pos = pos.offset(state.getValue(BlockBed.FACING));
				state = world.getBlockState(pos);

				if (!(state.getBlock() instanceof BlockBed)) {
					return;
				}
			}
		}

		if (Math.abs(player.posX - (double)pos.getX()) < 3.0D && Math.abs(player.posY - (double)pos.getY()) < 2.0D && Math.abs(player.posZ - (double)pos.getZ()) < 3.0D)
		{
			ItemStack currentItem = player.inventory.getCurrentItem();
			ResourceLocation registryName = currentItem.getItem().getRegistryName();
			if (currentItem != ItemStack.EMPTY && registryName != null && registryName.toString().equals(SomniaConfig.OPTIONS.wakeTimeSelectItem)) {
				if (world.isRemote) {
					Minecraft minecraft = Minecraft.getMinecraft();
					if (minecraft.currentScreen instanceof GuiSelectWakeTime) return;
				}
				else Somnia.eventChannel.sendTo(PacketHandler.buildGuiSelectWakeTimePacket(), (EntityPlayerMP) player);

				event.setCanceled(true);
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
			
	        Somnia.eventChannel.sendTo(PacketHandler.buildGUIClosePacket(), (EntityPlayerMP) event.getEntityLiving());
		}
	}

	/*@SubscribeEvent
	public void onGuiDraw(GuiScreenEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player == null) return;

		if (mc.player.isPlayerSleeping() && SomniaConfig.OPTIONS.somniaGui && !(event.getGui() instanceof GuiSomnia)) {
			mc.addScheduledTask(() -> mc.displayGuiScreen(new GuiSomnia()));
			event.setCanceled(true);
		}
	}*/

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