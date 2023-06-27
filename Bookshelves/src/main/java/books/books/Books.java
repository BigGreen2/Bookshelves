package books.books;

import com.dndcraft.atlas.Atlas;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;

public final class Books extends JavaPlugin implements Listener {
    private Connection connection;

    private static final String DATABASE_URL = "jdbc:sqlite:bookshelves2.db";

    public static void createBookshelvesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Bookshelves2 (" +
                "Location TEXT NOT NULL," +
                "Inventory TEXT NOT NULL," +
                "Slot INT NOT NULL" +
                ");";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Bookshelves table created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void addBookshelfEntry(String location, String inventory, int slot) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String sqlCommand = "INSERT INTO Bookshelves2 (Location, Inventory, Slot) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sqlCommand);
            statement.setString(1, location);
            statement.setString(2, inventory);
            statement.setInt(3, slot);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEntriesWithLocation(String location) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String deleteQuery = "DELETE FROM Bookshelves2 WHERE Location = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(deleteQuery);
            preparedStatement.setString(1, location);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any errors that may occur
        }
    }

    @Override
    public void onEnable() {
        // Registers the listener
        getServer().getPluginManager().registerEvents(this, this);

        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            getLogger().info("Database connection established.");
        } catch (SQLException e) {
            getLogger().severe("Failed to establish database connection: " + e.getMessage());
            // Handle the exception appropriately
        }

        createBookshelvesTable();
        addBookshelfEntry("homo", "deez", 0);

    }


    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            getLogger().severe("Failed to close database connection: " + e.getMessage());
            // Handle the exception appropriately
        }
    }



    public void sendBookshelfEntries(Player player) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Bookshelves2");

            StringBuilder message = new StringBuilder();
            while (resultSet.next()) {
                String location = resultSet.getString("Location");
                String inventory = resultSet.getString("Inventory");
                int slot = resultSet.getInt("Slot");
                message.append("Location: ").append(location).append(", Inventory: ").append(inventory)
                        .append(", Slot: ").append(slot).append("\n");
            }

            player.sendMessage(message.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteBookshelfEntriesByLocationAndSlot(String location, int slot) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String sqlCommand = "DELETE FROM Bookshelves2 WHERE Location = ? AND Slot = ?";
            PreparedStatement statement = connection.prepareStatement(sqlCommand);
            statement.setString(1, location);
            statement.setInt(2, slot);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *Location: "x: 1, y: 2, z:3"
     * Inventory:
     **/

    public void updateBookshelfInventory(String location, String inventory, int slot) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String sqlCommand = "UPDATE Bookshelves2 SET Inventory = ? WHERE Location = ? AND Slot = ?";
            PreparedStatement statement = connection.prepareStatement(sqlCommand);
            statement.setString(1, inventory);
            statement.setString(2, location);
            statement.setInt(3, slot);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean entryExists(String location) {
        String sql = "SELECT COUNT(*) FROM Bookshelves2 WHERE Location = ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, location);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block blockAgainst = event.getBlockAgainst();

        // Check if the block the player is placing against is a bookshelf
        if (blockAgainst.getType() == Material.BOOKSHELF) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();



        if (clickedInventory != null) {
            if (event.getView().getTitle().equals("Bookshelf Inventory") && clickedItem.getType() == Material.DIAMOND) {
                event.setCancelled(true);
                player.closeInventory();
                player.sendRawMessage("BookName");
                player.sendRawMessage("101011");
            }
        }
    }


    @EventHandler
    public void bookRight(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        Block clickedBlock = event.getClickedBlock();

        // Check if the player is holding an item and right-clicked on a bookshelf block
        if (clickedBlock.getType().equals(Material.BOOKSHELF) && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            if (!heldItem.getType().isAir() && clickedBlock != null) { // Player is holding something, add it to the table
                // Add the item into the database
                String location = "x: " + clickedBlock.getX() + ", y: " + clickedBlock.getY() + ", z: " + clickedBlock.getZ();
                String serial = serializeItemStack(heldItem);
                int slot = 0;
                if (entryExists(location)) {
                    slot = findMaxSlotByLocation(location);
                    if (slot > 16) {
                        player.sendRawMessage("There are only 18 slots per location. 17 is the index for the last one. If it is 17, that means it is full.");
                    } else {
                        slot++;
                        addBookshelfEntry(location, serial, slot);
                        player.getInventory().remove(heldItem);
                    }
                } else {
                    addBookshelfEntry(location, serial, slot);
                    player.getInventory().remove(heldItem);
                }
            } else { // Player is not holding anything, now they VIEW
                // Retrieve all inventories for the specified location
                String location = "x: " + clickedBlock.getX() + ", y: " + clickedBlock.getY() + ", z: " + clickedBlock.getZ();
                ArrayList<String> inventories = getBookshelfInventoryByLocation(location);

                // Create inventory GUI
                Inventory inventoryGUI = Bukkit.createInventory(null, 18, "Bookshelf Inventory");

                // Add items to the GUI
                for (String inventory : inventories) {
                    ItemStack itemStack = deserializeItemStack(inventory);
                    if (itemStack != null) {
                        inventoryGUI.addItem(itemStack);
                    }
                }

                // Open the GUI for the player
                player.openInventory(inventoryGUI);
            }



        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        if (block.getType().equals(Material.BOOKSHELF)) {
            String locationSerial = "x: " + block.getX() + ", y: " + block.getY() + ", z: " + block.getZ();
            if (entryExists(locationSerial)) {
                ArrayList<String> items = getBookshelfInventoryByLocation(locationSerial);

                for (int i = 0 ; i < items.size(); i++) {
                    ItemStack item = deserializeItemStack(items.get(i));
                    player.getWorld().dropItemNaturally(block.getLocation(), item);
                }
                deleteEntriesWithLocation(locationSerial);
            }
        }
    }
    public ArrayList<String> getBookshelfInventoryByLocation(String location) {
        ArrayList<String> inventories = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String sqlCommand = "SELECT Inventory FROM Bookshelves2 WHERE Location = ? ORDER BY Slot ASC";
            PreparedStatement statement = connection.prepareStatement(sqlCommand);
            statement.setString(1, location);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String inventory = resultSet.getString("Inventory");
                inventories.add(inventory);
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inventories;
    }

    public int findMaxSlotByLocation(String location) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String sqlCommand = "SELECT MAX(Slot) FROM Bookshelves2 WHERE Location = ?";
            PreparedStatement statement = connection.prepareStatement(sqlCommand);
            statement.setString(1, location);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return a default value (e.g., -1) if no entry found or an error occurred
        return -1;
    }

    public static void deleteAllEntries() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {

            String sql = "DELETE FROM Bookshelves2";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        String failmessage = "YOU SUCK!";

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equals("seesql")) {
                sendBookshelfEntries(player);
            }
            if (label.equals("addsql")) {
                if (args.length == 2) {
                    int slot = 0;

                    if (entryExists(args[0])) {
                        slot = findMaxSlotByLocation(args[0]);
                        if (slot > 16) {
                            player.sendRawMessage("There are only 18 slots per location. 17 is the index for the last one. If it is 17, that means it is full.");
                        }
                        else {
                            slot++;
                            addBookshelfEntry(args[0], args[1], slot);
                        }
                    }
                    else {
                        addBookshelfEntry(args[0], args[1], slot);
                    }
                }
                else {
                    player.sendRawMessage(failmessage);
                }
            }
            if (label.equals("deletesql")) {
                if (args.length == 2) {
                    deleteBookshelfEntriesByLocationAndSlot(args[0], Integer.parseInt(args[1]));
                }
                else {
                    player.sendRawMessage(failmessage);
                }
            }

            if (label.equals("serializeitem")) {
                ItemStack item = player.getInventory().getItemInMainHand();
                String serial = serializeItemStack(item);
                player.sendRawMessage(serial);
            }

            if (label.equals("deserializeitem")) {
                if (args.length == 1) {
                    String serial = args[0];
                    ItemStack item = deserializeItemStack(serial);
                    player.getInventory().addItem(item);
                }
                else {
                    player.sendRawMessage(failmessage);
                }
            }

            if (label.equals("deleteloc")) {
                if (args.length == 0) {
                    deleteAllEntries();
                }
                else {
                    player.sendRawMessage(failmessage);
                }
            }

        }
        return false;
    }

    private static String serializeItemStack(ItemStack itemStack) {
        StringBuilder builder = new StringBuilder();

        // Serialize material
        builder.append(itemStack.getType().toString());

        // Serialize amount
        builder.append(":").append(itemStack.getAmount());

        // Serialize durability
        builder.append(":").append(itemStack.getDurability());

        // Serialize item meta
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            // Serialize display name
            if (itemMeta.hasDisplayName()) {
                String displayName = itemMeta.getDisplayName().replace(" ", "&s");
                builder.append(":n").append(displayName);
            }

            // Serialize lore
            if (itemMeta.hasLore()) {
                for (String line : itemMeta.getLore()) {
                    String serializedLine = line.replace(" ", "&s");
                    builder.append(":l").append(serializedLine);
                }
            }

            // Serialize enchantments separately
            if (itemMeta.hasEnchants()) {
                for (Enchantment enchantment : itemMeta.getEnchants().keySet()) {
                    int level = itemMeta.getEnchantLevel(enchantment);
                    String enchantmentKey = enchantment.getKey().getKey();
                    builder.append(":e").append(enchantmentKey).append(",").append(level);
                }
            }

            // ... Serialize any other item meta properties you want to include ...
        }

        return builder.toString();
    }

    private static ItemStack deserializeItemStack(String serialized) {
        String[] parts = serialized.split(":");
        if (parts.length < 3) {
            // Invalid serialization format
            return null;
        }

        Material material = Material.getMaterial(parts[0]);
        if (material == null) {
            // Invalid material
            return null;
        }

        int amount = Integer.parseInt(parts[1]);
        short durability = Short.parseShort(parts[2]);

        ItemStack itemStack = new ItemStack(material, amount, durability);
        ItemMeta itemMeta = itemStack.getItemMeta();

        for (int i = 3; i < parts.length; i++) {
            String part = parts[i];

            if (part.startsWith("n")) {
                // Deserialize display name
                String displayName = part.substring(1).replace("&s", " ");
                itemMeta.setDisplayName(displayName);
            } else if (part.startsWith("l")) {
                // Deserialize lore
                String loreLine = part.substring(1).replace("&s", " ");
                if (!itemMeta.hasLore()) {
                    itemMeta.setLore(new ArrayList<>());
                }
                itemMeta.getLore().add(loreLine);
            } else if (part.startsWith("e")) {
                // Deserialize enchantments
                String[] enchantmentParts = part.substring(1).split(",");
                if (enchantmentParts.length != 2) {
                    continue; // Invalid enchantment format, skip
                }
                String enchantmentKey = enchantmentParts[0];
                int level = Integer.parseInt(enchantmentParts[1]);
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentKey));
                if (enchantment != null) {
                    itemMeta.addEnchant(enchantment, level, true);
                }
            }

            // ... Deserialize any other item meta properties you included ...
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }



}