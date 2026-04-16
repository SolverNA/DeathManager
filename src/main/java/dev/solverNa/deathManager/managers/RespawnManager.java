package dev.solverNa.deathManager.managers;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;

public class RespawnManager {
    private final Plugin plugin;
    private static Constructor<PlayerRespawnEvent> respawnConstructorNew;
    private static Constructor<PlayerRespawnEvent> respawnConstructorMid;
    private static Constructor<PlayerRespawnEvent> respawnConstructorOld;
    private static Object pluginReason;

    static {
        try {
            Class<?> reasonClass = Class.forName("org.bukkit.event.player.PlayerRespawnEvent$RespawnReason");
            for (Object enumConst : reasonClass.getEnumConstants()) {
                if (enumConst.toString().equals("PLUGIN")) {
                    pluginReason = enumConst;
                    break;
                }
            }
            respawnConstructorNew = PlayerRespawnEvent.class.getConstructor(
                Player.class, Location.class, boolean.class, boolean.class, boolean.class, reasonClass);
        } catch (Exception ignored) {}

        try {
            respawnConstructorMid = PlayerRespawnEvent.class.getConstructor(
                Player.class, Location.class, boolean.class, boolean.class);
        } catch (Exception ignored) {}

        try {
            respawnConstructorOld = PlayerRespawnEvent.class.getConstructor(
                Player.class, Location.class, boolean.class);
        } catch (Exception ignored) {}
    }

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

        PlayerRespawnEvent event = null;
        try {
            if (respawnConstructorNew != null && pluginReason != null) {
                event = respawnConstructorNew.newInstance(
                    player, targetLoc, bedSpawn != null, false, false, pluginReason);
            } else if (respawnConstructorMid != null) {
                event = respawnConstructorMid.newInstance(
                    player, targetLoc, bedSpawn != null, false);
            } else if (respawnConstructorOld != null) {
                event = respawnConstructorOld.newInstance(
                    player, targetLoc, bedSpawn != null);
            }
        } catch (Exception ignored) {}

        if (event != null) {
            Bukkit.getPluginManager().callEvent(event);
            player.teleport(event.getRespawnLocation());
        } else {
            player.teleport(targetLoc);
        }
    }
}
