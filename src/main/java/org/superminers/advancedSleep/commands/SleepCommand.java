package org.superminers.advancedSleep.commands;

import org.superminers.advancedSleep.AdvancedSleep;
import org.superminers.advancedSleep.ConfigManager;
import org.superminers.advancedSleep.GUIManager;
import org.superminers.advancedSleep.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SleepCommand implements TabExecutor {
    private final AdvancedSleep plugin;
    private final ConfigManager configManager;
    private final GUIManager guiManager;
    private final LanguageManager languageManager;

    public SleepCommand(AdvancedSleep plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.guiManager = plugin.getGuiManager();
        this.languageManager = plugin.getLanguageManager();

        plugin.getLogger().info("睡眠命令处理器已初始化");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage(languageManager.getMessage("console-not-allowed"));
                return true;
            }

            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("sleep")) {
                plugin.getLogger().info("玩家 " + player.getName() + " 执行了 /sleep 命令，参数: " + Arrays.toString(args));

                if (args.length == 0) {
                    // 打开GUI设置界面
                    guiManager.openSleepGUI(player);
                    return true;
                }

                // 处理子命令
                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("toggle") && player.hasPermission("advancedsleep.admin")) {
                        configManager.setSinglePlayerSleep(!configManager.isSinglePlayerSleep());

                        String mode = configManager.isSinglePlayerSleep() ?
                                languageManager.getMessage("single-sleep") :
                                languageManager.getMessage("percentage-sleep", (int)(configManager.getSleepPercentage() * 100));
                        player.sendMessage(languageManager.getMessage("toggle-mode", mode));

                        // 刷新所有打开的GUI
                        guiManager.refreshAllOpenGuis();
                        plugin.getLogger().info("玩家 " + player.getName() + " 切换了睡眠模式: " + mode);
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("percentage") && args.length >= 2 && player.hasPermission("advancedsleep.admin")) {
                        try {
                            double percentage = Double.parseDouble(args[1]);
                            if (percentage < 0.1 || percentage > 1.0) {
                                player.sendMessage(languageManager.getMessage("invalid-percentage"));
                                return true;
                            }
                            configManager.setSleepPercentage(percentage);
                            player.sendMessage(languageManager.getMessage("set-percentage", (int)(percentage * 100)));

                            // 刷新所有打开的GUI
                            guiManager.refreshAllOpenGuis();
                            plugin.getLogger().info("玩家 " + player.getName() + " 设置了睡眠百分比: " + percentage);
                        } catch (NumberFormatException e) {
                            player.sendMessage(languageManager.getMessage("invalid-number"));
                        }
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("acceleration") && args.length >= 2 && player.hasPermission("advancedsleep.admin")) {
                        boolean enabled = args[1].equalsIgnoreCase("on");
                        configManager.setTimeAccelerationEnabled(enabled);

                        String status = enabled ? languageManager.getMessage("enabled") : languageManager.getMessage("disabled");
                        player.sendMessage(languageManager.getMessage("toggle-acceleration", status));

                        // 刷新所有打开的GUI
                        guiManager.refreshAllOpenGuis();
                        plugin.getLogger().info("玩家 " + player.getName() + " 切换了时间加速: " + status);
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("speed") && args.length >= 2 && player.hasPermission("advancedsleep.admin")) {
                        try {
                            int speed = Integer.parseInt(args[1]);
                            if (speed < 1 || speed > 100) {
                                player.sendMessage(languageManager.getMessage("invalid-speed"));
                                return true;
                            }
                            configManager.setAccelerationSpeed(speed);
                            player.sendMessage(languageManager.getMessage("set-speed", speed));

                            // 刷新所有打开的GUI
                            guiManager.refreshAllOpenGuis();
                            plugin.getLogger().info("玩家 " + player.getName() + " 设置了时间加速速度: " + speed);
                        } catch (NumberFormatException e) {
                            player.sendMessage(languageManager.getMessage("invalid-number"));
                        }
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("delay") && args.length >= 2 && player.hasPermission("advancedsleep.admin")) {
                        try {
                            int delay = Integer.parseInt(args[1]);
                            if (delay < 0 || delay > 60) {
                                player.sendMessage(languageManager.getMessage("invalid-delay"));
                                return true;
                            }
                            configManager.setSkipDelay(delay);
                            player.sendMessage(languageManager.getMessage("set-delay", delay));

                            // 刷新所有打开的GUI
                            guiManager.refreshAllOpenGuis();
                            plugin.getLogger().info("玩家 " + player.getName() + " 设置了跳过延迟: " + delay);
                        } catch (NumberFormatException e) {
                            player.sendMessage(languageManager.getMessage("invalid-number"));
                        }
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("reload") && player.hasPermission("advancedsleep.admin")) {
                        configManager.reload();
                        languageManager.reloadLanguage();
                        plugin.getSleepManager().reload();
                        player.sendMessage(languageManager.getMessage("config-reloaded"));

                        // 刷新所有打开的GUI
                        guiManager.refreshAllOpenGuis();
                        plugin.getLogger().info("玩家 " + player.getName() + " 重新加载了配置");
                        return true;
                    }
                    else if (args[0].equalsIgnoreCase("help")) {
                        // 显示帮助
                        player.sendMessage(languageManager.getMessage("help-header"));
                        player.sendMessage(languageManager.getMessage("help-command", "/sleep"));
                        if (player.hasPermission("advancedsleep.admin")) {
                            player.sendMessage(languageManager.getMessage("help-command", "/sleep toggle"));
                            player.sendMessage(languageManager.getMessage("help-command", "/sleep percentage <百分比>"));
                            player.sendMessage(languageManager.getMessage("help-command", "/sleep acceleration <on|off>"));
                            player.sendMessage(languageManager.getMessage("help-command", "/sleep speed <速度>"));
                            player.sendMessage(languageManager.getMessage("help-command", "/sleep delay <秒数>"));
                            player.sendMessage(languageManager.getMessage("help-command", "/sleep reload"));
                        }
                        player.sendMessage(languageManager.getMessage("help-command", "/sleep help"));
                        plugin.getLogger().info("玩家 " + player.getName() + " 查看了帮助");
                        return true;
                    }
                }

                // 如果没有匹配的命令，显示帮助
                player.sendMessage(languageManager.getMessage("help-header"));
                player.sendMessage(languageManager.getMessage("help-command", "/sleep"));
                if (player.hasPermission("advancedsleep.admin")) {
                    player.sendMessage(languageManager.getMessage("help-command", "/sleep toggle"));
                    player.sendMessage(languageManager.getMessage("help-command", "/sleep percentage <百分比>"));
                    player.sendMessage(languageManager.getMessage("help-command", "/sleep acceleration <on|off>"));
                    player.sendMessage(languageManager.getMessage("help-command", "/sleep speed <速度>"));
                    player.sendMessage(languageManager.getMessage("help-command", "/sleep delay <秒数>"));
                    player.sendMessage(languageManager.getMessage("help-command", "/sleep reload"));
                }
                player.sendMessage(languageManager.getMessage("help-command", "/sleep help"));
                return true;
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "处理命令时发生错误", e);
            sender.sendMessage("§c处理命令时发生错误，请查看控制台日志。");
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        try {
            List<String> completions = new ArrayList<>();

            if (cmd.getName().equalsIgnoreCase("sleep")) {
                if (args.length == 1) {
                    // 第一级补全：子命令
                    List<String> subCommands = Arrays.asList("toggle", "percentage", "acceleration", "speed", "delay", "reload", "help");
                    for (String sub : subCommands) {
                        if (sub.startsWith(args[0].toLowerCase()) &&
                                (sender.hasPermission("advancedsleep.admin") || sub.equals("help"))) {
                            completions.add(sub);
                        }
                    }
                } else if (args.length == 2) {
                    // 第二级补全：根据第一个参数提供建议
                    switch (args[0].toLowerCase()) {
                        case "acceleration":
                            completions.add("on");
                            completions.add("off");
                            break;
                        case "percentage":
                            completions.add("0.25");
                            completions.add("0.5");
                            completions.add("0.75");
                            completions.add("1.0");
                            break;
                        case "speed":
                            completions.add("5");
                            completions.add("10");
                            completions.add("20");
                            completions.add("50");
                            break;
                        case "delay":
                            completions.add("0");
                            completions.add("3");
                            completions.add("5");
                            completions.add("10");
                            break;
                    }
                }
            }

            return completions;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "处理Tab补全时发生错误", e);
            return new ArrayList<>();
        }
    }
}