package com.kingrunes.somnia.setup;

import net.minecraft.entity.player.EntityPlayerMP;

import java.io.DataInputStream;

public class ServerProxy implements IProxy
{
	@Override
	public void register() {}

	@Override
	public void handleGUIOpenPacket() {}

	@Override
	public void handlePropUpdatePacket(DataInputStream in) {}

	@Override
	public void handleWakePacket(EntityPlayerMP player)
	{
		player.wakeUpPlayer(true, true, true);
	}
}