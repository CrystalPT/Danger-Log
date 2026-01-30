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
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRiptideEvent;

import java.util.Set;
import org.bukkit.scheduler.BukkitTask;
import secret.dangerLog.DangerLog;

public class CombatRestrictionListener implements Listener {
    
    private final DangerLog plugin;
    private BukkitTask cooldownRefreshTask;
    
    private static final int DISABLED_COOLDOWN_TICKS = 20 * 60 * 5;
    
    private static final Set<Material> CONTAINER_MATERIALS = Set.of(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST,
            Material.BARREL,
            Material.HOPPER,
            Material.DROPPER,
            Material.DISPENSER,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.BREWING_STAND,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX
    );
    
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
                
                if (plugin.getConfigManager().isDisableMaces()) {
                    if (player.getCooldown(Material.MACE) < DISABLED_COOLDOWN_TICKS - 40) {
                        player.setCooldown(Material.MACE, DISABLED_COOLDOWN_TICKS);
                    }
                } else {
                    int currentCooldown = player.getCooldown(Material.MACE);
                    int configCooldown = plugin.getConfigManager().getMaceCooldown() * 20;
                    if (currentCooldown > configCooldown + 40) {
                        player.setCooldown(Material.MACE, 0);
                    }
                }
                
                if (plugin.getConfigManager().isDisableWindCharges()) {
                    if (player.getCooldown(Material.WIND_CHARGE) < DISABLED_COOLDOWN_TICKS - 40) {
                        player.setCooldown(Material.WIND_CHARGE, DISABLED_COOLDOWN_TICKS);
                    }
                } else {
                    int currentCooldown = player.getCooldown(Material.WIND_CHARGE);
                    int configCooldown = plugin.getConfigManager().getWindChargeCooldown() * 20;
                    if (currentCooldown > configCooldown + 40) {
                        player.setCooldown(Material.WIND_CHARGE, 0);
                    }
                }
                
                if (plugin.getConfigManager().isDisableCobwebs()) {
                    if (player.getCooldown(Material.COBWEB) < DISABLED_COOLDOWN_TICKS - 40) {
                        player.setCooldown(Material.COBWEB, DISABLED_COOLDOWN_TICKS);
                    }
                } else {
                    int currentCooldown = player.getCooldown(Material.COBWEB);
                    int configCooldown = plugin.getConfigManager().getCobwebCooldown() * 20;
                    if (currentCooldown > configCooldown + 40) {
                        player.setCooldown(Material.COBWEB, 0);
                    }
                }
                
                handleSpearCooldowns(player);
            }
        }, 20L, 20L);
    }
    
    private void handleSpearCooldowns(Player player) {
        for (Material material : Material.values()) {
            if (!material.name().contains("SPEAR")) continue;
            
            if (plugin.getConfigManager().isDisableSpears()) {
                if (player.getCooldown(material) < DISABLED_COOLDOWN_TICKS - 40) {
                    player.setCooldown(material, DISABLED_COOLDOWN_TICKS);
                }
            } else {
                int currentCooldown = player.getCooldown(material);
                int configCooldown = plugin.getConfigManager().getSpearCooldown() * 20;
                if (currentCooldown > configCooldown + 40) {
                    player.setCooldown(material, 0);
                }
            }
        }
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
            
            if (plugin.getConfigManager().isPearlRefreshCombat()) {
                plugin.getCombatManager().refreshCombatTimer(player.getUniqueId());
            }
        }
        
        if (event.getEntity().getType() == org.bukkit.entity.EntityType.TRIDENT) {
            if (plugin.getConfigManager().isDisableTridents()) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Tridents are disabled while in combat!", NamedTextColor.RED));
            }
        }
        
        if (event.getEntity().getType() == org.bukkit.entity.EntityType.WIND_CHARGE) {
            if (plugin.getConfigManager().isDisableWindCharges()) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Wind charges are disabled while in combat!", NamedTextColor.RED));
                return;
            }
            
            int cooldown = plugin.getConfigManager().getWindChargeCooldown();
            if (cooldown > 0) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        player.setCooldown(Material.WIND_CHARGE, cooldown * 20);
                    }
                });
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        if (event.getBlock().getType() != Material.COBWEB) return;
        
        Player player = event.getPlayer();
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        
        if (plugin.getConfigManager().isDisableCobwebs()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Cobwebs are disabled while in combat!", NamedTextColor.RED));
            return;
        }
        
        int cooldown = plugin.getConfigManager().getCobwebCooldown();
        if (cooldown > 0) {
            if (player.hasCooldown(Material.COBWEB)) {
                event.setCancelled(true);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.setCooldown(Material.COBWEB, cooldown * 20);
                }
            });
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        if (!plugin.getConfigManager().isDisableContainers()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!CONTAINER_MATERIALS.contains(block.getType())) return;
        
        Player player = event.getPlayer();
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        
        event.setCancelled(true);
        player.sendMessage(Component.text("You cannot open containers while in combat!", NamedTextColor.RED));
    }
    
    public void clearCooldowns(Player player) {
        player.setCooldown(Material.ENDER_PEARL, 0);
        player.setCooldown(Material.FIREWORK_ROCKET, 0);
        player.setCooldown(Material.TRIDENT, 0);
        player.setCooldown(Material.MACE, 0);
        player.setCooldown(Material.WIND_CHARGE, 0);
        player.setCooldown(Material.COBWEB, 0);
        
        for (Material material : Material.values()) {
            if (material.name().contains("SPEAR")) {
                player.setCooldown(material, 0);
            }
        }
        
        if (plugin.getSpearPacketListener() != null) {
            plugin.getSpearPacketListener().clearCooldown(player.getUniqueId());
        }
    }
    
    public void shutdown() {
        if (cooldownRefreshTask != null) {
            cooldownRefreshTask.cancel();
        }
    }
}
