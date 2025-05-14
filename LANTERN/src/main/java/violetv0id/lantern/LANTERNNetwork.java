package violetv0id.lantern;

import net.minecraft.text.Text;

import java.beans.EventHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;




public class LANTERNNetwork
{
    // during runtime
    private static boolean hosting = false;

    // server
    private static CustomServer server;
    private static CustomClient client;

    private static int joinRequests = 0;
    private static int port = 49200;


    public static EventHandler handler;




    public static void onInitialize()
    {
        new Thread(() ->
        {
            Object lock = new Object();
            while(true)
            {
                // executes every 100 miliseconds or 0.1 seconds
                hosting = (server != null);
                joinRequests = server.joinRequests;
                try
                {
                    lock.wait(100);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
        {
            disconnect();
        });
    }

    public static void startServer(int playerLimit)
    {
        if(server != null || client != null)
        {
            LANTERN.ChatClient("You are already in a server! Please disconnect from the current server first.");
        }

        Object lock = new Object();
        if(server == null)
        {
            LANTERN.ChatClient("Starting LANTERN server on port " + port + "...");
            server = new CustomServer(port, playerLimit);
            server.start();

            int attempts = 0;
            while(!server.isRunning)
            {
                if(attempts < 19)
                {
                    attempts++;
                    try
                    {
                        lock.wait(100);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    LANTERN.ChatClient("Server failed to start. Did you set up a Port-Forwarding-Rule for '" + port + "'?");
                    LANTERN.Log("Server failed to start...");
                    return;
                }
            }
            LANTERN.Log("Server started!");
            LANTERN.ChatClient("Server started!");
        }
        else
        {
            LANTERN.Log("A server is already running! Please close your active server first.");
        }
    }

    public static void disconnect()
    {
        LANTERN.Log("Disconnecting...");
        int the = 0;
        if(client != null)
        {
            LANTERN.Log("Closing client...");
            client.disconnect();
            client = null;
            LANTERN.Log("Closed client...");
            the = 1;
        }
        if(server != null)
        {
            LANTERN.Log("Closing server...");
            server.stop();
            server = null;
            LANTERN.Log("Closed server...");
            
            if(the == 1)
                the = 3;
            else
                the = 2;
        }

        if(the == 1)
        {
            LANTERN.ChatClient("Disconnected from server!");
        }
        else if(the == 2)
        {
            LANTERN.ChatClient("Closed server!");
        }
        else if(the == 3)
        {
            // im pretty sure this is impossible. running off of -3 sleep so too lazy to check.
            LANTERN.ChatClient("Closed server and client!");
        }
    }

    public static void LogStatus()
    {
        String connectionType = "a client";
        if(server != null)
            connectionType = "the host";
        LANTERN.ChatClient("You are " + connectionType + ".");

        String LanternServerStatus = "null"; String LanternClientStatus = "null";
        if(server != null)
            LanternServerStatus = "not null";
        if(client != null)
            LanternClientStatus = "not null";
        LANTERN.ChatClient("[LanternServer is " + LanternServerStatus + "] [LanternClient is " + LanternClientStatus + "]");

        LANTERN.ChatClient("\nThere are " + LANTERN.currentServer.getCurrentPlayerCount() + " players connected.");
    }

    public static void connectTo(String target)
    {
        if(server != null || client != null)
        {
            LANTERN.ChatClient("You are already in a server! Please disconnect from the current server first.");
        }
        
        Object lock = new Object();
        if(client == null)
        {
            client = new CustomClient(target, port);
            client.connect();

            /*
            int attempts = 0;
            while(attempts < 11)
            {
                if(attempts == 200)
                {
                    LANTERN.Log("Connection timed out.");
                }
                
                try
                {
                    lock.wait(100); // 10 attempts is one second
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }

                if(client.connected)
                    break;
                attempts++;
            }
            */
        }
    }

    public static void showRequests()
    {
        if(IsActiveAndRunning(server))
        {
            if(server.pendingConnections.size() > 0)
            {
                LANTERN.ChatClient("# Active Join Requests [AJR's]\n"); // eventually implement timeout system and replace with "These time out after 60 seconds."
                for(Map.Entry<Socket, String> request : server.pendingConnections.entrySet())
                {
                    LANTERN.ChatClient(request.getValue() + " | " + request.getKey().getInetAddress().toString());
                }
                LANTERN.ChatClient("\n");
                LANTERN.ChatClient("# ===============");
            }
        }
    }

    public static void acceptLatest()
    {
        if(IsActiveAndRunning(server))
        {
            server.AcceptConnection(null);
        }
    }

    public static void acceptSpecific(String ip)
    {
        if(IsActiveAndRunning(server))
        {
            server.AcceptConnection(ip);
        }
    }

    public static void acceptAll()
    {
        if(IsActiveAndRunning(server))
        {
            server.AcceptAll();
        }
    }

    public static void reject(String ip)
    {
        if(IsActiveAndRunning(server))
        {
            server.RejectConnection(ip);
        }
    }
    
    public static void rejectAll()
    {
        if(IsActiveAndRunning(server))
        {
            server.RejectEvery();
        }
    }

    private static boolean IsActiveAndRunning(CustomServer server)
    {
        if(server != null)
        {
            LANTERN.Log_Dev("Server running: " + server.isRunning);
            return server.isRunning;
        }
        LANTERN.Log_Dev("LANTERNNetwork : server is null!");
        return false;
    }
}