package secret.dangerLog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker implements Listener {
    
    private final DangerLog plugin;
    private final String modrinthSlug;
    private String latestVersion = null;
    private boolean updateAvailable = false;
    
    public UpdateChecker(DangerLog plugin, String modrinthSlug) {
        this.plugin = plugin;
        this.modrinthSlug = modrinthSlug;
        
        checkForUpdates();
    }
    
    public void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                String apiUrl = "https://api.modrinth.com/v2/project/" + modrinthSlug + "/version";
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "DangerLog/" + plugin.getDescription().getVersion());
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning("Failed to check for updates: HTTP " + responseCode);
                    return;
                }
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JsonArray versions = JsonParser.parseString(response.toString()).getAsJsonArray();
                if (versions.isEmpty()) {
                    return;
                }
                
                JsonElement latestVersionElement = versions.get(0).getAsJsonObject().get("version_number");
                if (latestVersionElement == null) {
                    return;
                }
                
                latestVersion = latestVersionElement.getAsString();
                String currentVersion = plugin.getDescription().getVersion();
                
                if (isNewerVersion(latestVersion, currentVersion)) {
                    updateAvailable = true;
                    plugin.getLogger().info("A new version is available: " + latestVersion + " (current: " + currentVersion + ")");
                    plugin.getLogger().info("Download at: https://modrinth.com/plugin/" + modrinthSlug);
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }
    
    private boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.replaceAll("[^0-9.]", "").split("\\.");
        String[] currentParts = current.replaceAll("[^0-9.]", "").split("\\.");
        
        int maxLength = Math.max(latestParts.length, currentParts.length);
        
        for (int i = 0; i < maxLength; i++) {
            int latestPart = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
            
            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }
        
        return false;
    }
    
    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().isCheckUpdates()) return;
        if (!updateAvailable) return;
        
        Player player = event.getPlayer();
        if (!player.isOp() && !player.hasPermission("dangerlog.admin")) return;
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            
            String modrinthUrl = "https://modrinth.com/plugin/" + modrinthSlug;
            
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("[DangerLog] ", NamedTextColor.GOLD)
                    .append(Component.text("A new version is available!", NamedTextColor.YELLOW)));
            player.sendMessage(Component.text("  Current: ", NamedTextColor.GRAY)
                    .append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.RED))
                    .append(Component.text(" → Latest: ", NamedTextColor.GRAY))
                    .append(Component.text(latestVersion, NamedTextColor.GREEN)));
            player.sendMessage(Component.text("  ", NamedTextColor.GRAY)
                    .append(Component.text("[Click to download]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(modrinthUrl))));
            player.sendMessage(Component.empty());
        }, 40L);
    }
    
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
    
    public String getLatestVersion() {
        return latestVersion;
    }
}
