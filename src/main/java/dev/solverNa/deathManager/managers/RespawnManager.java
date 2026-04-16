package dev.solverNa.deathManager.managers;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

public class RespawnManager {
    private final Plugin plugin;

    public RespawnManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void startTimer(Player player, int timeSec, String title, String subtitle) {
        new BukkitRunnable() {
            int time = timeSec;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                if (time <= 0) {
                    this.cancel();
                    completeRespawn(player);
                    return;
                }

                String currentSubtitle = subtitle.replace("%time%", String.valueOf(time));
                player.sendTitle(title, currentSubtitle, 0, 30, 0);

                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void completeRespawn(Player player) {
        player.sendTitle("§aРЕСПАВН", "", 10, 40, 10);
        player.setGameMode(GameMode.SURVIVAL);

        Location bedSpawn = player.getBedSpawnLocation();
        Location worldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation(); // default primary world
        Location targetLoc = bedSpawn != null ? bedSpawn : worldSpawn;

        @SuppressWarnings("deprecation")
        org.bukkit.event.player.PlayerRespawnEvent event = null;
        try {
            event = new org.bukkit.event.player.PlayerRespawnEvent(player, targetLoc, bedSpawn != null, false, org.bukkit.event.player.PlayerRespawnEvent.RespawnReason.PLUGIN);
        } catch (Throwable e) {
            try {
                event = new org.bukkit.event.player.PlayerRespawnEvent(player, targetLoc, bedSpawn != null, false);
            } catch (Throwable e2) {
                event = new org.bukkit.event.player.PlayerRespawnEvent(player, targetLoc, bedSpawn != null);
            }
        }

        if (event != null) {
            Bukkit.getPluginManager().callEvent(event);
            player.teleport(event.getRespawnLocation());
        } else {
            player.teleport(targetLoc);
        }
    }
}
