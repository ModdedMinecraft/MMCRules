package net.moddedminecraft.mmcrules.Commands.ChildCommands;

import net.moddedminecraft.mmcrules.Config;
import net.moddedminecraft.mmcrules.Main;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.math.vector.Vector3d;

import java.io.IOException;

public class setTP implements CommandExecutor {

    private final Main plugin;
    public setTP(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        if (!(context.cause().root() instanceof ServerPlayer)) {
           throw new CommandException(plugin.fromLegacy(Config.chatPrefix + "You can only use this command as a player!"));
        }
        ServerPlayer player = (ServerPlayer) context.cause().root();
        Vector3d position = player.headRotation().get();

        Config.loader = HoconConfigurationLoader.builder().path(plugin.defaultConf).build();
        try {
            Config.config = Config.loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Config.config.node("teleport", "coordinates", "world").set(player.world().key().value());
            Config.config.node("teleport", "coordinates", "posx").set(player.location().x());
            Config.config.node("teleport", "coordinates", "posy").set(player.location().y());
            Config.config.node("teleport", "coordinates", "posz").set(player.location().z());
            Config.config.node("teleport", "coordinates", "yaw").set(position.y());
            Config.config.node("teleport", "coordinates", "pitch").set(position.x());
            Config.loader.save(Config.config);
            plugin.reloadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        player.sendMessage(plugin.fromLegacy(Config.chatPrefix + "Teleport location has been set."));
        return CommandResult.success();
    }
}
