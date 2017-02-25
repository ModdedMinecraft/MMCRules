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
import org.spongepowered.api.text.action.TextActions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class rulesCMD implements CommandExecutor {

    private final Main plugin;
    public rulesCMD(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        List<String> rules = Config.ruleList;
        List<Text> contents = new ArrayList<>();

        if (!Config.listHeader.isEmpty()) {
            Text.Builder send = Text.builder();
            send.append(plugin.fromLegacy(Config.listHeader));
            if (!Config.listHeaderHover.isEmpty()) {
                send.onHover(TextActions.showText(plugin.fromLegacy(Config.listHeaderHover)));
            }
            if (!Config.listHeaderURL.isEmpty()) {
                URL url = null;
                try {
                    url = new URL(Config.listHeaderURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if (url != null) {
                    send.onClick(TextActions.openUrl(url));
                } else {
                    send.onClick(TextActions.executeCallback(invalid()));
                }
            }
            contents.add(send.build());
        }

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

    private Consumer<CommandSource> invalid() {
        return consumer -> {
            plugin.sendMessage(consumer, "&4URL is invalid, Please report this to an admin.");
        };
    }
}
