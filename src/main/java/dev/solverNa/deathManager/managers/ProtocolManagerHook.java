package dev.solverNa.deathManager.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.ProtocolLibrary;
import dev.solverNa.deathManager.DeathManager;
import org.bukkit.Bukkit;

public class ProtocolManagerHook {
    private final DeathManager plugin;
    private ProtocolManager protocolManager;

    public ProtocolManagerHook(DeathManager plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            return false;
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        // Intercept Login packet to set hardcore mode flag for hearts
        final DeathManager dm = plugin;
        protocolManager.addPacketListener(new PacketAdapter(dm, PacketType.Play.Server.LOGIN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (dm.getSettings().hardcoreHearts) {
                    event.getPacket().getBooleans().write(0, true);
                }
            }
        });
        return true;
    }
}
