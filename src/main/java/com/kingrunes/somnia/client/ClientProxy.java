package com.kingrunes.somnia.client;

import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static double playerFatigue = -1;
	public static final ClientTickHandler clientTickHandler = new ClientTickHandler();
	
	@Override
	public void register()
	{
		super.register();
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(clientTickHandler);
	}
	
	@Override
	public void handleGUIOpenPacket() {
		final Minecraft mc = Minecraft.getMinecraft();
		mc.addScheduledTask(() -> mc.displayGuiScreen(new GuiSelectWakeTime()));
	}

	@Override
	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{
		byte target = in.readByte();
		
		switch (target)
		{
		case 0x00:
			if (Minecraft.getMinecraft().player.isPlayerSleeping())
			{
				int b = in.readInt();
				for (int a=0; a<b; a++)
					clientTickHandler.readField(in);
			}
			break;
		case 0x01:
			int b = in.readInt();
			for (int a=0; a<b; a++)
			{
				if (in.readByte() == 0x00) {
					playerFatigue = in.readDouble();
				}
			}
			break;
		}
	}

	@Override
	public void handleWakePacket(EntityPlayerMP player)
	{
		player.wakeUpPlayer(true, true, true);
	}
}