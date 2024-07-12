package com.kingrunes.somnia.common.util;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.api.capability.IFatigue;
import com.kingrunes.somnia.server.ServerTickHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

public enum SomniaState
{
	IDLE,
	ACTIVE,
	WAITING_PLAYERS,
	EXPIRED,
	NOT_NOW,
	COOLDOWN;
	
	public static SomniaState getState(ServerTickHandler handler)
	{	
		long worldTime = handler.worldServer.getWorldTime() % 24000;
		if (!Somnia.validSleepPeriod.isTimeWithin(worldTime))
			return NOT_NOW;
		
		if (handler.worldServer.playerEntities.isEmpty())
			return IDLE;
		
		List<EntityPlayer> players = handler.worldServer.playerEntities;
		
		boolean anySleeping = false, allSleeping = true;
		int somniaSleep = 0, normalSleep = 0;
		
		for (EntityPlayer entityPlayer : players) {
			EntityPlayerMP player = (EntityPlayerMP) entityPlayer;
			boolean sleeping = player.isPlayerSleeping() || ListUtils.containsRef(player, Somnia.instance.ignoreList);
			anySleeping |= sleeping;
			allSleeping &= sleeping;

			IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
			
			if (props != null && props.shouldSleepNormally()) normalSleep++;
			else somniaSleep++;
		}
		
		if (allSleeping) {
			if (somniaSleep >= normalSleep) return ACTIVE;
		} else if (anySleeping) return WAITING_PLAYERS;

		return IDLE;
	}
}
