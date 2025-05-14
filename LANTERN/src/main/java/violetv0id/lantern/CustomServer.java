package violetv0id.lantern;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.IIOException;

import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.mojang.authlib.GameProfile;
import violetv0id.lantern.LanternClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.GameMode;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

import net.minecraft.text.Text;




public class CustomServer
{
    // internal
    public boolean isRunning = false;

    public int maxPlayers = 8;
    public int joinRequests = 0;
    public int playerCount = 0;
    private Map<Socket, String> connectedClients = new LinkedHashMap<>();
    public Map<Socket, String> pendingConnections = new LinkedHashMap<>();
    private List<Socket> ignoreList = new ArrayList<>();


    // minecraft
    private MinecraftServer integratedServer = null;
    private PlayerManager serverPlayerManager = null;
    // private io.netty.channel.Channel integratedServerChannel = null;


    // server
    private int port = 0;
    private ServerSocket server_Socket = null;




    public CustomServer(int port, int playerLimit)
    {
        this.port = port;
        this.maxPlayers = playerLimit;
    }

    public void start()
    {
        // server initialization
        integratedServer = LANTERN.currentServer;
        // LANTERN.localClient.getNetworkHandler().getConnection();

        /*
        ClientConnection client_connection_ = LANTERN.localClient.networkHandler.getConnection();
        integratedServerChannel = client_connection_.getChannel();
        */

        
        // ...then open to LAN.
        LANTERN.Log_Dev("Opening to LAN..."); // this does a lot of the heavy-lifting for us
        try
        {
            if(integratedServer != null)
            {
                serverPlayerManager = integratedServer.getPlayerManager();

                // temporarily force creative until proper options are implemented. | side note - GameMode has to be all caps; e.g. "SURVIVAL".
                integratedServer.openToLan(GameMode.CREATIVE, true, port + 1); // port + 1 so it doesn't mess with the LANTERN server and the LAN server. [yes, they're different.]
            }
            else
            {
                // should be impossible buuuuuuut
                LANTERN.ChatClient("Current server is null! Try rejoining.");
            }
        }
        catch(Exception e)
        {
            LANTERN.ChatClient("Something went wrong. [Stage:LAN_SERVER]");
            e.printStackTrace();
            return;
        }

        // starting server and handling new connections
        new Thread(() ->
        {
            try
            {
                server_Socket = new ServerSocket(port, 5, InetAddress.getByName("0.0.0.0"));
                isRunning = !server_Socket.isClosed();
                LANTERN.ChatClient("Server started!");
                LANTERN.Log("Server started on port " + port + ".");
                while(server_Socket != null && !server_Socket.isClosed())
                {
                    if(playerCount < maxPlayers)
                    {
                        Socket client = server_Socket.accept();

                        // authenticate (aka 'aUtHeNtIcAtE' because it's not really authentication lmao)
                        if(Auth(client))
                        {
                            HandleConnection(client);
                        }
                        else
                        {
                            SendImportant(client, "4003"); // forbidden
                            client.close();
                        }
                    }
                    else
                    {
                        Socket client = server_Socket.accept();
                        SendImportant(client, "5001");
                        client.close();
                    }
                }
                LANTERN.ChatClient("Server closed... [ie3]");
            }
            catch(IOException e)
            {
                isRunning = false;
                if(e.getMessage().toString() == "Socket closed") // the server is already closed, so no reason to do anything.--this also happens when you close the server for some reason.
                    return;

                try
                {
                    LANTERN.ChatClient("Something went wrong. Closing server... [Stage:SERVER_INIT]");
                    LANTERN.Log("An error occurred. Attempting to close server...");
                    stop();
                }
                finally
                {
                    e.printStackTrace();
                }
            }
        }).start();


        // players leaving
        new Thread(() ->
        {
            while(server_Socket == null)
            {
                try
                {
                    Thread.sleep(100);
                    if(server_Socket == null)
                        LANTERN.ChatClient("Waiting for Server Socket to initialize...");
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            while(!server_Socket.isClosed())
            {
                for(Map.Entry<Socket, String> connection : connectedClients.entrySet())
                {
                    if(connection.getKey().isClosed())
                    {
                        connectedClients.remove(connection.getKey());
                        OnPlayerLeft(connection.getKey());
                    }
                }

                try
                {
                    Thread.sleep(100);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();


        // OnServerStarted() and OnServerStopped() logic
        new Thread(() ->
        {
            while(server_Socket != null)
            {
                if(!server_Socket.isClosed())
                {
                    if(!isRunning)
                    {
                        isRunning = true;
                        OnServerStarted();
                    }
                }
                else
                {
                    if(isRunning)
                    {
                        isRunning = false;
                        OnServerStopped();
                    }
                }
                try
                {
                    Thread.sleep(100);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean Auth(Socket client)
    {
        for(Socket c : ignoreList)
        {
            if(c.getInetAddress().toString().equals(client.getInetAddress().toString()))
            {
                LANTERN.Log("Ignored client '" + client.getInetAddress().toString() + "' tried to connect. [REJECTED]");
                return false;
            }
        }
        return true;
    }

    private void HandleConnection(Socket con)
    {
        new Thread(() ->
        {
            String response = Listen(con);
            LANTERN.ChatClient("Client responded : " + response);
            SendImportant(con, "1000"); // acknowledgement
    
            joinRequests++;
            pendingConnections.put(con, response);
            LANTERN.ChatClient(response + " has requested to join!");
        }).start();
    }

    public void stop()
    {
        try
        {
            if(server_Socket != null)
            {
                server_Socket.close();
                server_Socket = null;
                LANTERN.Log("Server stopped.");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void OnPlayerJoined(Socket playerSocket, String username)
    {
        new Thread(() ->
        {
            try
            {
                LANTERN.Log_Dev("Creating UUID from " + username + "...");
                UUID playerUUID = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));
    
                LANTERN.Log_Dev("Creating SPE from connection...");
                ServerPlayerEntity playerEntity = new ServerPlayerEntity(
                    integratedServer, 
                    integratedServer.getOverworld(), 
                    new GameProfile(playerUUID, username)
                );
        
                // this section has issues with the socket somehow being "closed".
                LANTERN.Log_Dev("Creating LCC from SPE...");
                if(playerSocket.isClosed())
                {
                    while(playerSocket.isClosed())
                    {
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        if(playerSocket.isClosed())
                        {
                            LANTERN.Log_Dev("OnPlayerJoined() is paused by playerSocket being closed!");
                        }
                    }
                }
                LanternClientConnection lanternClientConnection = new LanternClientConnection(playerSocket);
                LANTERN.Log_Dev("Registering connect...");
                serverPlayerManager.onPlayerConnect(lanternClientConnection, playerEntity);
    
                
                LANTERN.Log_Dev("Generating GJS2C packet...");
                GameJoinS2CPacket joinPacket = new GameJoinS2CPacket(
                    playerEntity.getId(), 
                    integratedServer.isHardcore(), 
                    playerEntity.interactionManager.getGameMode(), 
                    integratedServer.getDefaultGameMode(), 
                    integratedServer.getWorldRegistryKeys(), // Set of dimension keys
                    integratedServer.getRegistryManager(), 
                    RegistryKey.of(RegistryKeys.DIMENSION_TYPE, integratedServer.getOverworld().getDimensionKey().getValue()), // Dimension type (Overworld)
                    integratedServer.getOverworld().getRegistryKey(), 
                    integratedServer.getOverworld().getSeed(), 
                    serverPlayerManager.getMaxPlayerCount(), 
                    10, 
                    10, 
                    false, 
                    true, 
                    false, 
                    integratedServer.getOverworld().isFlat(), 
                    Optional.empty(), 
                    maxPlayers
                );
                LANTERN.Log_Dev("Sending packet...");
                lanternClientConnection.send(joinPacket);
                LANTERN.Log_Dev("Packet sent!");
    
        
                LANTERN.Log_Dev("Finalizing connection...");
                connectedClients.put(playerSocket, username);
                pendingConnections.remove(playerSocket);
    
                String ip = playerSocket.getInetAddress().toString();
                LANTERN.Log(ip + " has connected.");
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }).start();
    }

    private void OnPlayerLeft(Socket player)
    {
        String ip = player.getInetAddress().toString();
        LANTERN.Log(ip + " has disconnected.");
    }

    private void OnServerStarted()
    {
        // do le other things
        if(LANTERN.devMode)
        {
            LANTERN.ChatClient("Server initialized and running on your network on " + port);
        }
        else
        {
            LANTERN.ChatClient("Server started! ");
        }
    }

    private void OnServerStopped()
    {
        LANTERN.ChatClient("Server closed.");
        playerCount = 0;
        connectedClients.clear();
        pendingConnections.clear();
    }

    private String Listen(Socket client)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            return reader.readLine();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void SendImportant(Socket client, String message)
    {
        new Thread(() ->
        {
            try
            {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                out.println(message);
                out.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }).start();
    }

    public void AcceptAll()
    {
        LANTERN.ChatClient("Accepting all requests... [" + pendingConnections.size() + "]");
        for(Map.Entry<Socket, String> pending : pendingConnections.entrySet())
        {
            OnPlayerJoined(pending.getKey(), pending.getValue());
        }
    }

    public void AcceptConnection(String ip)
    {
        if(ip != null && ip != "")
        {
            for(Map.Entry<Socket, String> pending : pendingConnections.entrySet())
            {
                try
                {
                    if(pending.getKey().getInetAddress().equals(InetAddress.getByName(ip)))
                    {
                        LANTERN.ChatClient("Allowing '" + pending.getKey().getInetAddress() + "''...");
                        OnPlayerJoined(pending.getKey(), pending.getValue());
                    }
                    LANTERN.ChatClient("Couldn't find " + ip + " in pending connections!");
                }
                catch(UnknownHostException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            LANTERN.ChatClient("Fetching latest...");
            // cycle until it reaches the newest
            Map.Entry<Socket, String> lastEntry = null;
            for(Map.Entry<Socket, String> entry : pendingConnections.entrySet())
            {
                lastEntry = entry;
            }
            LANTERN.ChatClient("Allowing '" + lastEntry.getValue() + "''...");

            if(lastEntry != null)
            {
                OnPlayerJoined(lastEntry.getKey(), lastEntry.getValue());
                pendingConnections.remove(lastEntry);
            }
        }
    }

    public void RejectEvery()
    {
        for(Map.Entry<Socket, String> c : pendingConnections.entrySet())
        {
            RejectConnection(c.getKey().getInetAddress().toString());
            try
            {
                wait(10);
            }
            catch(InterruptedException interrupted)
            {
                interrupted.printStackTrace();
            }
        }
    }

    public void RejectConnection(String ip)
    {
        Map.Entry<Socket, String> p = GetInternalPlayerByIp(ip, PlayerSearchMode.Pending);
        if(p != null)
        {
            try
            {
                Socket p_c = p.getKey();
                LANTERN.ChatClient("Rejected " + p.getValue() + " and added them to the ignore list.");
                ignoreList.add(p_c);
                SendImportant(p_c, "5003");
                p_c.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Socket GetClientByIp(String ip, playerSearchMode mode)
    {
        // check active connections first
        for(Map.Entry<Socket, String> c : connectedClients.entrySet())
        {
            if(c.getKey().getInetAddress().toString().equals(ip))
            {
                return c.getKey();
            }
        }
        
        // then pending connections
        for(Map.Entry<Socket, String> c : pendingConnections.entrySet())
        {
            if(c.getKey().getInetAddress().toString().equals(ip))
            {
                return c.getKey();
            }
        }

        // and if none are found, return null.
        return null;
    }

    private Map.Entry<Socket, String> GetInternalPlayerByIp(String playerIp, playerSearchMode mode)
    {
        Socket c = GetClientByIp(playerIp, mode);
        if(c != null)
        {
            if(mode.equals(PlayerSearchMode.Connected) || mode.equals(PlayerSearchMode.Any))
            {
                for(Map.Entry<Socket, String> map : connectedClients.entrySet())
                {
                    if(map.getKey().equals(c))
                    {
                        return map;
                    }
                }
            }
            if(mode.equals(PlayerSearchMode.Pending) || mode.equals(PlayerSearchMode.Any))
            {
                for(Map.Entry<Socket, String> map : pendingConnections.entrySet())
                {
                    if(map.getKey().equals(c))
                    {
                        return map;
                    }
                }
            }
            return null;
        }
        return null;
    }

    private Map.Entry<Socket, String> GetInternalPlayerBySocket(Socket playerSocket, playerSearchMode mode)
    {
        if(playerSocket != null)
        {
            if(mode.equals(PlayerSearchMode.Connected) || mode.equals(PlayerSearchMode.Any))
            {
                for(Map.Entry<Socket, String> map : connectedClients.entrySet())
                {
                    if(map.getKey().equals(playerSocket))
                    {
                        return map;
                    }
                }
            }
            if(mode.equals(PlayerSearchMode.Pending) || mode.equals(PlayerSearchMode.Any))
            {
                for(Map.Entry<Socket, String> map : pendingConnections.entrySet())
                {
                    if(map.getKey().equals(playerSocket))
                    {
                        return map;
                    }
                }
            }
            return null;
        }
        return null;
    }

    private static playerSearchMode PlayerSearchMode;

    private static enum playerSearchMode
    {
        Connected,
        Pending,
        Admin,
        Any
    }
}