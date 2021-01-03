package com.kingrunes.somnia.setup;

import net.minecraft.entity.player.EntityPlayerMP;

import java.io.DataInputStream;
import java.io.IOException;

public interface IProxy {
    void register();

    void handleGUIOpenPacket();

    void handlePropUpdatePacket(DataInputStream in) throws IOException;

    void handleWakePacket(EntityPlayerMP player);
}
