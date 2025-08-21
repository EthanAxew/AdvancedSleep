package org.superminers.advancedSleep;

import org.bukkit.plugin.java.JavaPlugin;
import org.superminers.advancedSleep.commands.SleepCommand;

import java.util.logging.Level;

public class AdvancedSleep extends JavaPlugin {
    private SleepManager sleepManager;
    private ConfigManager configManager;
    private GUIManager guiManager;
    private LanguageManager languageManager;
    private SleepCommand sleepCommand;

    @Override
    public void onEnable() {
        try {
            // 确保数据文件夹存在
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            // 初始化管理器 - 按照正确的依赖顺序
            this.languageManager = new LanguageManager(this);
            this.configManager = new ConfigManager(this);
            this.sleepManager = new SleepManager(this);
            this.guiManager = new GUIManager(this);
            this.sleepCommand = new SleepCommand(this);

            // 注册事件
            getServer().getPluginManager().registerEvents(sleepManager, this);
            getServer().getPluginManager().registerEvents(guiManager, this);

            // 注册命令
            getCommand("sleep").setExecutor(sleepCommand);
            getCommand("sleep").setTabCompleter(sleepCommand);

            getLogger().info(languageManager.getMessage("plugin-enabled"));

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "插件启动过程中发生严重错误", e);
            // 禁用插件
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (sleepManager != null) {
                sleepManager.onDisable();
            }

            if (guiManager != null) {
                guiManager.clearAllGuis();
            }

            getLogger().info(languageManager != null ?
                    languageManager.getMessage("plugin-disabled") : "插件已禁用");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "插件禁用过程中发生错误", e);
        }
    }

    // Getter方法
    public SleepManager getSleepManager() {
        return sleepManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}