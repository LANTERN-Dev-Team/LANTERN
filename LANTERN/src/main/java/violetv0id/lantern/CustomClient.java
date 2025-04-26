package violetv0id.lantern;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
            // wait for response
            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            String response = in.readLine();
            LANTERN.ChatClient("Recieved from server : " + response);
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

    private void SendToServer(String mesasge)
    {
        LANTERN.Log("'SendToServer(String)' is not yet implimented.");
    }
}