package main.java.violetv0id.lantern;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Utils
{
    public String GetPlayerName(ServerPlayerEntity player)
    {
        return player.getName().getString();
    }

    public void SendChatToPlayer(ServerPlayerEntity player, String message)
    {
        player.sendMessage(Text.literal(message), false); // false = chat, true = action bar
    }
}