package com.kingrunes.somnia.server;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.SomniaAPI;
import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.api.capability.FatigueCapabilityProvider;
import com.kingrunes.somnia.api.capability.IFatigue;
import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.PlayerSleepTickHandler;
import com.kingrunes.somnia.common.SomniaConfig;
import com.kingrunes.somnia.common.SomniaPotions;
import com.kingrunes.somnia.common.compat.CompatModule;
import com.kingrunes.somnia.common.compat.RailcraftPlugin;
import com.kingrunes.somnia.common.potion.PotionAwakening;
import com.kingrunes.somnia.common.potion.PotionInsomnia;
import com.kingrunes.somnia.common.util.InvUtil;
import com.kingrunes.somnia.common.util.SideEffectStage;
import com.kingrunes.somnia.common.util.SomniaUtil;
import com.kingrunes.somnia.setup.ClientProxy;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ForgeEventHandler
{
	@SubscribeEvent
	public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
	{
		event.addCapability(new ResourceLocation(Somnia.MOD_ID, "fatigue"), new FatigueCapabilityProvider());
	}

	@SubscribeEvent
	public void onPotionRegister(RegistryEvent.Register<Potion> event)
	{
		event.getRegistry().registerAll(
				SomniaPotions.awakeningPotion = new PotionAwakening(),
				SomniaPotions.insomniaPotion = new PotionInsomnia()
		);
	}

	@SubscribeEvent
	public void onEffectRegister(RegistryEvent.Register<PotionType> event)
	{
		event.getRegistry().registerAll(
				SomniaPotions.awakeningPotionType = new PotionType("awakening", new PotionEffect(SomniaPotions.awakeningPotion, 2400)).setRegistryName("awakening"),
				SomniaPotions.longAwakeningPotionType = new PotionType("awakening", new PotionEffect(SomniaPotions.awakeningPotion, 3600)).setRegistryName("long_awakening"),
				SomniaPotions.strongAwakeningPotionType = new PotionType("awakening", new PotionEffect(SomniaPotions.awakeningPotion, 2400, 1)).setRegistryName("strong_awakening"),

				SomniaPotions.insomniaPotionType = new PotionType("insomnia", new PotionEffect(SomniaPotions.insomniaPotion, 1800)).setRegistryName("insomnia"),
				SomniaPotions.longInsomniaPotionType = new PotionType("insomnia", new PotionEffect(SomniaPotions.insomniaPotion, 3000)).setRegistryName("long_insomnia"),
				SomniaPotions.strongInsomniaPotionType = new PotionType("insomnia", new PotionEffect(SomniaPotions.insomniaPotion, 1800, 1)).setRegistryName("strong_insomnia")
		);
	}

	@SuppressWarnings("ConstantConditions")
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase != Phase.START || event.player.world.isRemote || (!event.player.isEntityAlive() || event.player.isCreative() || event.player.isSpectator()) && !event.player.isPlayerSleeping()) return;

		EntityPlayer player = event.player;
		if (!player.hasCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null)) return;

		IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
		double extraFatigueRate = props.getExtraFatigueRate();
		double replenishedFatigue = props.getReplenishedFatigue();
		double fatigue = props.getFatigue();
		boolean isSleeping = PlayerSleepTickHandler.serverState.sleepOverride || player.isPlayerSleeping();

		if (isSleeping) {
			fatigue -= SomniaConfig.FATIGUE.fatigueReplenishRate;
			double share = SomniaConfig.FATIGUE.fatigueReplenishRate / SomniaConfig.FATIGUE.fatigueRate;
			double replenish = SomniaConfig.FATIGUE.fatigueReplenishRate * share;
			extraFatigueRate -= SomniaConfig.FATIGUE.fatigueReplenishRate / share / replenishedFatigue / 10;
			replenishedFatigue -= replenish;
		}
		else {
			double rate = SomniaConfig.FATIGUE.fatigueRate;

			PotionEffect wakefulness = player.getActivePotionEffect(SomniaPotions.awakeningPotion);
			if (wakefulness != null)
			{
				int amplifier = wakefulness.getAmplifier();
				rate -= amplifier == 0 ? rate / 4 : rate / 3;
			}

			PotionEffect insomnia = player.getActivePotionEffect(SomniaPotions.insomniaPotion);
			if (insomnia != null)
			{
				int amplifier = insomnia.getAmplifier();
				rate += amplifier == 0 ? rate / 2 : rate;
			}

			fatigue += rate + props.getExtraFatigueRate();
		}

		if (fatigue > 100.0d)
			fatigue = 100.0d;
		else if (fatigue < .0d)
			fatigue = .0d;

		if (replenishedFatigue > 100)
			replenishedFatigue = 100;
		else if (replenishedFatigue < 0)
			replenishedFatigue = 0;

		if (extraFatigueRate < 0)
			extraFatigueRate = 0;

		props.setFatigue(fatigue);
		props.setReplenishedFatigue(replenishedFatigue);
		props.setExtraFatigueRate(extraFatigueRate);

		if (props.updateFatigueCounter() >= 100)
		{
			props.resetFatigueCounter();
			Somnia.eventChannel.sendTo(PacketHandler.buildPropUpdatePacket(0x01, 0x00, fatigue), (EntityPlayerMP) player);

			// Side effects
			if (SomniaConfig.FATIGUE.fatigueSideEffects)
			{
				int lastSideEffectStage = props.getSideEffectStage();
				SideEffectStage firstStage = SideEffectStage.stages[0];
				if (fatigue < firstStage.minFatigue)
				{
					props.setSideEffectStage(-1);
					for (SideEffectStage stage : SideEffectStage.stages)
					{
						if (lastSideEffectStage < stage.minFatigue)
							player.removePotionEffect(Potion.getPotionById(stage.potionID));
					}
				}

				for (int i = 0; i < SomniaConfig.FATIGUE.sideEffectStages.length; i++)
				{
					SideEffectStage stage = SideEffectStage.stages[i];
					boolean permanent = stage.duration < 0;
					if (fatigue >= stage.minFatigue && fatigue <= stage.maxFatigue && (permanent || lastSideEffectStage < stage.minFatigue))
					{
						if (!permanent) props.setSideEffectStage(stage.minFatigue);
						player.addPotionEffect(new PotionEffect(Potion.getPotionById(stage.potionID), permanent ? 150 : stage.duration, stage.amplifier));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onWakeUp(PlayerWakeUpEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
		if (props != null) {
			if (props.shouldSleepNormally() && player.sleepTimer == 100 && player.world.getTotalWorldTime() % 24000 < 7000) {
				props.setFatigue(props.getFatigue() - SomniaUtil.calculateFatigueToReplenish(player));
			}
			props.maxFatigueCounter();
			props.setResetSpawn(true);
			props.setSleepNormally(false);
		}
		if (player.world.isRemote) {
			ClientProxy.clientAutoWakeTime = -1;
		}
	}

	/**
	 * Re-implementation of the sleep method.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSleep(PlayerSleepInBedEvent event) {
		if (event.getResultStatus() == EntityPlayer.SleepResult.OTHER_PROBLEM) return;
		EntityPlayer player = event.getEntityPlayer();
		BlockPos pos = event.getPos();
		final IBlockState state = player.world.isBlockLoaded(pos) ? player.world.getBlockState(pos) : null;
		final boolean isBed = state != null && state.getBlock().isBed(state, player.world, pos, player);
		final EnumFacing enumfacing = isBed && state.getBlock() instanceof BlockHorizontal ? state.getValue(BlockHorizontal.FACING) : null;
		final boolean sleepCharm = Loader.isModLoaded("darkutils") && InvUtil.hasItem(player, CompatModule.CHARM_SLEEP);
		final boolean sleepNormally = player.isSneaking();

		if (!player.world.isRemote)
		{
			if (player.isPlayerSleeping() || !player.isEntityAlive())
			{
				event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
				return;
			}

			if (!player.world.provider.isSurfaceWorld())
			{
				event.setResult(EntityPlayer.SleepResult.NOT_POSSIBLE_HERE);
				return;
			}

			if (sleepNormally || sleepCharm) {
				if(!ForgeEventFactory.fireSleepingTimeCheck(player, pos)) {
					event.setResult(EntityPlayer.SleepResult.NOT_POSSIBLE_NOW);
					return;
				}
			}
			else if (!Somnia.enterSleepPeriod.isTimeWithin(24000)) {
				event.setResult(EntityPlayer.SleepResult.NOT_POSSIBLE_NOW);
				return;
			}

			if (!player.bedInRange(pos, enumfacing))
			{
				event.setResult(EntityPlayer.SleepResult.TOO_FAR_AWAY);
			}

			if (!SomniaUtil.checkFatigue(player)) {
				player.sendStatusMessage(new TextComponentTranslation("somnia.status.cooldown"), true);
				event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
				return;
			}

			if (!SomniaConfig.OPTIONS.sleepWithArmor && !player.capabilities.isCreativeMode && SomniaUtil.doesPlayHaveAnyArmor(player)) {
				player.sendStatusMessage(new TextComponentTranslation("somnia.status.armor"), true);
				event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
				return;
			}

			double d0 = 8.0D;
			double d1 = 5.0D;
			List<EntityMob> list = player.world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)pos.getX() - d0, (double)pos.getY() - d1, (double)pos.getZ() - d0, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d0), m -> m != null && m.isPreventingPlayerRest(player));

			if (!list.isEmpty() && !SomniaConfig.OPTIONS.ignoreMonsters && !player.capabilities.isCreativeMode)
			{
				event.setResult(EntityPlayer.SleepResult.NOT_SAFE);
				return;
			}
		}

		IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
		if (props != null) {
			if (sleepCharm) {
				props.setFatigue(props.getFatigue() - SomniaUtil.calculateFatigueToReplenish(player));
			}
			else if (sleepNormally || !CompatModule.checkBed(player, pos)) {
				props.setSleepNormally(true);
			}
		}

		if (player.isRiding() && !RailcraftPlugin.isBedCart(player.getRidingEntity()))
		{
			player.dismountRidingEntity();
		}

		player.spawnShoulderEntities();
		player.setSize(0.2F, 0.2F);

		if (enumfacing != null) {
			float f1 = 0.5F + (float)enumfacing.getXOffset() * 0.4F;
			float f = 0.5F + (float)enumfacing.getZOffset() * 0.4F;
			player.setRenderOffsetForSleep(enumfacing);
			player.setPosition(((float)pos.getX() + f1), ((float)pos.getY() + 0.6875F), ((float)pos.getZ() + f));
		}
		else
		{
			player.setPosition(((float)pos.getX() + 0.5F), ((float)pos.getY() + 0.6875F), ((float)pos.getZ() + 0.5F));
		}

		player.sleeping = true;
		player.sleepTimer = 0;
		player.bedLocation = pos;
		player.motionX = 0.0D;
		player.motionY = 0.0D;
		player.motionZ = 0.0D;

		if (!player.world.isRemote)
		{
			player.world.updateAllPlayersSleepingFlag();
		}

		Somnia.proxy.updateWakeTime(player);

		event.setResult(EntityPlayer.SleepResult.OK);
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
	public void interactHook(PlayerInteractEvent event) {
		World world = event.getWorld();

		BlockPos pos = event.getPos();

		EntityPlayer player = event.getEntityPlayer();

		if ((event instanceof PlayerInteractEvent.RightClickBlock && CompatModule.checkBed(player, pos)) || (event instanceof PlayerInteractEvent.EntityInteractSpecific && RailcraftPlugin.isBedCart(((PlayerInteractEvent.EntityInteractSpecific)event).getTarget()))) {
			if (player.bedInRange(pos, null)) //the facing can be null
			{
				ItemStack currentItem = player.inventory.getCurrentItem();
				ResourceLocation registryName = currentItem.getItem().getRegistryName();
				if (!currentItem.isEmpty() && registryName != null && registryName.toString().equals(SomniaConfig.OPTIONS.wakeTimeSelectItem)) {
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
	public void onLivingEntityUseItem(LivingEntityUseItemEvent.Finish event)
	{
		ItemStack stack = event.getItem();
		if (stack.getItemUseAction() == EnumAction.DRINK)
		{
			for (Triple<ItemStack, Double, Double> pair : SomniaAPI.getReplenishingItems())
			{
				if (OreDictionary.itemMatches(stack, pair.getLeft(), false))
				{
					EntityLivingBase entity = event.getEntityLiving();
					IFatigue props = entity.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
					if (props != null)
					{
						double fatigue = props.getFatigue();
						double replenishedFatigue = props.getReplenishedFatigue();
						double middle = pair.getMiddle();
						double fatigueToReplenish = Math.min(fatigue, middle);
						double newFatigue = replenishedFatigue + fatigueToReplenish;
						props.setReplenishedFatigue(newFatigue);

						double baseMultiplier = pair.getRight();
						double multiplier = newFatigue * 4 * SomniaConfig.FATIGUE.fatigueRate;
						props.setExtraFatigueRate(props.getExtraFatigueRate() + baseMultiplier * multiplier);
						props.setFatigue(fatigue - fatigueToReplenish);
						props.maxFatigueCounter();
					}
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

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		IFatigue props = event.getEntityLiving().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
		if (props != null) {
			props.setFatigue(0);
			props.setReplenishedFatigue(0);
			props.setExtraFatigueRate(0);
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
