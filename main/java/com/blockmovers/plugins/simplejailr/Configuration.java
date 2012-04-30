package com.blockmovers.plugins.simplejailr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration {

    SimpleJailr plugin = null;
    FileConfiguration jailedPlayers = null;
    private File jailedPlayersFile = null;
    //Config settings
    //Integer chanceAnnounce = null;
    //Boolean defaultNobreak = null;
    String jailLeaveUse = null;
    String[] jailLeaveCommand = null;
    String[] jailLeaveCoords = null;
    String jailLeaveJailee = null;
    String jailLeaveJailor = null;
    String jailLeaveBroadcast = null;
    String jailAddJailee = null;
    String jailAddJailor = null;
    String jailAddBlocked = null;
    String jailAddBroadcast = null;
    String jailInfoSelf = null;
    String jailInfoOther = null;
    String jailInfoNotinSelf = null;
    String jailInfoNotinOther = null;
    String jailInfoTimeextended = null;
    String jailMiscNoperm = null;
    String jailMiscNoconsole = null;
    String jailMiscNosuchuser = null;
    String jailMiscNouser = null;
    String jailMiscNocommand = null;
    String jailMiscMsgprefix = null;
    String jailMiscSecond = null;
    String jailMiscMinute = null;
    String jailMiscHour = null;
    String jailMiscDay = null;
    String jailMiscWeek = null;
    String jailMiscYear = null;
    String jailMiscForever = null;
    String jailMiscOn = null;
    String jailMiscOff = null;
    String[] jailCoords = null;
    Boolean jailChat = false;
    Boolean jailGriefingextends = null;
    Integer jailExtendtime = null;
    Boolean jailBroadcast = null;
    String jailAdminJail = null;
    String jailAdminLeave = null;
    String jailAdminRelease = null;
    String jailAdminCommand = null;
    String jailAdminChaton = null;
    String jailAdminChatoff = null;
    String jailAdminBroadcaston = null;
    String jailAdminBroadcastoff = null;
    String jailAdminGriefextendon = null;
    String jailAdminGriefextendoff = null;

    public Configuration(SimpleJailr plugin) {
        this.plugin = plugin;
    }

    public void loadConfiguration() {
        this.plugin.getConfig().addDefault("jail.leave.use", "command");
        this.plugin.getConfig().addDefault("jail.leave.command", "spawn");
        this.plugin.getConfig().addDefault("jail.leave.coords", "world,x,y,z,yaw,pitch");
        this.plugin.getConfig().addDefault("string.leave.jailee", "$s You have been released from jail!");
        this.plugin.getConfig().addDefault("string.leave.jailor", "$s You have released $p from jail!");
        this.plugin.getConfig().addDefault("string.leave.broadcast", "$s $p was released from jail!");

        this.plugin.getConfig().addDefault("string.add.jailee", "$s You have been thrown in jail by $p for $t!");
        this.plugin.getConfig().addDefault("string.add.jailor", "$s You have thrown $p in jail for $t!");
        this.plugin.getConfig().addDefault("string.add.blocked", "$s You cannot throw $p in jail!");
        this.plugin.getConfig().addDefault("string.add.broadcast", "$s $p was jailed for $t!");

        this.plugin.getConfig().addDefault("string.info.self", "$s You have $t left in jail!");
        this.plugin.getConfig().addDefault("string.info.other", "$s $p has $t left in jail!");
        this.plugin.getConfig().addDefault("string.info.notin.self", "$s You are not in jail!");
        this.plugin.getConfig().addDefault("string.info.notin.other", "$s $p is not in jail!");
        this.plugin.getConfig().addDefault("string.info.extended", "$s You have extended your stay by $t!");

        this.plugin.getConfig().addDefault("string.misc.noperm", "$s You do not have permission for that!");
        this.plugin.getConfig().addDefault("string.misc.noconsole", "$s You cannot do that from the console!");
        this.plugin.getConfig().addDefault("string.misc.nosuchuser", "$s That user was not found!");
        this.plugin.getConfig().addDefault("string.misc.nouser", "$s You need to specify a user!");
        this.plugin.getConfig().addDefault("string.misc.nocommand", "$s You need to specify an option!");
        this.plugin.getConfig().addDefault("string.misc.msgprefix", "&4[Jail]&f");

        this.plugin.getConfig().addDefault("string.misc.second", "second");
        this.plugin.getConfig().addDefault("string.misc.minute", "minute");
        this.plugin.getConfig().addDefault("string.misc.hour", "hour");
        this.plugin.getConfig().addDefault("string.misc.day", "day");
        this.plugin.getConfig().addDefault("string.misc.week", "week");
        this.plugin.getConfig().addDefault("string.misc.year", "year");
        this.plugin.getConfig().addDefault("string.misc.forever", "eternity");
        this.plugin.getConfig().addDefault("string.misc.on", "on");
        this.plugin.getConfig().addDefault("string.misc.off", "off");

        this.plugin.getConfig().addDefault("jail.coords", "world,x,y,z,yaw,pitch");
        this.plugin.getConfig().addDefault("jail.allowchat", false);
        this.plugin.getConfig().addDefault("jail.griefingextends", true);
        this.plugin.getConfig().addDefault("jail.extendtime", 30);
        this.plugin.getConfig().addDefault("jail.broadcast", true);

        this.plugin.getConfig().addDefault("string.admin.jail", "$s You set the jail point!");
        this.plugin.getConfig().addDefault("string.admin.leave", "$s You set the jail release use option!");
        this.plugin.getConfig().addDefault("string.admin.release", "$s You set the jail release point!");
        this.plugin.getConfig().addDefault("string.admin.command", "$s You set the jail leave command!");
        this.plugin.getConfig().addDefault("string.admin.chaton", "$s You turned jailee chat ON!");
        this.plugin.getConfig().addDefault("string.admin.chatoff", "$s You turned jailee chat OFF!");
        this.plugin.getConfig().addDefault("string.admin.broadcaston", "$s You turned jail broadcast ON!");
        this.plugin.getConfig().addDefault("string.admin.broadcastoff", "$s You turned jail broadcast OFF!");
        this.plugin.getConfig().addDefault("string.admin.griefextendon", "$s You turned jail grief time extender ON!");
        this.plugin.getConfig().addDefault("string.admin.griefextendoff", "$s You turned jail grief time extender OFF!");

        this.plugin.getConfig().options().copyDefaults(true);
        //Save the config whenever you manipulate it
        this.plugin.saveConfig();

        this.setVars();
    }

    public void setVars() {
        this.jailLeaveUse = this.plugin.getConfig().getString("jail.leave.use");
        this.jailLeaveCommand = this.plugin.getConfig().getString("jail.leave.command").split(";");
        this.jailLeaveCoords = this.plugin.getConfig().getString("jail.leave.coords").split(",");
        this.jailLeaveJailee = this.plugin.getConfig().getString("string.leave.jailee");
        this.jailLeaveJailor = this.plugin.getConfig().getString("string.leave.jailor");
        this.jailLeaveBroadcast = this.plugin.getConfig().getString("string.leave.broadcast");

        this.jailAddJailee = this.plugin.getConfig().getString("string.add.jailee");
        this.jailAddJailor = this.plugin.getConfig().getString("string.add.jailor");
        this.jailAddBlocked = this.plugin.getConfig().getString("string.add.blocked");
        this.jailAddBroadcast = this.plugin.getConfig().getString("string.add.broadcast");

        this.jailInfoSelf = this.plugin.getConfig().getString("string.info.self");
        this.jailInfoOther = this.plugin.getConfig().getString("string.info.other");
        this.jailInfoNotinSelf = this.plugin.getConfig().getString("string.info.notin.self");
        this.jailInfoNotinOther = this.plugin.getConfig().getString("string.info.notin.other");
        this.jailInfoTimeextended = this.plugin.getConfig().getString("string.info.extended");

        this.jailMiscNoperm = this.plugin.getConfig().getString("string.misc.noperm");
        this.jailMiscNoconsole = this.plugin.getConfig().getString("string.misc.noconsole");
        this.jailMiscNosuchuser = this.plugin.getConfig().getString("string.misc.nosuchuser");
        this.jailMiscNouser = this.plugin.getConfig().getString("string.misc.nouser");
        this.jailMiscNocommand = this.plugin.getConfig().getString("string.misc.nocommand");
        this.jailMiscMsgprefix = this.plugin.getConfig().getString("string.misc.msgprefix");

        this.jailMiscSecond = this.plugin.getConfig().getString("string.misc.second");
        this.jailMiscMinute = this.plugin.getConfig().getString("string.misc.minute");
        this.jailMiscHour = this.plugin.getConfig().getString("string.misc.hour");
        this.jailMiscDay = this.plugin.getConfig().getString("string.misc.day");
        this.jailMiscWeek = this.plugin.getConfig().getString("string.misc.week");
        this.jailMiscYear = this.plugin.getConfig().getString("string.misc.year");
        this.jailMiscForever = this.plugin.getConfig().getString("string.misc.forever");
        this.jailMiscOn = this.plugin.getConfig().getString("string.misc.on");
        this.jailMiscOff = this.plugin.getConfig().getString("string.misc.off");

        this.jailCoords = this.plugin.getConfig().getString("jail.coords").split(",");
        this.jailChat = this.plugin.getConfig().getBoolean("jail.allowchat");
        this.jailGriefingextends = this.plugin.getConfig().getBoolean("jail.griefingextends");
        this.jailExtendtime = this.plugin.getConfig().getInt("jail.extendtime");
        this.jailBroadcast = this.plugin.getConfig().getBoolean("jail.broadcast");

        this.jailAdminJail = this.plugin.getConfig().getString("string.admin.jail");
        this.jailAdminLeave = this.plugin.getConfig().getString("string.admin.leave");
        this.jailAdminRelease = this.plugin.getConfig().getString("string.admin.release");
        this.jailAdminCommand = this.plugin.getConfig().getString("string.admin.command");
        this.jailAdminChaton = this.plugin.getConfig().getString("string.admin.chaton");
        this.jailAdminChatoff = this.plugin.getConfig().getString("string.admin.chatoff");
        this.jailAdminBroadcaston = this.plugin.getConfig().getString("string.admin.broadcaston");
        this.jailAdminBroadcastoff = this.plugin.getConfig().getString("string.admin.broadcastoff");
        this.jailAdminGriefextendon = this.plugin.getConfig().getString("string.admin.griefextendon");
        this.jailAdminGriefextendoff = this.plugin.getConfig().getString("string.admin.griefextendoff");

        this.getjailedPlayers();
    }

    public void updateJailCoords(Location loc) {
        String[] jailCoords = new String[6];
        jailCoords[0] = loc.getWorld().getName();
        jailCoords[1] = Double.toString(loc.getX());
        jailCoords[2] = Double.toString(loc.getY());
        jailCoords[3] = Double.toString(loc.getZ());
        jailCoords[4] = Float.toString(loc.getYaw());
        jailCoords[5] = Float.toString(loc.getPitch());

        StringBuilder sb = new StringBuilder();
        sb.append(jailCoords[0]);

        for (int i = 1; i < jailCoords.length; i++) {
            sb.append(",");
            sb.append(jailCoords[i]);
        }

        this.plugin.getConfig().set("jail.coords", sb.toString());
        this.plugin.saveConfig();
        this.setVars();
    }
    
    public void updateReleaseCoords(Location loc) {
        String[] jailCoords = new String[6];
        jailCoords[0] = loc.getWorld().getName();
        jailCoords[1] = Double.toString(loc.getX());
        jailCoords[2] = Double.toString(loc.getY());
        jailCoords[3] = Double.toString(loc.getZ());
        jailCoords[4] = Float.toString(loc.getYaw());
        jailCoords[5] = Float.toString(loc.getPitch());

        StringBuilder sb = new StringBuilder();
        sb.append(jailCoords[0]);

        for (int i = 1; i < jailCoords.length; i++) {
            sb.append(",");
            sb.append(jailCoords[i]);
        }

        this.plugin.getConfig().set("jail.leave.coords", sb.toString());
        this.plugin.saveConfig();
        this.setVars();
    }
    
    public boolean updateLeaveUse(String use) {
        List options = new ArrayList();
        options.add("both");
        options.add("command");
        options.add("teleport");
        if (options.contains(use)) {
            this.plugin.getConfig().set("jail.leave.use", use);
            this.plugin.saveConfig();
            this.setVars();
            return true;
        }
        return false;
    }

    public void updateCommand(String command) {
        if (command.isEmpty()) {
            return;
        }
        this.plugin.getConfig().set("jail.leave.command", command);
        this.plugin.saveConfig();
        this.setVars();
    }
    
    public boolean updateGriefingextends(Boolean on) {
        if (on == null) {
            if (this.jailGriefingextends) {
                plugin.getConfig().set("jail.griefingextends", false);
            } else {
                plugin.getConfig().set("jail.griefingextends", true);
            }
        } else if (on) {
            plugin.getConfig().set("jail.griefingextends", true);
        } else {
            plugin.getConfig().set("jail.griefingextends", false);
        }
        this.plugin.saveConfig();
        this.setVars();
        return this.jailGriefingextends;
    }

    public boolean updateBroadcast(Boolean on) {
        if (on == null) {
            if (this.jailBroadcast) {
                plugin.getConfig().set("jail.broadcast", false);
            } else {
                plugin.getConfig().set("jail.broadcast", true);
            }
        } else if (on) {
            plugin.getConfig().set("jail.broadcast", true);
        } else {
            plugin.getConfig().set("jail.broadcast", false);
        }
        this.plugin.saveConfig();
        this.setVars();
        return this.jailBroadcast;
    }
    
    public boolean updateChat(Boolean on) {
        if (on == null) {
            if (this.jailChat) {
                plugin.getConfig().set("jail.allowchat", false);
            } else {
                plugin.getConfig().set("jail.allowchat", true);
            }
        } else if (on) {
            plugin.getConfig().set("jail.allowchat", true);
        } else {
            plugin.getConfig().set("jail.allowchat", false);
        }
        this.plugin.saveConfig();
        this.setVars();
        return this.jailChat;
    }

    public void reloadjailedPlayers() {
        if (jailedPlayersFile == null) {
            jailedPlayersFile = new File(plugin.getDataFolder(), "jailedPlayers.yml");
        }
        jailedPlayers = YamlConfiguration.loadConfiguration(jailedPlayersFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("jailedPlayers.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            jailedPlayers.setDefaults(defConfig);
        }
    }

    public FileConfiguration getjailedPlayers() {
        if (jailedPlayers == null) {
            reloadjailedPlayers();
        }
        return jailedPlayers;
    }

    public void savejailedPlayers() {
        if (jailedPlayers == null || jailedPlayersFile == null) {
            return;
        }
        try {
            jailedPlayers.save(jailedPlayersFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + jailedPlayersFile, ex);
        }
    }
}
