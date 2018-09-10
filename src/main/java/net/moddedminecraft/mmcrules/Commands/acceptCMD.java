package net.moddedminecraft.mmcrules.Commands;


import com.flowpowered.math.vector.Vector3d;
import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class acceptCMD implements CommandExecutor {

    private final Main plugin;

    public acceptCMD(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if (!(src instanceof Player)) {
            throw new CommandException(plugin.fromLegacy("You can only use this command as a player!"));
        }
        Player player = (Player) src;
        if (plugin.getDataStore().getAccepted().contains(player.getUniqueId().toString())) {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + Config.acceptedAlreadyMsg));
        }

        if (plugin.getUsersWhoReadRules().contains(player.getName())) {
            plugin.getDataStore().addPlayer(player.getUniqueId().toString());
            plugin.sendMessage(player, Config.chatPrefix + Config.acceptedMsg);
            player.offer(Keys.INVISIBLE, false);
            player.offer(Keys.VANISH_IGNORES_COLLISION, false);
            player.offer(Keys.VANISH_PREVENTS_TARGETING, false);

            if (Config.afterAccept) {
                Optional<World> worldOpt = Sponge.getServer().getWorld(Config.world);
                World world = null;
                if (worldOpt.isPresent()) {
                    world = worldOpt.get();
                }
                if (world == null) {
                    world = Sponge.getServer().getWorld("world").get();
                }

                Location loc = new Location(world, Config.posX, Config.posY, Config.posZ);
                Vector3d vect = new Vector3d(Config.pitch, Config.yaw, 0);

                if (loc.getX() == 0 && loc.getY() == 0 && loc.getZ() == 0) {
                    plugin.getLogger().info("No teleport location has been set yet. Please do so using /mmcrules settp");
                    if (player.hasPermission("mmcrules.commands.settp")) {
                        plugin.sendMessage(player, Config.chatPrefix + "No teleport location has been set yet. Please do so using /mmcrules settp");
                    }
                } else {
                    player.setLocation(loc);
                    player.setHeadRotation(vect);
                }
            }
            List<String> pCommands = Config.playerCommands;
            if (!pCommands.isEmpty()) {
                for (String command : pCommands) {
                    if (!command.equalsIgnoreCase("command 1") || !command.equalsIgnoreCase("command 2") ) {
                        String comm = command.replace("{player}", player.getName()).replace("/", "");
                        Sponge.getCommandManager().process(player, comm);
                    }
                }
            }
            List<String> cCommands = Config.consoleCommands;
            if (!cCommands.isEmpty()) {
                for (String command : cCommands) {
                    if (!command.equalsIgnoreCase("command 1") || !command.equalsIgnoreCase("command 2")) {
                        String comm = command.replace("{player}", player.getName()).replace("/", "");
                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), comm);
                    }
                }
            }
            Sponge.getServer().getConsole().sendMessage(plugin.fromLegacy(player.getName() + " has accepted the rules!"));
            return CommandResult.success();
        } else {
            throw new CommandException(plugin.fromLegacy(Config.chatPrefix + Config.mustReadRulesMsg));
        }
    }
}
