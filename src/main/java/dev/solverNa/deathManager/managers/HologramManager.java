package dev.solverNa.deathManager.managers;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class HologramManager {
    private final Plugin plugin;

    public HologramManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void spawnHologram(Location location, Player victim, Player killer, List<String> configLines, int durationSec) {
        if (configLines.isEmpty()) return;

        List<ArmorStand> stands = new ArrayList<>();
        double yOffset = 0.0;
        String killerName = killer != null ? killer.getName() : "Unknown";

        // Spawn upside down
        for (int i = configLines.size() - 1; i >= 0; i--) {
            String line = configLines.get(i)
                    .replace("%player%", victim.getName())
                    .replace("%killer%", killerName);

            Location spawnLoc = location.clone().add(0, yOffset, 0);
            ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
            as.setVisible(false);
            as.setGravity(false);
            as.setMarker(true);
            as.setCustomNameVisible(true);
            as.setCustomName(line);

            stands.add(as);
            yOffset += 0.3;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                stands.forEach(ArmorStand::remove);
            }
        }.runTaskLater(plugin, durationSec * 20L);
    }
}
