package net.moddedminecraft.mmcrules;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.moddedminecraft.mmcrules.Commands.ChildCommands.acceptFor;
import net.moddedminecraft.mmcrules.Commands.ChildCommands.reset;
import net.moddedminecraft.mmcrules.Commands.ChildCommands.resetall;
import net.moddedminecraft.mmcrules.Commands.ChildCommands.setTP;
import net.moddedminecraft.mmcrules.Commands.acceptCMD;
import net.moddedminecraft.mmcrules.Commands.mmcRulesCMD;
import net.moddedminecraft.mmcrules.Commands.rulesCMD;
import net.moddedminecraft.mmcrules.Data.RulesData;
import net.moddedminecraft.mmcrules.Data.RulesData.RulesDataSerializer;
import net.moddedminecraft.mmcrules.Database.DataStoreManager;
import net.moddedminecraft.mmcrules.Database.IDataStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Plugin("mmcrules")
public class Main {

    public static final Logger logger = LogManager.getLogger("MMCRules");

    @Inject
    @DefaultConfig(sharedRoot = false)
    public Path defaultConf;

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path configDir;

    public Config config;

    private TypeSerializerCollection ruleSerializer;

    private DataStoreManager dataStoreManager;

    public List<String> readRules = new ArrayList<String>();

    private LinkedHashMap<String, RulesData> rules;

    public final PluginContainer container;

    @Inject
    public Main(final PluginContainer container) {
        this.container = container;
    }

    @Listener
    public void onServerAboutStart(ConstructPluginEvent event) throws IOException {
        Sponge.eventManager().registerListeners(container, new PlayerListener(this));
        ruleSerializer = TypeSerializerCollection.builder().register(RulesData.class, new RulesDataSerializer()).build();
        reloadConfig();
        loadData();
    }

    @Listener
    public void onServerStart(StartedEngineEvent<Server> event) {
        dataStoreManager = new DataStoreManager(this);
        if (dataStoreManager.load()) {
            getLogger().info("MMCRules datastore Loaded");
        } else {
            getLogger().error("Unable to load a datastore please check your Console/Config!");
        }
        logger.info("MMCRules Loaded");
    }

    @Listener
    public void onPluginReload(RefreshGameEvent event) throws IOException {
        getUsersWhoReadRules().clear();
        reloadConfig();
        loadDataStore();
        loadData();
        logger.info("MMCRules Re-Loaded");
    }

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        // /mmcrules acceptfor [player]
        Command.Parameterized AcceptForCmd = Command.builder()
                .shortDescription(Component.text("Accept the rules for a specified player."))
                .executor(new acceptFor(this))
                .addParameter(Parameter.player().key("player").build())
                .permission("mmcrules.commands.acceptfor")
                .build();

        // /mmcrules reset [player]
        Command.Parameterized ResetCmd = Command.builder()
                .shortDescription(Component.text("Force a player to read and accept the rules again."))
                .executor(new reset(this))
                .addParameter(Parameter.player().key("player").build())
                .permission("mmcrules.commands.reset")
                .build();

        // /mmcrules resetall
        Command.Parameterized ResetAllCmd = Command.builder()
                .shortDescription(Component.text("Force all players to read and accept the rules again."))
                .executor(new resetall(this))
                .permission("mmcrules.commands.resetall")
                .build();

        // /mmcrules settp
        Command.Parameterized SetTPCmd = Command.builder()
                .shortDescription(Component.text("Set the teleport location for /acceptrules"))
                .executor(new setTP(this))
                .permission("mmcrules.commands.settp")
                .build();

        // /mmcrules
        event.register(this.container,
                Command.builder()
                        .shortDescription(Component.text("See the list of rules for the server."))
                        .executor(new mmcRulesCMD(this))
                        .addChild(SetTPCmd, "settp")
                        .addChild(ResetAllCmd, "resetall")
                        .addChild(ResetCmd, "reset")
                        .addChild(AcceptForCmd, "acceptfor")
                        .build(), "mmcrules", "mmcr"
        );

        // /acceptrules
        event.register(this.container,
                Command.builder()
                    .shortDescription(Component.text("Accept the rules for the server"))
                    .executor(new acceptCMD(this))
                    .build(), "acceptrules"
        );

        // /rules
        event.register(this.container,
                Command.builder()
                    .shortDescription(Component.text("See the list of rules for the server."))
                    .executor(new rulesCMD(this))
                    .build(), "rules", Config.rulesAlias
        );


    }

    public void reloadConfig() throws IOException {
        config = new Config(this);
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

    public HoconConfigurationLoader getRuleLoader() {
        return HoconConfigurationLoader.builder().path(defaultConf).build();
    }

    private void loadData() throws IOException {
        HoconConfigurationLoader loader = getRuleLoader();
        ConfigurationNode rootNode = loader.load();
        @Nullable List<RulesData> ruleslist = new ArrayList<>();
        List<? extends ConfigurationNode> ruleConfigNode = rootNode.node("list").childrenList();
        for (ConfigurationNode ruleList : ruleConfigNode) {
            String rule = ruleList.node("rule").getString();
            String desc = ruleList.node("desc").getString();
            ruleslist.add(new RulesData(rule, desc));
        }
        this.rules = new LinkedHashMap<>();
        for (RulesData rule : ruleslist) {
            addRule(rule);
        }
    }

    public Collection<RulesData> getRulesData() {
        return Collections.unmodifiableCollection(this.rules.values());
    }

    public RulesData addRule(RulesData rule) {
        return this.rules.put(rule.getRule(), rule);
    }

    public Logger getLogger() {
        return logger;
    }

    public Component fromLegacy(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
    }

}
