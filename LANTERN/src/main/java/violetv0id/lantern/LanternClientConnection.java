package violetv0id.lantern;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class LanternClientConnection extends ClientConnection
{
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public LanternClientConnection(Socket c_socket) throws IOException
    {
        super(NetworkSide.SERVERBOUND);
        if(c_socket == null || c_socket.isClosed())
        {
            throw new IOException("[SERVER] : new LCC's socket is null or closed!");
        }
        this.socket = c_socket;
        this.in = new DataInputStream(c_socket.getInputStream());
        this.out = new DataOutputStream(c_socket.getOutputStream());
    }

    @Override
    public void send(Packet<?> packet)
    {
        try
        {
            PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
            packet.write(buf);

            int packetLength = buf.readableBytes();
            out.writeInt(packetLength);

            out.write(buf.array(), 0, packetLength);
            out.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            this.disconnect();
        }
    }

    @Override
    public void tick()
    {
        try
        {
            if(socket.isClosed() || !socket.isConnected())
            {
                this.disconnect();
                return;
            }
            // eventually add reading for incoming packets
        }
        catch(Exception e)
        {
            e.printStackTrace();
            this.disconnect();
        }
    }

    public void disconnect()
    {
        try
        {
            if(!socket.isClosed())
            {
                socket.close();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}