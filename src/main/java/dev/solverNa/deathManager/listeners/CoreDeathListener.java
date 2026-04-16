package dev.solverNa.deathManager.listeners;

import dev.solverNa.deathManager.DeathManager;
import dev.solverNa.deathManager.Settings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            if (settings.hardcore) {
                event.setCancelled(true);
                handleFakeDeath(player, event);
            }
        }
    }

    private void handleFakeDeath(Player player, EntityDamageEvent event) {
        Settings settings = plugin.getSettings();

        // Restore
        @SuppressWarnings("deprecation")
        double maxHealth = player.getMaxHealth();
        try {
            maxHealth = player.getAttribute(Attribute.valueOf("MAX_HEALTH")).getValue();
        } catch (Exception e) {
            try {
                maxHealth = player.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).getValue();
            } catch (Exception ignored) {}
        }
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.clearActivePotionEffects();

        // Drops
        List<ItemStack> allDrops = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                allDrops.add(item.clone());
            }
        }
        player.getInventory().clear();
        int exp = player.getTotalExperience() / 2;
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        List<ItemStack> actualDrops = new ArrayList<>();
        if (settings.drop) {
            int percentage = Math.max(0, Math.min(100, settings.dropPercentage));
            if (percentage > 0) {
                for (ItemStack item : allDrops) {
                    if (random.nextInt(100) < percentage) {
                        actualDrops.add(item);
                    }
                }
            }
        } else {
            exp = 0;
            actualDrops.clear();
        }

        Player killer = null;
        if (event instanceof EntityDamageByEntityEvent edbeEvent) {
            if (edbeEvent.getDamager() instanceof Player dp) killer = dp;
        }

        // Fire fake event
        org.bukkit.damage.DamageSource damageSource = player.getLastDamageCause() != null ? player.getLastDamageCause().getDamageSource() : org.bukkit.damage.DamageSource.builder(org.bukkit.damage.DamageType.GENERIC).build();
        PlayerDeathEvent vanillaDeathEvent = new PlayerDeathEvent(player, damageSource, actualDrops, exp, player.getName() + " died.");
        Bukkit.getPluginManager().callEvent(vanillaDeathEvent);

        Location deathLoc = player.getLocation();

        if (settings.drop && !vanillaDeathEvent.getDrops().isEmpty()) {
            for (ItemStack d : vanillaDeathEvent.getDrops()) {
                deathLoc.getWorld().dropItemNaturally(deathLoc, d);
            }
            if (vanillaDeathEvent.getDroppedExp() > 0) {
                org.bukkit.entity.ExperienceOrb orb = deathLoc.getWorld().spawn(deathLoc, org.bukkit.entity.ExperienceOrb.class);
                orb.setExperience(vanillaDeathEvent.getDroppedExp());
            }
        }

        if (settings.lightEffect) {
            deathLoc.getWorld().strikeLightningEffect(deathLoc);
        }
        if (settings.bloodEffect) {
            deathLoc.getWorld().spawnParticle(Particle.BLOCK, deathLoc, 100, 0.5, 0.2, 0.5, Bukkit.createBlockData("minecraft:redstone_block"));
        }

        player.setGameMode(GameMode.SPECTATOR);
        if (settings.autoTarget && killer != null) {
            player.setSpectatorTarget(killer);
        }

        if (settings.hologramEnabled) {
            plugin.getHologramManager().spawnHologram(deathLoc, player, killer, settings.hologramLines, settings.hologramDuration);
        }

        if (settings.respawnTimerEnabled && settings.respawnTime > 0) {
            plugin.getRespawnManager().startTimer(player, settings.respawnTime, settings.respawnTitle, settings.respawnSubtitle);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.isDead()) return; // Ignore our manually fired fake death events

        Settings settings = plugin.getSettings();
        if (settings.hardcore) return; // If hardcore is enabled, we already handled it in EntityDamageEvent

        // Classic behavior (hardcore = false)
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
        Location deathLoc = player.getLocation();

        if (settings.lightEffect) {
            deathLoc.getWorld().strikeLightningEffect(deathLoc);
        }
        if (settings.bloodEffect) {
            deathLoc.getWorld().spawnParticle(Particle.BLOCK, deathLoc, 100, 0.5, 0.2, 0.5, Bukkit.createBlockData("minecraft:redstone_block"));
        }
        if (settings.hologramEnabled) {
            plugin.getHologramManager().spawnHologram(deathLoc, player, killer, settings.hologramLines, settings.hologramDuration);
        }
    }
}
