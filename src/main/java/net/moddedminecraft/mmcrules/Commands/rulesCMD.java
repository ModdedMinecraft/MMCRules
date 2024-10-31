package net.moddedminecraft.mmcrules.Commands;


import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Data.RulesData;
import net.moddedminecraft.mmcrules.Main;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

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
    public CommandResult execute(CommandContext context) throws CommandException {

        Collection<RulesData> rules = plugin.getRulesData();
        List<Component> contents = new ArrayList<>();
        Audience audience = context.cause().audience();

        if (!Config.listHeader.isEmpty()) {
            TextComponent.@NotNull Builder send = Component.text();
            send.append(plugin.fromLegacy(Config.listHeader));
            if (!Config.listHeaderHover.isEmpty()) {
                send.hoverEvent(HoverEvent.showText(plugin.fromLegacy(Config.listHeaderHover)));
            }
            if (!Config.listHeaderURL.isEmpty()) {
                URL url = null;
                try {
                    url = new URL(Config.listHeaderURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if (url != null) {
                    send.clickEvent(ClickEvent.openUrl(url));
                } else {
                    send.clickEvent(SpongeComponents.executeCallback(invalid()));
                }
            }
            contents.add(send.build());
        }
        if (rules.isEmpty()) {
            return CommandResult.error(plugin.fromLegacy(Config.chatPrefix + "&cThe server owner has not set any rules."));
        }

        int num = 1;
        for (RulesData rule : rules) {
            TextComponent.@NotNull Builder send = Component.text();
            String prefix = "";
            if (!Config.listPrefix.isEmpty()) {
                prefix = Config.listPrefix.replace("{pos}", String.valueOf(num)) + " ";
            }
            send.append(plugin.fromLegacy(prefix + "&f" + rule.getRule()));
            if (!rule.getDesc().isEmpty()) {
                send.hoverEvent(HoverEvent.showText(plugin.fromLegacy(rule.getDesc())));
            }
            num++;
            contents.add(send.build());
        }

        PaginationList.Builder pb = Sponge.serviceProvider().paginationService().builder()
                .title(plugin.fromLegacy(Config.rulesTitle))
                .contents(contents)
                .padding(plugin.fromLegacy(Config.listPadding));
        if (!(context.cause().root() instanceof ServerPlayer)) {
            pb.linesPerPage(-1);
        } else {
            ServerPlayer player = (ServerPlayer) context.cause().root();
            if (!Config.footerText.isEmpty()) {
                TextComponent.@NotNull Builder footer = Component.text();
                footer.append(plugin.fromLegacy(Config.footerText));
                if (!Config.footerHover.isEmpty()) {
                    footer.hoverEvent(HoverEvent.showText(plugin.fromLegacy(Config.footerHover)));
                }
                footer.clickEvent(ClickEvent.runCommand("/acceptrules"));
                if (!plugin.getDataStore().getAccepted().contains(player.uniqueId().toString())) {
                    pb.footer(footer.build());
                }
            }
            if (!plugin.readRules.contains(player.name())) {
                plugin.readRules.add(player.name());
            }
        }

        pb.sendTo(audience);

        return CommandResult.success();
    }

    private Consumer<CommandCause> invalid() {
        return consumer -> {
            consumer.audience().sendMessage(plugin.fromLegacy("&4URL is invalid, Please report this to an admin."));
        };
    }
}
