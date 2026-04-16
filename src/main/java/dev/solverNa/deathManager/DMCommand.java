package dev.solverNa.deathManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DMCommand implements CommandExecutor {
    private final DeathManager plugin;

    public DMCommand(DeathManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("deathmanager.admin")) {
                plugin.getSettings().load();
                sender.sendMessage("§aDeathManager configuration reloaded!");
                return true;
            } else {
                sender.sendMessage("§cNo permission.");
                return true;
            }
        }
        sender.sendMessage("§7Usage: /" + label + " reload");
        return true;
    }
}
