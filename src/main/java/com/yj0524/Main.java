package com.yj0524;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.io.File;

public class Main extends JavaPlugin implements Listener {

    private HashMap<Player, Inventory> chests;
    private int chestSize;

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        // Initialize chests map
        chests = new HashMap<>();
        // Load chest size from config
        loadConfig();
        File cfile = new File(getDataFolder(), "config.yml");
        if (cfile.length() == 0) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        getLogger().info("Plugin Enabled.");
    }

    @Override
    public void onDisable() {
        // Clean up chests map
        chests.clear();
        getLogger().info("Plugin Disabled.");
    }

    private void loadConfig() {
        // Load chest size from config
        FileConfiguration config = getConfig();
        chestSize = config.getInt("chestSize", 27);
        // Save config
        config.set("chestSize", chestSize);
        saveConfig();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (chests.containsKey(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You cannot break your personal chest!");
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (chests.containsKey(event.getPlayer())) {
            // Save chest contents to config
            Player player = (Player) event.getPlayer();
            Inventory chest = chests.get(player);
            for (int i = 0; i < chest.getSize(); i++) {
                ItemStack item = chest.getItem(i);
                if (item != null) {
                    getConfig().set(player.getUniqueId() + "." + i, item);
                } else {
                    getConfig().set(player.getUniqueId() + "." + i, null);
                }
            }
            saveConfig();
            // Remove chest from map
            chests.remove(player);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("personalchest")) {
                if (chests.containsKey(player)) {
                    // Open personal chest
                    player.openInventory(chests.get(player));
                } else {
                    // Create personal chest
                    Inventory chest = Bukkit.createInventory(null, chestSize, "Personal Chest");
                    // Load chest contents from config
                    for (int i = 0; i < chestSize; i++) {
                        if (getConfig().contains(player.getUniqueId() + "." + i)) {
                            ItemStack item = getConfig().getItemStack(player.getUniqueId() + "." + i);
                            chest.setItem(i, item);
                        }
                    }
                    chests.put(player, chest);
                    player.openInventory(chest);
                }
                return true;
            }
        }
        return false;
    }
}
