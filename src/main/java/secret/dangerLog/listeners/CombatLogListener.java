package secret.dangerLog.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import secret.dangerLog.DangerLog;

public class CombatLogListener implements Listener {
    
    private final DangerLog plugin;
    
    public CombatLogListener(DangerLog plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        
        Player player = event.getPlayer();
        
        if (player.isPermissionSet("dangerlog.bypass") && player.hasPermission("dangerlog.bypass")) return;
        
        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            plugin.getCombatManager().handleCombatLog(player);
        }
        
        plugin.getCombatManager().removeCombatTimer(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        
        Player player = event.getPlayer();
        plugin.getCombatManager().handlePlayerRejoin(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        
        Entity entity = event.getEntity();
        
        if (!(entity instanceof Zombie zombie)) return;
        
        if (!plugin.getCombatManager().isCombatLogZombie(zombie)) return;
        
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        String killerName = null;
        if (zombie.getKiller() != null) {
            killerName = zombie.getKiller().getName();
        } else if (zombie.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
            Entity damager = damageEvent.getDamager();
            if (damager instanceof Player damagerPlayer) {
                killerName = damagerPlayer.getName();
            } else {
                killerName = damager.getType().name();
            }
        }
        
        plugin.getCombatManager().handleZombieDeath(zombie, killerName);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        if (plugin.getCombatManager().shouldSuppressDeathMessage(player.getUniqueId())) {
            event.deathMessage(Component.empty());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();
        
        if (!(entity instanceof Zombie zombie)) return;
        
        if (plugin.getCombatManager().isCombatLogZombie(zombie)) {
            event.setCancelled(true);
        }
    }
}
