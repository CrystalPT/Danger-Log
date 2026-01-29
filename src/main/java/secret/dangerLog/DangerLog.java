package secret.dangerLog;

import org.bukkit.plugin.java.JavaPlugin;
import secret.dangerLog.commands.AllyCommand;
import secret.dangerLog.commands.DangerLogCommand;
import secret.dangerLog.listeners.CombatLogListener;
import secret.dangerLog.listeners.CombatRestrictionListener;
import secret.dangerLog.managers.AllyManager;
import secret.dangerLog.managers.CombatManager;
import secret.dangerLog.managers.ConfigManager;

public final class DangerLog extends JavaPlugin {
    
    private ConfigManager configManager;
    private AllyManager allyManager;
    private CombatManager combatManager;
    private CombatRestrictionListener combatRestrictionListener;
    
    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.allyManager = new AllyManager(this);
        this.combatManager = new CombatManager(this);
        
        getServer().getPluginManager().registerEvents(new CombatLogListener(this), this);
        this.combatRestrictionListener = new CombatRestrictionListener(this);
        getServer().getPluginManager().registerEvents(combatRestrictionListener, this);
        
        DangerLogCommand dangerLogCommand = new DangerLogCommand(this);
        getCommand("dangerlog").setExecutor(dangerLogCommand);
        getCommand("dangerlog").setTabCompleter(dangerLogCommand);
        
        AllyCommand allyCommand = new AllyCommand(this);
        getCommand("ally").setExecutor(allyCommand);
        getCommand("ally").setTabCompleter(allyCommand);
        
        if (configManager.isEnabled()) {
            combatManager.start();
        }
        
        getLogger().info("DangerLog has been enabled!");
    }
    
    @Override
    public void onDisable() {
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
}
