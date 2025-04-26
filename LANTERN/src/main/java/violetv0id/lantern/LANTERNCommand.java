package violetv0id.lantern;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import static net.minecraft.server.command.CommandManager.argument;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.text.Text;

public class LANTERNCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment)
    {
        dispatcher.register(CommandManager.literal("lantern")
            .then(CommandManager.literal("host")
                .then(argument("maxPlayers", IntegerArgumentType.integer(1, 16))
                    .executes(LANTERNCommand::hostCommand)))

            .then(CommandManager.literal("disconnect")
                .executes(LANTERNCommand::disconnectCommand))

            .then(CommandManager.literal("join")
                .then(argument("ipToJoin", StringArgumentType.string())
                    .executes(LANTERNCommand::joinCommand)))
            .then(CommandManager.literal("requestlist")
                .executes(LANTERNCommand::requestList))
            .then(CommandManager.literal("accept")
                .then(argument("ipToAccept", StringArgumentType.string())
                    .executes(LANTERNCommand::acceptSpecific))
                .executes(LANTERNCommand::acceptLatest))
            .then(CommandManager.literal("acceptall")
                .executes(LANTERNCommand::acceptAll))
            .then(CommandManager.literal("reject"))
                .then(argument("ipToReject", StringArgumentType.string()))
                    .executes(LANTERNCommand::rejectRequest));
    }

    private static int hostCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        int playerLimit = IntegerArgumentType.getInteger(context, "maxPlayers");
        LANTERNNetwork.startServer(playerLimit);
        return 1;
    }

    private static int joinCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        String ip = StringArgumentType.getString(context, "ipToJoin");
        LANTERN.ChatClient("Joining " + ip + "...");
        LANTERNNetwork.connectTo(ip);
        return 1;
    }

    private static int disconnectCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        LANTERN.ChatClient("Disconnecting...");
        LANTERNNetwork.disconnect();
        return 1;
    }

    private static int requestList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        LANTERNNetwork.showRequests();
        return 1;
    }

    private static int acceptLatest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        LANTERNNetwork.acceptLatest();
        return 1;
    }

    private static int acceptSpecific(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        String ipToAccept = StringArgumentType.getString(context, "ipToAccept");
        LANTERNNetwork.acceptSpecific(ipToAccept);
        return 1;
    }

    private static int acceptAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        LANTERNNetwork.acceptAll();
        return 1;
    }

    private static int rejectRequest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        String ipToReject = StringArgumentType.getString(context, "ipToReject");
        LANTERNNetwork.reject(ipToReject);
        return 1;
    }
}