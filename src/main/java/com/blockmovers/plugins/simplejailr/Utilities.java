/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blockmovers.plugins.simplejailr;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

/**
 *
 * @author MattC
 */
public class Utilities {

    SimpleJailr plugin = null;

    public Utilities(SimpleJailr plugin) {
        this.plugin = plugin;
    }

    public String replaceText(String string, String playername, String time) {
        string = string.replaceAll("\\$p", playername);
        string = string.replaceAll("\\$t", time);
        string = string.replaceAll("\\$s", this.plugin.config.jailMiscMsgprefix);
        string = string.replaceAll("&(?=[0-9a-f])", "\u00A7");
        return string;
    }

    public void jailAdd(String p, String time, Boolean online) {
        Long jailTime = this.getDate(time);
        this.jailAdd(p, jailTime, online);
    }

    public void jailAdd(String loveYou, Long time, Boolean online) {
        this.plugin.config.jailedPlayers.set(loveYou, time);
        this.plugin.config.savejailedPlayers();
        if (online == true) {
            this.teleportToJail(this.plugin.getServer().getPlayer(loveYou));
            if (time != 0) {
                this.plugin.jailed.add(loveYou);
            }
        }

        if (this.plugin.config.jailBroadcast) {
            this.plugin.getServer().broadcastMessage(this.replaceText(this.plugin.config.jailAddBroadcast, loveYou, this.jailTimeLeftString(loveYou)));
        }
    }

    public void jailRelease(String p, Boolean online, Boolean cmds) {
        if (online) {
            Player player = this.plugin.getServer().getPlayer(p);
            this.plugin.config.jailedPlayers.set(player.getName(), null);
            this.plugin.config.savejailedPlayers();
            if (this.plugin.jailed.contains(p)) {
                this.plugin.jailed.remove(p);
            }

            if (cmds) {
                if (this.plugin.config.jailLeaveUse.equalsIgnoreCase("both") || this.plugin.config.jailLeaveUse.equalsIgnoreCase("teleport")) {
                    String[] jailCoords = this.plugin.config.jailLeaveCoords;
                    player.teleport(new Location(player.getServer().getWorld(jailCoords[0]), Double.valueOf(jailCoords[1]), Double.valueOf(jailCoords[2]), Double.valueOf(jailCoords[3]), Float.valueOf(jailCoords[4]), Float.valueOf(jailCoords[5])));
                }
                if (this.plugin.config.jailLeaveUse.equalsIgnoreCase("both") || this.plugin.config.jailLeaveUse.equalsIgnoreCase("command")) {
                    for (String cmd : this.plugin.config.jailLeaveCommand) {
                        try {
                            this.plugin.getServer().dispatchCommand(player, cmd);
                        } catch (CommandException e) {
                            player.chat("/" + cmd);
                        }
                    }
                }
            }
        } else {
            this.plugin.config.jailedPlayers.set(p, -1);
            this.plugin.config.savejailedPlayers();
        }
        if (this.plugin.config.jailBroadcast) {
            this.plugin.getServer().broadcastMessage(this.replaceText(this.plugin.config.jailLeaveBroadcast, p, ""));
        }
    }

    public void teleportToJail(Player p) {
        String[] jailCoords = this.plugin.config.jailCoords;
        p.teleport(new Location(p.getServer().getWorld(jailCoords[0]), Double.valueOf(jailCoords[1]), Double.valueOf(jailCoords[2]), Double.valueOf(jailCoords[3]), Float.valueOf(jailCoords[4]), Float.valueOf(jailCoords[5])));
    }

    public long jailedUntil(String p) {
        if (this.plugin.config.jailedPlayers.contains(p)) {
            return this.plugin.config.jailedPlayers.getLong(p);
        }
        return 0;
    }

    public Boolean jailAddTime(String p) {
        if (this.isJailed(p)) {
            Long time = this.jailedUntil(p);
            if (time > 0) {
                time = time + this.plugin.config.jailExtendtime;
                this.plugin.config.jailedPlayers.set(p, time);
                this.plugin.config.savejailedPlayers();
                return true;
            }
        }
        return false;
    }

    public boolean isJailed(String p) {
        if (this.plugin.config.jailedPlayers.contains(p)) {
            long timestamp = System.currentTimeMillis() / 1000L;
            Long jailedUntil = this.jailedUntil(p);
            Boolean online = false;
            if (this.plugin.getServer().getPlayer(p) != null) {
                if (this.plugin.getServer().getPlayer(p).hasPermission("simplejailr.unjailable")) {
                    this.jailRelease(p, true, false);
                    return false;
                }
            }
            if (this.plugin.getServer().getPlayerExact(p) != null) {
                if (this.plugin.getServer().getPlayerExact(p).isOnline()) {
                    online = true;
                }
            }
            if (jailedUntil == 0) {
                return true;
            }
            if (jailedUntil > timestamp) {
                if (online == true) {
                    if (!this.plugin.jailed.contains(p)) {
                        this.plugin.jailed.add(p);
                    }
                }
                return true;
            }
            this.jailRelease(p, online, true);
            this.plugin.getServer().getPlayer(p).sendMessage(this.replaceText(this.plugin.config.jailLeaveJailee, "", ""));
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
            return this.plugin.config.jailMiscForever;
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
            sb.append(temp + " " + this.plugin.config.jailMiscYear);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % year);
        }
        if (time >= week) {
            temp = (int) Math.floor(time / week);
            sb.append(temp + " " + this.plugin.config.jailMiscWeek);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % week);
        }
        if (time >= day) {
            temp = (int) Math.floor(time / day);
            sb.append(temp + " " + this.plugin.config.jailMiscDay);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % day);
        }
        if (time >= hour) {
            temp = (int) Math.floor(time / hour);
            sb.append(temp + " " + this.plugin.config.jailMiscHour);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % hour);
        }
        if (time >= minute) {
            temp = (int) Math.floor(time / minute);
            sb.append(temp + " " + this.plugin.config.jailMiscMinute);
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % minute);
        }
        if (time > 0) {
            sb.append(time + " " + this.plugin.config.jailMiscSecond);
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
