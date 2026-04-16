package dev.solverNa.deathManager;

import org.bukkit.plugin.java.JavaPlugin;
import dev.solverNa.deathManager.listeners.CoreDeathListener;
import dev.solverNa.deathManager.managers.HologramManager;
import dev.solverNa.deathManager.managers.RespawnManager;
import dev.solverNa.deathManager.managers.ProtocolManagerHook;

public final class DeathManager extends JavaPlugin {
    private Settings settings;
    private HologramManager hologramManager;
    private RespawnManager respawnManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = new Settings(this);

        hologramManager = new HologramManager(this);
        respawnManager = new RespawnManager(this);

        getCommand("deathmanager").setExecutor(new DMCommand(this));

        getServer().getPluginManager().registerEvents(new CoreDeathListener(this), this);
        getLogger().info("DeathManager activated! Ready to manage minigame fatalities.");

        ProtocolManagerHook protocolHook = new ProtocolManagerHook(this);
        if (protocolHook.init()) {
            getLogger().info("ProtocolLib found! Hardcore hearts will be supported if enabled.");
        } else {
            getLogger().info("ProtocolLib not found. Hardcore hearts will not be used.");
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public RespawnManager getRespawnManager() {
        return respawnManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
