package net.moddedminecraft.mmcrules.Commands.ChildCommands;

import com.flowpowered.math.vector.Vector3d;
import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;

public class setTP implements CommandExecutor {

    private final Main plugin;
    public setTP(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
           throw new CommandException(plugin.fromLegacy(Config.chatPrefix + "You can only use this command as a player!"));
        }
        Player player = (Player) src;
        Vector3d position = player.getHeadRotation();

        Config.loader = HoconConfigurationLoader.builder().setPath(plugin.defaultConf).build();
        try {
            Config.config = Config.loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Config.config.getNode("teleport", "coordinates", "world").setValue(player.getWorld().getName());
        Config.config.getNode("teleport", "coordinates", "posx").setValue(player.getLocation().getX());
        Config.config.getNode("teleport", "coordinates", "posy").setValue(player.getLocation().getY());
        Config.config.getNode("teleport", "coordinates", "posz").setValue(player.getLocation().getZ());
        Config.config.getNode("teleport", "coordinates", "yaw").setValue(position.getY());
        Config.config.getNode("teleport", "coordinates", "pitch").setValue(position.getX());

        try {
            Config.loader.save(Config.config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        plugin.sendMessage(src, Config.chatPrefix + "Teleport location has been set.");
        return CommandResult.success();
    }
}
