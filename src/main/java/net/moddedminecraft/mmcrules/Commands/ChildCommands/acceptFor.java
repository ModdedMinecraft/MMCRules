package net.moddedminecraft.mmcrules.Commands.ChildCommands;

import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class acceptFor implements CommandExecutor {

    private final Main plugin;
    public acceptFor(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = args.<Player>getOne("player").get();

        if (player == null) {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + "Player by that name not found. (is he online?)"));
        }
        if (plugin.getAcceptedPlayers().contains(player.getUniqueId().toString())) {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + player.getName()+ " has already accepted the rules!"));
        }
        Sponge.getCommandManager().process(player, "acceptrules");
        plugin.sendMessage(src, Config.chatPrefix + "Performed /acceptrules on " + player.getName());
        return CommandResult.success();
    }
}
