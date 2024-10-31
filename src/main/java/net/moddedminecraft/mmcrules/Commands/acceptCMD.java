package net.moddedminecraft.mmcrules.Commands;


import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.VanishState;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;
import java.util.Optional;

public class acceptCMD implements CommandExecutor {

    private final Main plugin;

    public acceptCMD(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {

        if (!(context.cause().root() instanceof ServerPlayer)) {
            throw new CommandException(plugin.fromLegacy("You can only use this command as a player!"));
        }
        ServerPlayer player = (ServerPlayer) context.cause().root();
        if (plugin.getDataStore().getAccepted().contains(player.uniqueId().toString())) {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + Config.acceptedAlreadyMsg));
        }

        if (plugin.getUsersWhoReadRules().contains(player.name())) {
            plugin.getDataStore().addPlayer(player.uniqueId().toString());
            player.sendMessage(plugin.fromLegacy(Config.chatPrefix + Config.acceptedMsg));
            player.offer(Keys.VANISH_STATE, VanishState.unvanished());

            if (Config.afterAccept) {
                Optional<ServerWorld> worldOpt = Sponge.server().worldManager().world(ResourceKey.minecraft(Config.world));
                ServerWorld world = null;
                if (worldOpt.isPresent()) {
                    world = worldOpt.get();
                }
                if (world == null) {
                    world = Sponge.server().worldManager().world(ResourceKey.of("minecraft","world")).get();
                }

                ServerLocation loc = world.location(Config.posX, Config.posY, Config.posZ);
                Vector3d vect = new Vector3d(Config.pitch, Config.yaw, 0);

                if (loc.x() == 0 && loc.x() == 0 && loc.x() == 0) {
                    plugin.getLogger().info("No teleport location has been set yet. Please do so using /mmcrules settp");
                    if (player.hasPermission("mmcrules.commands.settp")) {
                        player.sendMessage(plugin.fromLegacy(Config.chatPrefix + "No teleport location has been set yet. Please do so using /mmcrules settp"));
                    }
                } else {
                    player.setLocationAndRotation(loc, vect);
                }
            }
            List<String> pCommands = Config.playerCommands;
            if (!pCommands.isEmpty()) {
                for (String command : pCommands) {
                    if (!command.equalsIgnoreCase("command 1") || !command.equalsIgnoreCase("command 2") ) {
                        String comm = command.replace("{player}", player.name()).replace("/", "");
                        Sponge.server().commandManager().process(player, comm);
                    }
                }
            }
            List<String> cCommands = Config.consoleCommands;
            if (!cCommands.isEmpty()) {
                for (String command : cCommands) {
                    if (!command.equalsIgnoreCase("command 1") || !command.equalsIgnoreCase("command 2")) {
                        String comm = command.replace("{player}", player.name()).replace("/", "");
                        Sponge.server().commandManager().process(Sponge.systemSubject(), comm);
                    }
                }
            }
            Sponge.systemSubject().sendMessage(plugin.fromLegacy(player.name() + " has accepted the rules!"));
            return CommandResult.success();
        } else {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + Config.mustReadRulesMsg));
        }
    }
}
