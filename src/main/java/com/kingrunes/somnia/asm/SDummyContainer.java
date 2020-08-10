package com.kingrunes.somnia.asm;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.kingrunes.somnia.SomniaVersion;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SDummyContainer extends DummyModContainer
{
	private static final String FORMAT = "%s.%s.%s";
	// Incremented when a significant change to the mod is made, never reset
	public static final int CORE_MAJOR_VERSION = 2,
	// Incremented for new features or significant rewrites, reset with every new MAJOR_VERSION
							CORE_MINOR_VERSION = 0,
	// Incremented when a bugfix is made and the mod can be considered 'stable', reset with every new MINOR_VERSION
							CORE_REVISION_VERSION = 0;
	// Incremented automatically by the build system, never reset

	public SDummyContainer()
	{
		super(new ModMetadata());
		ModMetadata meta = super.getMetadata();
		meta.modId = "somniacore";
		meta.name = "SomniaCore";
		meta.version = getCoreVersionString();
		meta.authorList = Lists.newArrayList("Kingrunes", "Su5eD");
		meta.description = "This mod modifies Minecraft to allow Somnia to hook in";
		meta.screenshots = new String[0];
	}

	public static String getCoreVersionString()
	{
		return String.format(FORMAT, CORE_MAJOR_VERSION, CORE_MINOR_VERSION, CORE_REVISION_VERSION);
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}
}