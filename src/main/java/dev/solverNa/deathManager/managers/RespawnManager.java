package dev.solverNa.deathManager.managers;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

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
                    player.sendTitle("§aРЕСПАВН", "", 10, 40, 10);
                    player.setGameMode(GameMode.SURVIVAL);
                    // Add logic to teleport to spawn if needed
                    this.cancel();
                    return;
                }

                String currentSubtitle = subtitle.replace("%time%", String.valueOf(time));
                player.sendTitle(title, currentSubtitle, 0, 30, 0);

                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
