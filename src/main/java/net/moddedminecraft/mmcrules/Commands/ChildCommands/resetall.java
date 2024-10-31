package net.moddedminecraft.mmcrules.Commands.ChildCommands;

import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;

public class resetall implements CommandExecutor {

    private final Main plugin;
    public resetall(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandContext context) {
        this.plugin.getUsersWhoReadRules().clear();
        this.plugin.getDataStore().clearList();
        context.cause().audience().sendMessage(plugin.fromLegacy(Config.chatPrefix + "All users have been cleared. Everyone has to re-accept the rules."));
        return CommandResult.success();
    }
}
