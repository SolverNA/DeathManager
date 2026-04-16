package dev.solverNa.deathManager.listeners;

import dev.solverNa.deathManager.DeathManager;
import dev.solverNa.deathManager.Settings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.Random;

public class CoreDeathListener implements Listener {
    private final DeathManager plugin;
    private final Random random = new Random();

    public CoreDeathListener(DeathManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Settings settings = plugin.getSettings();

        // Blood effect on hit
        if (settings.bloodEffect) {
            player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, Bukkit.createBlockData("minecraft:redstone_block"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Settings settings = plugin.getSettings();

        // Handle drops logic modifying the real death drops
        if (!settings.drop) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        } else {
            int percentage = Math.max(0, Math.min(100, settings.dropPercentage));
            if (percentage < 100 && percentage > 0) {
                event.getDrops().removeIf(item -> random.nextInt(100) >= percentage);
            } else if (percentage == 0) {
                event.getDrops().clear();
            }
        }

        Player killer = player.getKiller();
        final Location deathLoc = player.getLocation(); // need to be final for task

        if (settings.lightEffect) {
            deathLoc.getWorld().strikeLightningEffect(deathLoc);
        }

        // More blood logic
        if (settings.bloodEffect) {
            deathLoc.getWorld().spawnParticle(Particle.BLOCK, deathLoc, 100, 0.5, 0.2, 0.5, Bukkit.createBlockData("minecraft:redstone_block"));
        }

        if (settings.hologramEnabled) {
            plugin.getHologramManager().spawnHologram(deathLoc, player, killer, settings.hologramLines, settings.hologramDuration);
        }

        // Instantly respawn next tick to let death fully process
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;

            if (player.isDead()) {
                player.spigot().respawn();
            }

            // Capture the respawn location that the server calculated natively
            Location targetSpawn = player.getLocation();

            if (settings.hardcore) {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(deathLoc); // Return to death site for spectating
                if (settings.autoTarget && killer != null) {
                    player.setSpectatorTarget(killer);
                }
            }

            if (settings.respawnTimerEnabled) {
                plugin.getRespawnManager().startTimer(player, targetSpawn, settings.respawnTime, settings.respawnTitle, settings.respawnSubtitle);
            }
        });
    }
}
