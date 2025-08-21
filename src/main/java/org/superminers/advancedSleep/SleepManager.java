package org.superminers.advancedSleep;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

public class SleepManager implements Listener {
    private final AdvancedSleep plugin;
    private final ConfigManager configManager;
    private final LanguageManager languageManager;
    private final SleepCalculator sleepCalculator;

    // 使用玩家名而不是UUID来跟踪睡觉玩家
    private Set<String> sleepingPlayers;
    private int timeAccelerationTask = -1;
    private int timeDisplayTask = -1;
    private Map<World, Integer> pendingSkipTasks;

    public SleepManager(AdvancedSleep plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.languageManager = plugin.getLanguageManager();
        this.sleepCalculator = new SleepCalculator(plugin);

        this.sleepingPlayers = new HashSet<>();
        this.pendingSkipTasks = new HashMap<>();
    }

    public void onDisable() {
        // 取消所有任务
        cancelAllTasks();

        // 清空所有列表
        sleepingPlayers.clear();
        pendingSkipTasks.clear();
    }

    private void cancelAllTasks() {
        if (timeAccelerationTask != -1) {
            Bukkit.getScheduler().cancelTask(timeAccelerationTask);
            timeAccelerationTask = -1;
        }

        if (timeDisplayTask != -1) {
            Bukkit.getScheduler().cancelTask(timeDisplayTask);
            timeDisplayTask = -1;
        }

        // 取消所有待处理的跳过任务
        for (Map.Entry<World, Integer> entry : pendingSkipTasks.entrySet()) {
            if (entry.getValue() != null) {
                Bukkit.getScheduler().cancelTask(entry.getValue());
            }
        }
        pendingSkipTasks.clear();
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        try {
            Player player = event.getPlayer();
            World world = player.getWorld();

            // 检查世界是否启用
            if (!configManager.getEnabledWorlds().contains(world.getName())) {
                player.sendMessage(languageManager.getMessage("world-disabled"));
                event.setCancelled(true);
                return;
            }

            // 检查是否是夜晚
            if (!isNight(world)) {
                player.sendMessage(languageManager.getMessage("not-night"));
                event.setCancelled(true);
                return;
            }

            // 检查玩家是否有权限使用睡眠功能
            if (!player.hasPermission("advancedsleep.use")) {
                player.sendMessage(languageManager.getMessage("no-permission"));
                event.setCancelled(true);
                return;
            }

            // 检查玩家是否忽略睡觉
            if (player.isSleepingIgnored()) {
                player.sendMessage(languageManager.getMessage("sleep-ignored"));
                event.setCancelled(true);
                return;
            }

            // 添加玩家到睡眠列表
            sleepingPlayers.add(player.getName());

            // 检查是否可以跳过夜晚
            checkSleepConditions(world);

            // 如果启用时间加速，启动加速任务
            if (configManager.isTimeAccelerationEnabled() && timeAccelerationTask == -1) {
                startTimeAcceleration(world);
            }

            // 广播消息
            int sleepingCount = sleepingPlayers.size();
            int required = sleepCalculator.calculateRequiredSleepers(world, sleepingPlayers);
            String message = languageManager.getMessage("player-sleeping",
                    player.getName(), sleepingCount, required);
            Bukkit.broadcastMessage(message);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "处理玩家上床事件时发生错误", e);
        }
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        try {
            Player player = event.getPlayer();
            World world = player.getWorld();

            // 检查世界是否启用
            if (!configManager.getEnabledWorlds().contains(world.getName())) {
                return;
            }

            // 从睡眠列表中移除玩家
            if (sleepingPlayers.remove(player.getName())) {
                // 取消该世界的跳过任务（如果有）
                cancelPendingSkip(world);

                // 如果没有玩家睡觉，取消时间加速
                if (sleepingPlayers.isEmpty()) {
                    cancelAllTasks();
                } else {
                    // 如果还有玩家在睡觉，重新检查睡眠条件
                    checkSleepConditions(world);
                }

                // 广播消息
                int sleepingCount = sleepingPlayers.size();
                int required = sleepCalculator.calculateRequiredSleepers(world, sleepingPlayers);
                String message = languageManager.getMessage("player-wakeup",
                        player.getName(), sleepingCount, required);
                Bukkit.broadcastMessage(message);
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "处理玩家起床事件时发生错误", e);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            Player player = event.getPlayer();
            World world = player.getWorld();

            // 检查世界是否启用
            if (!configManager.getEnabledWorlds().contains(world.getName())) {
                return;
            }

            // 从睡眠列表中移除玩家
            if (sleepingPlayers.remove(player.getName())) {
                // 取消该世界的跳过任务（如果有）
                cancelPendingSkip(world);

                // 如果没有玩家睡觉，取消时间加速
                if (sleepingPlayers.isEmpty()) {
                    cancelAllTasks();
                } else {
                    // 如果还有玩家在睡觉，重新检查睡眠条件
                    checkSleepConditions(world);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "处理玩家退出事件时发生错误", e);
        }
    }

    private void checkSleepConditions(World world) {
        try {
            boolean canSkip = sleepCalculator.canSkipNight(world, sleepingPlayers);

            if (canSkip) {
                // 如果有延迟，安排延迟跳过
                if (configManager.getSkipDelay() > 0) {
                    scheduleDelayedSkip(world);
                } else {
                    // 没有延迟，立即跳过
                    skipNight(world);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "检查睡眠条件时发生错误", e);
        }
    }

    private void scheduleDelayedSkip(World world) {
        try {
            // 取消现有的跳过任务（如果有）
            cancelPendingSkip(world);

            // 广播跳过倒计时
            String message = languageManager.getMessage("skip-countdown-start", configManager.getSkipDelay());
            Bukkit.broadcastMessage(message);

            // 安排延迟跳过任务
            int taskId = new BukkitRunnable() {
                int countdown = configManager.getSkipDelay();

                @Override
                public void run() {
                    try {
                        if (countdown <= 0) {
                            // 检查是否仍然满足条件
                            if (sleepCalculator.canSkipNight(world, sleepingPlayers)) {
                                skipNight(world);
                            } else {
                                String cancelMessage = languageManager.getMessage("skip-cancelled");
                                Bukkit.broadcastMessage(cancelMessage);
                            }
                            pendingSkipTasks.remove(world);
                            this.cancel();
                            return;
                        }

                        // 每秒广播倒计时
                        if (countdown <= 5 || countdown % 5 == 0) {
                            String countdownMessage = languageManager.getMessage("skip-countdown", countdown);
                            Bukkit.broadcastMessage(countdownMessage);
                        }

                        countdown--;
                    } catch (Exception e) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 20).getTaskId();

            // 保存任务ID以便可以取消
            pendingSkipTasks.put(world, taskId);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "安排延迟跳过时发生错误", e);
        }
    }

    private void cancelPendingSkip(World world) {
        try {
            Integer taskId = pendingSkipTasks.get(world);
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
                pendingSkipTasks.remove(world);
                String message = languageManager.getMessage("skip-cancelled");
                Bukkit.broadcastMessage(message);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "取消跳过任务时发生错误", e);
        }
    }

    private void skipNight(World world) {
        try {
            // 取消该世界的跳过任务（如果有）
            cancelPendingSkip(world);

            // 设置时间为早上
            world.setTime(0);

            // 天气控制
            world.setStorm(false);
            world.setThundering(false);

            // 唤醒所有玩家
            for (String playerName : sleepingPlayers) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null && player.isSleeping()) {
                    player.wakeup(false);
                }
            }

            sleepingPlayers.clear();

            // 取消时间加速和时间显示任务
            cancelAllTasks();

            // 广播消息
            String message = languageManager.getMessage("morning-come");
            Bukkit.broadcastMessage(message);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "跳过夜晚时发生错误", e);
        }
    }

    private void startTimeAcceleration(World world) {
        try {
            timeAccelerationTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (sleepingPlayers.isEmpty()) {
                            this.cancel();
                            timeAccelerationTask = -1;
                            return;
                        }

                        // 加速时间流逝
                        long currentTime = world.getTime();
                        world.setTime(currentTime + configManager.getAccelerationSpeed());

                        // 检查是否已经是早上了
                        if (!isNight(world)) {
                            skipNight(world);
                            this.cancel();
                            timeAccelerationTask = -1;
                        }
                    } catch (Exception e) {
                        this.cancel();
                        timeAccelerationTask = -1;
                    }
                }
            }.runTaskTimer(plugin, 0, 1).getTaskId();

            // 启动时间显示任务
            startTimeDisplay(world);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "启动时间加速时发生错误", e);
        }
    }

    private void startTimeDisplay(World world) {
        try {
            if (timeDisplayTask != -1) {
                Bukkit.getScheduler().cancelTask(timeDisplayTask);
            }

            // 如果不显示时间，则不启动任务
            if (!configManager.isShowTimeDuringAcceleration()) {
                return;
            }

            timeDisplayTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (sleepingPlayers.isEmpty()) {
                            this.cancel();
                            timeDisplayTask = -1;
                            return;
                        }

                        // 为所有在线玩家显示时间
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (configManager.getEnabledWorlds().contains(player.getWorld().getName())) {
                                displayFormattedTime(player, world.getTime());
                            }
                        }
                    } catch (Exception e) {
                        this.cancel();
                        timeDisplayTask = -1;
                    }
                }
            }.runTaskTimer(plugin, 0, 5).getTaskId(); // 每5 ticks更新一次时间显示

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "启动时间显示时发生错误", e);
        }
    }

    private void displayFormattedTime(Player player, long minecraftTime) {
        try {
            // 将Minecraft时间转换为24小时制时间
            String formattedTime = formatMinecraftTime(minecraftTime);

            // 获取时间显示格式
            String timeFormat = languageManager.getMessage("time-format");

            // 在玩家的操作栏上显示时间
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(timeFormat.replace("{0}", formattedTime))
            );
        } catch (Exception e) {
            // 静默处理显示错误
        }
    }

    private String formatMinecraftTime(long minecraftTime) {
        // Minecraft时间转换为现实时间（24小时制）
        // Minecraft时间0对应现实时间6:00
        // 每1000 Minecraft ticks对应1现实小时

        // 计算总分钟数
        long totalMinutes = (minecraftTime + 6000) % 24000; // 调整到从6:00开始
        int hours = (int) (totalMinutes / 1000) % 24;
        int minutes = (int) ((totalMinutes % 1000) * 60 / 1000);

        // 格式化为24小时制时间
        return String.format("%02d:%02d", hours, minutes);
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time > 12500 && time < 23450;
    }

    public void reload() {
        sleepingPlayers.clear();
        pendingSkipTasks.clear();
        cancelAllTasks();
    }
}