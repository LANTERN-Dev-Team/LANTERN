package violetv0id.lantern;

// fabric
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

// java server / exceptions
import java.io.IOException;
import java.net.ServerSocket;

// debugging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// commands
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

// world
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

// misc
import net.minecraft.server.network.ServerPlayerEntity;




public class LANTERN implements ModInitializer
{
    // internal
	public static final String MOD_ID = "lantern";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final boolean devMode = true;


    // server
    public static ServerPlayerEntity localClient = null;
    public static MinecraftServer currentServer = null;


    // commands
    private final CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher();




	@Override
	public void onInitialize()
    {
		Log("Initializing...");

		Log("Registering commands...");
        RegisterCommands();
		Log("Commands registered!");

        // do ui
        

		Log("LANTERN initialized!");

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            Log("Ready to host world!");
            currentServer = server;
        });
	}

    private void RegisterCommands()
    {
        CommandRegistrationCallback.EVENT.register(LANTERNCommand::register);
    }

    public static void ChatClient(String message)
    {
        ServerPlayerEntity player = currentServer.getPlayerManager().getPlayerList().stream().findFirst().orElse(null);
        if(player != null)
        {
            player.sendMessage(Text.literal(message), false);
        }
        else
        {
            Log("LOCAL CLIENT IS NULL.");
        }
    }

    public static void Log(String message)
    {
        System.out.println("[LANTERN] : " + message);
    }

    public static void Log_Dev(String message)
    {
        if(devMode)
            System.out.println("[LANTERN - DEBUG] : " + message);
    }
}