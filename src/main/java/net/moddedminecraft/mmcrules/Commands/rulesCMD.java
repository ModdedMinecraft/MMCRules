package net.moddedminecraft.mmcrules.Commands;


import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

public class rulesCMD implements CommandExecutor {

    private final Main plugin;
    public rulesCMD(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        List<String> rules = Config.ruleList;
        List<Text> contents = new ArrayList<>();

        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).equals("") && rules.size() <= 1) {
                plugin.sendMessage(src, Config.chatPrefix + "&cThe server owner has not set any rules.");
                return CommandResult.empty();
            } else {
                Text.Builder send = Text.builder();
                send.append(plugin.fromLegacy("&f[&3" + String.valueOf(i + 1) + "&f] &f" + rules.get(i)));
                contents.add(send.build());
            }
        }

        PaginationList.Builder pb = Sponge.getServiceManager().provideUnchecked(PaginationService.class).builder()
                .title(plugin.fromLegacy(Config.rulesTitle))
                .contents(contents)
                .padding(Text.of("="));

        if (!(src instanceof Player)) {
            pb.linesPerPage(-1);
        } else {
            if (!plugin.readRules.contains(src.getName())) {
                plugin.readRules.add(src.getName());
            }
        }

        pb.sendTo(src);

        return CommandResult.success();
    }
}
