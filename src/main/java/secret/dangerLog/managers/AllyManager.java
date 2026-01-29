package secret.dangerLog.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import secret.dangerLog.DangerLog;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AllyManager {
    
    private final DangerLog plugin;
    private final File allyFile;
    private FileConfiguration allyConfig;
    
    private final Map<UUID, Set<UUID>> allies = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> pendingInvites = new HashMap<>();
    
    public AllyManager(DangerLog plugin) {
        this.plugin = plugin;
        this.allyFile = new File(plugin.getDataFolder(), "allies.yml");
        loadAllies();
    }
    
    private void loadAllies() {
        if (!allyFile.exists()) {
            try {
                allyFile.getParentFile().mkdirs();
                allyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create allies.yml file!");
                e.printStackTrace();
            }
        }
        
        allyConfig = YamlConfiguration.loadConfiguration(allyFile);
        allies.clear();
        
        if (allyConfig.contains("allies")) {
            for (String uuidStr : allyConfig.getConfigurationSection("allies").getKeys(false)) {
                UUID playerUUID = UUID.fromString(uuidStr);
                List<String> allyList = allyConfig.getStringList("allies." + uuidStr);
                Set<UUID> allySet = new HashSet<>();
                for (String allyStr : allyList) {
                    allySet.add(UUID.fromString(allyStr));
                }
                allies.put(playerUUID, allySet);
            }
        }
    }
    
    public void saveAllies() {
        for (Map.Entry<UUID, Set<UUID>> entry : allies.entrySet()) {
            List<String> allyList = new ArrayList<>();
            for (UUID ally : entry.getValue()) {
                allyList.add(ally.toString());
            }
            allyConfig.set("allies." + entry.getKey().toString(), allyList);
        }
        
        try {
            allyConfig.save(allyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save allies.yml file!");
            e.printStackTrace();
        }
    }
    
    public boolean areAllies(UUID player1, UUID player2) {
        Set<UUID> player1Allies = allies.get(player1);
        return player1Allies != null && player1Allies.contains(player2);
    }
    
    public void addAlly(UUID player1, UUID player2) {
        allies.computeIfAbsent(player1, k -> new HashSet<>()).add(player2);
        allies.computeIfAbsent(player2, k -> new HashSet<>()).add(player1);
        saveAllies();
    }
    
    public void removeAlly(UUID player1, UUID player2) {
        Set<UUID> player1Allies = allies.get(player1);
        Set<UUID> player2Allies = allies.get(player2);
        
        if (player1Allies != null) {
            player1Allies.remove(player2);
        }
        if (player2Allies != null) {
            player2Allies.remove(player1);
        }
        saveAllies();
    }
    
    public Set<UUID> getAllies(UUID player) {
        return allies.getOrDefault(player, new HashSet<>());
    }
    
    public void sendInvite(Player inviter, Player invited) {
        UUID inviterUUID = inviter.getUniqueId();
        UUID invitedUUID = invited.getUniqueId();
        
        int inviteDuration = plugin.getConfigManager().getInviteDuration();
        long expiryTime = System.currentTimeMillis() + (inviteDuration * 1000L);
        
        pendingInvites.computeIfAbsent(inviterUUID, k -> new HashMap<>()).put(invitedUUID, expiryTime);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<UUID, Long> invites = pendingInvites.get(inviterUUID);
                if (invites != null && invites.containsKey(invitedUUID)) {
                    Long storedExpiry = invites.get(invitedUUID);
                    if (storedExpiry != null && storedExpiry == expiryTime) {
                        invites.remove(invitedUUID);
                        
                        Player inviterOnline = Bukkit.getPlayer(inviterUUID);
                        Player invitedOnline = Bukkit.getPlayer(invitedUUID);
                        
                        // Notify inviter that invite expired
                        if (inviterOnline != null) {
                            inviterOnline.sendMessage("§cYour ally invite to §e" + invited.getName() + " §chas expired.");
                        }
                        // Notify invited player that invite expired
                        if (invitedOnline != null) {
                            invitedOnline.sendMessage("§cThe ally invite from §e" + inviter.getName() + " §chas expired.");
                        }
                    }
                }
            }
        }.runTaskLater(plugin, inviteDuration * 20L);
    }
    
    public boolean hasPendingInvite(UUID inviter, UUID invited) {
        Map<UUID, Long> invites = pendingInvites.get(inviter);
        if (invites == null) return false;
        
        Long expiry = invites.get(invited);
        if (expiry == null) return false;
        
        return System.currentTimeMillis() < expiry;
    }
    
    public boolean acceptInvite(Player invited, Player inviter) {
        UUID inviterUUID = inviter.getUniqueId();
        UUID invitedUUID = invited.getUniqueId();
        
        Map<UUID, Long> invites = pendingInvites.get(inviterUUID);
        if (invites == null || !invites.containsKey(invitedUUID)) {
            return false;
        }
        
        Long expiry = invites.get(invitedUUID);
        invites.remove(invitedUUID);
        
        if (System.currentTimeMillis() > expiry) {
            // Notify invited player that invite expired
            invited.sendMessage("§cThis ally invite has expired.");
            return false;
        }
        
        addAlly(inviterUUID, invitedUUID);
        return true;
    }
    
    public UUID findPendingInviteFrom(UUID invited) {
        for (Map.Entry<UUID, Map<UUID, Long>> entry : pendingInvites.entrySet()) {
            if (entry.getValue().containsKey(invited)) {
                Long expiry = entry.getValue().get(invited);
                if (System.currentTimeMillis() < expiry) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    public void reload() {
        loadAllies();
    }
}
