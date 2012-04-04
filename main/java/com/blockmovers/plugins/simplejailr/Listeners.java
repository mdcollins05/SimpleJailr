/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blockmovers.plugins.simplejailr;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

/**
 *
 * @author MattC
 */
public class Listeners implements Listener {

    SimpleJailr plugin = null;
    //Config settings
    //Integer chanceAnnounce = null;
    //Boolean defaultNobreak = null;
    //String stringNoplant = null;

    public Listeners(SimpleJailr plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.isJailed(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.isJailed(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.isJailed(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!plugin.config.jailChat) {
            if (plugin.isJailed(event.getPlayer().getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.isJailed(event.getPlayer().getName())) {
            Boolean exception = false;
            if (event.getMessage().equals("/jail")) {
                exception = true;
            }
            if (event.getMessage().startsWith("/jail ")) {
                exception = true;
            }
            if (!exception) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.isJailed(event.getPlayer().getName())) {
            Player p = event.getPlayer();
            String player = p.getName();
            plugin.teleportToJail(p);
            p.sendMessage(plugin.replaceText(plugin.config.jailInfoSelf, "", plugin.jailTimeLeftString(player)));
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.jailed.contains(event.getPlayer().getName())) {
            plugin.jailed.remove(event.getPlayer().getName());
        }
    }
}
