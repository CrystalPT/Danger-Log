package secret.dangerLog.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import secret.dangerLog.DangerLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DangerLogCommand implements CommandExecutor, TabCompleter {
    
    private final DangerLog plugin;
    
    public DangerLogCommand(DangerLog plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("dangerlog.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "on" -> {
                plugin.getConfigManager().setEnabled(true);
                plugin.getCombatManager().stop();
                plugin.getCombatManager().start();
                sender.sendMessage(Component.text("DangerLog has been enabled.", NamedTextColor.GREEN));
            }
            case "off" -> {
                plugin.getConfigManager().setEnabled(false);
                plugin.getCombatManager().stop();
                sender.sendMessage(Component.text("DangerLog has been disabled.", NamedTextColor.RED));
            }
            case "reload" -> {
                plugin.getConfigManager().loadConfig();
                plugin.getAllyManager().reload();
                sender.sendMessage(Component.text("DangerLog configuration reloaded.", NamedTextColor.GREEN));
            }
            default -> sendUsage(sender);
        }
        
        return true;
    }
    
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("DangerLog Commands:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/dangerlog on", NamedTextColor.YELLOW)
                .append(Component.text(" - Enable DangerLog", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/dangerlog off", NamedTextColor.YELLOW)
                .append(Component.text(" - Disable DangerLog", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/dangerlog reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload configuration", NamedTextColor.GRAY)));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("dangerlog.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> completions = Arrays.asList("on", "off", "reload");
            List<String> result = new ArrayList<>();
            for (String s : completions) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }
        
        return new ArrayList<>();
    }
}
