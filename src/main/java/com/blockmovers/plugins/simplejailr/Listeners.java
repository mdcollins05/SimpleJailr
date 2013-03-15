package com.blockmovers.plugins.simplejailr;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class Listeners implements Listener {

    SimpleJailr plugin = null;

    public Listeners(SimpleJailr plugin) {
        this.plugin = plugin;
    }

//    @EventHandler
//    public void onBlockPlace(BlockPlaceEvent event) {
//        if (event.isCancelled()) {
//            return;
//        }
//        Player p = event.getPlayer();
//        String player = p.getName();
//        if (plugin.isJailed(player)) {
//            if (plugin.config.jailGriefingextends) {
//                plugin.jailAddTime(player);
//                p.sendMessage(plugin.replaceText(plugin.config.jailInfoTimeextended, player, plugin.timeToString(plugin.config.jailExtendtime)));
//            }
//            event.setCancelled(true);
//        }
//    }
//
//    @EventHandler
//    public void onBlockBreak(BlockBreakEvent event) {
//        if (event.isCancelled()) {
//            return;
//        }
//        Player p = event.getPlayer();
//        String player = p.getName();
//        if (plugin.isJailed(player)) {
//            if (plugin.config.jailGriefingextends) {
//                plugin.jailAddTime(player);
//                p.sendMessage(plugin.replaceText(plugin.config.jailInfoTimeextended, player, plugin.timeToString(plugin.config.jailExtendtime)));
//            }
//            event.setCancelled(true);
//        }
//    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        String player = p.getName();
        if (this.plugin.utils.isJailed(player)) {
            if (this.plugin.config.jailGriefingextends) {
                if (this.plugin.utils.jailAddTime(player)) {
                    p.sendMessage(this.plugin.utils.replaceText(this.plugin.config.jailInfoTimeextended, player, this.plugin.utils.timeToString(this.plugin.config.jailExtendtime)));
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!this.plugin.config.jailChat) {
            if (this.plugin.utils.isJailed(event.getPlayer().getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.plugin.utils.isJailed(event.getPlayer().getName())) {
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
        if (this.plugin.utils.isJailed(event.getPlayer().getName())) {
            Player p = event.getPlayer();
            String player = p.getName();
            this.plugin.utils.teleportToJail(p);
            p.sendMessage(this.plugin.utils.replaceText(this.plugin.config.jailInfoSelf, "", this.plugin.utils.jailTimeLeftString(player)));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.jailed.contains(event.getPlayer().getName())) {
            plugin.jailed.remove(event.getPlayer().getName());
        }
    }
}
