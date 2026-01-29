package secret.dangerLog.listeners;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.scheduler.BukkitTask;
import secret.dangerLog.DangerLog;

public class CombatRestrictionListener implements Listener {
    
    private final DangerLog plugin;
    private BukkitTask cooldownRefreshTask;
    
    private static final int DISABLED_COOLDOWN_TICKS = 20 * 60 * 5;
    
    public CombatRestrictionListener(DangerLog plugin) {
        this.plugin = plugin;
        startCooldownRefreshTask();
    }
    
    private void startCooldownRefreshTask() {
        cooldownRefreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.getConfigManager().isEnabled()) return;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) {
                    continue;
                }
                
                if (plugin.getConfigManager().isDisablePearls()) {
                    if (player.getCooldown(Material.ENDER_PEARL) < DISABLED_COOLDOWN_TICKS - 40) {
                        player.setCooldown(Material.ENDER_PEARL, DISABLED_COOLDOWN_TICKS);
                    }
                } else {
                    int currentCooldown = player.getCooldown(Material.ENDER_PEARL);
                    int configCooldown = plugin.getConfigManager().getPearlCooldown() * 20;
                    if (currentCooldown > configCooldown + 40) {
                        player.setCooldown(Material.ENDER_PEARL, 0);
                    }
                }
                
                if (plugin.getConfigManager().isDisableFireworks()) {
                    if (player.getCooldown(Material.FIREWORK_ROCKET) < DISABLED_COOLDOWN_TICKS - 40) {
                        player.setCooldown(Material.FIREWORK_ROCKET, DISABLED_COOLDOWN_TICKS);
                    }
                } else {
                    int currentCooldown = player.getCooldown(Material.FIREWORK_ROCKET);
                    int configCooldown = plugin.getConfigManager().getFireworkCooldown() * 20;
                    if (currentCooldown > configCooldown + 40) {
                        player.setCooldown(Material.FIREWORK_ROCKET, 0);
                    }
                }
                
                if (plugin.getConfigManager().isDisableTridents()) {
                    if (player.getCooldown(Material.TRIDENT) < DISABLED_COOLDOWN_TICKS - 40) {
                        player.setCooldown(Material.TRIDENT, DISABLED_COOLDOWN_TICKS);
                    }
                } else {
                    int currentCooldown = player.getCooldown(Material.TRIDENT);
                    int configCooldown = plugin.getConfigManager().getTridentCooldown() * 20;
                    if (currentCooldown > configCooldown + 40) {
                        player.setCooldown(Material.TRIDENT, 0);
                    }
                }
            }
        }, 20L, 20L);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        
        if (event.getEntity().getType() == org.bukkit.entity.EntityType.ENDER_PEARL) {
            if (plugin.getConfigManager().isDisablePearls()) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Ender pearls are disabled while in combat!", NamedTextColor.RED));
                return;
            }
            
            int cooldown = plugin.getConfigManager().getPearlCooldown();
            if (cooldown > 0) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        player.setCooldown(Material.ENDER_PEARL, cooldown * 20);
                    }
                });
            }
        }
        
        if (event.getEntity().getType() == org.bukkit.entity.EntityType.TRIDENT) {
            if (plugin.getConfigManager().isDisableTridents()) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Tridents are disabled while in combat!", NamedTextColor.RED));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRiptide(PlayerRiptideEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        
        Player player = event.getPlayer();
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        
        if (plugin.getConfigManager().isDisableTridents()) {
            player.sendMessage(Component.text("Tridents with Riptide are disabled while in combat!", NamedTextColor.RED));
            player.setVelocity(player.getVelocity().multiply(0.1));
        } else {
            int cooldown = plugin.getConfigManager().getTridentCooldown();
            if (cooldown > 0) {
                player.setCooldown(Material.TRIDENT, cooldown * 20);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onElytraBoost(PlayerElytraBoostEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        
        Player player = event.getPlayer();
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        
        if (plugin.getConfigManager().isDisableFireworks()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Firework rockets are disabled while in combat!", NamedTextColor.RED));
        } else {
            int cooldown = plugin.getConfigManager().getFireworkCooldown();
            if (cooldown > 0) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        player.setCooldown(Material.FIREWORK_ROCKET, cooldown * 20);
                    }
                });
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        if (!plugin.getConfigManager().isDisableElytras()) return;
        
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        
        if (event.isGliding()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Elytras are disabled while in combat!", NamedTextColor.RED));
        }
    }
    
    public void clearCooldowns(Player player) {
        player.setCooldown(Material.ENDER_PEARL, 0);
        player.setCooldown(Material.FIREWORK_ROCKET, 0);
        player.setCooldown(Material.TRIDENT, 0);
    }
    
    public void shutdown() {
        if (cooldownRefreshTask != null) {
            cooldownRefreshTask.cancel();
        }
    }
}
