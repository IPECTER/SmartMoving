package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMovingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractBlock implements Listener {
    SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().equals(e.getPlayer().getLocation().add(0, 1.5, 0).getBlock())) {
            SMPlayer smPlayer = smartMovingManager.getPlayer(e.getPlayer());
            if (smPlayer == null) return;
            if (smPlayer.isCrawling()) {
                e.setCancelled(true);
            }
        }
    }
}
