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

    public void startTimer(Player player, Location targetLoc, int timeSec, String title, String subtitle) {
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
                    completeRespawn(player, targetLoc);
                    return;
                }

                String currentSubtitle = subtitle.replace("%time%", String.valueOf(time));
                player.sendTitle(title, currentSubtitle, 0, 30, 0);

                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void completeRespawn(Player player, Location targetLoc) {
        player.sendTitle("§aРЕСПАВН", "", 10, 40, 10);
        player.setGameMode(GameMode.SURVIVAL);

        // the Player is already cleanly processed by Bukkit logic, just teleport them back to the calculated spawn location!
        if (targetLoc != null) {
            player.teleport(targetLoc);
        }
    }
}
