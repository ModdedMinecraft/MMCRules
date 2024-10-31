package net.moddedminecraft.mmcrules;


import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.VanishState;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.util.concurrent.TimeUnit;

public class PlayerListener {

    private final Main plugin;
    public PlayerListener(Main instance) {
        plugin = instance;
    }

    @Listener
    public void serverPlayerLoginEvent(ServerSideConnectionEvent.Join event) {
        ServerPlayer player = event.player();
        if (Config.vanishBeforeAccept) {
            if (!plugin.getDataStore().getAccepted().contains(player.uniqueId().toString())) {
                Sponge.asyncScheduler().executor(plugin.container).schedule(() -> {
                    player.offer(Keys.VANISH_STATE, VanishState.vanished());
                }, 1, TimeUnit.SECONDS);
            }
        }

        if (Config.informOnLogin && !player.hasPermission("mmcrules.bypass")) {
            if (plugin.getDataStore().getAccepted().contains(player.uniqueId().toString())) {
                return;
            }
            Sponge.asyncScheduler().executor(plugin.container).schedule(() -> {
                player.sendMessage(plugin.fromLegacy(Config.chatPrefix + Config.informMsg));
            }, 10, TimeUnit.SECONDS);
        }
    }

    @Listener
    public void changeBlockEvent(ChangeBlockEvent.All event) {
        if (event.cause().root() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.cause().root();
            if (Config.blockBuildBeforeAccept) {
                event.setCancelled(checkForAccepted(player, Config.cantBuildMsg));
            }
        }
    }

    @Listener
    public void executeCommandEvent(ExecuteCommandEvent.Pre event, @Root ServerPlayer player) {
        if (Config.blockCommandsBeforeAccept) {
            String command = event.command().toLowerCase();
            switch (command) {
                case "acceptrules":
                case "rules":
                case "sponge":
                case "pagination":
                    break;
                default:
                    event.setCancelled(checkForAccepted(player, Config.informMsg));
                    break;
            }
        }
    }

    @Listener
    public void moveEntityEvent(MoveEntityEvent event, @Root ServerPlayer player) {
        if (Config.blockMovementBeforeAccept) {
            if (plugin.getDataStore().getAccepted().contains(player.uniqueId().toString())) {
                return;
            }
            event.setCancelled(checkForAccepted(player, ""));
        }
    }

    @Listener
    public void playerChatEvent(PlayerChatEvent event, @Root ServerPlayer player) {
        if (Config.blockChatBeforeAccept) {
            if (plugin.getDataStore().getAccepted().contains(player.uniqueId().toString())) {
                return;
            }
            event.setCancelled(checkForAccepted(player, Config.chatMsg));
        }
    }

    private boolean checkForAccepted(ServerPlayer player, String message) {
        if ((plugin.getDataStore().getAccepted().contains(player.uniqueId().toString()))
                || (player.hasPermission("mmcrules.bypass"))) {
            return false;
        }
        if (!message.isEmpty()) {
            player.sendMessage(plugin.fromLegacy(Config.chatPrefix + message));
        }
        return true;
    }
}
