package net.moddedminecraft.mmcrules.Commands.ChildCommands;

import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class acceptFor implements CommandExecutor {

    private final Main plugin;
    public acceptFor(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<ServerPlayer> playerParameter = Parameter.player().key("player").build();
        ServerPlayer player = context.requireOne(playerParameter);

        if (player == null) {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + "Player by that name not found. (are they online?)"));
        }
        if (plugin.getDataStore().getAccepted().contains(player.uniqueId().toString())) {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + player.name()+ " has already accepted the rules!"));
        }
        Sponge.server().commandManager().process(player, "acceptrules");
        context.cause().audience().sendMessage(plugin.fromLegacy(Config.chatPrefix + "Performed /acceptrules on " + player.name()));
        return CommandResult.success();
    }
}
