package net.moddedminecraft.mmcrules.Commands;


import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Data.RulesData;
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
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class rulesCMD implements CommandExecutor {

    private final Main plugin;
    public rulesCMD(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Collection<RulesData> rules = plugin.getRulesData();
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
        if (rules.isEmpty()) {
            plugin.sendMessage(src, Config.chatPrefix + "&cThe server owner has not set any rules.");
            return CommandResult.empty();
        }

        int num = 1;
        for (RulesData rule : rules) {
            Text.Builder send = Text.builder();
            String prefix = "";
            if (!Config.listPrefix.isEmpty()) {
                prefix = Config.listPrefix.replace("{pos}", String.valueOf(num)) + " ";
            }
            send.append(plugin.fromLegacy(prefix + "&f" + rule.getRule()));
            if (!rule.getDesc().isEmpty()) {
                send.onHover(TextActions.showText(plugin.fromLegacy(rule.getDesc())));
            }
            num++;
            contents.add(send.build());
        }

        PaginationList.Builder pb = Sponge.getServiceManager().provideUnchecked(PaginationService.class).builder()
                .title(plugin.fromLegacy(Config.rulesTitle))
                .contents(contents)
                .padding(plugin.fromLegacy(Config.listPadding));
        if (!(src instanceof Player)) {
            pb.linesPerPage(-1);
        } else {
            Player player = (Player) src;
            if (!Config.footerText.isEmpty()) {
                Text.Builder footer = Text.builder();
                footer.append(plugin.fromLegacy(Config.footerText));
                if (!Config.footerHover.isEmpty()) {
                    footer.onHover(TextActions.showText(plugin.fromLegacy(Config.footerHover)));
                }
                footer.onClick(TextActions.runCommand("/acceptrules"));
                if (!plugin.getDataStore().getAccepted().contains(player.getUniqueId().toString())) {
                    pb.footer(footer.build());
                }
            }
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
