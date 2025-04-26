package violetv0id.lantern;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

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
    private MinecraftServer integratedServer;


    // server
    private int port;
    private ServerSocket socket;




    public CustomServer(int port, int playerLimit)
    {
        this.port = port;
        this.maxPlayers = playerLimit;
    }

    public void start()
    {
        // server initialization
        // lan currently breaks the server :c
        /*
        LANTERN.Log_Dev("Opening to LAN..."); // this does a lot of the heavy-lifting for us
        try
        {
            if(LANTERN.currentServer != null)
            {
                // temporarily force creative until proper options are implemented. | side note - GameMode has to be all caps; e.g. "CREATIVE".
                LANTERN.currentServer.openToLan(GameMode.CREATIVE, true, port);
            }
            else
            {
                LANTERN.ChatClient("Current server is null! Try rejoining.");
            }
        }
        catch(Exception e)
        {
            LANTERN.ChatClient("Something went wrong. [Stage:LAN_SERVER]");
            e.printStackTrace();
        }
        */

        // starting server and handling new connections
        new Thread(() ->
        {
            try
            {
                socket = new ServerSocket(port, 5, InetAddress.getByName("0.0.0.0"));
                LANTERN.ChatClient("Server started!");
                LANTERN.Log("Server started on port " + port + ".");
                while(socket != null && !socket.isClosed())
                {
                    if(playerCount < maxPlayers)
                    {
                        Socket client = socket.accept();

                        // authenticate (aka 'aUtHenTiCaTe' because it's not really authentication lmao)
                        if(Auth(client))
                        {
                            SendImportant(client, "1000"); // acknowledgement
                            joinRequests++;
                            String username = "[USERNAME]"; // temporary
                            pendingConnections.put(client, username);
                            LANTERN.ChatClient(username + " has requested to join! [Type '/lantern requestlist' for more details.]");
                        }
                        else
                        {
                            SendImportant(client, "4003"); // forbidden
                            client.close();
                        }
                    }
                    else
                    {
                        Socket client = socket.accept();
                        SendImportant(client, "5001");
                        client.close();
                    }
                }
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
            for(Map.Entry<Socket, String> connection : connectedClients.entrySet())
            {
                if(connection.getKey().isClosed())
                {
                    connectedClients.remove(connection.getKey());
                    OnPlayerLeft(connection.getKey());
                }
            }
        }).start();


        // OnServerStarted() and OnServerStopped() logic
        new Thread(() ->
        {
            while(socket != null)
            {
                if(!socket.isClosed())
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
                    wait(100);
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

    public void stop()
    {
        try
        {
            if(socket != null)
            {
                socket.close();
                socket = null;
                LANTERN.Log("Server stopped.");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void OnPlayerJoined(Socket player, String username) // 'player' is a Socket, not a ServerPlayerEntity.
    {
        // create player

        // ...

        // integratedServer.getPlayerList().addPlayer(playerObject);

        // other
        playerCount++;
        Map.Entry<Socket, String> newPlayer = Map.entry(player, username);
        connectedClients.put(player, username);
        if(pendingConnections.containsKey(player))
            pendingConnections.remove(player);

        String ip = player.getInetAddress().toString();
        LANTERN.ChatClient(ip + " has connected.");
        LANTERN.Log(ip + " has connected.");
    }

    private void OnPlayerLeft(Socket player)
    {
        String ip = player.getInetAddress().toString();
        LANTERN.ChatClient(ip + " has disconnected.");
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
                        LANTERN.ChatClient("Allowing '" + ip + "''...");
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
            LANTERN.ChatClient("Allowing '" + ip + "''...");

            if(lastEntry != null)
                OnPlayerJoined(lastEntry.getKey(), lastEntry.getValue());
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
        Any
    }
}