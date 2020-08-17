package com.kingrunes.somnia.server;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.api.capability.FatigueCapabilityProvider;
import com.kingrunes.somnia.api.capability.IFatigue;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.PlayerSleepTickHandler;
import com.kingrunes.somnia.common.SomniaConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ForgeEventHandler
{
	@SubscribeEvent
	public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
	{
		event.addCapability(new ResourceLocation(Somnia.MOD_ID, "fatigue"), new FatigueCapabilityProvider());
	}

	@SuppressWarnings("ConstantConditions")
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase != Phase.START || event.player.world.isRemote || (event.player.capabilities.isCreativeMode && !event.player.isPlayerSleeping())) return;
		
		EntityPlayer player = event.player;
		if (!player.hasCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null)) return;

		IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
		double fatigue = props.getFatigue();
		
		boolean isSleeping = PlayerSleepTickHandler.serverState.sleepOverride || player.isPlayerSleeping();
		
		if (isSleeping)
			fatigue -= SomniaConfig.FATIGUE.fatigueReplenishRate;
		else
			fatigue += SomniaConfig.FATIGUE.fatigueRate;
		
		if (fatigue > 100.0d)
			fatigue = 100.0d;
		else if (fatigue < .0d)
			fatigue = .0d;
//		fatigue = 69.8d;
		props.setFatigue(fatigue);
		if (props.updateFatigueCounter() >= 100)
		{
			props.resetFatigueCounter();
			Somnia.eventChannel.sendTo(PacketHandler.buildPropUpdatePacket(0x01, 0x00, fatigue), (EntityPlayerMP) player);
			
			// Side effects
			if (SomniaConfig.FATIGUE.fatigueSideEffects)
			{
				int lastSideEffectStage = props.getSideEffectStage();
				if (fatigue > SomniaConfig.SIDE_EFFECTS.sideEffectStage1 && lastSideEffectStage < SomniaConfig.SIDE_EFFECTS.sideEffectStage1)
				{
					props.setSideEffectStage(SomniaConfig.SIDE_EFFECTS.sideEffectStage1);
					player.addPotionEffect(new PotionEffect(Potion.getPotionById(SomniaConfig.SIDE_EFFECTS.sideEffectStage1Potion), SomniaConfig.SIDE_EFFECTS.sideEffectStage1Duration, SomniaConfig.SIDE_EFFECTS.sideEffectStage1Amplifier));
				}
				else if (fatigue > SomniaConfig.SIDE_EFFECTS.sideEffectStage2 && lastSideEffectStage < SomniaConfig.SIDE_EFFECTS.sideEffectStage2)
				{
					props.setSideEffectStage(SomniaConfig.SIDE_EFFECTS.sideEffectStage2);
					player.addPotionEffect(new PotionEffect(Potion.getPotionById(SomniaConfig.SIDE_EFFECTS.sideEffectStage2Potion), SomniaConfig.SIDE_EFFECTS.sideEffectStage2Duration, SomniaConfig.SIDE_EFFECTS.sideEffectStage2Amplifier));
				}
				else if (fatigue > SomniaConfig.SIDE_EFFECTS.sideEffectStage3 && lastSideEffectStage < SomniaConfig.SIDE_EFFECTS.sideEffectStage3)
				{
					props.setSideEffectStage(SomniaConfig.SIDE_EFFECTS.sideEffectStage3);
					player.addPotionEffect(new PotionEffect(Potion.getPotionById(SomniaConfig.SIDE_EFFECTS.sideEffectStage3Potion), SomniaConfig.SIDE_EFFECTS.sideEffectStage3Duration, SomniaConfig.SIDE_EFFECTS.sideEffectStage3Amplifier));
				}
				else if (fatigue > SomniaConfig.SIDE_EFFECTS.sideEffectStage4)
					player.addPotionEffect(new PotionEffect(Potion.getPotionById(SomniaConfig.SIDE_EFFECTS.sideEffectStage4Potion), 150, SomniaConfig.SIDE_EFFECTS.sideEffectStage4Amplifier));
				else if (fatigue < SomniaConfig.SIDE_EFFECTS.sideEffectStage1) {
					props.setSideEffectStage(-1);
					player.clearActivePotions();
				}
			}
		}
	}

	@SubscribeEvent
	public void onWakeUp(PlayerWakeUpEvent event) {
		IFatigue props = event.getEntityPlayer().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
		if (props != null) {
			props.maxFatigueCounter();
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		NBTTagCompound old = event.getOriginal().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).serializeNBT();
		event.getEntityPlayer().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).deserializeNBT(old);
	}

	@SubscribeEvent
	public void onPlayerLogin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
		sync(event.player);
	}

	@SubscribeEvent
	public void onPlayerDimensionChange(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
		sync(event.player);
	}

	@SubscribeEvent
	public void onPlayerRespawn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
		sync(event.player);
	}

	private void sync(EntityPlayer player) {
		if (!player.hasCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null) || !(player instanceof EntityPlayerMP)) return;
		Somnia.eventChannel.sendTo(PacketHandler.buildPropUpdatePacket(0x01, 0x00, player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null).getFatigue()), (EntityPlayerMP) player);
	}
}
