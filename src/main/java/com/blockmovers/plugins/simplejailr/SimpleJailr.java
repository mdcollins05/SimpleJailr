package com.blockmovers.plugins.simplejailr;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleJailr extends JavaPlugin {

    static final Logger log = Logger.getLogger("Minecraft"); //set up our logger
    public Configuration config = new Configuration(this);
    public Utilities utils = new Utilities(this);
    public List<String> jailed = new ArrayList();
    public Map<String, String[]> jailMap = new HashMap();
    public Set<String> jails = null;

    @Override
    public void onEnable() {
        PluginDescriptionFile pdffile = this.getDescription();
        PluginManager pm = this.getServer().getPluginManager(); //the plugin object which allows us to add listeners later on

        this.config.loadConfiguration();
        
        try { //mcstats.org
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        } //mcstats.org

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {
                if (!jailed.isEmpty()) {
                    //log.info(getDescription().getName() + "- Checking jailed list, " + jailed.size() + " items.");
                    List<String> jailedList = new ArrayList(jailed);
                    Iterator<String> jailee = jailedList.iterator();
                    while (jailee.hasNext()) {
                        String convict = jailee.next();
                        utils.isJailed(convict);
                    }

                }
            }
        }, 600L, 600L);

        pm.registerEvents(new Listeners(this), this);

        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdffile = this.getDescription();


        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is disabled.");
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("version")) {
                PluginDescriptionFile pdf = this.getDescription();
                cs.sendMessage(pdf.getName() + " " + pdf.getVersion() + " by MDCollins05");
                return true;
            }
            if (args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("add")) {
                String time = "";
                if (cs instanceof Player) {
                    if (!cs.hasPermission("simplejailr.jailor")) {
                        cs.sendMessage(this.utils.replaceText(this.config.jailMiscNoperm, "", ""));
                        return false;
                    }
                }
                if (args.length == 1) {
                    cs.sendMessage(this.utils.replaceText(this.config.jailMiscNouser, "", ""));
                    return false;
                }
                String player = null;
                Boolean online = false;
                if (getServer().getPlayer(args[1]) != null) {
                    player = getServer().getPlayer(args[1]).getName();
                    online = true;
                    if (getServer().getPlayer(args[1]).hasPermission("simplejailr.unjailable")) {
                        cs.sendMessage(this.utils.replaceText(this.config.jailAddBlocked, player, ""));
                        return false;
                    }
                } else if (getServer().getOfflinePlayer(args[1]).hasPlayedBefore()) {
                    player = getServer().getOfflinePlayer(args[1]).getName();
                    online = false;
                } else {
                    cs.sendMessage(this.utils.replaceText(this.config.jailMiscNosuchuser, "", ""));
                    return false;
                }
                if (args.length >= 3) {
                    time = args[2];
                }
                this.utils.jailAdd(player, time, online);
                log.info(getDescription().getName() + "- " + cs.getName() + " jailed " + player + " for " + this.utils.jailTimeLeftString(player));
                if (online) {
                    getServer().getPlayer(player).sendMessage(this.utils.replaceText(this.config.jailAddJailee, cs.getName(), this.utils.jailTimeLeftString(player)));
                }
                cs.sendMessage(this.utils.replaceText(this.config.jailAddJailor, player, this.utils.jailTimeLeftString(player)));
                return true;
            }
            if (args[0].equalsIgnoreCase("r") || args[0].equalsIgnoreCase("release")) {
                if (cs instanceof Player) {
                    if (!cs.hasPermission("simplejailr.jailor")) {
                        cs.sendMessage(this.utils.replaceText(this.config.jailMiscNoperm, "", ""));
                        return false;
                    }
                }
                if (args.length == 1) {
                    cs.sendMessage(this.utils.replaceText(this.config.jailMiscNouser, "", ""));
                    return false;
                }
                String player = null;
                Boolean online = false;
                if (getServer().getPlayer(args[1]) != null) {
                    player = getServer().getPlayer(args[1]).getName();
                    if (!this.utils.isJailed(player)) {
                        cs.sendMessage(this.utils.replaceText(this.config.jailInfoNotinOther, player, ""));
                        return false;
                    }
                    online = true;
                } else if (getServer().getOfflinePlayer(args[1]).hasPlayedBefore()) {
                    player = getServer().getOfflinePlayer(args[1]).getName();
                    if (!this.utils.isJailed(player)) {
                        cs.sendMessage(this.utils.replaceText(this.config.jailInfoNotinOther, player, ""));
                        return false;
                    }
                    online = false;
                } else {
                    cs.sendMessage(this.utils.replaceText(this.config.jailMiscNosuchuser, "", ""));
                    return false;
                }
                this.utils.jailRelease(player, online, true);
                log.info(getDescription().getName() + "- " + cs.getName() + " released " + player);
                if (online) {
                    getServer().getPlayer(player).sendMessage(this.utils.replaceText(this.config.jailLeaveJailee, "", ""));
                }
                cs.sendMessage(this.utils.replaceText(this.config.jailLeaveJailor, player, ""));
                return true;
            }
            if (args[0].equalsIgnoreCase("set")) {
                if (cs instanceof Player) {
                    if (!cs.hasPermission("simplejailr.admin")) {
                        cs.sendMessage(this.utils.replaceText(this.config.jailMiscNoperm, "", ""));
                        return false;
                    }
                } else {
                    cs.sendMessage(this.utils.replaceText(this.config.jailMiscNoconsole, "", ""));
                }
                if (args.length == 1) {
                    cs.sendMessage("Valid options to set are: jail or leave (command) or chat [on or off].");
                    return false;
                }
                Player p = (Player) cs;
                if (args[1].equalsIgnoreCase("jail")) {
                    Location loc = p.getLocation();

                    this.config.updateJailCoords(loc);
                    p.sendMessage(this.utils.replaceText(this.config.jailAdminJail, "", ""));
                    return true;
                } else if (args[1].equalsIgnoreCase("leave")) {
                    if (args.length == 3) {
                        if (this.config.updateLeaveUse(args[2])) {
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminLeave, "", ""));
                            return true;
                        } else {
                            p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                            return false;
                        }
                    } else {
                        p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                        return false;
                    }
                } else if (args[1].equalsIgnoreCase("leavetp")) {
                    Location loc = p.getLocation();

                    this.config.updateReleaseCoords(loc);
                    p.sendMessage(this.utils.replaceText(this.config.jailAdminRelease, "", ""));
                    return true;
                } else if (args[1].equalsIgnoreCase("leavecmd")) {
                    if (args.length >= 3) {
                        String command = "";
                        for (int i = 2; i < args.length; i++) {
                            command += (i == args.length - 1) ? args[i] : args[i] + " ";
                        }
                        this.config.updateCommand(command);
                        p.sendMessage(this.utils.replaceText(this.config.jailAdminCommand, "", ""));
                        return true;
                    } else {
                        p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                        return false;
                    }
                } else if (args[1].equalsIgnoreCase("chat")) {
                    if (args.length == 2) {
                        if (this.config.updateChat(null)) {
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminChaton, "", ""));
                        } else {
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminChatoff, "", ""));
                        }
                        return true;
                    } else if (args.length == 3) {
                        if (args[2].equalsIgnoreCase(this.config.jailMiscOn)) {
                            this.config.updateChat(true);
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminChaton, "", ""));
                            return true;
                        } else if (args[2].equalsIgnoreCase(this.config.jailMiscOff)) {
                            this.config.updateChat(false);
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminChatoff, "", ""));
                            return true;
                        } else {
                            p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                            return false;
                        }
                    } else {
                        p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                        return false;
                    }
                } else if (args[1].equalsIgnoreCase("broadcast")) {
                    if (args.length == 2) {
                        if (this.config.updateBroadcast(null)) {
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminBroadcaston, "", ""));
                        } else {
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminBroadcastoff, "", ""));
                        }
                        return true;
                    } else if (args.length == 3) {
                        if (args[2].equalsIgnoreCase(this.config.jailMiscOn)) {
                            this.config.updateBroadcast(true);
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminBroadcaston, "", ""));
                            return true;
                        } else if (args[2].equalsIgnoreCase(this.config.jailMiscOff)) {
                            this.config.updateBroadcast(false);
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminBroadcastoff, "", ""));
                            return true;
                        } else {
                            p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                            return false;
                        }
                    } else {
                        p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                        return false;
                    }
                } else if (args[1].equalsIgnoreCase("griefingextends") || args[1].equalsIgnoreCase("griefing") || args[1].equalsIgnoreCase("grief")) {
                    if (args.length == 2) {
                        if (this.config.updateGriefingextends(null)) {
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminGriefextendon, "", ""));
                        } else {
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminGriefextendoff, "", ""));
                        }
                        return true;
                    } else if (args.length == 3) {
                        if (args[2].equalsIgnoreCase(this.config.jailMiscOn)) {
                            this.config.updateGriefingextends(true);
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminGriefextendon, "", ""));
                            return true;
                        } else if (args[2].equalsIgnoreCase(this.config.jailMiscOff)) {
                            this.config.updateGriefingextends(false);
                            p.sendMessage(this.utils.replaceText(this.config.jailAdminGriefextendoff, "", ""));
                            return true;
                        } else {
                            p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                            return false;
                        }
                    } else {
                        p.sendMessage(this.utils.replaceText(this.config.jailMiscNocommand, "", ""));
                        return false;
                    }
                } else {
                    cs.sendMessage("Valid options to set are: jail or leave (command) or chat [on or off].");
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("i") || args[0].equalsIgnoreCase("info")) {
                if (args.length == 1) {
                    if (cs instanceof Player) {
                        Player p = (Player) cs;
                        if (this.utils.isJailed(p.getName())) {
                            p.sendMessage(this.utils.replaceText(this.config.jailInfoSelf, "", this.utils.jailTimeLeftString(p.getName())));
                            return true;
                        } else {
                            p.sendMessage(this.utils.replaceText(this.config.jailInfoNotinSelf, "", ""));
                            return false;
                        }
                    } else {
                        cs.sendMessage(this.utils.replaceText(this.config.jailMiscNoconsole, "", ""));
                        return false;
                    }
                } else if (args.length == 2) {
                    if (cs instanceof Player) {
                        Player p = (Player) cs;
                        if (!p.hasPermission("simplejailr.jailor")) {
                            p.sendMessage(this.utils.replaceText(this.config.jailMiscNoperm, "", ""));
                            return false;
                        }
                    }
                    String player = null;
                    Boolean online = false;
                    if (getServer().getPlayer(args[1]) != null) {
                        player = getServer().getPlayer(args[1]).getName();
                        if (!this.utils.isJailed(player)) {
                            cs.sendMessage(this.utils.replaceText(this.config.jailInfoNotinOther, player, ""));
                            return false;
                        }
                        online = true;
                    } else if (getServer().getOfflinePlayer(args[1]).hasPlayedBefore()) {
                        player = getServer().getOfflinePlayer(args[1]).getName();
                        if (!this.utils.isJailed(player)) {
                            cs.sendMessage(this.utils.replaceText(this.config.jailInfoNotinOther, player, ""));
                            return false;
                        }
                        online = true;
                    } else {
                        cs.sendMessage(this.utils.replaceText(this.config.jailMiscNosuchuser, "", ""));
                        return false;
                    }
                    cs.sendMessage(this.utils.replaceText(this.config.jailInfoOther, player, this.utils.jailTimeLeftString(player)));
                    return true;
                }
            }
        }

        return false;
    }
}
