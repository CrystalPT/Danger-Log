package secret.dangerLog.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import secret.dangerLog.DangerLog;

public class WeaponRestrictionListener implements Listener {
    
    private final DangerLog plugin;
    
    public WeaponRestrictionListener(DangerLog plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        
        Material heldItem = player.getInventory().getItemInMainHand().getType();
        
        if (heldItem == Material.MACE) {
            handleMaceAttack(event, player);
        }
        
        if (heldItem.name().contains("SPEAR")) {
            handleSpearAttack(event, player);
        }
    }
    
    private void handleMaceAttack(EntityDamageByEntityEvent event, Player player) {
        if (plugin.getConfigManager().isDisableMaces()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Maces are disabled while in combat!", NamedTextColor.RED));
            return;
        }
        
        int cooldown = plugin.getConfigManager().getMaceCooldown();
        if (cooldown > 0) {
            if (player.hasCooldown(Material.MACE)) {
                event.setCancelled(true);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.setCooldown(Material.MACE, cooldown * 20);
                }
            });
        }
    }
    
    private void handleSpearAttack(EntityDamageByEntityEvent event, Player player) {
        Material spearMaterial = player.getInventory().getItemInMainHand().getType();
        
        if (plugin.getConfigManager().isDisableSpears()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Spears are disabled while in combat!", NamedTextColor.RED));
            return;
        }
        
        int cooldown = plugin.getConfigManager().getSpearCooldown();
        if (cooldown > 0) {
            if (player.hasCooldown(spearMaterial)) {
                event.setCancelled(true);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.setCooldown(spearMaterial, cooldown * 20);
                }
            });
        }
    }
}
