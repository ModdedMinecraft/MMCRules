package net.moddedminecraft.mmcrules;

import com.google.inject.Inject;
import net.moddedminecraft.mmcrules.Commands.ChildCommands.acceptFor;
import net.moddedminecraft.mmcrules.Commands.ChildCommands.reset;
import net.moddedminecraft.mmcrules.Commands.ChildCommands.resetall;
import net.moddedminecraft.mmcrules.Commands.ChildCommands.setTP;
import net.moddedminecraft.mmcrules.Commands.acceptCMD;
import net.moddedminecraft.mmcrules.Commands.mmcRulesCMD;
import net.moddedminecraft.mmcrules.Commands.rulesCMD;
import net.moddedminecraft.mmcrules.Database.DataStoreManager;
import net.moddedminecraft.mmcrules.Database.IDataStore;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(id = "mmcrules", name = "MMCRules", version = "2.0.1", description = "Force players to read and accept the rules.")
public class Main {

    @Inject
    private Logger logger;

    @Inject
    private Metrics metrics;

    @Inject
    @DefaultConfig(sharedRoot = false)
    public Path defaultConf;

    @Inject
    @DefaultConfig(sharedRoot = false)
    public File defaultConfFile;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    public File userFile;

    public Config config;

    private DataStoreManager dataStoreManager;

    private CommandManager cmdManager = Sponge.getCommandManager();

    public List<String> readRules = new ArrayList<String>();

    @Listener
    public void Init(GameInitializationEvent event) throws IOException, ObjectMappingException {
        Sponge.getEventManager().registerListeners(this, new PlayerListener(this));
        config = new Config(this);
        loadCommands();
        userFile = new File(configDir.toFile(), "users.dat");
    }

    @Listener
    public void onServerAboutStart(GameAboutToStartServerEvent event) {
        dataStoreManager = new DataStoreManager(this);
        if (dataStoreManager.load()) {
            getLogger().info("MMCRules datastore Loaded");
        } else {
            getLogger().error("Unable to load a datastore please check your Console/Config!");
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
        logger.info("MMCRules Loaded");
    }

    @Listener
    public void onPluginReload(GameReloadEvent event) throws IOException, ObjectMappingException {
        getUsersWhoReadRules().clear();
        this.config = new Config(this);
        loadDataStore();
    }

    private void loadCommands() {

        // /mmcrules acceptfor [player]
        CommandSpec acceptfor = CommandSpec.builder()
                .description(Text.of("Accept the rules for a specified player."))
                .executor(new acceptFor(this))
                .arguments(GenericArguments.player(Text.of("player")))
                .permission("mmcrules.commands.acceptfor")
                .build();

        // /mmcrules reset [player]
        CommandSpec reset = CommandSpec.builder()
                .description(Text.of("Force a player to read and accept the rules again."))
                .executor(new reset(this))
                .arguments(GenericArguments.user(Text.of("player")))
                .permission("mmcrules.commands.reset")
                .build();

        // /mmcrules resetall
        CommandSpec resetall = CommandSpec.builder()
                .description(Text.of("Force all players to read and accept the rules again."))
                .executor(new resetall(this))
                .permission("mmcrules.commands.resetall")
                .build();

        // /mmcrules settp
        CommandSpec settp = CommandSpec.builder()
                .description(Text.of("Set the teleport location for /acceptrules"))
                .executor(new setTP(this))
                .permission("mmcrules.commands.settp")
                .build();

        // /mmcrules
        CommandSpec mmcRules = CommandSpec.builder()
                .description(Text.of("See the list of rules for the server."))
                .executor(new mmcRulesCMD(this))
                .child(settp, "settp")
                .child(resetall, "resetall")
                .child(reset, "reset")
                .child(acceptfor, "acceptfor")
                .build();

        // /rules
        CommandSpec rules = CommandSpec.builder()
                .description(Text.of("See the list of rules for the server."))
                .executor(new rulesCMD(this))
                .build();

        // /acceptrules
        CommandSpec acceptRules = CommandSpec.builder()
                .description(Text.of("Accept the rules for the server"))
                .executor(new acceptCMD(this))
                .build();

        if (!Config.rulesAlias.isEmpty()) {
            cmdManager.register(this, rules, "rules", Config.rulesAlias);
        } else {
            cmdManager.register(this, rules, "rules");
        }
        cmdManager.register(this, acceptRules, "acceptrules");
        cmdManager.register(this, mmcRules, "mmcrules", "mmcr");
    }

    public void loadDataStore() {
        if (dataStoreManager.load()) {
            getLogger().info("MMCRules datastore Loaded");
        } else {
            getLogger().error("Unable to load a datastore please check your Console/Config!");
        }
    }

    public List getUsersWhoReadRules() {
        return readRules;
    }

    public IDataStore getDataStore() {
        return dataStoreManager.getDataStore();
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event, @Root Player player) {
        if (Config.vanishBeforeAccept) {
            if (!getDataStore().getAccepted().contains(player.getUniqueId().toString())) {
                Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {

                    public void run() {
                        player.offer(Keys.INVISIBLE, true);
                        player.offer(Keys.VANISH_IGNORES_COLLISION, true);
                        player.offer(Keys.VANISH_PREVENTS_TARGETING, true);
                    }
                }).delay(1, TimeUnit.SECONDS).name("mmcrules-s-setPlayerInvisible").submit(this);
            }
        }

        if (Config.informOnLogin) {
            if (getDataStore().getAccepted().contains(player.getUniqueId().toString())) {
                return;
            }
            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                public void run() {
                    sendMessage(player, Config.chatPrefix + Config.informMsg);
                }
            }).delay(10, TimeUnit.SECONDS).name("mmcrules-s-sendInformOnLogin").submit(this);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public Optional<User> getUser(UUID uuid) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(uuid);
    }

    public void sendMessage(CommandSource sender, String message) {
        sender.sendMessage(fromLegacy(message));
    }

    public Text fromLegacy(String legacy) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(legacy);
    }

}
