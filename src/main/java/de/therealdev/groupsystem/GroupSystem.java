package de.therealdev.groupsystem;

import co.aikar.commands.PaperCommandManager;
import de.therealdev.groupsystem.command.GroupCommand;
import de.therealdev.groupsystem.database.mysql.AsyncMySQL;
import de.therealdev.groupsystem.database.mysql.GroupPlayerRepository;
import de.therealdev.groupsystem.database.mysql.GroupRepository;
import de.therealdev.groupsystem.listeners.ChatListener;
import de.therealdev.groupsystem.listeners.JoinListener;
import de.therealdev.groupsystem.listeners.LoginListener;
import de.therealdev.groupsystem.listeners.SignListener;
import de.therealdev.groupsystem.manager.GroupManager;
import de.therealdev.groupsystem.manager.GroupPlayerManager;
import de.therealdev.groupsystem.manager.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class GroupSystem extends JavaPlugin {

    private JedisPool jedisPool;
    private GroupManager groupManager;
    private GroupPlayerManager groupPlayerManager;

    private GroupRepository groupRepository;

    private GroupPlayerRepository groupPlayerRepository;

    private MessageManager messageManager;

    private AsyncMySQL asyncMySQL;

    /**
     * This method is called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(jedisPoolConfig, "localhost", 49154, 50000000);
        asyncMySQL = new AsyncMySQL("localhost", 3306, "bob", "bob", "bob");
        groupRepository = new GroupRepository(asyncMySQL);
        groupPlayerRepository = new GroupPlayerRepository(asyncMySQL, groupRepository);
        groupManager = new GroupManager(jedisPool, 1, groupRepository);
        groupPlayerManager = new GroupPlayerManager(jedisPool, 0, groupPlayerRepository, groupManager);

        groupRepository.createTable();
        groupPlayerRepository.createGroupPlayerTable();
        groupPlayerRepository.createPlayerGroupTable();
        messageManager = new MessageManager(this);
        messageManager.setFileConfiguration();
        registerListeners();
        registerCommands();
        Bukkit.getLogger().info("GroupSystem enabled!");
    }

    /**
     * This method registers all listeners.
     */
    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new LoginListener(groupPlayerManager), this);
        pluginManager.registerEvents(new JoinListener(groupPlayerManager, messageManager), this);
        pluginManager.registerEvents(new ChatListener(groupPlayerManager, messageManager), this);
        pluginManager.registerEvents(new SignListener(groupPlayerManager, messageManager), this);
    }

    /**
     * This method registers all commands.
     */
    private void registerCommands() {
        PaperCommandManager paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.registerCommand(new GroupCommand(groupPlayerManager, groupManager, messageManager));
    }

    /**
     * This method is called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        jedisPool.close();
        asyncMySQL.getMySQL().closeConnection();
        Bukkit.getLogger().info("GroupSystem disabled!");
    }
}
