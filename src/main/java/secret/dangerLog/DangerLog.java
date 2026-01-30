package secret.dangerLog;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import secret.dangerLog.commands.AllyCommand;
import secret.dangerLog.commands.DangerLogCommand;
import secret.dangerLog.listeners.CombatLogListener;
import secret.dangerLog.listeners.CombatRestrictionListener;
import secret.dangerLog.listeners.SpearPacketListener;
import secret.dangerLog.listeners.WeaponRestrictionListener;
import secret.dangerLog.managers.AllyManager;
import secret.dangerLog.managers.CombatManager;
import secret.dangerLog.managers.ConfigManager;

public final class DangerLog extends JavaPlugin {
    
    private ConfigManager configManager;
    private AllyManager allyManager;
    private CombatManager combatManager;
    private CombatRestrictionListener combatRestrictionListener;
    private SpearPacketListener spearPacketListener;
    private UpdateChecker updateChecker;
    private boolean hasProtocolLib = false;
    
    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.allyManager = new AllyManager(this);
        this.combatManager = new CombatManager(this);
        
        getServer().getPluginManager().registerEvents(new CombatLogListener(this), this);
        this.combatRestrictionListener = new CombatRestrictionListener(this);
        getServer().getPluginManager().registerEvents(combatRestrictionListener, this);
        
        getServer().getPluginManager().registerEvents(new WeaponRestrictionListener(this), this);
        
        setupProtocolLib();
        
        DangerLogCommand dangerLogCommand = new DangerLogCommand(this);
        getCommand("dangerlog").setExecutor(dangerLogCommand);
        getCommand("dangerlog").setTabCompleter(dangerLogCommand);
        
        AllyCommand allyCommand = new AllyCommand(this);
        getCommand("ally").setExecutor(allyCommand);
        getCommand("ally").setTabCompleter(allyCommand);
        
        if (configManager.isEnabled()) {
            combatManager.start();
        }
        
        if (configManager.isCheckUpdates()) {
            updateChecker = new UpdateChecker(this, configManager.getModrinthSlug());
            getServer().getPluginManager().registerEvents(updateChecker, this);
        }
        
        getLogger().info("DangerLog has been enabled!");
    }
    
    private void setupProtocolLib() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            hasProtocolLib = true;
            spearPacketListener = new SpearPacketListener(this);
            getLogger().info("ProtocolLib found - Spear lunge detection enabled!");
        } else {
            hasProtocolLib = false;
            if (configManager.isDisableSpearLunge() || configManager.getSpearLungeCooldown() > 0) {
                getLogger().warning("ProtocolLib not found - Spear lunge features will not work!");
                getLogger().warning("Install ProtocolLib to enable spear lunge detection.");
            }
        }
    }
    
    @Override
    public void onDisable() {
        if (spearPacketListener != null) {
            spearPacketListener.shutdown();
        }
        
        if (combatRestrictionListener != null) {
            combatRestrictionListener.shutdown();
        }
        
        if (combatManager != null) {
            combatManager.saveZombieData();
            combatManager.stop();
        }
        
        if (allyManager != null) {
            allyManager.saveAllies();
        }
        
        getLogger().info("DangerLog has been disabled!");
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public AllyManager getAllyManager() {
        return allyManager;
    }
    
    public CombatManager getCombatManager() {
        return combatManager;
    }
    
    public CombatRestrictionListener getCombatRestrictionListener() {
        return combatRestrictionListener;
    }
    
    public SpearPacketListener getSpearPacketListener() {
        return spearPacketListener;
    }
    
    public boolean hasProtocolLib() {
        return hasProtocolLib;
    }
}
