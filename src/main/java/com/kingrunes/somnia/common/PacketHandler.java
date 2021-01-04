package com.kingrunes.somnia.common;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.api.capability.IFatigue;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketHandler
{
	/*
	 * Handling
	 */
	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event)
	{
		if (event.getPacket().channel().equals(Somnia.MOD_ID))
			onPacket(event.getPacket(), ((NetHandlerPlayServer)event.getHandler()).player);
	}
	
	@SubscribeEvent
	public void onClientPacket(ClientCustomPacketEvent event)
	{
		if (event.getPacket().channel().equals(Somnia.MOD_ID))
			onPacket(event.getPacket(), null);
	}
	
	public void onPacket(FMLProxyPacket packet, EntityPlayerMP player)
	{
		DataInputStream in = new DataInputStream(new ByteBufInputStream(packet.payload()));
		try
		{
			byte id = in.readByte();
			
			switch (id)
			{
			case 0x00:
				handleGUIOpenPacket();
				break;
			case 0x01:
				handleWakePacket(player, in);
				break;
			case 0x02:
				handlePropUpdatePacket(in, player);
				break;
			case 0x03:
				handleRightClickBlockPacket(player, in);
				break;
			case 0x04:
				handleRideEntityPacket(player, in);
				break;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// CLIENT
	private void handleGUIOpenPacket() {
		Somnia.proxy.handleGUIOpenPacket();
	}
	
	private void handlePropUpdatePacket(DataInputStream in, @Nullable EntityPlayerMP player) throws IOException
	{
		if (player == null || player.world.isRemote) {
			Somnia.proxy.handlePropUpdatePacket(in);
			return;
		}

		byte target = in.readByte();
		IFatigue props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);

		if (target == 0x01 && props != null) {
			int b = in.readInt();
			for (int a=0; a<b; a++)
			{
				int val = in.readByte();
				if (val == 0x01) {
					props.setResetSpawn(in.readBoolean());
				}
			}
		}
	}
	
	
	private void handleWakePacket(EntityPlayerMP player, DataInputStream in)
	{
		Somnia.proxy.handleWakePacket(player);
	}

	private void handleRightClickBlockPacket(EntityPlayerMP player, DataInputStream in) throws IOException {
		BlockPos pos = new BlockPos(in.readInt(), in.readInt(), in.readInt());
		EnumFacing facing = EnumFacing.values()[in.readByte()];
		IBlockState state = player.world.getBlockState(pos);

		state.getBlock().onBlockActivated(player.world, pos, state, player, EnumHand.MAIN_HAND, facing, in.readFloat(), in.readFloat(), in.readFloat());
	}

	private void handleRideEntityPacket(EntityPlayerMP player, DataInputStream in) throws IOException {
		Entity entity = player.world.getEntityByID(in.readInt());
		if (entity == null) return;

		player.startRiding(entity, true);
	}

	public static FMLProxyPacket buildGUIOpenPacket() {
		return doBuildGUIOpenPacket();
	}

	private static FMLProxyPacket doBuildGUIOpenPacket()
	{
		PacketBuffer buffer = unpooled();
		
    	buffer.writeByte(0x00);
    	return new FMLProxyPacket(buffer, Somnia.MOD_ID);
	}
	
	public static FMLProxyPacket buildWakePacket()
	{
		return doBuildWakePacket();
	}

	public static FMLProxyPacket doBuildWakePacket()
	{
		PacketBuffer buffer = unpooled();
		
    	buffer.writeByte(0x01);
    	return new FMLProxyPacket(buffer, Somnia.MOD_ID);
	}
	
	public static FMLProxyPacket buildPropUpdatePacket(int target, Object... fields)
	{
		PacketBuffer buffer = unpooled();
		
    	buffer.writeByte(0x02);
    	buffer.writeByte(target);
    	buffer.writeInt(fields.length/2);
    	for (int i=0; i<fields.length; i++)
    	{
    		buffer.writeByte((Integer) fields[i]);
    		StreamUtils.writeObject(fields[++i], buffer);
    	}
    	
    	return new FMLProxyPacket(buffer, Somnia.MOD_ID);
	}

	public static FMLProxyPacket buildRightClickBlockPacket(BlockPos pos, EnumFacing side, float x, float y, float z)
	{
		PacketBuffer buffer = unpooled();

		buffer.writeByte(0x03);
		buffer.writeInt(pos.getX());
		buffer.writeInt(pos.getY());
		buffer.writeInt(pos.getZ());
		buffer.writeByte(side.ordinal());
		buffer.writeFloat(x);
		buffer.writeFloat(y);
		buffer.writeFloat(z);
		return new FMLProxyPacket(buffer, Somnia.MOD_ID);
	}

	public static FMLProxyPacket buildRideEntityPacket(Entity entity) {
		PacketBuffer buffer = unpooled();

		buffer.writeByte(0x04);
		buffer.writeInt(entity.getEntityId());

		return new FMLProxyPacket(buffer, Somnia.MOD_ID);
	}
	
	/*
	 * Utils
	 */
	private static PacketBuffer unpooled()
	{
		return new PacketBuffer(Unpooled.buffer());
	}
}