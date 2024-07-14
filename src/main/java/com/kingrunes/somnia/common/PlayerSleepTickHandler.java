package com.kingrunes.somnia.common;

import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.api.capability.IFatigue;
import com.kingrunes.somnia.common.compat.CompatModule;
import com.kingrunes.somnia.common.util.InvUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerSleepTickHandler
{
	/*
	 * A sided state for caching player data 
	 */
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == Phase.START) tickStart(event.player);
	}

	public void tickStart(EntityPlayer player)
	{
		if (player.isPlayerSleeping())
		{			
			BlockPos pos = player.bedLocation;
			boolean sleepOverride = true;

			//Reset fatigue in case you pick the charm up while sleeping. Doesn't trigger otherwise, because Somnia keeps the sleep timer below 100
			if (player.sleepTimer > 99 && Loader.isModLoaded("darkutils") && InvUtil.hasItem(player, CompatModule.CHARM_SLEEP)) {
				player.sleepTimer = 100;
				sleepOverride = false;
				return;
			}

			if (player.hasCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null)) {
				IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
				if (props.shouldSleepNormally()) {
					sleepOverride = false;
					return;
				}
			}

			if (!CompatModule.checkBed(player, pos)) {
				sleepOverride = false;
				
			}

			if (!sleepOverride)
			{
				if (player.hasCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null)) {
					IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
					props.setSleepNormally(true);
				}
				return;
			}
			
			if (player.world.isRemote && SomniaConfig.OPTIONS.fading)
			{
				int sleepTimer = player.getSleepTimer()+1;
				if (sleepTimer >= 99)
					sleepTimer = 98;
				player.sleepTimer = sleepTimer;
			}
		}
	}
}