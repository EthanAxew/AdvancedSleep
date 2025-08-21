package org.superminers.advancedSleep;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

public class SleepCalculator {
    private final ConfigManager configManager;

    public SleepCalculator(AdvancedSleep plugin) {
        this.configManager = plugin.getConfigManager();
    }

    /**
     * 计算世界中有效的玩家数量（有权限且不忽略睡觉的玩家）
     */
    public int getValidPlayerCount(World world) {
        int count = 0;
        for (Player player : world.getPlayers()) {
            if (isValidSleepingPlayer(player)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 检查玩家是否是有效的睡眠玩家
     */
    public boolean isValidSleepingPlayer(Player player) {
        return player.hasPermission("advancedsleep.use") &&
                !player.isSleepingIgnored() &&
                player.isOnline();
    }

    /**
     * 计算需要多少玩家睡觉才能跳过夜晚
     */
    public int calculateRequiredSleepers(World world, Set<String> sleepingPlayers) {
        if (configManager.isSinglePlayerSleep()) {
            return 1;
        }

        int validPlayerCount = getValidPlayerCount(world);
        double sleepPercentage = configManager.getSleepPercentage();

        // 计算需要睡觉的玩家数量（向上取整）
        int required = (int) Math.ceil(validPlayerCount * sleepPercentage);

        // 确保至少需要1个玩家睡觉
        return Math.max(1, required);
    }

    /**
     * 检查是否满足跳过夜晚的条件
     */
    public boolean canSkipNight(World world, Set<String> sleepingPlayers) {
        try {
            int required = calculateRequiredSleepers(world, sleepingPlayers);
            return sleepingPlayers.size() >= required;
        } catch (Exception e) {
            return false;
        }
    }
}