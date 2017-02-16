package net.moddedminecraft.mmcrules.Commands;


import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

public class mmcRulesCMD implements CommandExecutor {

    private final Main plugin;
    public mmcRulesCMD(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        showHelp(src);
        return CommandResult.success();
    }

    void showHelp(CommandSource sender) {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();

        List<Text> contents = new ArrayList<>();
        if (sender.hasPermission("mmcrules.commands.settp")) contents.add(plugin.fromLegacy("&3/mmcrules &bsettp - &7Set the teleport location for /acceptrules (If afterAccept=true)"));
        if (sender.hasPermission("mmcrules.commands.acceptfor")) contents.add(plugin.fromLegacy("&3/mmcrules &bacceptfor &7[player] &b- &7restart the server after a given time"));
        if (sender.hasPermission("mmcrules.commands.reset")) contents.add(plugin.fromLegacy("&3/mmcrules &breset [player] - &7Force a single player to accept the rules again."));
        if (sender.hasPermission("mmcrules.commands.resetall")) contents.add(plugin.fromLegacy("&3/mmcrules &bresetall - &7Force all users to accept the rules again."));
        contents.add(plugin.fromLegacy("&3/rules - &7See the list of rules"));
        contents.add(plugin.fromLegacy("&3/acceptrules - &7Accept the rules"));

        paginationService.builder()
                .title(plugin.fromLegacy("&6MMCRules Help"))
                .contents(contents)
                .header(plugin.fromLegacy("&3[] = required"))
                .padding(Text.of("="))
                .sendTo(sender);
    }
}
