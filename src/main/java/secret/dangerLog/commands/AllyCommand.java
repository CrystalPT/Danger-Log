package secret.dangerLog.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import secret.dangerLog.DangerLog;

import java.util.*;

public class AllyCommand implements CommandExecutor, TabCompleter {
    
    private final DangerLog plugin;
    
    public AllyCommand(DangerLog plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }
        
        if (!plugin.getConfigManager().isAllowAllies()) {
            player.sendMessage(Component.text("The ally system is currently disabled.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "invite" -> handleInvite(player, args);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player);
            case "accept" -> handleAccept(player, args);
            default -> sendUsage(player);
        }
        
        return true;
    }
    
    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /ally invite <player>", NamedTextColor.RED));
            return;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(Component.text("Player not found or is not online.", NamedTextColor.RED));
            return;
        }
        
        if (target.equals(player)) {
            player.sendMessage(Component.text("You cannot invite yourself to be an ally.", NamedTextColor.RED));
            return;
        }
        
        if (plugin.getAllyManager().areAllies(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(Component.text("You are already allies with " + target.getName() + ".", NamedTextColor.RED));
            return;
        }
        
        if (plugin.getAllyManager().hasPendingInvite(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(Component.text("You already have a pending invite to " + target.getName() + ".", NamedTextColor.RED));
            return;
        }
        
        int maxAllies = plugin.getConfigManager().getMaxAllies();
        if (maxAllies > 0 && plugin.getAllyManager().getAllies(player.getUniqueId()).size() >= maxAllies) {
            player.sendMessage(Component.text("You have reached the maximum number of allies (" + maxAllies + ").", NamedTextColor.RED));
            return;
        }
        
        if (maxAllies > 0 && plugin.getAllyManager().getAllies(target.getUniqueId()).size() >= maxAllies) {
            player.sendMessage(Component.text(target.getName() + " has reached the maximum number of allies.", NamedTextColor.RED));
            return;
        }
        
        plugin.getAllyManager().sendInvite(player, target);
        
        player.sendMessage(Component.text("You have invited ", NamedTextColor.GREEN)
                .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" to be an ally.", NamedTextColor.GREEN)));
        
        Component clickHere = Component.text("[Click Here]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/ally accept " + player.getName()));
        
        target.sendMessage(Component.text("You have been invited by ", NamedTextColor.GREEN)
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" to be an ally. ", NamedTextColor.GREEN))
                .append(clickHere)
                .append(Component.text(" to accept!", NamedTextColor.GREEN)));
    }
    
    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /ally remove <player>", NamedTextColor.RED));
            return;
        }
        
        String targetName = args[1];
        
        UUID targetUUID = null;
        String actualName = targetName;
        
        for (UUID allyUUID : plugin.getAllyManager().getAllies(player.getUniqueId())) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(allyUUID);
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                targetUUID = allyUUID;
                actualName = offlinePlayer.getName();
                break;
            }
        }
        
        if (targetUUID == null) {
            player.sendMessage(Component.text("You are not allies with " + targetName + ".", NamedTextColor.RED));
            return;
        }
        
        plugin.getAllyManager().removeAlly(player.getUniqueId(), targetUUID);
        
        player.sendMessage(Component.text("You are no longer allies with ", NamedTextColor.YELLOW)
                .append(Component.text(actualName, NamedTextColor.RED))
                .append(Component.text(".", NamedTextColor.YELLOW)));
        
        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null) {
            target.sendMessage(Component.text(player.getName(), NamedTextColor.RED)
                    .append(Component.text(" has removed you as an ally.", NamedTextColor.YELLOW)));
        }
    }
    
    private void handleList(Player player) {
        Set<UUID> allies = plugin.getAllyManager().getAllies(player.getUniqueId());
        
        if (allies.isEmpty()) {
            player.sendMessage(Component.text("You don't have any allies.", NamedTextColor.YELLOW));
            return;
        }
        
        player.sendMessage(Component.text("Your Allies:", NamedTextColor.GOLD));
        
        for (UUID allyUUID : allies) {
            OfflinePlayer allyPlayer = Bukkit.getOfflinePlayer(allyUUID);
            String name = allyPlayer.getName() != null ? allyPlayer.getName() : "Unknown";
            boolean online = allyPlayer.isOnline();
            
            Component status = online 
                    ? Component.text(" (Online)", NamedTextColor.GREEN)
                    : Component.text(" (Offline)", NamedTextColor.GRAY);
            
            player.sendMessage(Component.text("- ", NamedTextColor.GRAY)
                    .append(Component.text(name, NamedTextColor.YELLOW))
                    .append(status));
        }
    }
    
    private void handleAccept(Player player, String[] args) {
        UUID inviterUUID;
        
        if (args.length < 2) {
            inviterUUID = plugin.getAllyManager().findPendingInviteFrom(player.getUniqueId());
            if (inviterUUID == null) {
                player.sendMessage(Component.text("You don't have any pending ally invites.", NamedTextColor.RED));
                return;
            }
        } else {
            String inviterName = args[1];
            Player inviter = Bukkit.getPlayer(inviterName);
            
            if (inviter == null) {
                player.sendMessage(Component.text("Player not found or is not online.", NamedTextColor.RED));
                return;
            }
            
            inviterUUID = inviter.getUniqueId();
        }
        
        Player inviter = Bukkit.getPlayer(inviterUUID);
        if (inviter == null) {
            player.sendMessage(Component.text("The player who invited you is no longer online.", NamedTextColor.RED));
            return;
        }
        
        if (!plugin.getAllyManager().hasPendingInvite(inviterUUID, player.getUniqueId())) {
            player.sendMessage(Component.text("You don't have a pending invite from " + inviter.getName() + ".", NamedTextColor.RED));
            return;
        }
        
        if (plugin.getAllyManager().acceptInvite(player, inviter)) {
            player.sendMessage(Component.text("You are now allies with ", NamedTextColor.GREEN)
                    .append(Component.text(inviter.getName(), NamedTextColor.YELLOW))
                    .append(Component.text("!", NamedTextColor.GREEN)));
            
            inviter.sendMessage(Component.text(player.getName(), NamedTextColor.YELLOW)
                    .append(Component.text(" has accepted your ally invite!", NamedTextColor.GREEN)));
            
            if (!plugin.getCombatManager().hasNearbyEnemies(player)) {
                plugin.getCombatManager().removeCombatTimer(player.getUniqueId());
            }
            if (!plugin.getCombatManager().hasNearbyEnemies(inviter)) {
                plugin.getCombatManager().removeCombatTimer(inviter.getUniqueId());
            }
        }
    }
    
    private void sendUsage(Player player) {
        player.sendMessage(Component.text("Ally Commands:", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/ally invite <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Invite a player to be your ally", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/ally remove <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Remove a player from your allies", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/ally list", NamedTextColor.YELLOW)
                .append(Component.text(" - List all your allies", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/ally accept <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Accept an ally invite", NamedTextColor.GRAY)));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> completions = Arrays.asList("invite", "remove", "list", "accept");
            List<String> result = new ArrayList<>();
            for (String s : completions) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            
            switch (subCommand) {
                case "invite" -> {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (!online.equals(player) && 
                            !plugin.getAllyManager().areAllies(player.getUniqueId(), online.getUniqueId()) &&
                            online.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(online.getName());
                        }
                    }
                }
                case "remove" -> {
                    for (UUID allyUUID : plugin.getAllyManager().getAllies(player.getUniqueId())) {
                        OfflinePlayer ally = Bukkit.getOfflinePlayer(allyUUID);
                        if (ally.getName() != null && ally.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(ally.getName());
                        }
                    }
                }
                case "accept" -> {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (plugin.getAllyManager().hasPendingInvite(online.getUniqueId(), player.getUniqueId()) &&
                            online.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            result.add(online.getName());
                        }
                    }
                }
            }
            
            return result;
        }
        
        return new ArrayList<>();
    }
}
