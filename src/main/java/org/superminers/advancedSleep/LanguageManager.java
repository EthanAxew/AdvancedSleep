package org.superminers.advancedSleep;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class LanguageManager {
    private final AdvancedSleep plugin;
    private FileConfiguration langConfig;
    private File langFile;

    public LanguageManager(AdvancedSleep plugin) {
        this.plugin = plugin;
        setupLanguage();
    }

    private void setupLanguage() {
        // 确保插件数据文件夹存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        langFile = new File(plugin.getDataFolder(), "messages.yml");

        // 如果文件不存在，从JAR中提取默认配置
        if (!langFile.exists()) {
            try {
                // 从JAR中获取默认配置
                InputStream inputStream = plugin.getResource("messages.yml");
                if (inputStream != null) {
                    // 复制文件
                    Files.copy(inputStream, langFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    // 创建空的配置文件作为后备
                    createFallbackConfig();
                }
            } catch (IOException e) {
                // 创建空的配置文件作为后备
                createFallbackConfig();
            }
        }

        reloadLanguage();
    }

    private void createFallbackConfig() {
        try {
            if (langFile.createNewFile()) {
                // 写入基本的默认配置
                langConfig = YamlConfiguration.loadConfiguration(langFile);

                // 基本消息
                langConfig.set("plugin-enabled", "&a高级睡眠插件已启用!");
                langConfig.set("plugin-disabled", "&c高级睡眠插件已禁用!");
                langConfig.set("config-reloaded", "&a配置已重新加载!");

                // 错误消息
                langConfig.set("console-not-allowed", "&c只有玩家可以使用此命令!");
                langConfig.set("not-night", "&e现在不是夜晚，不需要睡觉!");
                langConfig.set("no-percision", "&c你没有使用睡眠功能的权限!");
                langConfig.set("sleep-ignored", "&c你当前忽略睡觉状态!");
                langConfig.set("world-disabled", "&c这个世界没有启用睡眠功能!");
                langConfig.set("invalid-percentage", "&c百分比必须在0.1到1.0之间!");
                langConfig.set("invalid-number", "&c请输入有效的数字!");
                langConfig.set("invalid-speed", "&c速度必须在1到100之间!");
                langConfig.set("invalid-delay", "&c延迟必须在0到60秒之间!");

                // 状态消息
                langConfig.set("single-sleep", "&a单人睡眠");
                langConfig.set("percentage-sleep", "&e比例睡眠 ({0}%)");
                langConfig.set("enabled", "&a启用");
                langConfig.set("disabled", "&c禁用");
                langConfig.set("toggle-mode", "&a睡眠模式已切换为: {0}");
                langConfig.set("set-percentage", "&a睡眠百分比已设置为: {0}%");
                langConfig.set("toggle-acceleration", "&a时间加速已{0}");
                langConfig.set("set-speed", "&a时间加速速度已设置为: {0}");
                langConfig.set("set-delay", "&a跳过延迟已设置为: {0}秒");
                langConfig.set("set-delay-prompt", "&e请在聊天框中输入新的跳过延迟(0-60秒):");
                langConfig.set("set-delay-usage", "&e使用 /sleep delay <秒数> 命令设置跳过延迟");

                // 睡眠相关消息
                langConfig.set("player-sleeping", "&e{0} 上床睡觉了 ({1}/{2})");
                langConfig.set("player-wakeup", "&e{0} 起床了 ({1}/{2})");
                langConfig.set("skip-countdown-start", "&6满足睡眠条件! 将在 {0} 秒后跳过夜晚...");
                langConfig.set("skip-countdown", "&e跳过夜晚倒计时: {0}秒");
                langConfig.set("skip-cancelled", "&e跳过夜晚已取消!");
                langConfig.set("morning-come", "&6天亮了! 大家起床啦!");
                langConfig.set("time-acceleration-started", "&e时间加速已开始，当前时间: {0}");
                langConfig.set("time-acceleration-stopped", "&e时间加速已停止");

                // GUI消息
                langConfig.set("gui-title", "&9睡眠设置");
                langConfig.set("gui-mode-name", "&a睡眠模式");
                langConfig.set("gui-mode-current", "&7当前模式: {0}");
                langConfig.set("gui-acceleration-name", "&6时间加速");
                langConfig.set("gui-acceleration-current", "&7当前状态: {0}");
                langConfig.set("gui-acceleration-speed", "&7速度: {0}");
                langConfig.set("gui-delay-name", "&d跳过延迟");
                langConfig.set("gui-delay-current", "&7当前延迟: {0}秒");
                langConfig.set("gui-info-name", "&b插件信息");
                langConfig.set("gui-info-version", "&7版本: {0}");
                langConfig.set("gui-info-author", "&7作者: {0}");
                langConfig.set("gui-info-feature1", "&e支持单人睡觉、比例睡觉");
                langConfig.set("gui-info-feature2", "&e时间加速和GUI设置");
                langConfig.set("gui-click-to-toggle", "&e点击切换状态");
                langConfig.set("gui-click-to-change", "&e点击修改设置");

                // 帮助消息
                langConfig.set("help-header", "&6===== &e睡眠插件帮助 &6=====");
                langConfig.set("help-command", "&e{0}");

                // 时间显示消息
                langConfig.set("time-format", "&6⏰ &e{0}");

                langConfig.save(langFile);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法创建后备语言文件", e);
        }
    }

    public void reloadLanguage() {
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String key) {
        String message = langConfig.getString(key);
        if (message == null) {
            return "&cMissing message: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String key, Object... args) {
        String message = getMessage(key);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return message;
    }
}