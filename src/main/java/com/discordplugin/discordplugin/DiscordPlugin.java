// bitcoinjake09 11/9/2019 - a bitcoin tressure hunt in minecraft - discordplugin
package com.discordplugin.discordplugin;

import com.discordplugin.discordplugin.commands.*;
import com.discordplugin.discordplugin.events.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DiscordPlugin extends JavaPlugin {

  boolean useJSON = false;
  boolean loadENV = true;
  public boolean eventsLoaded = false;
  public boolean maintenance_mode = false;
  
  public static UUID ADMIN_UUID = null;
  public static String DISCORD_HOOK_URL = null;
  public static String DISCORD_URL = null;
  public static String DISCORD_HOOK_CHANNEL_ID = null;
  
  public static final String PLUGIN_WEBSITE = "https://github.com/BitcoinJake09/DiscordPlugin";

  private Map<String, CommandAction> modCommands;
  private Player[] moderators;

  public void onEnable() {
    log("[startup] DiscordPlugin starting");
    try {
      if (ADMIN_UUID == null) {
        log("[warning] ADMIN_UUID env variable to is not set.");
      }
      // registers listener classes
      if (eventsLoaded == false) {
        getServer().getPluginManager().registerEvents(new EntityEvents(this), this);
        eventsLoaded = true;
      }

      System.out.println("[startup] Starting DiscordPlugin");

      // loads config file. If it doesn't exist, creates it.
      getDataFolder().mkdir();
      System.out.println("[startup] checking default config file");

      if (!new java.io.File(getDataFolder(), "config.yml").exists()) {
        saveDefaultConfig();
        System.out.println("[startup] config file does not exist. creating default.");
      }
  	useJSON = loadJSON();

      // creates scheduled timers (update balances, etc)
      createScheduledTimers();
      modCommands = new HashMap<String, CommandAction>();
      modCommands.put("crashTest", new CrashtestCommand(this));
      modCommands.put("emergencystop", new EmergencystopCommand());

      System.out.println("[startup] finished");

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("[fatal] plugin enable fails");
      Bukkit.shutdown();
    }
  }

  public boolean loadJSON() {
  JSONParser jsonParser = new JSONParser();
  System.out.println("[DiscordPlugin] attempting to load json config files.");
          try {
		File configFile = new File(System.getProperty("user.dir") + "/plugins/DiscordPlugin/config.json");

            FileReader reader = new FileReader(configFile);

            Object obj = jsonParser.parse(reader);
            JSONArray configData = (JSONArray) obj; 
	configData.forEach( tConfig -> parseJSON( (JSONObject) tConfig) );
       
            
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
           System.out.println("[DiscordPlugin] no config json files found in directory plugins/DiscordPlugin/* .");
          return false;
        } catch (IOException e) {
	   System.out.println("[DiscordPlugin] error reading json files.");
            e.printStackTrace();
		return false;
        } catch (ParseException e) {
        System.out.println("[DiscordPlugin] error parsing json files.");
            e.printStackTrace();
		return false;
        }
  }
 
   private static void parseJSON(JSONObject tConfig) {
   try {
   	JSONObject configDataObj = (JSONObject) tConfig.get("config");
	String tmpAdminUUID = (String) configDataObj.get("ADMIN_UUID") != null ? (String) configDataObj.get("ADMIN_UUID").toString() : null;    
	ADMIN_UUID = UUID.fromString(tmpAdminUUID);
	DISCORD_HOOK_URL = (String) configDataObj.get("DISCORD_HOOK_URL") != null ? configDataObj.get("DISCORD_HOOK_URL").toString() : null;            
	DISCORD_URL = (String) configDataObj.get("DISCORD_URL") != null ? configDataObj.get("DISCORD_URL").toString() : null;              
	DISCORD_HOOK_CHANNEL_ID = (String) configDataObj.get("DISCORD_HOOK_CHANNEL_ID") != null ? configDataObj.get("DISCORD_HOOK_CHANNEL_ID").toString() : null;  
        //System.out.println("[DiscordPlugin] [TEST]: DISCORD_URL: " + DISCORD_URL + " DISCORD_HOOK_CHANNEL_ID: " + DISCORD_HOOK_CHANNEL_ID + " DISCORD_HOOK_URL: " +  DISCORD_HOOK_URL);
            } catch (Exception e) {
      e.printStackTrace();
      System.out.println("[DiscordPlugin] [fatal] plugin enable failed to get config.json");
    }
  }
 
 
 
  public static void announce(final String message) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      player.sendMessage(message);
    }
  }

  public void createScheduledTimers() {
    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    scheduler.scheduleSyncRepeatingTask(
        this,
        new Runnable() {
          @Override
          public void run() {}
        },
        0,
        7200L);
        }



  public void publish_stats() {
    try {

      if (System.getenv("ELASTICSEARCH_ENDPOINT") != null) {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();


        jsonObject.put("time", new Date().getTime());
        URL url = new URL(System.getenv("ELASTICSEARCH_ENDPOINT") + "-stats/_doc");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(jsonObject.toString());
        out.close();

        if (con.getResponseCode() == 200) {

          BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
          String inputLine;
          StringBuffer response = new StringBuffer();

          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          in.close();
          // System.out.println(response.toString());
          JSONObject response_object = (JSONObject) parser.parse(response.toString());
          // System.out.println(response_object);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void log(String msg) {
    Bukkit.getLogger().info(msg);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // we don't allow server commands (yet?)
    if (sender instanceof Player) {
      final Player player = (Player) sender;
      // MODERATOR COMMANDS
      for (Map.Entry<String, CommandAction> entry : modCommands.entrySet()) {
        if (cmd.getName().equalsIgnoreCase(entry.getKey())) {
          if (player.getUniqueId().toString() == ADMIN_UUID.toString()) {
            entry.getValue().run(sender, cmd, label, args, player);
          } else {
            sender.sendMessage(
                ChatColor.DARK_RED + "You don't have enough permissions to execute this command!");
          }
        }
      }
    }
    return true;
  }

  public void crashtest() {
    this.setEnabled(false);
  }
  
  public void sendDiscordMessage(String content) {
    if(DISCORD_HOOK_URL!=null) {
      //System.out.println("[discord] "+content);
      try {
          String json = "{\"content\":\""+content+"\"}";

          JSONParser parser = new JSONParser();

          final JSONObject jsonObject = new JSONObject();
          jsonObject.put("content", content);
          CookieHandler.setDefault(new CookieManager());

          URL url = new URL(DISCORD_HOOK_URL);
          HttpsURLConnection con = null;

          System.setProperty("http.agent", "");

          con = (HttpsURLConnection) url.openConnection();

          con.setRequestMethod("POST");
          con.setRequestProperty("Content-Type", "application/json");
          con.setRequestProperty("Cookie", "DiscordPlugin=true");
          con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

          con.setDoOutput(true);
          OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
          out.write(json);
          out.close();
	if(con.getResponseCode()==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            //return true;
          } else {
            //return false;
          }
          

      } catch (Exception e) {
          e.printStackTrace();
          //return false;
      }
    }
    //return false;

  } // EO discord
  
  
} // EOF
