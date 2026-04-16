package dev.solverNa.deathManager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class Settings {
    private final DeathManager plugin;

    public boolean hardcore;
    public boolean lightEffect;
    public boolean drop;
    public int dropPercentage;
    public boolean bloodEffect;
    public boolean autoTarget;
    public boolean hologramEnabled;
    public int hologramDuration;
    public List<String> hologramLines;
    public boolean respawnTimerEnabled;
    public int respawnTime;
    public String respawnTitle;
    public String respawnSubtitle;
    public boolean hardcoreHearts;

    public Settings(DeathManager plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        hardcore = config.getBoolean("hardcore", true);
        lightEffect = config.getBoolean("light-effect", true);
        drop = config.getBoolean("drop", true);
        dropPercentage = config.getInt("drop-percentage", 100);
        bloodEffect = config.getBoolean("blood-effect", true);
        autoTarget = config.getBoolean("auto-target", true);

        hologramEnabled = config.getBoolean("hologram.enabled", true);
        hologramDuration = config.getInt("hologram.duration", 10);
        hologramLines = config.getStringList("hologram.lines").stream()
                .map(this::color)
                .collect(Collectors.toList());

        respawnTimerEnabled = config.getBoolean("respawn-timer.enabled", true);
        respawnTime = config.getInt("respawn-timer.time", 5);
        respawnTitle = color(config.getString("respawn-timer.title", "&cВЫ ПОГИБЛИ"));
        respawnSubtitle = color(config.getString("respawn-timer.subtitle", "&7Ожидание игры... %time%"));

        hardcoreHearts = config.getBoolean("hardcore-hearts", true);
    }

    private String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
