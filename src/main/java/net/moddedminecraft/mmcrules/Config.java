package net.moddedminecraft.mmcrules;


import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import net.moddedminecraft.mmcrules.Data.RulesData;
import net.moddedminecraft.mmcrules.Data.RulesData.RulesDataSerializer;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class Config {

    private final Main plugin;

    public static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode config;

    public Config(Main main) throws IOException, ObjectMappingException {
        plugin = main;
        loader = HoconConfigurationLoader.builder().setPath(plugin.defaultConf).build();
        config = loader.load();
        configCheck();
    }

    public static String chatPrefix = "&f[&6MMCRules&f] ";
    public static String rulesAlias;

    //rules
    private static List<String> RulesData = Lists.newLinkedList();
    public static boolean informOnLogin = true;
    public static String rulesTitle = "Rules";
    public static List<String> playerCommands;
    public static List<String> consoleCommands;
    public static String listHeader;
    public static String listHeaderURL;
    public static String listHeaderHover;
    public static String listPadding = "=";
    public static String listPrefix = "[&3{pos}&f]";

    //messages
    public static String acceptedMsg = "&cYou have successfully accepted the &6rules&c! Have fun!";
    public static String mustReadRulesMsg = "&cYou must read the &6Rules &cin order to accept them! Use &b/rules!";
    public static String acceptedAlreadyMsg = "&cYou have already accepted the &6rules&c!";
    public static String cantBuildMsg = "&cYou have to accept the &6Rules &cto build! Use &b/rules &cand then &b/acceptrules&c!";
    public static String informMsg = "&cYou have to accept the &6Rules&c! Use &b/rules &cand then &b/acceptrules&c!";

    //restrictions
    public static boolean blockBuildBeforeAccept = false;
    public static boolean blockMovementBeforeAccept = false;
    public static boolean blockCommandsBeforeAccept = false;
    public static boolean vanishBeforeAccept = false;

    //teleport
    public static boolean afterAccept = false;
    public static String world;
    public static Double posX;
    public static Double posY;
    public static Double posZ;
    public static Double pitch;
    public static Double yaw;

    //database
    public static String storageEngine;
    public static String databaseFile;
    public static String h2Prefix;
    public static String mysqlHost;
    public static int mysqlPort;
    public static String mysqlDatabase;
    public static String mysqlUser;
    public static String mysqlPass;
    public static String mysqlPrefix;
    public static String server;

    public void configCheck() throws IOException, ObjectMappingException {
        if (!plugin.defaultConfFile.exists()) {
            plugin.defaultConfFile.createNewFile();
        }

        //cmd alias
        rulesAlias = check(config.getNode("command", "alias"), "", "Command alias for /rules, This will only accept one alias. (Requires server restart to register)").getString();

        //teleport
        afterAccept = check(config.getNode("teleport", "afterAccept"), false, "Do you want the player to be teleported to a set location after they /acceptrules (set this with /mmcrules settp)").getBoolean();
        world = check(config.getNode("teleport", "coordinates", "world"), "World").getString();
        posX = check(config.getNode("teleport", "coordinates", "posx"), 0).getDouble();
        posY = check(config.getNode("teleport", "coordinates", "posy"), 0).getDouble();
        posZ = check(config.getNode("teleport", "coordinates", "posz"), 0).getDouble();
        pitch = check(config.getNode("teleport", "coordinates", "pitch"), 0).getDouble();
        yaw = check(config.getNode("teleport", "coordinates", "yaw"), 0).getDouble();

        //messages
        acceptedMsg = check(config.getNode("messages", "accepted"), acceptedMsg).getString();
        mustReadRulesMsg = check(config.getNode("messages", "mustReadRules"), mustReadRulesMsg).getString();
        acceptedAlreadyMsg = check(config.getNode("messages", "acceptedAlready"), acceptedAlreadyMsg).getString();
        cantBuildMsg = check(config.getNode("messages", "cantBuild"), cantBuildMsg).getString();
        informMsg = check(config.getNode("messages", "inform"), informMsg).getString();
        chatPrefix = check(config.getNode("messages", "prefix"), chatPrefix, "The prefix of messages sent in chat").getString();

        //restrictions
        blockBuildBeforeAccept = check(config.getNode("restrictions", "blockBuildBeforeAccept"), blockBuildBeforeAccept, "Should players be blocked from placing and breaking blocks before reading the rules?").getBoolean();
        blockMovementBeforeAccept = check(config.getNode("restrictions", "blockMovementBeforeAccept"), blockMovementBeforeAccept, "Should players be blocked from moving before reading the rules?").getBoolean();
        blockCommandsBeforeAccept = check(config.getNode("restrictions", "blockCommandsBeforeAccept"), blockCommandsBeforeAccept, "Should players be blocked from sending commands before reading the rules?").getBoolean();
        vanishBeforeAccept = check(config.getNode("restrictions", "vanishBeforeAccept"), vanishBeforeAccept, "Should players be invisible to all players before reading the rules?").getBoolean();

        //rules
        listHeader = check(config.getNode("rules", "header", "message"), "", "This text is displayed above the rules in /rules").getString();
        listHeaderURL = check(config.getNode("rules", "header", "url"), "", "When players click the text set in message, they will be prompted to this URL (Must have http:// or https:// at the beginning)").getString();
        listHeaderHover = check(config.getNode("rules", "header", "hover"), "", "This message will be displayed when the player hovers over the header message.").getString();
        listPadding = check(config.getNode("rules", "padding"), listPadding).getString();
        listPrefix = check(config.getNode("rules", "prefix"), listPrefix).getString();

        //server
        server = check(config.getNode("server"), "Global", "Name of the server. Used for indavidual server identification. If a different name is set, It will check if the player has accepted the rules for that specific server instead of globally.  Default: \"Global\"").getString();

        //database
        storageEngine = check(config.getNode("storage", "storage-engine"), "h2", "The stoage engine that should be used, Allowed values: h2 or mysql").getString();
        databaseFile = check(config.getNode("storage", "h2", "database-file"), "Database.db", "Where the databaseFile will be stored. Can be a relative or absolute path. An absolute path is recommended when using this to synchronize over several servers").getString();
        h2Prefix = check(config.getNode("storage", "h2", "prefix"), "mmcrules_", "Prefix for the plugin tables").getString();
        mysqlHost = check(config.getNode("storage", "mysql", "host"), "localhost", "Host of the MySQL Server").getString();
        mysqlPort = check(config.getNode("storage", "mysql", "port"), "3306", "Port of the MySQL server. Default: 3306").getInt();
        mysqlDatabase = check(config.getNode("storage", "mysql", "database"), "mmcrules", "The database to store in").getString();
        mysqlUser = check(config.getNode("storage", "mysql", "user"), "root", "The user for the database").getString();
        mysqlPass = check(config.getNode("storage", "mysql", "password"), "pass", "Password for that user").getString();
        mysqlPrefix = check(config.getNode("storage", "mysql", "table-prefix"), "mmcrules_", "Prefix for the plugin tables").getString();

        if (!config.getNode("list").hasListChildren()) {
            LinkedHashMap<String, RulesData> map = new LinkedHashMap<>();
            map.put("Rule 1", new RulesData("Rule 1", "Example hover description, leave blank to ignore"));
            map.put("Rule 2", new RulesData("Rule 2", "Example hover description, leave blank to ignore"));
            config.getNode("list").setComment("List of /rules, with optional hover description (leave blank to ignore)").setValue(RulesDataSerializer.token, new ArrayList<RulesData>(map.values()));
        }

        informOnLogin = check(config.getNode("rules", "informOnLogin"), informOnLogin, "Do you want the player to be sent the above 'Inform' message after logging in?").getBoolean();
        rulesTitle = check(config.getNode("rules", "title"), rulesTitle, "The tile for the /rules").getString();
        if (config.getNode("rules", "onAccept", "playerCommands").hasListChildren()) {
            playerCommands = check(config.getNode("rules", "onAccept", "playerCommands"), Collections.emptyList(), "Commands to be run after the player accepts the rules, These commands are sent by the player").getList(TypeToken.of(String.class));
        } else {
            playerCommands = config.getNode("rules", "onAccept", "playerCommands").setValue(Collections.emptyList()).setComment("Commands to be run after the player accepts the rules, These commands are sent by the player").getList(TypeToken.of(String.class));
        }
        if (config.getNode("rules", "onAccept", "consoleCommands").hasListChildren()) {
            consoleCommands = check(config.getNode("rules", "onAccept", "consoleCommands"), Collections.emptyList(), "Commands to be run after the player accepts the rules, These commands are sent by the console ({player} will be replaced by the player's name)").getList(TypeToken.of(String.class));
        } else {
            consoleCommands = config.getNode("rules", "onAccept", "consoleCommands").setValue(Collections.emptyList()).setComment("Commands to be run after the player accepts the rules, These commands are sent by the console ({player} will be replaced by the player's name)").getList(TypeToken.of(String.class));
        }
        loader.save(config);
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue, String comment) {
        if (node.isVirtual()) {
            node.setValue(defaultValue).setComment(comment);
        }
        return node;
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue) {
        if (node.isVirtual()) {
            node.setValue(defaultValue);
        }
        return node;
    }
}
