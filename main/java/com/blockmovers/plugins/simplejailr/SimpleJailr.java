package com.blockmovers.plugins.simplejailr;

import java.util.*;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleJailr extends JavaPlugin {

    static final Logger log = Logger.getLogger("Minecraft"); //set up our logger
    public Configuration config = new Configuration(this);
    public List<String> jailed = new ArrayList();

    @Override
    public void onEnable() {
        PluginDescriptionFile pdffile = this.getDescription();
        PluginManager pm = this.getServer().getPluginManager(); //the plugin object which allows us to add listeners later on

        config.loadConfiguration();

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {
                if (!jailed.isEmpty()) {
                    //log.info(getDescription().getName() + "- Checking jailed list, " + jailed.size() + " items.");
                    List<String> jailedList = new ArrayList(jailed);
                    Iterator<String> jailee = jailedList.iterator();
                    while (jailee.hasNext()) {
                        String convict = jailee.next();
                        isJailed(convict);
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
            if (args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("add")) {
                String time = "";
                if (cs instanceof Player) {
                    if (!cs.hasPermission("simplejailr.jailor")) {
                        cs.sendMessage(this.replaceText(config.jailMiscNoperm, "", ""));
                        return false;
                    }
                }
                if (args.length == 1) {
                    cs.sendMessage(this.replaceText(config.jailMiscNouser, "", ""));
                    return false;
                }
                String player = null;
                Boolean online = false;
                if (getServer().getPlayer(args[1]) != null) {
                    player = getServer().getPlayer(args[1]).getName();
                    online = true;
                    if (getServer().getPlayer(args[1]).hasPermission("simplejailr.unjailable")) {
                        cs.sendMessage(this.replaceText(config.jailAddBlocked, player, ""));
                        return false;
                    }
                } else if (getServer().getOfflinePlayer(args[1]).hasPlayedBefore()) {
                    player = getServer().getOfflinePlayer(args[1]).getName();
                    online = false;
                } else {
                    cs.sendMessage(this.replaceText(config.jailMiscNosuchuser, "", ""));
                    return false;
                }
                if (args.length >= 3) {
                    time = args[2];
                }
                this.jailAdd(player, time, online);
                log.info(getDescription().getName() + "- " + cs.getName() + " jailed " + player + " for " + this.jailTimeLeftString(player));
                if (online) {
                    getServer().getPlayer(player).sendMessage(this.replaceText(config.jailAddJailee, cs.getName(), this.jailTimeLeftString(player)));
                }
                cs.sendMessage(this.replaceText(config.jailAddJailor, player, this.jailTimeLeftString(player)));
                return true;
            }
            if (args[0].equalsIgnoreCase("r") || args[0].equalsIgnoreCase("release")) {
                if (cs instanceof Player) {
                    if (!cs.hasPermission("simplejailr.jailor")) {
                        cs.sendMessage(this.replaceText(config.jailMiscNoperm, "", ""));
                        return false;
                    }
                }
                if (args.length == 1) {
                    cs.sendMessage(this.replaceText(config.jailMiscNouser, "", ""));
                    return false;
                }
                String player = null;
                Boolean online = false;
                if (getServer().getPlayer(args[1]) != null) {
                    player = getServer().getPlayer(args[1]).getName();
                    if (!this.isJailed(player)) {
                        cs.sendMessage(this.replaceText(config.jailInfoNotinOther, player, ""));
                        return false;
                    }
                    online = true;
                } else if (getServer().getOfflinePlayer(args[1]).hasPlayedBefore()) {
                    player = getServer().getOfflinePlayer(args[1]).getName();
                    if (!this.isJailed(player)) {
                        cs.sendMessage(this.replaceText(config.jailInfoNotinOther, player, ""));
                        return false;
                    }
                    online = true;
                } else {
                    cs.sendMessage(this.replaceText(config.jailMiscNosuchuser, "", ""));
                    return false;
                }
                this.jailRelease(player, online, true);
                log.info(getDescription().getName() + "- " + cs.getName() + " released " + player);
                if (online) {
                    getServer().getPlayer(args[1]).sendMessage(this.replaceText(config.jailLeaveJailee, "", ""));
                }
                cs.sendMessage(this.replaceText(config.jailLeaveJailor, player, ""));
                return true;
            }
            if (args[0].equalsIgnoreCase("set")) {
                if (cs instanceof Player) {
                    if (!cs.hasPermission("simplejailr.admin")) {
                        cs.sendMessage(this.replaceText(config.jailMiscNoperm, "", ""));
                        return false;
                    }
                } else {
                    cs.sendMessage(this.replaceText(config.jailMiscNoconsole, "", ""));
                }
                if (args.length == 1) {
                    cs.sendMessage("Valid options to set are: jail or leave (command) or chat [on or off].");
                    return false;
                }
                Player p = (Player) cs;
                if (args[1].equalsIgnoreCase("jail")) {
                    Location loc = p.getLocation();

                    config.updateCoords(loc);
                    p.sendMessage(this.replaceText(config.jailAdminJail, "", ""));
                    return true;
                } else if (args[1].equalsIgnoreCase("leave")) {
                    if (args.length >= 3) {
                        String command = "";
                        for (int i = 2; i < args.length; i++) {
                            command += (i == args.length - 1) ? args[i] : args[i] + " ";
                        }
                        config.updateCommand(command);
                        p.sendMessage(this.replaceText(config.jailAdminCommand, "", ""));
                        return true;
                    } else {
                        p.sendMessage(this.replaceText(config.jailMiscNocommand, "", ""));
                        return false;
                    }
                } else if (args[1].equalsIgnoreCase("chat")) {
                    if (args.length == 2) {
                        if (config.updateChat(null)) {
                            p.sendMessage(this.replaceText(config.jailAdminChaton, "", ""));
                        } else {
                            p.sendMessage(this.replaceText(config.jailAdminChatoff, "", ""));
                        }
                        return true;
                    } else if (args.length == 3) {
                        if (args[2].equalsIgnoreCase(config.jailMiscOn)) {
                            config.updateChat(true);
                            p.sendMessage(this.replaceText(config.jailAdminChaton, "", ""));
                            return true;
                        } else if (args[2].equalsIgnoreCase(config.jailMiscOff)) {
                            config.updateChat(false);
                            p.sendMessage(this.replaceText(config.jailAdminChatoff, "", ""));
                            return true;
                        } else {
                            p.sendMessage(this.replaceText(config.jailMiscNocommand, "", ""));
                            return false;
                        }
                    } else {
                        p.sendMessage(this.replaceText(config.jailMiscNocommand, "", ""));
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
                        if (this.isJailed(p.getName())) {
                            p.sendMessage(this.replaceText(config.jailInfoSelf, "", this.jailTimeLeftString(p.getName())));
                            return true;
                        } else {
                            p.sendMessage(this.replaceText(config.jailInfoNotinSelf, "", ""));
                            return false;
                        }
                    } else {
                        cs.sendMessage(this.replaceText(config.jailMiscNoconsole, "", ""));
                        return false;
                    }
                } else if (args.length == 2) {
                    if (cs instanceof Player) {
                        Player p = (Player) cs;
                        if (!p.hasPermission("simplejailr.jailor")) {
                            p.sendMessage(this.replaceText(config.jailMiscNoperm, "", ""));
                            return false;
                        }
                    }
                    String player = null;
                    Boolean online = false;
                    if (getServer().getPlayer(args[1]) != null) {
                        player = getServer().getPlayer(args[1]).getName();
                        if (!this.isJailed(player)) {
                            cs.sendMessage(this.replaceText(config.jailInfoNotinOther, player, ""));
                            return false;
                        }
                        online = true;
                    } else if (getServer().getOfflinePlayer(args[1]).hasPlayedBefore()) {
                        player = getServer().getOfflinePlayer(args[1]).getName();
                        if (!this.isJailed(player)) {
                            cs.sendMessage(this.replaceText(config.jailInfoNotinOther, player, ""));
                            return false;
                        }
                        online = true;
                    } else {
                        cs.sendMessage(this.replaceText(config.jailMiscNosuchuser, "", ""));
                        return false;
                    }
                    cs.sendMessage(this.replaceText(config.jailInfoOther, player, this.jailTimeLeftString(player)));
                    return true;
                }
            }
        }

        return false;
    }

    public String replaceText(String string, String playername, String time) {
        string = string.replaceAll("\\$p", playername);
        string = string.replaceAll("\\$t", time);
        string = string.replaceAll("\\$s", config.jailMiscMsgprefix);
        string = string.replaceAll("&(?=[0-9a-f])", "\u00A7");
        return string;
    }

    public void jailAdd(String p, String time, Boolean online) {
        Long jailTime = this.getDate(time);
        this.jailAdd(p, jailTime, online);
    }

    public void jailAdd(String p, Long time, Boolean online) {
        config.jailedPlayers.set(p, time);
        config.savejailedPlayers();
        if (online == true) {
            this.teleportToJail(getServer().getPlayer(p));
            if (time != 0) {
                this.jailed.add(p);
            }
        }
        if (this.config.jailBroadcast) {
            getServer().broadcastMessage(this.replaceText(this.config.jailAddBroadcast, p, this.timeToString(Integer.valueOf(time.toString()))));
        }
    }

    public void jailRelease(String p, Boolean online, Boolean cmds) {
        if (online) {
            Player player = getServer().getPlayer(p);
            config.jailedPlayers.set(player.getName(), null);
            config.savejailedPlayers();
            if (this.jailed.contains(p)) {
                this.jailed.remove(p);
            }

            if (cmds) {
                for (String cmd : config.jailLeaveCommand) {
                    try {
                        getServer().dispatchCommand(player, cmd);
                    } catch (CommandException e) {
                        player.chat("/" + cmd);
                    }
                }
            }
        } else {
            config.jailedPlayers.set(p, -1);
            config.savejailedPlayers();
        }
        if (this.config.jailBroadcast) {
            getServer().broadcastMessage(this.replaceText(this.config.jailLeaveBroadcast, p, ""));
        }
    }

    public void teleportToJail(Player p) {
        String[] jailCoords = this.config.jailCoords;
        p.teleport(new Location(p.getServer().getWorld(jailCoords[0]), Double.valueOf(jailCoords[1]), Double.valueOf(jailCoords[2]), Double.valueOf(jailCoords[3]), Float.valueOf(jailCoords[4]), Float.valueOf(jailCoords[5])));
    }

    public long jailedUntil(String p) {
        if (config.jailedPlayers.contains(p)) {
            return config.jailedPlayers.getLong(p);
        }
        return 0;
    }

    public Boolean jailAddTime(String p) {
        if (this.isJailed(p)) {
            Long time = this.jailedUntil(p);
            if (time > 0) {
                time = time + this.config.jailExtendtime;
                this.jailAdd(p, time, false);
                return true;
            }
        }
        return false;
    }

    public boolean isJailed(String p) {
        if (config.jailedPlayers.contains(p)) {
            long timestamp = System.currentTimeMillis() / 1000L;
            Long jailedUntil = this.jailedUntil(p);
            Boolean online = false;
            if (getServer().getPlayer(p) != null) {
                if (getServer().getPlayer(p).hasPermission("simplejailr.unjailable")) {
                    this.jailRelease(p, true, false);
                    return false;
                }
            }
            if (getServer().getPlayerExact(p) != null) {
                if (getServer().getPlayerExact(p).isOnline()) {
                    online = true;
                }
            }
            if (jailedUntil == 0) {
                return true;
            }
            if (jailedUntil > timestamp) {
                if (online == true) {
                    if (!this.jailed.contains(p)) {
                        this.jailed.add(p);
                    }
                }
                return true;
            }
            this.jailRelease(p, online, true);
            getServer().getPlayer(p).sendMessage(this.replaceText(config.jailLeaveJailee, "", ""));
        }

        return false;
    }

    public Integer jailedTimeLeft(String p) {
        Long time = 0L;
        if (!this.isJailed(p)) {
            return 0;
        }
        time = this.jailedUntil(p);
        if (time <= 0) {
            return 0;
        }
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());

        Integer timeLeft = (int) (time - (cal.getTimeInMillis() / 1000L));

        if (timeLeft <= 0) {
            return 0;
        }
        return timeLeft;
    }

    public String jailTimeLeftString(String p) {
        Integer time = this.jailedTimeLeft(p);

        if (time == 0) {
            return config.jailMiscForever;
        }

        return this.timeToString(time);
    }

    public String timeToString(Integer time) {
        StringBuilder sb = new StringBuilder();
        String s = null;
        Integer temp = 0;

        Integer year = 31536000;
        Integer week = 604800;
        Integer day = 86400;
        Integer hour = 3600;
        Integer minute = 60;

        if (time >= year) {
            temp = (int) Math.floor(time / year);
            sb.append(temp + " " + config.jailMiscYear);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % year);
        }
        if (time >= week) {
            temp = (int) Math.floor(time / week);
            sb.append(temp + " " + config.jailMiscWeek);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % week);
        }
        if (time >= day) {
            temp = (int) Math.floor(time / day);
            sb.append(temp + " " + config.jailMiscDay);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % day);
        }
        if (time >= hour) {
            temp = (int) Math.floor(time / hour);
            sb.append(temp + " " + config.jailMiscHour);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % hour);
        }
        if (time >= minute) {
            temp = (int) Math.floor(time / minute);
            sb.append(temp + " " + config.jailMiscMinute);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % minute);
        }
        if (time > 0) {
            sb.append(time + " " + config.jailMiscSecond);
            if (time > 1) {
                sb.append("s");
            }
            s = sb.toString();
        }
        if (time == 0) {
            s = sb.substring(0, sb.length() - 2);
        }


        return s;
    }

    public long getDate(String filter) {
        if (filter.equalsIgnoreCase("")) {
            return 0;
        }
        if (filter.equalsIgnoreCase("now")) {
            return System.currentTimeMillis();
        }
        String[] groupings = filter.split(",");
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
