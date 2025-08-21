package org.superminers.advancedSleep;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class ConfigManager {
    private final AdvancedSleep plugin;
    private FileConfiguration config;

    private boolean singlePlayerSleep;
    private double sleepPercentage;
    private boolean timeAccelerationEnabled;
    private int accelerationSpeed;
    private int skipDelay;
    private boolean showTimeDuringAcceleration;
    private Set<String> enabledWorlds;

    private boolean isValid = false;

    public ConfigManager(AdvancedSleep plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        try {
            // 保存默认配置
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();

            // 设置默认值（如果不存在）
            config.addDefault("single-player-sleep", true);
            config.addDefault("sleep-percentage", 0.5);
            config.addDefault("time-acceleration", true);
            config.addDefault("acceleration-speed", 10);
            config.addDefault("skip-delay", 3);
            config.addDefault("show-time-during-acceleration", true);
            config.addDefault("enabled-worlds", new String[]{"world", "world_nether", "world_the_end"});
            config.options().copyDefaults(true);
            plugin.saveConfig();

            // 加载配置值
            singlePlayerSleep = config.getBoolean("single-player-sleep");
            sleepPercentage = config.getDouble("sleep-percentage");
            timeAccelerationEnabled = config.getBoolean("time-acceleration");
            accelerationSpeed = config.getInt("acceleration-speed");
            skipDelay = config.getInt("skip-delay");
            showTimeDuringAcceleration = config.getBoolean("show-time-during-acceleration");
            enabledWorlds = new HashSet<>(config.getStringList("enabled-worlds"));

            isValid = true;

        } catch (Exception e) {
            // 使用默认值
            singlePlayerSleep = true;
            sleepPercentage = 0.5;
            timeAccelerationEnabled = true;
            accelerationSpeed = 10;
            skipDelay = 3;
            showTimeDuringAcceleration = true;
            enabledWorlds = new HashSet<>();
            enabledWorlds.add("world");
            enabledWorlds.add("world_nether");
            enabledWorlds.add("world_the_end");

            isValid = false;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    // Getter和Setter方法
    public boolean isSinglePlayerSleep() {
        return singlePlayerSleep;
    }

    public void setSinglePlayerSleep(boolean singlePlayerSleep) {
        this.singlePlayerSleep = singlePlayerSleep;
        try {
            config.set("single-player-sleep", singlePlayerSleep);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "保存配置失败", e);
        }
    }

    public double getSleepPercentage() {
        return sleepPercentage;
    }

    public void setSleepPercentage(double sleepPercentage) {
        this.sleepPercentage = sleepPercentage;
        try {
            config.set("sleep-percentage", sleepPercentage);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "保存配置失败", e);
        }
    }

    public boolean isTimeAccelerationEnabled() {
        return timeAccelerationEnabled;
    }

    public void setTimeAccelerationEnabled(boolean timeAccelerationEnabled) {
        this.timeAccelerationEnabled = timeAccelerationEnabled;
        try {
            config.set("time-acceleration", timeAccelerationEnabled);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "保存配置失败", e);
        }
    }

    public int getAccelerationSpeed() {
        return accelerationSpeed;
    }

    public void setAccelerationSpeed(int accelerationSpeed) {
        this.accelerationSpeed = accelerationSpeed;
        try {
            config.set("acceleration-speed", accelerationSpeed);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "保存配置失败", e);
        }
    }

    public int getSkipDelay() {
        return skipDelay;
    }

    public void setSkipDelay(int skipDelay) {
        this.skipDelay = skipDelay;
        try {
            config.set("skip-delay", skipDelay);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "保存配置失败", e);
        }
    }

    public boolean isShowTimeDuringAcceleration() {
        return showTimeDuringAcceleration;
    }

    public void setShowTimeDuringAcceleration(boolean showTimeDuringAcceleration) {
        this.showTimeDuringAcceleration = showTimeDuringAcceleration;
        try {
            config.set("show-time-during-acceleration", showTimeDuringAcceleration);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "保存配置失败", e);
        }
    }

    public Set<String> getEnabledWorlds() {
        return enabledWorlds;
    }

    public void setEnabledWorlds(Set<String> enabledWorlds) {
        this.enabledWorlds = enabledWorlds;
        try {
            config.set("enabled-worlds", new ArrayList<>(enabledWorlds));
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "保存配置失败", e);
        }
    }
}