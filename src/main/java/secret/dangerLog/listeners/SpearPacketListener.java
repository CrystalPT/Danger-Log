package secret.dangerLog.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import secret.dangerLog.DangerLog;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpearPacketListener {
    
    private final DangerLog plugin;
    private final ProtocolManager protocolManager;
    private final Map<UUID, Long> lungeCooldowns = new HashMap<>();
    
    public SpearPacketListener(DangerLog plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerListeners();
    }
    
    private void registerListeners() {
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGH, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!plugin.getConfigManager().isEnabled()) return;
                
                Player player = event.getPlayer();
                if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
                
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (!isSpear(mainHand.getType())) return;
                
                EnumWrappers.PlayerDigType digType = event.getPacket().getPlayerDigTypes().read(0);
                
                if (digType == EnumWrappers.PlayerDigType.RELEASE_USE_ITEM) {
                    handleSpearLunge(event, player, mainHand.getType());
                }
            }
        });
        
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGH, PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!plugin.getConfigManager().isEnabled()) return;
                
                Player player = event.getPlayer();
                if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
                
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (!isSpear(mainHand.getType())) return;
                
                if (plugin.getConfigManager().isDisableSpears()) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("Spears are disabled while in combat!", NamedTextColor.RED));
                }
            }
        });
    }
    
    private void handleSpearLunge(PacketEvent event, Player player, Material spearMaterial) {
        UUID playerUUID = player.getUniqueId();
        
        if (plugin.getConfigManager().isDisableSpearLunge()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Spear lunge is disabled while in combat!", NamedTextColor.RED));
            return;
        }
        
        int cooldownSeconds = plugin.getConfigManager().getSpearLungeCooldown();
        if (cooldownSeconds > 0) {
            long currentTime = System.currentTimeMillis();
            long cooldownEnd = lungeCooldowns.getOrDefault(playerUUID, 0L);
            
            if (currentTime < cooldownEnd) {
                event.setCancelled(true);
                return;
            }
            
            lungeCooldowns.put(playerUUID, currentTime + (cooldownSeconds * 1000L));
            
            player.setCooldown(spearMaterial, cooldownSeconds * 20);
        }
    }
    
    private boolean isSpear(Material material) {
        return material.name().contains("SPEAR");
    }
    
    public void clearCooldown(UUID playerUUID) {
        lungeCooldowns.remove(playerUUID);
    }
    
    public void shutdown() {
        protocolManager.removePacketListeners(plugin);
        lungeCooldowns.clear();
    }
}
