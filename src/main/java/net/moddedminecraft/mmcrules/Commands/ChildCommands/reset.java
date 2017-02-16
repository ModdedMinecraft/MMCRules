package net.moddedminecraft.mmcrules.Commands.ChildCommands;

import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;

public class reset implements CommandExecutor {

    private final Main plugin;
    public reset(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("player").get();

        Optional<User> op = plugin.getUser(user.getUniqueId());

        if (op.isPresent()) {
            User player = op.get();
            if (!plugin.getAcceptedPlayers().contains(player.getUniqueId().toString())) {
                throw new CommandException(plugin.fromLegacy(Config.chatPrefix + "User with the name " + player.getName() + " either has not been found or hasn't accepted the rules yet"));
            }

            plugin.getAcceptedPlayers().remove(player.getUniqueId().toString());
            plugin.getUsersWhoReadRules().remove(player.getName());
            plugin.sendMessage(src, Config.chatPrefix + "Player " + player.getName() + " has to re-accept the rules.");
            return CommandResult.success();
        } else {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + "Unable to find player."));
        }
    }
}
