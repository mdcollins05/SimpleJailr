/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blockmovers.plugins.simplejail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author MattC
 */
public class Configuration {

    SimpleJail plugin = null;
    FileConfiguration jailedPlayers = null;
    private File jailedPlayersFile = null;
    //Config settings
    //Integer chanceAnnounce = null;
    //Boolean defaultNobreak = null;
    String[] jailLeaveCommand = null;
    String jailLeaveJailee = null;
    String jailLeaveJailor = null;
    String jailAddJailee = null;
    String jailAddJailor = null;
    String jailAddBlocked = null;
    String[] jailCoords = null;
    Boolean jailChat = false;
    String jailAdminJail = null;
    String jailAdminCommand = null;

    public Configuration(SimpleJail plugin) {
        this.plugin = plugin;
    }

    public void loadConfiguration() {
        this.plugin.getConfig().addDefault("jail.leave.command", "spawn");
        this.plugin.getConfig().addDefault("jail.leave.jailee", "&4[Jail]&f You have been released from jail!");
        this.plugin.getConfig().addDefault("jail.leave.jailor", "&4[Jail]&f You have released $p from jail!");

        this.plugin.getConfig().addDefault("jail.add.jailee", "&4[Jail]&f You have been thrown in jail by $p!");
        this.plugin.getConfig().addDefault("jail.add.jailor", "&4[Jail]&f You have thrown $p in jail!");
        this.plugin.getConfig().addDefault("jail.add.blocked", "&4[Jail]&f You cannot throw $p in jail!");

        this.plugin.getConfig().addDefault("jail.coords", "world,x,y,z,yaw,pitch");
        this.plugin.getConfig().addDefault("jail.allowchat", false);

        this.plugin.getConfig().addDefault("jail.admin.jail", "&4[Jail]&f You set the jail point!");
        this.plugin.getConfig().addDefault("jail.admin.command", "&4[Jail]&f You set the jail leave command!");

        this.plugin.getConfig().options().copyDefaults(true);
        //Save the config whenever you manipulate it
        this.plugin.saveConfig();

        this.setVars();
    }

    public void setVars() {
        this.jailLeaveCommand = this.plugin.getConfig().getString("jail.leave.command").split(";");
        this.jailLeaveJailee = this.plugin.getConfig().getString("jail.leave.jailee");
        this.jailLeaveJailor = this.plugin.getConfig().getString("jail.leave.jailor");

        this.jailAddJailee = this.plugin.getConfig().getString("jail.add.jailee");
        this.jailAddJailor = this.plugin.getConfig().getString("jail.add.jailor");
        this.jailAddBlocked = this.plugin.getConfig().getString("jail.add.blocked");

        this.jailCoords = this.plugin.getConfig().getString("jail.coords").split(",");
        this.jailChat = this.plugin.getConfig().getBoolean("jail.allowchat");

        this.jailAdminJail = this.plugin.getConfig().getString("jail.admin.jail");
        this.jailAdminCommand = this.plugin.getConfig().getString("jail.admin.command");
        //optionClose = this.plugin.getConfig().getInt("option.close");
        //stringNoplant = this.plugin.getConfig().getString("string.tooclose.noplant");

        this.getjailedPlayers();
    }

    public void updateCoords(Location loc) {
        String[] jailCoords = new String[6];
        jailCoords[0] = loc.getWorld().getName();
        jailCoords[1] = Double.toString(loc.getX());
        jailCoords[2] = Double.toString(loc.getY());
        jailCoords[3] = Double.toString(loc.getZ());
        jailCoords[4] = Float.toString(loc.getYaw());
        jailCoords[5] = Float.toString(loc.getPitch());

	StringBuilder sb = new StringBuilder();
	sb.append(jailCoords[0]);

	for (int i=1; i<jailCoords.length; i++) {
		sb.append(",");
		sb.append(jailCoords[i]);
	}

        this.plugin.getConfig().set("jail.coords", sb.toString());
        this.plugin.saveConfig();
        this.setVars();
    }

    public void updateCommand(String command) {
        if (command.isEmpty()) {
            return;
        }
        this.plugin.getConfig().set("jail.leave.command", command);
        this.plugin.saveConfig();
        this.setVars();
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
