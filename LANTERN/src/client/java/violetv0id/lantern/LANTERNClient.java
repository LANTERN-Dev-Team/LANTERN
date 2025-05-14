package client.java.violetv0id.lantern;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.network.PacketByteBuf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import net.minecraft.util.Identifier;

public class LanternClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        violetv0id.lantern.common.ClientUsername_Shared.username = MinecraftClient.getInstance().getSession().getUsername();
    }
}