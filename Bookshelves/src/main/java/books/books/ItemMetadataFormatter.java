package books.books;

import com.dndcraft.atlas.Atlas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ItemMetadataFormatter {
    private Gson gson;

    public ItemMetadataFormatter() {
        // Create Gson instance with pretty printing enabled
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public String formatItemMetadata(ItemStack itemStack) {
        // Serialize the item metadata to JSON string
        String json = gson.toJson(itemStack);

        // Return the JSON string
        return json;
    }

    public ItemStack parseItemMetadata(String metadata) {
        // Deserialize the JSON string to ItemStack object
        ItemStack itemStack = gson.fromJson(metadata, ItemStack.class);

        // Return the ItemStack object
        return itemStack;
    }
}
