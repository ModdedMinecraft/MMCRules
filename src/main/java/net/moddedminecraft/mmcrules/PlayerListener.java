package net.moddedminecraft.mmcrules;


import com.google.common.collect.Maps;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class PlayerListener {

    private final Main plugin;
    public PlayerListener(Main instance) {
        plugin = instance;
    }

    private Map<UUID, Instant> lastMoveNotification = Maps.newHashMap();

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player) {
        if (Config.blockBuildBeforeAccept) {
            event.setCancelled(checkForAccepted(player, Config.cantBuildMsg));
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player) {
        if (Config.blockBuildBeforeAccept) {
            event.setCancelled(checkForAccepted(player, Config.cantBuildMsg));
        }
    }

    @Listener
    public void beforeCommand(SendCommandEvent event, @Root Player player) {
        if (Config.blockCommandsBeforeAccept) {
            String command = event.getCommand().toLowerCase();
            if (command.equals("acceptrules") || command.equals("rules") || command.equals("sponge") || command.equals("pagination")) {
                return;
            } else {
                event.setCancelled(checkForAccepted(player, Config.informMsg));
            }
        }
    }

    @Listener
    public void onEntityMove(MoveEntityEvent event, @Root Player player) {
        if (Config.blockMovementBeforeAccept) {
            if (plugin.getDataStore().getAccepted().contains(player.getUniqueId().toString())) {
                return;
            }
            event.setCancelled(true);
        }
    }

    private boolean checkForAccepted(Player player, String message) {
        if (plugin.getDataStore().getAccepted().contains(player.getUniqueId().toString())) {
            return false;
        }
        plugin.sendMessage(player, Config.chatPrefix + message);
        return true;
    }
}
