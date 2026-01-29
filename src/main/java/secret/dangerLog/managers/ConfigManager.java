package secret.dangerLog.managers;

import org.bukkit.configuration.file.FileConfiguration;
import secret.dangerLog.DangerLog;

public class ConfigManager {
    
    private final DangerLog plugin;
    private int radius;
    private boolean allowAllies;
    private int maxAllies;
    private String deathType;
    private int zombieLogTimerMax;
    private int timerDuration;
    private int inviteDuration;
    private boolean enabled;
    
    private boolean disableTridents;
    private boolean disableElytras;
    private boolean disablePearls;
    private boolean disableFireworks;
    private int tridentCooldown;
    private int pearlCooldown;
    private int fireworkCooldown;
    
    public ConfigManager(DangerLog plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        
        this.radius = config.getInt("radius", 50);
        this.allowAllies = config.getBoolean("allow-allies", true);
        this.maxAllies = config.getInt("max-allies", 6);
        this.deathType = config.getString("death-type", "ZOMBIE").toUpperCase();
        this.zombieLogTimerMax = config.getInt("zombie-log-timer-max", -1);
        
        if (zombieLogTimerMax == 0) {
            zombieLogTimerMax = -1;
        } else if (zombieLogTimerMax != -1 && zombieLogTimerMax < 10) {
            plugin.getLogger().warning("zombie-log-timer-max must be at least 10 seconds or -1/0 for infinite. Setting to 10.");
            zombieLogTimerMax = 10;
        }
        
        this.timerDuration = config.getInt("timer-duration", 45);
        this.inviteDuration = config.getInt("invite-duration", 120);
        this.enabled = config.getBoolean("enabled", true);
        
        if (!deathType.equals("ZOMBIE") && !deathType.equals("INSTANT")) {
            plugin.getLogger().warning("Invalid death-type in config. Defaulting to ZOMBIE.");
            deathType = "ZOMBIE";
        }
        
        this.disableTridents = config.getBoolean("disable-tridents", true);
        this.disableElytras = config.getBoolean("disable-elytras", true);
        this.disablePearls = config.getBoolean("disable-pearls", false);
        this.disableFireworks = config.getBoolean("disable-fireworks", false);
        this.tridentCooldown = config.getInt("trident-cooldown", 10);
        this.pearlCooldown = config.getInt("pearl-cooldown", 10);
        this.fireworkCooldown = config.getInt("firework-cooldown", 10);
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("enabled", enabled);
        plugin.saveConfig();
    }
    
    public int getRadius() {
        return radius;
    }
    
    public boolean isAllowAllies() {
        return allowAllies;
    }
    
    public int getMaxAllies() {
        return maxAllies;
    }
    
    public String getDeathType() {
        return deathType;
    }
    
    public int getZombieLogTimerMax() {
        return zombieLogTimerMax;
    }
    
    public int getTimerDuration() {
        return timerDuration;
    }
    
    public int getInviteDuration() {
        return inviteDuration;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isDisableTridents() {
        return disableTridents;
    }
    
    public boolean isDisableElytras() {
        return disableElytras;
    }
    
    public boolean isDisablePearls() {
        return disablePearls;
    }
    
    public boolean isDisableFireworks() {
        return disableFireworks;
    }
    
    public int getTridentCooldown() {
        return tridentCooldown;
    }
    
    public int getPearlCooldown() {
        return pearlCooldown;
    }
    
    public int getFireworkCooldown() {
        return fireworkCooldown;
    }
}
