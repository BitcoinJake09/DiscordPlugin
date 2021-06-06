package com.discordplugin.discordplugin.commands;

import com.discordplugin.discordplugin.DiscordPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrashtestCommand extends CommandAction {
  private DiscordPlugin discordPlugin;

  public CrashtestCommand(DiscordPlugin plugin) {
    this.discordPlugin = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    discordPlugin.crashtest();
    return true;
  }
}
