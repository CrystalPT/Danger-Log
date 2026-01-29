package secret.dangerLog.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import secret.dangerLog.DangerLog;

import java.io.*;
import java.util.*;
import java.util.Base64;

public class CombatManager {
    
    private final DangerLog plugin;
    private final File zombieDataFile;
    private FileConfiguration zombieData;
    
    private final Map<UUID, Integer> combatTimers = new HashMap<>();
    private final Map<UUID, Boolean> inCombatRange = new HashMap<>();
    private final Map<UUID, Zombie> combatLogZombies = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, Boolean> zombieKilled = new HashMap<>();
    private final Map<UUID, String> zombieKillers = new HashMap<>();
    private final Set<UUID> suppressDeathMessage = new HashSet<>();
    private final Map<UUID, Long> zombieSpawnTimes = new HashMap<>();
    
    private BukkitTask timerTask;
    private BukkitTask proximityTask;
    private BukkitTask zombieExpiryTask;
    
    public CombatManager(DangerLog plugin) {
        this.plugin = plugin;
        this.zombieDataFile = new File(plugin.getDataFolder(), "zombies.yml");
        loadZombieData();
    }
    
    public void start() {
        startProximityCheck();
        startTimerCountdown();
        startZombieExpiryCheck();
    }
    
    public void stop() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (proximityTask != null) {
            proximityTask.cancel();
            proximityTask = null;
        }
        if (zombieExpiryTask != null) {
            zombieExpiryTask.cancel();
            zombieExpiryTask = null;
        }
        combatTimers.clear();
        inCombatRange.clear();
    }
    
    private void loadZombieData() {
        if (!zombieDataFile.exists()) {
            zombieData = new YamlConfiguration();
            return;
        }
        
        zombieData = YamlConfiguration.loadConfiguration(zombieDataFile);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                respawnZombiesFromData();
            }
        }.runTaskLater(plugin, 20L);
    }
    
    private void respawnZombiesFromData() {
        if (!zombieData.contains("zombies")) return;
        
        for (String uuidStr : zombieData.getConfigurationSection("zombies").getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(uuidStr);
                String path = "zombies." + uuidStr;
                
                if (zombieData.getBoolean(path + ".killed", false)) {
                    zombieKilled.put(playerUUID, true);
                    zombieKillers.put(playerUUID, zombieData.getString(path + ".killer"));
                    
                    String inventoryData = zombieData.getString(path + ".inventory");
                    if (inventoryData != null) {
                        savedInventories.put(playerUUID, deserializeInventory(inventoryData));
                    }
                    continue;
                }
                
                String worldName = zombieData.getString(path + ".world");
                double x = zombieData.getDouble(path + ".x");
                double y = zombieData.getDouble(path + ".y");
                double z = zombieData.getDouble(path + ".z");
                double health = zombieData.getDouble(path + ".health");
                double maxHealth = zombieData.getDouble(path + ".maxHealth");
                String playerName = zombieData.getString(path + ".playerName");
                String inventoryData = zombieData.getString(path + ".inventory");
                
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("Could not find world " + worldName + " for combat log zombie of " + playerName);
                    continue;
                }
                
                Location loc = new Location(world, x, y, z);
                
                Zombie zombie = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
                
                zombie.setAI(false);
                zombie.setShouldBurnInDay(false);
                zombie.setRemoveWhenFarAway(false);
                zombie.setCanPickupItems(false);
                zombie.setAdult();
                
                zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                zombie.setHealth(Math.min(health, maxHealth));
                
                Component customName = Component.text(playerName, NamedTextColor.RED, TextDecoration.BOLD);
                zombie.customName(customName);
                zombie.setCustomNameVisible(true);
                
                zombie.getEquipment().clear();
                
                combatLogZombies.put(playerUUID, zombie);
                zombieKilled.put(playerUUID, false);
                
                long spawnTime = zombieData.getLong(path + ".spawnTime", System.currentTimeMillis());
                zombieSpawnTimes.put(playerUUID, spawnTime);
                
                if (inventoryData != null) {
                    savedInventories.put(playerUUID, deserializeInventory(inventoryData));
                }
                
                plugin.getLogger().info("Respawned combat log zombie for " + playerName);
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to respawn combat log zombie: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void saveZombieData() {
        zombieData = new YamlConfiguration();
        
        for (Map.Entry<UUID, Zombie> entry : combatLogZombies.entrySet()) {
            UUID playerUUID = entry.getKey();
            Zombie zombie = entry.getValue();
            
            if (zombie == null || zombie.isDead()) continue;
            
            String path = "zombies." + playerUUID.toString();
            Location loc = zombie.getLocation();
            
            zombieData.set(path + ".world", loc.getWorld().getName());
            zombieData.set(path + ".x", loc.getX());
            zombieData.set(path + ".y", loc.getY());
            zombieData.set(path + ".z", loc.getZ());
            zombieData.set(path + ".health", zombie.getHealth());
            zombieData.set(path + ".maxHealth", zombie.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
            zombieData.set(path + ".playerName", Bukkit.getOfflinePlayer(playerUUID).getName());
            zombieData.set(path + ".killed", false);
            zombieData.set(path + ".spawnTime", zombieSpawnTimes.getOrDefault(playerUUID, System.currentTimeMillis()));
            
            ItemStack[] inventory = savedInventories.get(playerUUID);
            if (inventory != null) {
                zombieData.set(path + ".inventory", serializeInventory(inventory));
            }
        }
        
        for (Map.Entry<UUID, Boolean> entry : zombieKilled.entrySet()) {
            if (!entry.getValue()) continue;
            
            UUID playerUUID = entry.getKey();
            if (combatLogZombies.containsKey(playerUUID)) continue;
            
            String path = "zombies." + playerUUID.toString();
            zombieData.set(path + ".killed", true);
            zombieData.set(path + ".killer", zombieKillers.get(playerUUID));
            zombieData.set(path + ".playerName", Bukkit.getOfflinePlayer(playerUUID).getName());
            
            ItemStack[] inventory = savedInventories.get(playerUUID);
            if (inventory != null) {
                zombieData.set(path + ".inventory", serializeInventory(inventory));
            }
        }
        
        try {
            zombieData.save(zombieDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save zombie data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String serializeInventory(ItemStack[] inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeInt(inventory.length);
            for (ItemStack item : inventory) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to serialize inventory: " + e.getMessage());
            return null;
        }
    }
    
    private ItemStack[] deserializeInventory(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            int size = dataInput.readInt();
            ItemStack[] inventory = new ItemStack[size];
            
            for (int i = 0; i < size; i++) {
                inventory[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            
            return inventory;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to deserialize inventory: " + e.getMessage());
            return null;
        }
    }
    
    private void startProximityCheck() {
        proximityTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfigManager().isEnabled()) return;
                
                int radius = plugin.getConfigManager().getRadius();
                int timerDuration = plugin.getConfigManager().getTimerDuration();
                boolean allowAllies = plugin.getConfigManager().isAllowAllies();
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isPermissionSet("dangerlog.bypass") && player.hasPermission("dangerlog.bypass")) continue;
                    if (player.getGameMode() != GameMode.SURVIVAL) continue;
                    
                    boolean nearEnemy = false;
                    
                    for (Player other : Bukkit.getOnlinePlayers()) {
                        if (other.equals(player)) continue;
                        if (other.isPermissionSet("dangerlog.bypass") && other.hasPermission("dangerlog.bypass")) continue;
                        if (other.getGameMode() != GameMode.SURVIVAL) continue;
                        if (!player.getWorld().equals(other.getWorld())) continue;
                        
                        if (allowAllies && plugin.getAllyManager().areAllies(player.getUniqueId(), other.getUniqueId())) {
                            continue;
                        }
                        
                        double distance = player.getLocation().distance(other.getLocation());
                        if (distance <= radius) {
                            nearEnemy = true;
                            break;
                        }
                    }
                    
                    UUID playerUUID = player.getUniqueId();
                    boolean wasInRange = inCombatRange.getOrDefault(playerUUID, false);
                    inCombatRange.put(playerUUID, nearEnemy);
                    
                    if (nearEnemy) {
                        combatTimers.put(playerUUID, timerDuration);
                    } else if (wasInRange && !combatTimers.containsKey(playerUUID)) {
                        combatTimers.put(playerUUID, timerDuration);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
    
    private void startTimerCountdown() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfigManager().isEnabled()) return;
                
                int timerDuration = plugin.getConfigManager().getTimerDuration();
                Iterator<Map.Entry<UUID, Integer>> iterator = combatTimers.entrySet().iterator();
                
                while (iterator.hasNext()) {
                    Map.Entry<UUID, Integer> entry = iterator.next();
                    UUID playerUUID = entry.getKey();
                    int currentTimer = entry.getValue();
                    
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player == null || !player.isOnline()) {
                        iterator.remove();
                        inCombatRange.remove(playerUUID);
                        continue;
                    }
                    
                    boolean isInRange = inCombatRange.getOrDefault(playerUUID, false);
                    
                    if (isInRange) {
                        entry.setValue(timerDuration);
                        sendActionBar(player, timerDuration, timerDuration);
                    } else {
                        if (currentTimer > 0) {
                            entry.setValue(currentTimer - 1);
                            sendActionBar(player, currentTimer - 1, timerDuration);
                            
                            if (currentTimer - 1 <= 0) {
                                iterator.remove();
                                inCombatRange.remove(playerUUID);
                                player.sendActionBar(Component.empty());
                                if (plugin.getCombatRestrictionListener() != null) {
                                    plugin.getCombatRestrictionListener().clearCooldowns(player);
                                }
                            }
                        } else {
                            iterator.remove();
                            inCombatRange.remove(playerUUID);
                            player.sendActionBar(Component.empty());
                            if (plugin.getCombatRestrictionListener() != null) {
                                plugin.getCombatRestrictionListener().clearCooldowns(player);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void sendActionBar(Player player, int current, int max) {
        Component message = Component.text(current + "/" + max, NamedTextColor.RED);
        player.sendActionBar(message);
    }
    
    public boolean isInCombat(UUID playerUUID) {
        return combatTimers.containsKey(playerUUID) && combatTimers.get(playerUUID) > 0;
    }
    
    public void handleCombatLog(Player player) {
        String deathType = plugin.getConfigManager().getDeathType();
        
        // Announce combat log to server
        Bukkit.broadcast(Component.text(player.getName() + " has logged out in combat.", NamedTextColor.RED));
        
        if (deathType.equals("INSTANT")) {
            player.setHealth(0);
        } else {
            spawnCombatLogZombie(player);
        }
    }
    
    private void spawnCombatLogZombie(Player player) {
        UUID playerUUID = player.getUniqueId();
        Location loc = player.getLocation();
        
        savedInventories.put(playerUUID, player.getInventory().getContents().clone());
        
        Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        
        zombie.setAI(false);
        zombie.setShouldBurnInDay(false);
        zombie.setRemoveWhenFarAway(false);
        zombie.setCanPickupItems(false);
        zombie.setAdult();
        
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        zombie.setHealth(player.getHealth());
        
        Component customName = Component.text(player.getName(), NamedTextColor.RED, TextDecoration.BOLD);
        zombie.customName(customName);
        zombie.setCustomNameVisible(true);
        
        zombie.getEquipment().clear();
        
        combatLogZombies.put(playerUUID, zombie);
        zombieKilled.put(playerUUID, false);
        zombieSpawnTimes.put(playerUUID, System.currentTimeMillis());
        
        player.getInventory().clear();
        
        saveZombieData();
    }
    
    private void startZombieExpiryCheck() {
        zombieExpiryTask = new BukkitRunnable() {
            @Override
            public void run() {
                int zombieLogTimerMax = plugin.getConfigManager().getZombieLogTimerMax();
                
                if (zombieLogTimerMax == -1) return;
                
                long currentTime = System.currentTimeMillis();
                long maxAge = zombieLogTimerMax * 1000L;
                
                List<UUID> expiredZombies = new ArrayList<>();
                
                for (Map.Entry<UUID, Long> entry : zombieSpawnTimes.entrySet()) {
                    UUID playerUUID = entry.getKey();
                    long spawnTime = entry.getValue();
                    
                    if (currentTime - spawnTime >= maxAge) {
                        Zombie zombie = combatLogZombies.get(playerUUID);
                        if (zombie != null && !zombie.isDead()) {
                            expiredZombies.add(playerUUID);
                        }
                    }
                }
                
                for (UUID playerUUID : expiredZombies) {
                    handleZombieExpiry(playerUUID);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
    
    private void handleZombieExpiry(UUID playerUUID) {
        Zombie zombie = combatLogZombies.get(playerUUID);
        if (zombie == null || zombie.isDead()) return;
        
        String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
        Location zombieLocation = zombie.getLocation();
        
        zombie.remove();
        
        dropPlayerLoot(playerUUID, zombieLocation);
        
        // Announce zombie expiry to server
        Bukkit.broadcast(Component.text(playerName + "'s combat log zombie has expired.", NamedTextColor.RED));
        
        zombieKilled.put(playerUUID, true);
        zombieKillers.put(playerUUID, "timer expiry");
        
        combatLogZombies.remove(playerUUID);
        zombieSpawnTimes.remove(playerUUID);
        
        saveZombieData();
    }
    
    public void handleZombieDeath(Zombie zombie, String killerName) {
        UUID playerUUID = null;
        
        for (Map.Entry<UUID, Zombie> entry : combatLogZombies.entrySet()) {
            if (entry.getValue().equals(zombie)) {
                playerUUID = entry.getKey();
                break;
            }
        }
        
        if (playerUUID == null) return;
        
        Player player = Bukkit.getPlayer(playerUUID);
        String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
        
        zombieKilled.put(playerUUID, true);
        zombieKillers.put(playerUUID, killerName);
        
        // Announce death to server
        if (killerName != null) {
            Bukkit.broadcast(Component.text(playerName + " was killed by " + killerName + ".", NamedTextColor.RED));
        } else {
            Bukkit.broadcast(Component.text(playerName + " died.", NamedTextColor.RED));
        }
        
        if (player != null && player.isOnline()) {
            player.setHealth(0);
            
            combatLogZombies.remove(playerUUID);
            savedInventories.remove(playerUUID);
            zombieKilled.remove(playerUUID);
            zombieKillers.remove(playerUUID);
            zombieSpawnTimes.remove(playerUUID);
        } else {
            dropPlayerLoot(playerUUID, zombie.getLocation());
            
            combatLogZombies.remove(playerUUID);
            zombieSpawnTimes.remove(playerUUID);
        }
        
        saveZombieData();
    }
    
    private void dropPlayerLoot(UUID playerUUID, Location location) {
        ItemStack[] inventory = savedInventories.remove(playerUUID);
        
        if (inventory != null) {
            for (ItemStack item : inventory) {
                if (item != null && !item.getType().isAir()) {
                    location.getWorld().dropItemNaturally(location, item);
                }
            }
        }
    }
    
    public void handlePlayerRejoin(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        Zombie zombie = combatLogZombies.get(playerUUID);
        
        if (zombie != null && !zombie.isDead()) {
            ItemStack[] inventory = savedInventories.remove(playerUUID);
            
            if (inventory != null) {
                player.getInventory().setContents(inventory);
            }
            
            zombie.remove();
            combatLogZombies.remove(playerUUID);
            zombieKilled.remove(playerUUID);
            zombieKillers.remove(playerUUID);
            zombieSpawnTimes.remove(playerUUID);
            
            // Notify player their zombie was removed
            player.sendMessage(Component.text("You have rejoined. Your combat log zombie has been removed.", NamedTextColor.GREEN));
            
            saveZombieData();
        } else if (zombieKilled.getOrDefault(playerUUID, false)) {
            // Notify player their zombie was killed
            player.sendMessage(Component.text("Your combat log zombie was killed while you were offline!", NamedTextColor.RED));
            
            suppressDeathMessage.add(playerUUID);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setHealth(0);
                }
            }.runTaskLater(plugin, 5L);
            
            combatLogZombies.remove(playerUUID);
            savedInventories.remove(playerUUID);
            zombieKilled.remove(playerUUID);
            zombieKillers.remove(playerUUID);
            zombieSpawnTimes.remove(playerUUID);
            
            saveZombieData();
        }
    }
    
    public boolean isCombatLogZombie(Zombie zombie) {
        return combatLogZombies.containsValue(zombie);
    }
    
    public void removeCombatTimer(UUID playerUUID) {
        combatTimers.remove(playerUUID);
        inCombatRange.remove(playerUUID);
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.sendActionBar(Component.empty());
            if (plugin.getCombatRestrictionListener() != null) {
                plugin.getCombatRestrictionListener().clearCooldowns(player);
            }
        }
    }
    
    public boolean shouldSuppressDeathMessage(UUID playerUUID) {
        return suppressDeathMessage.remove(playerUUID);
    }
    
    public boolean hasNearbyEnemies(Player player) {
        if (!plugin.getConfigManager().isEnabled()) return false;
        
        int radius = plugin.getConfigManager().getRadius();
        boolean allowAllies = plugin.getConfigManager().isAllowAllies();
        
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (other.isPermissionSet("dangerlog.bypass") && other.hasPermission("dangerlog.bypass")) continue;
            if (other.getGameMode() != GameMode.SURVIVAL) continue;
            if (!player.getWorld().equals(other.getWorld())) continue;
            
            if (allowAllies && plugin.getAllyManager().areAllies(player.getUniqueId(), other.getUniqueId())) {
                continue;
            }
            
            double distance = player.getLocation().distance(other.getLocation());
            if (distance <= radius) {
                return true;
            }
        }
        return false;
    }
}
