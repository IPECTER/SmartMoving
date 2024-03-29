package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import com.github.ipecter.smartmoving.utils.CrawlingUtil;
import com.github.ipecter.smartmoving.utils.WallJumpUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerToggleSneak implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();
    private final ConfigManager config = ConfigManager.getInstance();

    private final Set<Player> doubleSneakingCheck = new HashSet<>();
    private final Map<Player, BukkitTask> holdCheck = new HashMap<>();
    private final Plugin plugin = smartMovingManager.getPlugin();

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        SMPlayer smPlayer = smartMovingManager.getPlayer(player);
        if (!player.isFlying()) {
            if (smPlayer.isWallJumping() && !e.isSneaking()) {
                smPlayer.stopWallJump();
            } else if (WallJumpUtil.isTouchingAWall(player) && e.isSneaking() && !player.isOnGround()) {
                smPlayer.startWallJump();
            }
        }
        if (!CrawlingUtil.canCrawl(player)) {
            SmartMoving.debug("ToggleSneak - Crawling - Cancel (Return)");
            return;
        }
        if (e.isSneaking()) {
            if (smPlayer.isCrawling()) {
                smPlayer.stopCrawling();
                SmartMoving.debug("ToggleSneak - Crawling - Stop - Sneak - TOGGLE (Return)");
                return;
            }
            if (!smPlayer.isCrawling() && player.isSwimming() && config.getCrawlingModes().contains("HOLD") && !player.getLocation().getBlock().isLiquid()) {
                Bukkit.getScheduler().runTask(plugin, () -> smPlayer.startCrawling());
                return;
            }
            if (config.getCrawlingModes().contains("TUNNELS")) {
                if (CrawlingUtil.isInFrontOfATunnel(player)) {
                    Bukkit.getScheduler().runTask(plugin, () -> smPlayer.startCrawling());
                    SmartMoving.debug("ToggleSneak - Crawling - Start - TUNNELS");
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        SMPlayer smPlayer1 = smartMovingManager.getPlayer(player);
                        if (smPlayer1 != null) {
                            smPlayer1.stopCrawling();
                            SmartMoving.debug("ToggleSneak - Crawling - Stop - TUNNELS");
                        }
                    }, 15);
                    SmartMoving.debug("ToggleSneak - Crawling - TUNNELS (Return)");
                    return;
                }
            }

            if (player.getLocation().getPitch() > 85) { // The player is looking downwards and is not crawling
                if (config.getCrawlingKey().contains("DOUBLE_SHIFT")) { //if double sneaking is enabled
                    if (!doubleSneakingCheck.contains(player)) {
                        doubleSneakingCheck.add(player);
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> doubleSneakingCheck.remove(player), 8);
                    } else {
                        doubleSneakingCheck.remove(player);
                        SmartMoving.debug("ToggleSneak - Crawling - Stop - DOUBLE_SHIFT");
                        smPlayer.startCrawling();
                    }
                    SmartMoving.debug("ToggleSneak - Crawling - DOUBLE_SHIFT (Return)");
                    return;
                }
                if (config.getCrawlingKey().startsWith("HOLD")) {
                    BukkitTask bukkitTask = holdCheck.get(player);
                    if (bukkitTask != null) {
                        bukkitTask.cancel();
                    }
                    holdCheck.remove(player);
                    long time = Long.parseLong(config.getCrawlingKey().split("_")[1]);
                    holdCheck.put(player, Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isSneaking() && player.getLocation().getPitch() > 85) {
                            SmartMoving.debug("ToggleSneak - Crawling - Stop - HOLD_X");
                            smPlayer.startCrawling();
                            holdCheck.remove(player);
                        }
                    }, time * 20));
                    SmartMoving.debug("ToggleSneak - Crawling - HOLD_X (Return)");
                    return;
                }

            }

        } else {
            if (smPlayer.toggleMode() != null && !smPlayer.toggleMode() && config.getCrawlingModes().contains("HOLD") && smPlayer.isHoldCheck()) {
                SmartMoving.debug("ToggleSneak - Crawling - Stop - Sneak - HOLD (Return)");
                smPlayer.stopCrawling();
            }
        }
    }
}
