package net.moddedminecraft.mmcrules.Commands.ChildCommands;

import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class resetall implements CommandExecutor {

    private final Main plugin;
    public resetall(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        this.plugin.getUsersWhoReadRules().clear();
        this.plugin.getDataStore().clearList();
        plugin.sendMessage(src, Config.chatPrefix + "All users have been cleared. Everyone has to re-accept the rules.");
        return CommandResult.success();
    }
}
