package client.java.violetv0id.lantern;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class LanternClient implements ClientModInitializer
{
    public static final Identifier ID = new Identifier("lantern", "string_packet");

    @Override
    public void onInitializeClient()
    {
        MinecraftClient.getInstance().execute(() ->
        {
            try
            {
                Thread.sleep(5000); // give the server-side time to start
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
    
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            if(player != null)
            {
                sendToServer(player.getName().getString());
            }
        });
    }

    public static void sendToServer(String message)
    {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(message);
        ClientPlayNetworking.send(ID, buf);
    }
}