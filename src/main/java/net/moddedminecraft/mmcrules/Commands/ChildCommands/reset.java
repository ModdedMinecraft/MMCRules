package net.moddedminecraft.mmcrules.Commands.ChildCommands;

import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class reset implements CommandExecutor {

    private final Main plugin;
    public reset(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<ServerPlayer> playerParameter = Parameter.player().key("player").build();

        final ServerPlayer userString = context.requireOne(playerParameter);
        Optional<User> userOp;
        try {
            userOp = Sponge.server().userManager().load(userString.uniqueId()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        if (userOp.isPresent()) {
            User player = userOp.get();
            if (!plugin.getDataStore().getAccepted().contains(player.uniqueId().toString())) {
                throw new CommandException(plugin.fromLegacy(Config.chatPrefix + "User with the name " + player.name() + " either has not been found or hasn't accepted the rules yet"));
            }
            plugin.getDataStore().removePlayer(player.uniqueId().toString());
            plugin.getUsersWhoReadRules().remove(player.name());
            context.cause().audience().sendMessage(plugin.fromLegacy(Config.chatPrefix + "Player " + player.name() + " has to re-accept the rules."));
            return CommandResult.success();
        } else {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + "Unable to find player."));
        }
    }
}
