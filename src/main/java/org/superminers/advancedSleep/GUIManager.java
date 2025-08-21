package org.superminers.advancedSleep;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class GUIManager implements Listener {
    private final AdvancedSleep plugin;
    private final ConfigManager configManager;
    private final LanguageManager languageManager;
    private Map<UUID, Inventory> playerGuis;

    public GUIManager(AdvancedSleep plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.languageManager = plugin.getLanguageManager();
        this.playerGuis = new HashMap<>();
    }

    public void openSleepGUI(Player player) {
        try {
            // 检查是否已经有打开的GUI
            if (playerGuis.containsKey(player.getUniqueId())) {
                // 刷新已有的GUI
                refreshPlayerGUI(player);
                return;
            }

            // 创建GUI界面
            Inventory gui = Bukkit.createInventory(null, 27, languageManager.getMessage("gui-title"));

            // 设置GUI内容
            setupGUIItems(gui);

            // 保存GUI引用
            playerGuis.put(player.getUniqueId(), gui);

            // 打开GUI
            player.openInventory(gui);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "打开GUI时发生错误", e);
            player.sendMessage("§c打开GUI时发生错误，请查看控制台日志。");
        }
    }

    private void setupGUIItems(Inventory gui) {
        try {
            // 清除现有内容
            gui.clear();

            // 添加模式切换按钮
            ItemStack modeItem = new ItemStack(configManager.isSinglePlayerSleep() ? Material.GREEN_BED : Material.RED_BED);
            ItemMeta modeMeta = modeItem.getItemMeta();
            modeMeta.setDisplayName(languageManager.getMessage("gui-mode-name"));
            modeMeta.setLore(Arrays.asList(
                    languageManager.getMessage("gui-mode-current",
                            configManager.isSinglePlayerSleep() ?
                                    languageManager.getMessage("single-sleep") :
                                    languageManager.getMessage("percentage-sleep", (int)(configManager.getSleepPercentage() * 100))),
                    "",
                    languageManager.getMessage("gui-click-to-toggle")
            ));
            modeItem.setItemMeta(modeMeta);
            gui.setItem(11, modeItem);

            // 添加时间加速按钮
            ItemStack accelItem = new ItemStack(configManager.isTimeAccelerationEnabled() ? Material.CLOCK : Material.BARRIER);
            ItemMeta accelMeta = accelItem.getItemMeta();
            accelMeta.setDisplayName(languageManager.getMessage("gui-acceleration-name"));
            accelMeta.setLore(Arrays.asList(
                    languageManager.getMessage("gui-acceleration-current",
                            configManager.isTimeAccelerationEnabled() ?
                                    languageManager.getMessage("enabled") :
                                    languageManager.getMessage("disabled")),
                    languageManager.getMessage("gui-acceleration-speed", configManager.getAccelerationSpeed()),
                    "",
                    languageManager.getMessage("gui-click-to-toggle")
            ));
            accelItem.setItemMeta(accelMeta);
            gui.setItem(13, accelItem);

            // 添加跳过延迟按钮
            ItemStack delayItem = new ItemStack(Material.REPEATER);
            ItemMeta delayMeta = delayItem.getItemMeta();
            delayMeta.setDisplayName(languageManager.getMessage("gui-delay-name"));
            delayMeta.setLore(Arrays.asList(
                    languageManager.getMessage("gui-delay-current", configManager.getSkipDelay()),
                    "",
                    languageManager.getMessage("gui-click-to-change")
            ));
            delayItem.setItemMeta(delayMeta);
            gui.setItem(15, delayItem);

            // 添加信息按钮
            ItemStack infoItem = new ItemStack(Material.BOOK);
            ItemMeta infoMeta = infoItem.getItemMeta();
            infoMeta.setDisplayName(languageManager.getMessage("gui-info-name"));
            infoMeta.setLore(Arrays.asList(
                    languageManager.getMessage("gui-info-version", "1.0.0"),
                    languageManager.getMessage("gui-info-author", "EthanAxe"),
                    "",
                    languageManager.getMessage("gui-info-feature1"),
                    languageManager.getMessage("gui-info-feature2")
            ));
            infoItem.setItemMeta(infoMeta);
            gui.setItem(16, infoItem);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "设置GUI物品时发生错误", e);
        }
    }

    private void refreshPlayerGUI(Player player) {
        try {
            Inventory gui = playerGuis.get(player.getUniqueId());
            if (gui != null && player.getOpenInventory().getTopInventory().equals(gui)) {
                setupGUIItems(gui);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "刷新玩家GUI时发生错误", e);
        }
    }

    public void refreshAllOpenGuis() {
        try {
            for (Map.Entry<UUID, Inventory> entry : playerGuis.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline() &&
                        player.getOpenInventory().getTopInventory().equals(entry.getValue())) {
                    setupGUIItems(entry.getValue());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "刷新所有GUI时发生错误", e);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getInventory();
            ItemStack clickedItem = event.getCurrentItem();

            // 检查是否是我们的GUI
            if (!event.getView().getTitle().equals(languageManager.getMessage("gui-title"))) {
                return;
            }

            event.setCancelled(true); // 防止玩家移动物品

            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }

            String itemName = clickedItem.getItemMeta().getDisplayName();

            // 处理模式切换按钮点击
            if (itemName.equals(languageManager.getMessage("gui-mode-name")) && player.hasPermission("advancedsleep.admin")) {
                configManager.setSinglePlayerSleep(!configManager.isSinglePlayerSleep());

                String mode = configManager.isSinglePlayerSleep() ?
                        languageManager.getMessage("single-sleep") :
                        languageManager.getMessage("percentage-sleep", (int)(configManager.getSleepPercentage() * 100));
                player.sendMessage(languageManager.getMessage("toggle-mode", mode));

                // 刷新当前GUI
                setupGUIItems(inventory);
            }
            // 处理时间加速按钮点击
            else if (itemName.equals(languageManager.getMessage("gui-acceleration-name")) && player.hasPermission("advancedsleep.admin")) {
                configManager.setTimeAccelerationEnabled(!configManager.isTimeAccelerationEnabled());

                String status = configManager.isTimeAccelerationEnabled() ?
                        languageManager.getMessage("enabled") : languageManager.getMessage("disabled");
                player.sendMessage(languageManager.getMessage("toggle-acceleration", status));

                // 刷新当前GUI
                setupGUIItems(inventory);
            }
            // 处理跳过延迟按钮点击
            else if (itemName.equals(languageManager.getMessage("gui-delay-name")) && player.hasPermission("advancedsleep.admin")) {
                player.sendMessage(languageManager.getMessage("set-delay-prompt"));
                player.sendMessage(languageManager.getMessage("set-delay-usage"));
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "处理库存点击事件时发生错误", e);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        try {
            if (!(event.getPlayer() instanceof Player)) return;

            Player player = (Player) event.getPlayer();

            // 检查是否是我们的GUI
            if (!event.getView().getTitle().equals(languageManager.getMessage("gui-title"))) {
                return;
            }

            // 从缓存中移除GUI
            playerGuis.remove(player.getUniqueId());

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "处理库存关闭事件时发生错误", e);
        }
    }

    public void removePlayerGUI(Player player) {
        try {
            playerGuis.remove(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "移除玩家GUI缓存时发生错误", e);
        }
    }

    public void clearAllGuis() {
        try {
            playerGuis.clear();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "清除所有GUI缓存时发生错误", e);
        }
    }
}