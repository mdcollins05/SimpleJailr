package com.blockmovers.plugins.simplejail;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleJail extends JavaPlugin {

    static final Logger log = Logger.getLogger("Minecraft"); //set up our logger
    public Configuration config = new Configuration(this);

    public void onEnable() {
        PluginDescriptionFile pdffile = this.getDescription();
        PluginManager pm = this.getServer().getPluginManager(); //the plugin object which allows us to add listeners later on

        config.loadConfiguration();

        pm.registerEvents(new Listeners(this), this);

        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
    }

    public void onDisable() {
        PluginDescriptionFile pdffile = this.getDescription();


        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is disabled.");
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        if (args.length >= 1) {
            if (args[0] == "a" | args[0] == "add") {
                if (cs instanceof Player) {
                    if (cs.hasPermission("simplejail.jailor")) {
                        
                    }
                }
            }
            //jail a or add
            //jail r or release
            //jail set jail
            //jail set leave
        }
        return false;
    }

    public String replaceText(String string, String playername) {
        string = string.replaceAll("\\$p", playername);
        string = string.replaceAll("&(?=[0-9a-f])", "\u00A7");
        return string;
    }

    public void jailAdd(Player p, String time) {
        config.jailedPlayers.set(p.getName(), this.getDate(time));
        config.savejailedPlayers();
    }

    public void jailRelease(Player p) {
        config.jailedPlayers.set(p.getName(), null);
        try {
            getServer().dispatchCommand(p, config.jailLeaveCommand);
        }
        finally {
            p.chat("/" + config.jailLeaveCommand);
        }

        p.sendMessage(this.replaceText(config.jailLeaveString, ""));
    }

    public long jailedUntil(Player p) {
        if (config.jailedPlayers.contains(p.getName())) {
            return config.jailedPlayers.getLong(p.getName());
        }
        return 0;
    }

    public boolean isJailed(Player p) {
        if (config.jailedPlayers.contains(p.getName())) {
            long timestamp = System.currentTimeMillis() / 1000L;
            if (this.jailedUntil(p) > timestamp) {
                return true;
            }
        }
        return false;
    }

    public long getDate(String filter) {
        if (filter.equalsIgnoreCase("now")) {
            return System.currentTimeMillis();
        }
        String[] groupings = filter.split("-");
        if (groupings.length == 0) {
            return 0;
        }
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        for (String str : groupings) {
            int type;
            switch (str.charAt(str.length() - 1)) {
                case 'm':
                    type = 12;
                    break;
                case 'h':
                    type = 10;
                    break;
                case 'd':
                    type = 5;
                    break;
                case 'w':
                    type = 3;
                    break;
                case 'y':
                    type = 1;
                    break;
                default:
                    return 0;
            }
            cal.add(type, Integer.valueOf(str.substring(0, str.length() - 1)).intValue());
        }
        return cal.getTimeInMillis() / 1000L;
    }
}
