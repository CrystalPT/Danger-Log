package secret.dangerLog.managers;

import org.bukkit.configuration.file.FileConfiguration;
import secret.dangerLog.DangerLog;

public class ConfigManager {
    
    private final DangerLog plugin;
    private boolean checkUpdates;
    private String modrinthSlug;
    private int radius;
    private boolean allowAllies;
    private int maxAllies;
    private String deathType;
    private int zombieLogTimerMax;
    private boolean zombieProximityCombat;
    private int timerDuration;
    private int inviteDuration;
    private boolean enabled;
    private boolean pearlRefreshCombat;
    
    private boolean disableTridents;
    private boolean disableElytras;
    private boolean disablePearls;
    private boolean disableFireworks;
    private boolean disableWindCharges;
    private boolean disableCobwebs;
    private boolean disableContainers;
    private int tridentCooldown;
    private int pearlCooldown;
    private int fireworkCooldown;
    private int windChargeCooldown;
    private int cobwebCooldown;
    
    private boolean disableSpears;
    private boolean disableSpearLunge;
    private int spearCooldown;
    private int spearLungeCooldown;
    private boolean disableMaces;
    private int maceCooldown;
    
    public ConfigManager(DangerLog plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        
        this.checkUpdates = config.getBoolean("check-updates", true);
        this.modrinthSlug = config.getString("modrinth-slug", "dangerlog");
        
        this.radius = config.getInt("radius", 50);
        this.allowAllies = config.getBoolean("allow-allies", true);
        this.maxAllies = config.getInt("max-allies", 6);
        this.deathType = config.getString("death-type", "ZOMBIE").toUpperCase();
        this.zombieLogTimerMax = config.getInt("zombie-log-timer-max", -1);
        this.zombieProximityCombat = config.getBoolean("zombie-proximity-combat", true);
        
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
        this.disableWindCharges = config.getBoolean("disable-wind-charges", false);
        this.disableCobwebs = config.getBoolean("disable-cobwebs", false);
        this.disableContainers = config.getBoolean("disable-containers", false);
        this.tridentCooldown = config.getInt("trident-cooldown", 10);
        this.pearlCooldown = config.getInt("pearl-cooldown", 10);
        this.pearlRefreshCombat = config.getBoolean("pearl-refresh-combat", true);
        this.fireworkCooldown = config.getInt("firework-cooldown", 10);
        this.windChargeCooldown = config.getInt("wind-charge-cooldown", 0);
        this.cobwebCooldown = config.getInt("cobweb-cooldown", 0);
        
        this.disableSpears = config.getBoolean("disable-spears", false);
        this.disableSpearLunge = config.getBoolean("disable-spear-lunge", false);
        this.spearCooldown = config.getInt("spear-cooldown", 0);
        this.spearLungeCooldown = config.getInt("spear-lunge-cooldown", 5);
        this.disableMaces = config.getBoolean("disable-maces", false);
        this.maceCooldown = config.getInt("mace-cooldown", 5);
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("enabled", enabled);
        plugin.saveConfig();
    }
    
    public boolean isCheckUpdates() {
        return checkUpdates;
    }
    
    public String getModrinthSlug() {
        return modrinthSlug;
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
    
    public boolean isZombieProximityCombat() {
        return zombieProximityCombat;
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
    
    public boolean isPearlRefreshCombat() {
        return pearlRefreshCombat;
    }
    
    public int getFireworkCooldown() {
        return fireworkCooldown;
    }
    
    public boolean isDisableWindCharges() {
        return disableWindCharges;
    }
    
    public boolean isDisableCobwebs() {
        return disableCobwebs;
    }
    
    public int getWindChargeCooldown() {
        return windChargeCooldown;
    }
    
    public int getCobwebCooldown() {
        return cobwebCooldown;
    }
    
    public boolean isDisableContainers() {
        return disableContainers;
    }
    
    public boolean isDisableSpears() {
        return disableSpears;
    }
    
    public boolean isDisableSpearLunge() {
        return disableSpearLunge;
    }
    
    public int getSpearCooldown() {
        return spearCooldown;
    }
    
    public int getSpearLungeCooldown() {
        return spearLungeCooldown;
    }
    
    public boolean isDisableMaces() {
        return disableMaces;
    }
    
    public int getMaceCooldown() {
        return maceCooldown;
    }
}
