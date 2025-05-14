package violetv0id.lantern;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import io.netty.buffer.Unpooled;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

public class CustomClient
{
    public boolean connected = false;

    private String host;
    private int port;
    private Socket serverSocket;

    public CustomClient(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public void connect()
    {
        LANTERN.ChatClient("[~]");

        new Thread(() ->
        {
            try
            {
                LANTERN.ChatClient("Sent request to join!");
                serverSocket = new Socket(host, port);
                if(serverSocket.isConnected() && !connected)
                {
                    connected = true;
                    OnConnected();
                }
            }
            catch(IOException e)
            {
                connected = false;
                LANTERN.Log("Failed to connect to server...");
                e.printStackTrace();
            }
        }).start();
    }

    public void disconnect()
    {
        try
        {
            if(serverSocket != null && !serverSocket.isClosed())
            {
                connected = false;
                serverSocket.close();
                LANTERN.ChatClient("Disconnected...");
                LANTERN.Log("Disconnected from server.");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void OnConnected()
    {
        LANTERN.ChatClient("Connecting to server... [" + host + " : " + port + "]");

        LANTERN.Log("Internal connection established.");
        LANTERN.Log("Authenticating...");
        try
        {
            // send username to server
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            writer.println(LANTERN.localClientUsername);

            // wait for response, hopefully 1000--acknowledged.
            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            String response = in.readLine();
            LANTERN.ChatClient("Recieved from server : " + response);

            // wait for second response.
            String response2 = in.readLine();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // # old
        // String localPlayerName = Utils.GetPlayerName();
        // serverSocket.sendUrgentData(localPlayerName + " | sjr_");
        // # ===============
    }
}