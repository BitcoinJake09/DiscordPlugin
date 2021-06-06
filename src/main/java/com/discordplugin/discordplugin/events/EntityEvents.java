package com.discordplugin.discordplugin.events;

import com.discordplugin.discordplugin.DiscordPlugin;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import java.util.*;

public class EntityEvents implements Listener {
  DiscordPlugin discordPlugin;

  StringBuilder rawwelcome = new StringBuilder();

  public EntityEvents(DiscordPlugin plugin) {
    discordPlugin = plugin;

    for (String line : discordPlugin.getConfig().getStringList("welcomeMessage")) {
      for (ChatColor color : ChatColor.values()) {
        line = line.replaceAll("<" + color.name() + ">", color.toString());
      }
      final Pattern pattern = Pattern.compile("<link>(.+?)</link>");
      final Matcher matcher = pattern.matcher(line);
      matcher.find();
      String link = matcher.group(1);
      line = line.replaceAll("<link>" + link + "<link>", link);
      rawwelcome.append(line);
    }
  }

  
  @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Called when a player leaves a server
        Player player = event.getPlayer();
	String quitMessage = "left the server";
	discordPlugin.sendDiscordMessage("Player " + player.getName() + " Lvl: " +player.getLevel() + " " + quitMessage);
	}
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
	Player p = event.getEntity();
	Player player = (Player) p;
	discordPlugin.sendDiscordMessage("" +event.getDeathMessage());
    }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) throws ParseException{
    final Player player = event.getPlayer();
     	
      player.sendMessage(ChatColor.YELLOW + "This server runs DiscordPlugin! " + discordPlugin.PLUGIN_WEBSITE);



    final String ip = player.getAddress().toString().split("/")[1].split(":")[0];
    System.out.println("User " + player.getName() + "logged in with IP " + ip);
    System.out.println("displayname:" + player.getDisplayName());
    System.out.println("uuid:" + player.getUniqueId().toString());



    String welcome = rawwelcome.toString();
    welcome = welcome.replace("<name>", player.getName());
    player.sendMessage(welcome);
    discordPlugin.sendDiscordMessage("Player " + player.getName() + " Lvl: " +player.getLevel() + " joined the server!");
  }
}
